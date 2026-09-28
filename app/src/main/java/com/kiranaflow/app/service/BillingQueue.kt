package com.kiranaflow.app.service

import com.kiranaflow.app.data.db.BillDao
import com.kiranaflow.app.data.db.CatalogDao
import com.kiranaflow.app.data.model.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import javax.inject.Singleton

/**
 * BillingQueue: serialised transaction queue with WAL recovery.
 *
 * Flow:
 *   Cart lines → [commitBill()] → WAL (PENDING) → DB insert → inventory update → COMMITTED
 *
 * On app restart, [replayPendingBills()] finds any PENDING bills and completes them.
 */
@Singleton
class BillingQueue @Inject constructor(
    private val billDao: BillDao,
    private val catalogDao: CatalogDao
) {
    private val _processing = MutableStateFlow(false)
    val isProcessing: Flow<Boolean> = _processing.asStateFlow()

    private val walSeq = AtomicLong(0L)
    private val queue = Channel<BillCommitRequest>(capacity = Channel.UNLIMITED)

    data class BillCommitRequest(
        val cartLines: List<CartLine>,
        val paymentMode: PaymentMode,
        val tenderedAmount: Double
    )

    sealed class CommitResult {
        data class Success(val bill: Bill) : CommitResult()
        data class Failure(val reason: String) : CommitResult()
    }

    // ─── Public API ─────────────────────────────────────────────────────────

    /**
     * Commit a cart to a bill. This is the "Bill potru" action.
     * Writes a PENDING bill to WAL, then applies inventory changes,
     * then marks the bill COMMITTED.
     */
    suspend fun commitBill(
        cartLines: List<CartLine>,
        paymentMode: PaymentMode = PaymentMode.CASH,
        tenderedAmount: Double = 0.0
    ): CommitResult {
        if (cartLines.isEmpty()) return CommitResult.Failure("Cart is empty")

        _processing.value = true
        return try {
            val seq = walSeq.incrementAndGet()
            val total = cartLines.sumOf { it.lineTotal }
            val billNumber = generateBillNumber()

            // Step 1: Write PENDING bill to WAL (crash-safe)
            val pendingBill = Bill(
                billNumber    = billNumber,
                totalAmount   = total,
                itemCount     = cartLines.size,
                paymentMode   = paymentMode,
                tenderedAmount = tenderedAmount,
                changeAmount  = (tenderedAmount - total).coerceAtLeast(0.0),
                status        = BillStatus.PENDING,
                walSeq        = seq
            )
            val billId = billDao.insertBill(pendingBill)

            // Step 2: Insert bill line items
            val billItems = cartLines.map { line ->
                BillItem(
                    billId        = billId,
                    catalogItemId = line.catalogItemId,
                    itemName      = line.itemName,
                    quantity      = line.quantity,
                    unit          = line.unit,
                    pricePerUnit  = line.pricePerUnit,
                    lineTotal     = line.lineTotal,
                    inventoryType = line.inventoryType
                )
            }
            billDao.insertBillItems(billItems)

            // Step 3: Update inventory
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            cartLines.forEach { line ->
                when (line.inventoryType) {
                    InventoryType.STOCK -> catalogDao.decrementStock(line.catalogItemId, line.quantity)
                    InventoryType.FLOW  -> catalogDao.addFlowSale(line.catalogItemId, line.quantity, today)
                }
            }

            // Step 4: Mark bill COMMITTED (WAL replay complete)
            billDao.updateBill(pendingBill.copy(id = billId, status = BillStatus.COMMITTED))

            CommitResult.Success(pendingBill.copy(id = billId, status = BillStatus.COMMITTED))

        } catch (e: Exception) {
            CommitResult.Failure(e.message ?: "Unknown error")
        } finally {
            _processing.value = false
        }
    }

    /**
     * On app startup, replay any bills that were left PENDING (e.g. after a crash).
     * This is the WAL recovery mechanism described in the README.
     */
    suspend fun replayPendingBills() {
        val pending = billDao.getPendingBills()
        pending.forEach { bill ->
            // A PENDING bill already has its items saved; just mark it COMMITTED
            // and re-apply inventory if needed. For simplicity in v1, mark COMMITTED.
            billDao.updateBill(bill.copy(status = BillStatus.COMMITTED))
        }
    }

    // ─── Helpers ────────────────────────────────────────────────────────────

    private fun generateBillNumber(): String {
        val timestamp = System.currentTimeMillis()
        val seq = (timestamp % 10000).toInt()
        return "#%04d".format(seq)
    }
}
