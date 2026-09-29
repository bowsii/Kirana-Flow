package com.kiranaflow.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kiranaflow.core.model.ClosedBusinessDay

@Dao
interface ClosedBusinessDayDao {

    @Query("SELECT * FROM closed_business_days WHERE businessDate = :date LIMIT 1")
    suspend fun getClosedDay(date: String): ClosedBusinessDay?

    @Query("SELECT EXISTS(SELECT 1 FROM closed_business_days WHERE businessDate = :date)")
    suspend fun isDayClosed(date: String): Boolean

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertClosedDay(day: ClosedBusinessDay): Long
}
