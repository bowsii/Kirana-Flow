package com.kiranaflow.core.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "closed_business_days")
data class ClosedBusinessDay(
    @PrimaryKey
    val businessDate: String,
    val closedAt: Long = System.currentTimeMillis(),
    val totalRevenuePaise: Long,
    val totalBills: Int,
    val cashRevenuePaise: Long,
    val upiRevenuePaise: Long
)
