package com.kiranaflow.core.database

import androidx.room.*
import com.kiranaflow.core.model.Bill
import com.kiranaflow.core.model.BillItem
import kotlinx.coroutines.flow.Flow

@Dao
interface BillDao {

    @Query("SELECT * FROM bills WHERE deletedAt IS NULL ORDER BY createdAt DESC")
    fun getAllBills(): Flow<List<Bill>>

    @Query("SELECT * FROM bills WHERE deletedAt IS NULL ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentBills(limit: Int = 50): Flow<List<Bill>>

    @Query("SELECT * FROM bills WHERE id = :id AND deletedAt IS NULL LIMIT 1")
    suspend fun getBillById(id: String): Bill?

    @Query("SELECT * FROM bills WHERE billNumber = :billNumber LIMIT 1")
    suspend fun getBillByNumber(billNumber: String): Bill?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBill(bill: Bill)

    @Update
    suspend fun updateBill(bill: Bill)

    @Query("SELECT * FROM bill_items WHERE billId = :billId")
    suspend fun getItemsForBill(billId: String): List<BillItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBillItems(items: List<BillItem>)

    @Query("""
        SELECT COALESCE(SUM(totalPaise), 0) FROM bills 
        WHERE createdAt >= :fromEpoch AND status = 'COMMITTED' AND deletedAt IS NULL
    """)
    suspend fun totalRevenueSince(fromEpoch: Long): Long

    @Query("SELECT COUNT(*) FROM bills WHERE status = 'COMMITTED' AND deletedAt IS NULL")
    suspend fun totalBillCount(): Int

    @Query("""
        SELECT COUNT(*) FROM bills 
        WHERE businessDate = :businessDate AND status = 'COMMITTED' AND deletedAt IS NULL
    """)
    suspend fun getBillCountForDate(businessDate: String): Int

    @Query("""
        SELECT COALESCE(SUM(totalPaise), 0) FROM bills 
        WHERE businessDate = :businessDate AND status = 'COMMITTED' AND deletedAt IS NULL
    """)
    suspend fun getRevenueForDate(businessDate: String): Long

    @Query("""
        SELECT COALESCE(SUM(totalPaise), 0) FROM bills 
        WHERE businessDate = :businessDate AND paymentMode = 'CASH' AND status = 'COMMITTED' AND deletedAt IS NULL
    """)
    suspend fun getCashRevenueForDate(businessDate: String): Long

    @Query("""
        SELECT COALESCE(SUM(totalPaise), 0) FROM bills 
        WHERE businessDate = :businessDate AND paymentMode IN ('UPI', 'QR') AND status = 'COMMITTED' AND deletedAt IS NULL
    """)
    suspend fun getUpiRevenueForDate(businessDate: String): Long

    @Query("""
        SELECT COUNT(*) FROM bills 
        WHERE businessDate = :businessDate AND paymentMode = 'CASH' AND status = 'COMMITTED' AND deletedAt IS NULL
    """)
    suspend fun getCashCountForDate(businessDate: String): Int

    @Query("""
        SELECT COUNT(*) FROM bills 
        WHERE businessDate = :businessDate AND paymentMode IN ('UPI', 'QR') AND status = 'COMMITTED' AND deletedAt IS NULL
    """)
    suspend fun getUpiCountForDate(businessDate: String): Int

    @Query("SELECT * FROM bills WHERE status = 'PENDING' ORDER BY createdAt ASC")
    suspend fun getPendingBills(): List<Bill>
}
