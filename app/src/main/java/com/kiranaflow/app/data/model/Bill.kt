package com.kiranaflow.app.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * A committed bill / transaction.
 * Once written (via WAL), this record is immutable.
 */
@Serializable
@Entity(tableName = "bills")
data class Bill(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    /** Human-readable bill number e.g. "#1049" */
    val billNumber: String,

    /** Total amount in ₹ */
    val totalAmount: Double,

    /** Number of line items */
    val itemCount: Int,

    /** Payment mode: CASH, UPI, QR */
    val paymentMode: PaymentMode = PaymentMode.CASH,

    /** Amount tendered by customer */
    val tenderedAmount: Double = 0.0,

    /** Change returned */
    val changeAmount: Double = 0.0,

    /** Bill status */
    val status: BillStatus = BillStatus.COMMITTED,

    /** WAL sequence number for crash recovery */
    val walSeq: Long = 0L,

    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
enum class PaymentMode { CASH, UPI, QR }

@Serializable
enum class BillStatus { PENDING, COMMITTED, FAILED }

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
    indices = [Index(value = ["billId"])]
)
data class BillItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val billId: Long,
    val catalogItemId: Long,
    val itemName: String,
    val quantity: Double,
    val unit: String,
    val pricePerUnit: Double,
    val lineTotal: Double,
    val inventoryType: InventoryType = InventoryType.STOCK
)
