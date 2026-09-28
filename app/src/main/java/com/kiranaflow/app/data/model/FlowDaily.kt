package com.kiranaflow.app.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.kiranaflow.app.util.UuidV7
import kotlinx.serialization.Serializable

/**
 * FLOW items: daily sales summary per item per business date.
 * Tomorrow's purchase quantity = today's soldBaseUnits.
 */
@Serializable
@Entity(
    tableName = "flow_daily",
    indices = [
        Index("itemId"),
        Index("businessDate"),
        Index(value = ["itemId", "businessDate"], unique = true)
    ]
)
data class FlowDaily(
    @PrimaryKey
    val id: String = UuidV7.generate(),
    val itemId: String,
    val businessDate: String,
    val soldBaseUnits: Long = 0L,
    val updatedAt: Long = System.currentTimeMillis()
)
