package com.kiranaflow.core.data

import androidx.room.withTransaction
import com.kiranaflow.core.database.*
import com.kiranaflow.core.model.*
import com.kiranaflow.core.common.UuidV7
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Serialized transaction queue with durable WAL journal recovery.
 *
 * Architecture:
 * 1. Actor pattern: single-consumer Kotlin Channel scoped to Application.
 * 2. Real commit log: Writes PENDING entry to `bill_journal` table first.
 * 3. Atomic @Transaction: Inserts Bill, BillItems, StockMovements (ledger),
 *    updates materialized CatalogItem stock, aggregates FlowDaily sales,
 *    and marks journal APPLIED.
 * 4. Idempotent crash recovery: Replays any PENDING journals on startup.
 */
@Singleton
class BillingQueue @Inject constructor(
    private val db: KiranaFlowDatabase,
    private val billDao: BillDao,
    private val catalogDao: CatalogDao,
    private val journalDao: JournalDao,
    private val stockMovementDao: StockMovementDao,
    private val flowDailyDao: FlowDailyDao,
    private val draftCartDao: DraftCartDao,
    private val businessDayManager: BusinessDayManager
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val billCounter = AtomicInteger(1048)

    private val queue = Channel<BillCommitRequest>(capacity = Channel.UNLIMITED)
    private val _commitResults = MutableSharedFlow<CommitResult>(replay = 1)
    val commitResults: Flow<CommitResult> = _commitResults.asSharedFlow()

    @Serializable
    data class JournalPayload(
        val billId: String,
        val billNumber: String,
        val cartLines: List<CartLine>,
        val paymentMode: PaymentMode,
        val tenderedPaise: Long,
        val totalPaise: Long,
        val businessDate: String,
        val timestamp: Long
    )

    data class BillCommitRequest(
        val cartLines: List<CartLine>,
        val paymentMode: PaymentMode,
        val tenderedPaise: Long,
        val responseChannel: CompletableDeferred<CommitResult>? = null
    )

    sealed class CommitResult {
        data class Success(val bill: Bill) : CommitResult()
        data class Failure(val reason: String) : CommitResult()
    }

    init {
        // Actor consumer loop
        scope.launch {
            for (request in queue) {
                val result = processCommit(request)
                _commitResults.emit(result)
                request.responseChannel?.complete(result)
            }
        }
    }

    fun shutdown() {
        scope.cancel()
    }

    /**
     * Enqueue a bill commit request into the serial queue.
     * UI gets a Flow of commit results or awaits the response.
     */
    suspend fun enqueueCommit(
        cartLines: List<CartLine>,
        paymentMode: PaymentMode = PaymentMode.CASH,
        tenderedPaise: Long = 0L
    ): CommitResult {
        if (cartLines.isEmpty()) return CommitResult.Failure("Cart is empty")
        val deferred = CompletableDeferred<CommitResult>()
        queue.send(BillCommitRequest(cartLines, paymentMode, tenderedPaise, deferred))
        return deferred.await()
    }

    /**
     * Backwards compatible commitBill signature.
     */
    suspend fun commitBill(
        cartLines: List<CartLine>,
        paymentMode: PaymentMode = PaymentMode.CASH,
        tenderedAmount: Double = 0.0
    ): CommitResult {
        val tenderedPaise = (tenderedAmount * 100.0 + 0.5).toLong()
        return enqueueCommit(cartLines, paymentMode, tenderedPaise)
    }

    private suspend fun processCommit(req: BillCommitRequest): CommitResult {
        return try {
            val totalPaise = req.cartLines.sumOf { it.lineTotalPaise }
            val billId = UuidV7.generate()
            val billNumber = generateBillNumber()
            val now = System.currentTimeMillis()
            val businessDate = businessDayManager.getBusinessDate(now)

            val payload = JournalPayload(
                billId = billId,
                billNumber = billNumber,
                cartLines = req.cartLines,
                paymentMode = req.paymentMode,
                tenderedPaise = req.tenderedPaise,
                totalPaise = totalPaise,
                businessDate = businessDate,
                timestamp = now
            )
            val payloadJson = Json.encodeToString(payload)

            // Step 1: Write PENDING journal entry (fsync'd, own transaction)
            val journalEntry = BillJournal(
                journalId = UuidV7.generate(),
                billId = billId,
                payloadJson = payloadJson,
                status = JournalStatus.PENDING,
                createdAt = now
            )
            journalDao.insertJournal(journalEntry)

            // Step 2: Atomic @Transaction commit of Bill + BillItems + StockMovements + FlowDaily + mark APPLIED
            val committedBill = applyJournalTransaction(journalEntry.journalId, payload)

            // Step 3: Clear active draft cart
            draftCartDao.clearDraft()

            CommitResult.Success(committedBill)
        } catch (e: Exception) {
            CommitResult.Failure(e.message ?: "Failed to commit bill")
        }
    }

    /**
     * Executes atomic Room transaction across all tables.
     * Guaranteed idempotent by checking bill existence.
     */
    suspend fun applyJournalTransaction(journalId: String, payload: JournalPayload): Bill {
        return db.withTransaction {
            val existingBill = billDao.getBillById(payload.billId)
            if (existingBill != null) {
                journalDao.markApplied(journalId)
                return@withTransaction existingBill
            }

            val bill = Bill(
                id = payload.billId,
                billNumber = payload.billNumber,
                totalPaise = payload.totalPaise,
                itemCount = payload.cartLines.size,
                paymentMode = payload.paymentMode,
                tenderedPaise = payload.tenderedPaise,
                changePaise = (payload.tenderedPaise - payload.totalPaise).coerceAtLeast(0L),
                status = BillStatus.COMMITTED,
                businessDate = payload.businessDate,
                createdAt = payload.timestamp,
                updatedAt = payload.timestamp
            )
            billDao.insertBill(bill)

            val billItems = payload.cartLines.map { line ->
                BillItem(
                    id = UuidV7.generate(),
                    billId = bill.id,
                    catalogItemId = line.catalogItemId,
                    itemName = line.itemName,
                    quantityBaseUnits = line.quantityBaseUnits,
                    unit = line.unit,
                    pricePerUnitPaise = line.pricePerUnitPaise,
                    lineTotalPaise = line.lineTotalPaise,
                    inventoryType = line.inventoryType,
                    createdAt = payload.timestamp
                )
            }
            billDao.insertBillItems(billItems)

            // Inventory movements & flow daily aggregations
            payload.cartLines.forEach { line ->
                when (line.inventoryType) {
                    InventoryType.STOCK -> {
                        val movement = StockMovement(
                            id = UuidV7.generate(),
                            itemId = line.catalogItemId,
                            deltaBaseUnits = -line.quantityBaseUnits,
                            reason = MovementReason.SALE,
                            refId = bill.id,
                            businessDate = payload.businessDate,
                            createdAt = payload.timestamp
                        )
                        stockMovementDao.insertMovement(movement)
                        catalogDao.decrementStock(line.catalogItemId, line.quantityBaseUnits, payload.timestamp)
                    }
                    InventoryType.FLOW -> {
                        flowDailyDao.recordFlowSale(
                            id = UuidV7.generate(),
                            itemId = line.catalogItemId,
                            businessDate = payload.businessDate,
                            deltaBaseUnits = line.quantityBaseUnits,
                            timestamp = payload.timestamp
                        )
                    }
                }
            }

            // Mark journal entry as APPLIED
            journalDao.markApplied(journalId, JournalStatus.APPLIED, System.currentTimeMillis())

            bill
        }
    }

    /**
     * Replays all PENDING journal entries idempotently on startup.
     */
    suspend fun replayPendingJournals() {
        try {
            val pendingList = journalDao.getJournalsByStatus(JournalStatus.PENDING)
            for (entry in pendingList) {
                val payload = Json.decodeFromString<JournalPayload>(entry.payloadJson)
                applyJournalTransaction(entry.journalId, payload)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Voids a committed bill by writing reverse VOID movements to the ledger
     * and incrementing stock / reversing flow daily sales.
     * The bill record is never deleted.
     */
    suspend fun voidBill(billId: String, reason: String = "Customer Void"): Boolean {
        return db.withTransaction {
            val bill = billDao.getBillById(billId) ?: return@withTransaction false
            if (bill.status == BillStatus.VOIDED) return@withTransaction false

            val items = billDao.getItemsForBill(billId)
            val now = System.currentTimeMillis()
            val businessDate = businessDayManager.getBusinessDate(now)

            items.forEach { item ->
                when (item.inventoryType) {
                    InventoryType.STOCK -> {
                        val reverseMovement = StockMovement(
                            id = UuidV7.generate(),
                            itemId = item.catalogItemId,
                            deltaBaseUnits = item.quantityBaseUnits, // Add back to stock
                            reason = MovementReason.VOID,
                            refId = bill.id,
                            businessDate = businessDate,
                            createdAt = now
                        )
                        stockMovementDao.insertMovement(reverseMovement)
                        catalogDao.decrementStock(item.catalogItemId, -item.quantityBaseUnits, now)
                    }
                    InventoryType.FLOW -> {
                        flowDailyDao.recordFlowSale(
                            id = UuidV7.generate(),
                            itemId = item.catalogItemId,
                            businessDate = businessDate,
                            deltaBaseUnits = -item.quantityBaseUnits,
                            timestamp = now
                        )
                    }
                }
            }

            billDao.updateBill(bill.copy(status = BillStatus.VOIDED, updatedAt = now))
            true
        }
    }

    private fun generateBillNumber(): String {
        val num = billCounter.incrementAndGet()
        return "#%04d".format(num)
    }
}
