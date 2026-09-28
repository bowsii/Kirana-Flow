package com.kiranaflow.app.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.kiranaflow.app.util.UuidV7
import kotlinx.serialization.Serializable

@Serializable
enum class MovementReason {
    SALE,
    PURCHASE,
    ADJUSTMENT,
    RETURN,
    VOID
}

@Serializable
@Entity(
    tableName = "stock_movements",
    indices = [
        Index("itemId"),
        Index("businessDate"),
        Index("createdAt"),
        Index("refId")
    ]
)
data class StockMovement(
    @PrimaryKey
    val id: String = UuidV7.generate(),
    val itemId: String,
    val deltaBaseUnits: Long,
    val reason: MovementReason,
    val refId: String = "",
    val businessDate: String,
    val deviceId: String = "DEV_01",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val deletedAt: Long? = null
)
