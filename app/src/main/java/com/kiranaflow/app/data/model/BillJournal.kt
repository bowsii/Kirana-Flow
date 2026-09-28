package com.kiranaflow.app.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.kiranaflow.app.util.UuidV7
import kotlinx.serialization.Serializable

@Serializable
enum class JournalStatus {
    PENDING,
    APPLIED
}

/**
 * Durable Write-Ahead Log table for crash recovery and audit.
 * When "Bill potru" is triggered:
 * 1. An fsync'd PENDING journal entry is written first.
 * 2. The full atomic transaction commits Bill + BillItems + StockMovements + FlowDaily,
 *    and updates journal status to APPLIED.
 * 3. On startup, all PENDING journal entries are replayed idempotently.
 */
@Serializable
@Entity(
    tableName = "bill_journal",
    indices = [
        Index("billId"),
        Index("status"),
        Index("createdAt")
    ]
)
data class BillJournal(
    @PrimaryKey
    val journalId: String = UuidV7.generate(),
    val billId: String,
    val payloadJson: String,
    val status: JournalStatus = JournalStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis(),
    val appliedAt: Long? = null
)
