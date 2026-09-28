package com.kiranaflow.app.data.db

import androidx.room.*
import com.kiranaflow.app.data.model.FlowDaily
import kotlinx.coroutines.flow.Flow

@Dao
interface FlowDailyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(flowDaily: FlowDaily)

    @Query("SELECT * FROM flow_daily WHERE businessDate = :businessDate")
    fun getFlowDailyForDate(businessDate: String): Flow<List<FlowDaily>>

    @Query("SELECT * FROM flow_daily WHERE businessDate = :businessDate")
    suspend fun getFlowDailyListForDate(businessDate: String): List<FlowDaily>

    @Query("SELECT * FROM flow_daily WHERE itemId = :itemId AND businessDate = :businessDate LIMIT 1")
    suspend fun getFlowDaily(itemId: String, businessDate: String): FlowDaily?

    @Query("""
        INSERT INTO flow_daily (id, itemId, businessDate, soldBaseUnits, deviceId, createdAt, updatedAt)
        VALUES (:id, :itemId, :businessDate, :deltaBaseUnits, 'DEV_01', :timestamp, :timestamp)
        ON CONFLICT(itemId, businessDate) DO UPDATE SET
            soldBaseUnits = soldBaseUnits + :deltaBaseUnits,
            updatedAt = :timestamp
    """)
    suspend fun recordFlowSale(
        id: String,
        itemId: String,
        businessDate: String,
        deltaBaseUnits: Long,
        timestamp: Long = System.currentTimeMillis()
    )
}
