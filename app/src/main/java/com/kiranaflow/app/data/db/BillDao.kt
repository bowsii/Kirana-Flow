package com.kiranaflow.app.data.db

import androidx.room.*
import com.kiranaflow.app.data.model.Bill
import com.kiranaflow.app.data.model.BillItem
import kotlinx.coroutines.flow.Flow

@Dao
interface BillDao {

    // ─── Bills ─────────────────────────────────────────────────────────────────

    @Query("SELECT * FROM bills ORDER BY createdAt DESC")
    fun getAllBills(): Flow<List<Bill>>

    @Query("SELECT * FROM bills ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentBills(limit: Int = 50): Flow<List<Bill>>

    @Query("SELECT * FROM bills WHERE id = :id LIMIT 1")
    suspend fun getBillById(id: Long): Bill?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBill(bill: Bill): Long

    @Update
    suspend fun updateBill(bill: Bill)

    // ─── Bill Items ────────────────────────────────────────────────────────────

    @Query("SELECT * FROM bill_items WHERE billId = :billId")
    suspend fun getItemsForBill(billId: Long): List<BillItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBillItems(items: List<BillItem>)

    // ─── Analytics helpers ─────────────────────────────────────────────────────

    @Query("""
        SELECT SUM(totalAmount) FROM bills 
        WHERE createdAt >= :fromEpoch AND status = 'COMMITTED'
    """)
    suspend fun totalRevenueSince(fromEpoch: Long): Double?

    @Query("SELECT COUNT(*) FROM bills WHERE status = 'COMMITTED'")
    suspend fun totalBillCount(): Int

    // ─── WAL recovery ─────────────────────────────────────────────────────────

    @Query("SELECT * FROM bills WHERE status = 'PENDING' ORDER BY walSeq ASC")
    suspend fun getPendingBills(): List<Bill>
}
