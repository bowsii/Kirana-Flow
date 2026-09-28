package com.kiranaflow.core.database

import androidx.room.*
import com.kiranaflow.core.model.BillJournal
import com.kiranaflow.core.model.JournalStatus

@Dao
interface JournalDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJournal(entry: BillJournal)

    @Update
    suspend fun updateJournal(entry: BillJournal)

    @Query("SELECT * FROM bill_journal WHERE status = :status ORDER BY createdAt ASC")
    suspend fun getJournalsByStatus(status: JournalStatus): List<BillJournal>

    @Query("SELECT * FROM bill_journal WHERE billId = :billId LIMIT 1")
    suspend fun getJournalByBillId(billId: String): BillJournal?

    @Query("UPDATE bill_journal SET status = :status, appliedAt = :appliedAt WHERE journalId = :journalId")
    suspend fun markApplied(
        journalId: String,
        status: JournalStatus = JournalStatus.APPLIED,
        appliedAt: Long = System.currentTimeMillis()
    )
}
