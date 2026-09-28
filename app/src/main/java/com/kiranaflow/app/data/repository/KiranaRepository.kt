package com.kiranaflow.app.data.repository

import com.kiranaflow.app.data.db.BillDao
import com.kiranaflow.app.data.db.CatalogDao
import com.kiranaflow.app.data.model.*
import com.kiranaflow.app.service.BillingQueue
import com.kiranaflow.app.service.CatalogValidator
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KiranaRepository @Inject constructor(
    private val catalogDao: CatalogDao,
    private val billDao: BillDao,
    private val billingQueue: BillingQueue,
    private val catalogValidator: CatalogValidator
) {

    // ─── Catalog ────────────────────────────────────────────────────────────

    fun getActiveCatalog(): Flow<List<CatalogItem>> = catalogDao.getAllActive()

    fun getLowStockItems(): Flow<List<CatalogItem>> = catalogDao.getLowStockItems()

    suspend fun searchCatalog(query: String): List<CatalogItem> = catalogDao.search(query)

    suspend fun findBestMatch(query: String, catalog: List<CatalogItem>): CatalogItem? =
        catalogValidator.findBestMatch(query, catalog)

    suspend fun addCatalogItem(item: CatalogItem): Long = catalogDao.insert(item)

    suspend fun updateCatalogItem(item: CatalogItem) = catalogDao.update(item)

    // ─── Bills ──────────────────────────────────────────────────────────────

    fun getRecentBills(): Flow<List<Bill>> = billDao.getRecentBills(50)

    suspend fun getBillItems(billId: Long): List<BillItem> = billDao.getItemsForBill(billId)

    suspend fun commitBill(
        cartLines: List<CartLine>,
        paymentMode: PaymentMode = PaymentMode.CASH,
        tenderedAmount: Double = 0.0
    ): BillingQueue.CommitResult = billingQueue.commitBill(cartLines, paymentMode, tenderedAmount)

    // ─── Analytics ──────────────────────────────────────────────────────────

    suspend fun totalBillCount(): Int = billDao.totalBillCount()

    suspend fun totalRevenueSince(fromEpoch: Long): Double =
        billDao.totalRevenueSince(fromEpoch) ?: 0.0

    // ─── WAL Recovery ───────────────────────────────────────────────────────

    suspend fun replayPendingBills() = billingQueue.replayPendingBills()
}
