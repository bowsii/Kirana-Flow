package com.kiranaflow.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Represents a product in the kirana shop's catalog.
 *
 * STOCK items: tracked by shelf count (e.g. biscuits, soap)
 * FLOW  items: tracked by daily sold quantity (e.g. milk, curd, eggs)
 */
@Serializable
@Entity(tableName = "catalog_items")
data class CatalogItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    /** Display name (e.g. "Parle-G", "Amul Milk") */
    val name: String,

    /** Aliases for fuzzy voice matching (Tamil / Tanglish / English) */
    val aliases: String = "",          // JSON array stored as string

    /** Price in Indian Rupees */
    val price: Double,

    /** Unit of sale: "pcs", "kg", "g", "L", "ml", "pack" */
    val unit: String = "pcs",

    /** STOCK or FLOW */
    val inventoryType: InventoryType = InventoryType.STOCK,

    /** Only relevant for STOCK items */
    val stockQty: Double = 0.0,

    /** Reorder alert threshold (STOCK only) */
    val reorderThreshold: Double = 5.0,

    /** Units sold today (FLOW only, resets each day) */
    val flowSoldToday: Double = 0.0,

    /** Date string "YYYY-MM-DD" for FLOW daily reset */
    val flowDate: String = "",

    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Serializable
enum class InventoryType { STOCK, FLOW }
