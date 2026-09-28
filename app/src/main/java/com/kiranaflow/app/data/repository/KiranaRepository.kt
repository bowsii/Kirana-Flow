package com.kiranaflow.app.data.repository

import com.kiranaflow.app.data.db.*
import com.kiranaflow.app.data.model.*
import com.kiranaflow.app.service.BillingQueue
import com.kiranaflow.app.service.CatalogValidator
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KiranaRepository @Inject constructor(
    private val catalogDao: CatalogDao,
    private val billDao: BillDao,
    private val billingQueue: BillingQueue,
    private val catalogValidator: CatalogValidator,
    private val draftCartDao: DraftCartDao,
    private val stockMovementDao: StockMovementDao,
    private val flowDailyDao: FlowDailyDao,
    private val businessDayManager: BusinessDayManager
) {

    // ─── Catalog ────────────────────────────────────────────────────────────

    fun getActiveCatalog(): Flow<List<CatalogItem>> = catalogDao.getAllActive()

    fun getLowStockItems(): Flow<List<CatalogItem>> = catalogDao.getLowStockItems()

    suspend fun searchCatalog(query: String): List<CatalogItem> = catalogDao.search(query)

    fun findBestMatch(query: String, catalog: List<CatalogItem>): CatalogItem? =
        catalogValidator.findBestMatch(query, catalog)

    suspend fun addCatalogItem(item: CatalogItem) = catalogDao.insert(item)

    suspend fun updateCatalogItem(item: CatalogItem) = catalogDao.update(item)

    suspend fun updateReorderThreshold(id: String, thresholdBaseUnits: Long) =
        catalogDao.updateReorderThreshold(id, thresholdBaseUnits)

    suspend fun rebuildStockFromLedger(itemId: String): Long {
        val netStock = stockMovementDao.computeStockFromLedger(itemId)
        catalogDao.setStock(itemId, netStock)
        return netStock
    }

    // ─── Bills ──────────────────────────────────────────────────────────────

    fun getRecentBills(limit: Int = 50): Flow<List<Bill>> = billDao.getRecentBills(limit)

    suspend fun getBillItems(billId: String): List<BillItem> = billDao.getItemsForBill(billId)

    suspend fun commitBill(
        cartLines: List<CartLine>,
        paymentMode: PaymentMode = PaymentMode.CASH,
        tenderedPaise: Long = 0L
    ): BillingQueue.CommitResult = billingQueue.enqueueCommit(cartLines, paymentMode, tenderedPaise)

    suspend fun commitBill(
        cartLines: List<CartLine>,
        paymentMode: PaymentMode = PaymentMode.CASH,
        tenderedAmount: Double = 0.0
    ): BillingQueue.CommitResult {
        val tenderedPaise = (tenderedAmount * 100.0 + 0.5).toLong()
        return billingQueue.enqueueCommit(cartLines, paymentMode, tenderedPaise)
    }

    suspend fun voidBill(billId: String, reason: String = "Customer Void"): Boolean =
        billingQueue.voidBill(billId, reason)

    // ─── Draft Cart (Crash Resilience) ──────────────────────────────────────

    suspend fun saveDraftCart(cart: Cart) {
        try {
            val json = Json.encodeToString(cart)
            draftCartDao.saveDraft(DraftCartEntity(cartJson = json))
        } catch (_: Exception) {}
    }

    suspend fun loadDraftCart(): Cart? {
        val entity = draftCartDao.getDraft() ?: return null
        return try {
            Json.decodeFromString<Cart>(entity.cartJson)
        } catch (_: Exception) {
            null
        }
    }

    suspend fun clearDraftCart() {
        draftCartDao.clearDraft()
    }

    // ─── Daily Flow Plan & Reorder ──────────────────────────────────────────

    fun getFlowDailyForDate(date: String = businessDayManager.getBusinessDate()): Flow<List<FlowDaily>> =
        flowDailyDao.getFlowDailyForDate(date)

    suspend fun getFlowPurchasePlan(businessDate: String = businessDayManager.getBusinessDate()): List<FlowPurchasePlanItem> {
        val flowRecords = flowDailyDao.getFlowDailyListForDate(businessDate)
        return flowRecords.mapNotNull { record ->
            val catalogItem = catalogDao.getById(record.itemId)
            if (catalogItem != null && record.soldBaseUnits > 0) {
                val displayQty = record.soldBaseUnits.toDouble() / catalogItem.displayUnit.multiplierToBase
                FlowPurchasePlanItem(
                    itemId = record.itemId,
                    itemName = catalogItem.name,
                    soldBaseUnits = record.soldBaseUnits,
                    suggestedPurchaseDisplayUnits = displayQty,
                    displayUnit = catalogItem.displayUnit.name
                )
            } else null
        }
    }

    suspend fun getStockReorderList(): List<CatalogItem> =
        catalogDao.getLowStockItemsList()

    suspend fun closeBusinessDay(businessDate: String = businessDayManager.getBusinessDate()): DaySummary {
        val totalRevenuePaise = billDao.getRevenueForDate(businessDate)
        val cashRevenuePaise  = billDao.getCashRevenueForDate(businessDate)
        val upiRevenuePaise   = billDao.getUpiRevenueForDate(businessDate)
        val billCount         = billDao.getBillCountForDate(businessDate)
        val flowPlan          = getFlowPurchasePlan(businessDate)
        val reorderList       = getStockReorderList()

        return DaySummary(
            businessDate      = businessDate,
            totalRevenuePaise = totalRevenuePaise,
            totalBills        = billCount,
            cashRevenuePaise  = cashRevenuePaise,
            upiRevenuePaise   = upiRevenuePaise,
            flowPurchasePlan  = flowPlan,
            stockReorderList  = reorderList
        )
    }

    // ─── Analytics ──────────────────────────────────────────────────────────

    suspend fun totalBillCount(): Int = billDao.totalBillCount()

    suspend fun totalRevenueSince(fromEpoch: Long): Long =
        billDao.totalRevenueSince(fromEpoch)

    suspend fun getTodayRevenue(businessDate: String = businessDayManager.getBusinessDate()): Long =
        billDao.getRevenueForDate(businessDate)

    suspend fun getTodayCashRevenue(businessDate: String = businessDayManager.getBusinessDate()): Long =
        billDao.getCashRevenueForDate(businessDate)

    suspend fun getTodayUpiRevenue(businessDate: String = businessDayManager.getBusinessDate()): Long =
        billDao.getUpiRevenueForDate(businessDate)

    // ─── WAL Recovery ───────────────────────────────────────────────────────

    suspend fun replayPendingBills() = billingQueue.replayPendingJournals()
}
