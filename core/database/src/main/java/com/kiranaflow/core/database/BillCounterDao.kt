package com.kiranaflow.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.kiranaflow.core.model.BillCounter

@Dao
interface BillCounterDao {

    @Query("SELECT lastSequence FROM bill_counter WHERE deviceId = :deviceId LIMIT 1")
    suspend fun getLastSequence(deviceId: String): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(counter: BillCounter)

    /**
     * Atomically increments and returns the next sequential bill number per device within a transaction.
     */
    @Transaction
    suspend fun getNextSequence(deviceId: String): Long {
        val current = getLastSequence(deviceId) ?: 0L
        val next = current + 1L
        insertOrUpdate(
            BillCounter(
                deviceId = deviceId,
                lastSequence = next,
                updatedAt = System.currentTimeMillis()
            )
        )
        return next
    }
}
