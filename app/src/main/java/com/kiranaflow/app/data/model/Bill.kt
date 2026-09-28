package com.kiranaflow.app.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.kiranaflow.app.util.UuidV7
import kotlinx.serialization.Serializable

/**
 * A committed bill / transaction.
 * All amounts stored as Long in paise.
 */
@Serializable
@Entity(
    tableName = "bills",
    indices = [
        Index("billNumber", unique = true),
        Index("businessDate"),
        Index("createdAt"),
        Index("status")
    ]
)
data class Bill(
    @PrimaryKey
    val id: String = UuidV7.generate(),

    /** Human-readable bill number e.g. "#1049" */
    val billNumber: String,

    /** Total amount in paise */
    val totalPaise: Long,

    /** Number of line items */
    val itemCount: Int,

    /** Payment mode: CASH, UPI, QR */
    val paymentMode: PaymentMode = PaymentMode.CASH,

    /** Amount tendered by customer in paise */
    val tenderedPaise: Long = 0L,

    /** Change returned in paise */
    val changePaise: Long = 0L,

    /** Bill status */
    val status: BillStatus = BillStatus.COMMITTED,

    /** Business date string "YYYY-MM-DD" */
    val businessDate: String = "",

    /** Terminal / Counter Device ID */
    val deviceId: String = "DEV_01",

    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val deletedAt: Long? = null
) {
    val totalAmount: Double get() = totalPaise / 100.0
    val tenderedAmount: Double get() = tenderedPaise / 100.0
    val changeAmount: Double get() = changePaise / 100.0

    val total: Money get() = Money(totalPaise)
    val tendered: Money get() = Money(tenderedPaise)
    val change: Money get() = Money(changePaise)
}

@Serializable
enum class PaymentMode {
    CASH,
    UPI,
    QR
}

@Serializable
enum class BillStatus {
    PENDING,
    COMMITTED,
    VOIDED,
    FAILED
}

/**
 * A single line item in a bill.
 */
@Serializable
@Entity(
    tableName = "bill_items",
    foreignKeys = [
        ForeignKey(
            entity = Bill::class,
            parentColumns = ["id"],
            childColumns = ["billId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("billId"),
        Index("catalogItemId"),
        Index("createdAt")
    ]
)
data class BillItem(
    @PrimaryKey
    val id: String = UuidV7.generate(),

    val billId: String,

    val catalogItemId: String,

    val itemName: String,

    /** Quantity in base units (grams, milliliters, pieces) */
    val quantityBaseUnits: Long,

    /** Display unit label e.g. "pcs", "kg", "L" */
    val unit: String = "pcs",

    /** Price per display unit in paise */
    val pricePerUnitPaise: Long,

    /** Line total in paise */
    val lineTotalPaise: Long,

    val inventoryType: InventoryType = InventoryType.STOCK,

    val deviceId: String = "DEV_01",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val deletedAt: Long? = null
) {
    val pricePerUnit: Double get() = pricePerUnitPaise / 100.0
    val lineTotal: Double get() = lineTotalPaise / 100.0
    val quantity: Double get() = when (unit.lowercase()) {
        "kg", "l" -> quantityBaseUnits / 1000.0
        "dozen" -> quantityBaseUnits / 12.0
        else -> quantityBaseUnits.toDouble()
    }
}
