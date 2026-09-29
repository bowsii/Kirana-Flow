package com.kiranaflow.core.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.kiranaflow.core.database.*
import com.kiranaflow.core.model.*
import com.kiranaflow.core.data.KiranaRepository
import com.kiranaflow.core.data.BillingQueue
import com.kiranaflow.core.common.UuidV7
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class BillingQueueIntegrationTest {

    private lateinit var db: KiranaFlowDatabase
    private lateinit var catalogDao: CatalogDao
    private lateinit var billDao: BillDao
    private lateinit var journalDao: JournalDao
    private lateinit var stockMovementDao: StockMovementDao
    private lateinit var flowDailyDao: FlowDailyDao
    private lateinit var draftCartDao: DraftCartDao
    private lateinit var billCounterDao: BillCounterDao
    private lateinit var bdm: BusinessDayManager
    private lateinit var billingQueue: BillingQueue
    private lateinit var repository: KiranaRepository

    private val stockItem = CatalogItem(
        id = "item_stock_1",
        name = "Parle-G",
        pricePaise = 500L,
        baseUnit = BaseUnitType.PIECE,
        displayUnit = DisplayUnit.PCS,
        inventoryType = InventoryType.STOCK,
        stockBaseUnits = 50L,
        reorderThresholdBaseUnits = 10L
    )

    private val flowItem = CatalogItem(
        id = "item_flow_1",
        name = "Amul Milk",
        pricePaise = 2800L,
        baseUnit = BaseUnitType.MILLILITER,
        displayUnit = DisplayUnit.L,
        inventoryType = InventoryType.FLOW
    )

    @Before
    fun setup() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, KiranaFlowDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        catalogDao = db.catalogDao()
        billDao = db.billDao()
        journalDao = db.journalDao()
        stockMovementDao = db.stockMovementDao()
        flowDailyDao = db.flowDailyDao()
        draftCartDao = db.draftCartDao()
        billCounterDao = db.billCounterDao()
        val closedBusinessDayDao = db.closedBusinessDayDao()
        bdm = BusinessDayManager()

        catalogDao.insert(stockItem)
        catalogDao.insert(flowItem)

        billingQueue = BillingQueue(
            db, billDao, catalogDao, journalDao,
            stockMovementDao, flowDailyDao, draftCartDao,
            billCounterDao, closedBusinessDayDao, bdm
        )

        repository = KiranaRepository(
            catalogDao, billDao, billingQueue,
            draftCartDao, stockMovementDao, flowDailyDao,
            closedBusinessDayDao, bdm
        )
    }

    @After
    fun tearDown() {
        billingQueue.shutdown()
        db.close()
    }

    @Test
    fun testCrashAfterBillPotruBeforeConsumerRuns_billIsRecoveredOnRestart() = runBlocking {
        // Step 1: Active draft cart exists
        val cartLines = listOf(
            CartLine(stockItem.id, stockItem.name, 5L, stockItem.unit, stockItem.pricePaise, InventoryType.STOCK),
            CartLine(flowItem.id, flowItem.name, 500L, flowItem.unit, flowItem.pricePaise, InventoryType.FLOW)
        )
        val draftCart = Cart(lines = cartLines)
        repository.saveDraftCart(draftCart)
        assertNotNull(repository.loadDraftCart())

        // Step 2: "Bill potru" triggered -> Caller writes PENDING journal & clears draft cart
        val billId = UuidV7.generate()
        val payload = BillingQueue.JournalPayload(
            billId = billId,
            billNumber = "#1049",
            cartLines = cartLines,
            paymentMode = PaymentMode.CASH,
            tenderedPaise = 5000L,
            totalPaise = 3900L,
            businessDate = bdm.getBusinessDate(),
            timestamp = System.currentTimeMillis()
        )
        val payloadJson = kotlinx.serialization.json.Json.encodeToString(
            BillingQueue.JournalPayload.serializer(), payload
        )
        val journalId = UuidV7.generate()
        journalDao.insertJournal(
            BillJournal(
                journalId = journalId,
                billId = billId,
                payloadJson = payloadJson,
                status = JournalStatus.PENDING,
                createdAt = System.currentTimeMillis()
            )
        )
        draftCartDao.clearDraft()

        // Simulate crash right here before consumer executes apply transaction
        assertNull(repository.loadDraftCart()) // Draft cart cleared
        assertNull(billDao.getBillById(billId)) // Bill not yet committed
        assertEquals(50L, catalogDao.getById(stockItem.id)?.stockBaseUnits) // Stock not yet deducted
        assertEquals(JournalStatus.PENDING, journalDao.getJournalByBillId(billId)?.status)

        // Step 3: Process restart -> replayPendingJournals()
        billingQueue.replayPendingJournals()

        // Verify recovered
        val bill = billDao.getBillById(billId)
        assertNotNull(bill)
        assertEquals(BillStatus.COMMITTED, bill?.status)
        assertEquals(3900L, bill?.totalPaise)
        assertEquals(45L, catalogDao.getById(stockItem.id)?.stockBaseUnits) // 50 - 5 = 45
        assertEquals(JournalStatus.APPLIED, journalDao.getJournalByBillId(billId)?.status)
    }

    @Test
    fun testProcessDeathBeforeApply_replayProducesSingleBillAndCorrectInventory() = runBlocking {
        // Step 1: Simulate "process died after journal written, before atomic apply"
        val billId = UuidV7.generate()
        val cartLines = listOf(
            CartLine(stockItem.id, stockItem.name, 2L, stockItem.unit, stockItem.pricePaise, InventoryType.STOCK),
            CartLine(flowItem.id, flowItem.name, 1000L, flowItem.unit, flowItem.pricePaise, InventoryType.FLOW)
        )
        val payload = BillingQueue.JournalPayload(
            billId = billId,
            billNumber = "#1001",
            cartLines = cartLines,
            paymentMode = PaymentMode.CASH,
            tenderedPaise = 5000L,
            totalPaise = 3800L,
            businessDate = bdm.getBusinessDate(),
            timestamp = System.currentTimeMillis()
        )
        val payloadJson = kotlinx.serialization.json.Json.encodeToString(
            BillingQueue.JournalPayload.serializer(), payload
        )

        val pendingJournal = BillJournal(
            journalId = UuidV7.generate(),
            billId = billId,
            payloadJson = payloadJson,
            status = JournalStatus.PENDING,
            createdAt = System.currentTimeMillis()
        )
        journalDao.insertJournal(pendingJournal)

        // Verify bill is not yet in bills table
        assertNull(billDao.getBillById(billId))
        assertEquals(50L, catalogDao.getById(stockItem.id)?.stockBaseUnits)

        // Step 2: Relaunch app -> triggers replayPendingJournals()
        billingQueue.replayPendingJournals()

        // Verify exactly one bill exists and status is COMMITTED
        val bill = billDao.getBillById(billId)
        assertNotNull(bill)
        assertEquals(BillStatus.COMMITTED, bill?.status)
        assertEquals(3800L, bill?.totalPaise)

        // Verify STOCK was deducted: 50 - 2 = 48
        assertEquals(48L, catalogDao.getById(stockItem.id)?.stockBaseUnits)

        // Verify FLOW sales recorded: 1000 ml
        val flowRecord = flowDailyDao.getFlowDaily(flowItem.id, bdm.getBusinessDate())
        assertNotNull(flowRecord)
        assertEquals(1000L, flowRecord?.soldBaseUnits)

        // Verify journal entry marked APPLIED
        val updatedJournal = journalDao.getJournalByBillId(billId)
        assertEquals(JournalStatus.APPLIED, updatedJournal?.status)
    }

    @Test
    fun testReplayRunTwice_isIdempotentAndNeverDoubleDeducts() = runBlocking {
        val billId = UuidV7.generate()
        val cartLines = listOf(
            CartLine(stockItem.id, stockItem.name, 5L, stockItem.unit, stockItem.pricePaise, InventoryType.STOCK)
        )
        val payload = BillingQueue.JournalPayload(
            billId = billId,
            billNumber = "#1002",
            cartLines = cartLines,
            paymentMode = PaymentMode.UPI,
            tenderedPaise = 2500L,
            totalPaise = 2500L,
            businessDate = bdm.getBusinessDate(),
            timestamp = System.currentTimeMillis()
        )
        val payloadJson = kotlinx.serialization.json.Json.encodeToString(
            BillingQueue.JournalPayload.serializer(), payload
        )

        val pendingJournal = BillJournal(
            journalId = UuidV7.generate(),
            billId = billId,
            payloadJson = payloadJson,
            status = JournalStatus.PENDING
        )
        journalDao.insertJournal(pendingJournal)

        // First replay
        billingQueue.replayPendingJournals()
        assertEquals(45L, catalogDao.getById(stockItem.id)?.stockBaseUnits)
        assertEquals(1, billDao.totalBillCount())

        // Second replay (simulating another restart or repeated call)
        billingQueue.replayPendingJournals()

        // Stock MUST remain 45, and total bill count MUST remain 1
        assertEquals(45L, catalogDao.getById(stockItem.id)?.stockBaseUnits)
        assertEquals(1, billDao.totalBillCount())
    }

    @Test
    fun testTwoBillsCommittedBackToBack_appliedInOrder() = runBlocking {
        val lines1 = listOf(
            CartLine(stockItem.id, stockItem.name, 3L, stockItem.unit, stockItem.pricePaise, InventoryType.STOCK)
        )
        val lines2 = listOf(
            CartLine(stockItem.id, stockItem.name, 4L, stockItem.unit, stockItem.pricePaise, InventoryType.STOCK)
        )

        val res1 = billingQueue.enqueueCommit(lines1, PaymentMode.CASH, 2000L)
        val res2 = billingQueue.enqueueCommit(lines2, PaymentMode.UPI, 2000L)

        assertTrue(res1 is BillingQueue.CommitResult.Success)
        assertTrue(res2 is BillingQueue.CommitResult.Success)

        val bill1 = (res1 as BillingQueue.CommitResult.Success).bill
        val bill2 = (res2 as BillingQueue.CommitResult.Success).bill

        assertTrue(bill1.createdAt <= bill2.createdAt)

        // Total stock deduction: 50 - 3 - 4 = 43
        val currentStock = catalogDao.getById(stockItem.id)?.stockBaseUnits
        assertEquals(43L, currentStock)
    }

    @Test
    fun testDraftCartSurvivesProcessRecreation() = runBlocking {
        val cart = Cart(
            lines = listOf(
                CartLine(stockItem.id, stockItem.name, 2L, stockItem.unit, stockItem.pricePaise, InventoryType.STOCK)
            )
        )

        // Save draft cart before simulated kill
        repository.saveDraftCart(cart)

        // Create new repository instance simulating process relaunch
        val restoredCart = repository.loadDraftCart()
        assertNotNull(restoredCart)
        assertEquals(1, restoredCart?.itemCount)
        assertEquals(stockItem.id, restoredCart?.lines?.first()?.catalogItemId)
        assertEquals(2L, restoredCart?.lines?.first()?.quantityBaseUnits)
    }

    @Test
    fun testStockRebuildFromLedger_matchesSumOfMovements() = runBlocking {
        val itemId = stockItem.id
        val businessDate = bdm.getBusinessDate()

        // 1. Initial count in movements: 50 (PURCHASE)
        stockMovementDao.insertMovement(
            StockMovement(itemId = itemId, deltaBaseUnits = 50L, reason = MovementReason.PURCHASE, businessDate = businessDate)
        )

        // 2. Customer buys 5: -5 (SALE)
        stockMovementDao.insertMovement(
            StockMovement(itemId = itemId, deltaBaseUnits = -5L, reason = MovementReason.SALE, businessDate = businessDate)
        )

        // 3. New shipment arrives: +20 (PURCHASE)
        stockMovementDao.insertMovement(
            StockMovement(itemId = itemId, deltaBaseUnits = 20L, reason = MovementReason.PURCHASE, businessDate = businessDate)
        )

        // 4. Damaged goods adjustment: -2 (ADJUSTMENT)
        stockMovementDao.insertMovement(
            StockMovement(itemId = itemId, deltaBaseUnits = -2L, reason = MovementReason.ADJUSTMENT, businessDate = businessDate)
        )

        // 5. Voided transaction reversal: +3 (VOID)
        stockMovementDao.insertMovement(
            StockMovement(itemId = itemId, deltaBaseUnits = 3L, reason = MovementReason.VOID, businessDate = businessDate)
        )

        // Expected Net: 50 - 5 + 20 - 2 + 3 = 66
        val netStock = repository.rebuildStockFromLedger(itemId)
        assertEquals(66L, netStock)

        val updatedCatalogItem = catalogDao.getById(itemId)
        assertEquals(66L, updatedCatalogItem?.stockBaseUnits)
    }

    @Test
    fun testStockRebuild_sumsAllMovementsAcrossMultipleDates_notJustOneDay() = runBlocking {
        val itemId = "item_multi_day_stock"
        val item = CatalogItem(
            id = itemId,
            name = "Sugar 1kg",
            pricePaise = 4400L,
            baseUnit = BaseUnitType.GRAM,
            displayUnit = DisplayUnit.KG,
            inventoryType = InventoryType.STOCK,
            stockBaseUnits = 0L
        )
        catalogDao.insert(item)

        // Day 1 (2026-09-25): Initial shipment +100 kg
        stockMovementDao.insertMovement(
            StockMovement(itemId = itemId, deltaBaseUnits = 100_000L, reason = MovementReason.PURCHASE, businessDate = "2026-09-25")
        )
        // Day 2 (2026-09-26): Sold -15 kg
        stockMovementDao.insertMovement(
            StockMovement(itemId = itemId, deltaBaseUnits = -15_000L, reason = MovementReason.SALE, businessDate = "2026-09-26")
        )
        // Day 3 (2026-09-27): Sold -20 kg
        stockMovementDao.insertMovement(
            StockMovement(itemId = itemId, deltaBaseUnits = -20_000L, reason = MovementReason.SALE, businessDate = "2026-09-27")
        )
        // Day 4 (2026-09-28): Restocked +50 kg, Spoilage -2 kg
        stockMovementDao.insertMovement(
            StockMovement(itemId = itemId, deltaBaseUnits = 50_000L, reason = MovementReason.PURCHASE, businessDate = "2026-09-28")
        )
        stockMovementDao.insertMovement(
            StockMovement(itemId = itemId, deltaBaseUnits = -2_000L, reason = MovementReason.ADJUSTMENT, businessDate = "2026-09-28")
        )
        // Day 5 (Today 2026-09-29): Sold -10 kg
        stockMovementDao.insertMovement(
            StockMovement(itemId = itemId, deltaBaseUnits = -10_000L, reason = MovementReason.SALE, businessDate = "2026-09-29")
        )

        // Expected Net across ALL 5 days: 100 - 15 - 20 + 50 - 2 - 10 = 103 kg (103,000 grams)
        val netStock = repository.rebuildStockFromLedger(itemId)
        assertEquals(103_000L, netStock)

        // Confirm database was updated
        val updated = catalogDao.getById(itemId)
        assertEquals(103_000L, updated?.stockBaseUnits)
    }

    @Test
    fun testVoidBill_writesReverseVoidMovementsAndDoesNotDelete() = runBlocking {
        val lines = listOf(
            CartLine(stockItem.id, stockItem.name, 4L, stockItem.unit, stockItem.pricePaise, InventoryType.STOCK)
        )
        val commitRes = billingQueue.enqueueCommit(lines, PaymentMode.CASH, 2000L)
        assertTrue(commitRes is BillingQueue.CommitResult.Success)
        val billId = (commitRes as BillingQueue.CommitResult.Success).bill.id

        // Stock decreased: 50 - 4 = 46
        assertEquals(46L, catalogDao.getById(stockItem.id)?.stockBaseUnits)

        // Void the bill
        val voided = repository.voidBill(billId)
        assertTrue(voided)

        // Stock restored to 50
        assertEquals(50L, catalogDao.getById(stockItem.id)?.stockBaseUnits)

        // Bill record still exists but marked VOIDED (never deleted)
        val bill = billDao.getBillById(billId)
        assertNotNull(bill)
        assertEquals(BillStatus.VOIDED, bill?.status)

        // Movements ledger contains reverse VOID movement
        val movements = stockMovementDao.getMovementsForItem(stockItem.id).first()
        assertTrue(movements.any { it.reason == MovementReason.VOID && it.deltaBaseUnits == 4L })
    }

    @Test
    fun testCloseBusinessDay_snapshotsFlowAndProducesCorrectSummaryAndReorderList() = runBlocking {
        // Bill 1: 2 Parle-G (Cash ₹10.00)
        billingQueue.enqueueCommit(
            listOf(CartLine(stockItem.id, stockItem.name, 2L, stockItem.unit, stockItem.pricePaise, InventoryType.STOCK)),
            PaymentMode.CASH,
            1000L
        )

        // Bill 2: 40 Parle-G (UPI ₹200.00) -> brings remaining stock to 8 <= threshold (10)
        billingQueue.enqueueCommit(
            listOf(CartLine(stockItem.id, stockItem.name, 40L, stockItem.unit, stockItem.pricePaise, InventoryType.STOCK)),
            PaymentMode.UPI,
            20000L
        )

        // Bill 3: 1500 ml (1.5 L) Milk (Cash ₹42.00)
        billingQueue.enqueueCommit(
            listOf(CartLine(flowItem.id, flowItem.name, 1500L, flowItem.unit, flowItem.pricePaise, InventoryType.FLOW)),
            PaymentMode.CASH,
            4200L
        )

        val summary = repository.closeBusinessDay()

        assertEquals(25200L, summary.totalRevenuePaise)
        assertEquals(5200L, summary.cashRevenuePaise)
        assertEquals(20000L, summary.upiRevenuePaise)
        assertEquals(3, summary.totalBills)

        // FLOW purchase plan contains Amul Milk with 1.5 L
        val milkPlan = summary.flowPurchasePlan.find { it.itemId == flowItem.id }
        assertNotNull(milkPlan)
        assertEquals(1500L, milkPlan?.soldBaseUnits)
        assertEquals(1.5, milkPlan?.suggestedPurchaseDisplayUnits ?: 0.0, 0.001)

        // STOCK reorder list contains Parle-G (stock 8 <= threshold 10)
        val reorderParleG = summary.stockReorderList.find { it.id == stockItem.id }
        assertNotNull(reorderParleG)
        assertEquals(8L, reorderParleG?.stockBaseUnits)
    }

    @Test
    fun testReorderThresholdCrossing() = runBlocking {
        val biscuit = CatalogItem(
            id = "item_biscuit_threshold",
            name = "Marie Gold",
            pricePaise = 1000L,
            baseUnit = BaseUnitType.PIECE,
            displayUnit = DisplayUnit.PCS,
            inventoryType = InventoryType.STOCK,
            stockBaseUnits = 12L,
            reorderThresholdBaseUnits = 10L
        )
        catalogDao.insert(biscuit)

        // 1. Initially stock is 12 (> 10 threshold) -> NOT in low stock list
        val initialLowStock = catalogDao.getLowStockItemsList()
        assertFalse(
            "Item with stock > threshold should not be in reorder list",
            initialLowStock.any { it.id == biscuit.id }
        )

        // 2. Sell 3 units -> stock becomes 9, which crosses below the threshold (<= 10)
        val commitRes = billingQueue.enqueueCommit(
            listOf(CartLine(biscuit.id, biscuit.name, 3L, biscuit.unit, biscuit.pricePaise, InventoryType.STOCK)),
            PaymentMode.CASH,
            3000L
        )
        assertTrue(commitRes is BillingQueue.CommitResult.Success)

        // Verify stock is now 9
        val updated = catalogDao.getById(biscuit.id)
        assertNotNull(updated)
        assertEquals(9L, updated?.stockBaseUnits)

        // 3. Confirm threshold crossing: item MUST appear in low stock / reorder list
        val afterSaleLowStock = catalogDao.getLowStockItemsList()
        val lowStockItem = afterSaleLowStock.find { it.id == biscuit.id }
        assertNotNull("Item must appear in reorder list when stock crosses threshold", lowStockItem)
        assertEquals(9L, lowStockItem?.stockBaseUnits)
        assertEquals(10L, lowStockItem?.reorderThresholdBaseUnits)

        // 4. Restock 20 units -> stock becomes 29 (> 10 threshold)
        catalogDao.setStock(biscuit.id, 29L)

        // 5. Item should no longer appear in low stock list
        val afterRestockLowStock = catalogDao.getLowStockItemsList()
        assertFalse(
            "Item must exit reorder list when restocked above threshold",
            afterRestockLowStock.any { it.id == biscuit.id }
        )
    }

    @Test
    fun testSequentialBillNumbers_assignedPerDeviceInCommitTransaction() = runBlocking {
        // Confirm initial sequence in bill_counter
        val initialSeq = billCounterDao.getLastSequence("DEV_01") ?: 0L

        // Commit bill 1
        val res1 = billingQueue.enqueueCommit(
            listOf(CartLine(stockItem.id, stockItem.name, 1L, stockItem.unit, stockItem.pricePaise, InventoryType.STOCK)),
            PaymentMode.CASH,
            500L
        )
        assertTrue(res1 is BillingQueue.CommitResult.Success)
        val bill1 = (res1 as BillingQueue.CommitResult.Success).bill
        val expectedSeq1 = initialSeq + 1L
        assertEquals("#%04d".format(expectedSeq1), bill1.billNumber)

        // Commit bill 2
        val res2 = billingQueue.enqueueCommit(
            listOf(CartLine(stockItem.id, stockItem.name, 2L, stockItem.unit, stockItem.pricePaise, InventoryType.STOCK)),
            PaymentMode.UPI,
            1000L
        )
        assertTrue(res2 is BillingQueue.CommitResult.Success)
        val bill2 = (res2 as BillingQueue.CommitResult.Success).bill
        val expectedSeq2 = initialSeq + 2L
        assertEquals("#%04d".format(expectedSeq2), bill2.billNumber)

        // Commit bill 3
        val res3 = billingQueue.enqueueCommit(
            listOf(CartLine(flowItem.id, flowItem.name, 500L, flowItem.unit, flowItem.pricePaise, InventoryType.FLOW)),
            PaymentMode.CASH,
            1400L
        )
        assertTrue(res3 is BillingQueue.CommitResult.Success)
        val bill3 = (res3 as BillingQueue.CommitResult.Success).bill
        val expectedSeq3 = initialSeq + 3L
        assertEquals("#%04d".format(expectedSeq3), bill3.billNumber)

        // Verify bill_counter table persisted lastSequence correctly
        val finalSeq = billCounterDao.getLastSequence("DEV_01")
        assertEquals(expectedSeq3, finalSeq)

        // Replaying existing bill does NOT increment counter
        val replayBill = billingQueue.applyJournalTransaction(
            "fake_journal_id",
            BillingQueue.JournalPayload(
                billId = bill1.id, // already committed
                billNumber = "ignored",
                cartLines = emptyList(),
                paymentMode = PaymentMode.CASH,
                tenderedPaise = 500L,
                totalPaise = 500L,
                businessDate = bill1.businessDate,
                timestamp = System.currentTimeMillis()
            )
        )
        assertEquals(bill1.billNumber, replayBill.billNumber)
        assertEquals(finalSeq, billCounterDao.getLastSequence("DEV_01"))
    }

    @Test
    fun testCloseBusinessDay_isIdempotent() = runBlocking {
        val date = "2026-09-29"
        billingQueue.enqueueCommit(
            listOf(CartLine(stockItem.id, stockItem.name, 2L, stockItem.unit, stockItem.pricePaise, InventoryType.STOCK)),
            PaymentMode.CASH,
            1000L
        )

        // First Close Day execution
        val summary1 = repository.closeBusinessDay(date)
        assertEquals(date, summary1.businessDate)
        assertEquals(1000L, summary1.totalRevenuePaise)
        assertEquals(1, summary1.totalBills)

        // Second Close Day execution (idempotency check)
        val summary2 = repository.closeBusinessDay(date)
        assertEquals(summary1.businessDate, summary2.businessDate)
        assertEquals(summary1.totalRevenuePaise, summary2.totalRevenuePaise)
        assertEquals(summary1.totalBills, summary2.totalBills)
        assertEquals(summary1.cashRevenuePaise, summary2.cashRevenuePaise)
        assertEquals(summary1.upiRevenuePaise, summary2.upiRevenuePaise)

        // Verify closed_business_days record exists and is unique
        val closedDay = db.closedBusinessDayDao().getClosedDay(date)
        assertNotNull(closedDay)
        assertEquals(1000L, closedDay?.totalRevenuePaise)
        assertEquals(1, closedDay?.totalBills)
    }

    @Test
    fun testVoidAfterCloseDay_isBlocked() = runBlocking {
        val date = "2026-09-29"
        val commitRes = billingQueue.enqueueCommit(
            listOf(CartLine(stockItem.id, stockItem.name, 3L, stockItem.unit, stockItem.pricePaise, InventoryType.STOCK)),
            PaymentMode.CASH,
            1500L
        )
        assertTrue(commitRes is BillingQueue.CommitResult.Success)
        val billId = (commitRes as BillingQueue.CommitResult.Success).bill.id

        // Stock was decremented: 50 - 3 = 47
        assertEquals(47L, catalogDao.getById(stockItem.id)?.stockBaseUnits)

        // Close the business day
        repository.closeBusinessDay(date)
        assertTrue(db.closedBusinessDayDao().isDayClosed(date))

        // Attempt to void the bill after the day has been closed -> MUST BE BLOCKED
        val voidResult = repository.voidBill(billId)
        assertFalse("Void must be blocked after Close Day", voidResult)

        // Bill remains COMMITTED and stock is NOT refunded
        val bill = billDao.getBillById(billId)
        assertNotNull(bill)
        assertEquals(BillStatus.COMMITTED, bill?.status)
        assertEquals(47L, catalogDao.getById(stockItem.id)?.stockBaseUnits)
    }
}
