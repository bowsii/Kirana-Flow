package com.kiranaflow.app.data.db

import androidx.room.*
import com.kiranaflow.app.data.model.StockMovement
import kotlinx.coroutines.flow.Flow

@Dao
interface StockMovementDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovement(movement: StockMovement)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovements(movements: List<StockMovement>)

    @Query("SELECT * FROM stock_movements WHERE itemId = :itemId ORDER BY createdAt DESC")
    fun getMovementsForItem(itemId: String): Flow<List<StockMovement>>

    @Query("SELECT * FROM stock_movements WHERE businessDate = :businessDate ORDER BY createdAt DESC")
    fun getMovementsForDate(businessDate: String): Flow<List<StockMovement>>

    @Query("SELECT COALESCE(SUM(deltaBaseUnits), 0) FROM stock_movements WHERE itemId = :itemId")
    suspend fun computeStockFromLedger(itemId: String): Long
}
