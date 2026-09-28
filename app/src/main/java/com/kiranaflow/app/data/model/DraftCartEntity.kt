package com.kiranaflow.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Persists the current in-progress cart to disk.
 * If the app is killed or restarts mid-billing, the draft is restored automatically.
 */
@Serializable
@Entity(tableName = "draft_cart")
data class DraftCartEntity(
    @PrimaryKey
    val id: String = "ACTIVE_CART",
    val cartJson: String,
    val updatedAt: Long = System.currentTimeMillis()
)
