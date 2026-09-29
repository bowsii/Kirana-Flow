package com.kiranaflow.core.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "bill_counter")
data class BillCounter(
    @PrimaryKey
    val deviceId: String = "DEV_01",
    val lastSequence: Long = 0L,
    val updatedAt: Long = System.currentTimeMillis()
)
