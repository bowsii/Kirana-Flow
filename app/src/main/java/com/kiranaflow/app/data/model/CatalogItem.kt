package com.kiranaflow.app.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.kiranaflow.app.util.UuidV7
import kotlinx.serialization.Serializable

/**
 * Represents a product in the kirana shop's catalog.
 *
 * All money stored as Long in paise.
 * All quantities stored as Long in base units (pieces, grams, milliliters).
 *
 * STOCK items: tracked by shelf count (e.g. biscuits, soap, oil)
 * FLOW  items: tracked by daily sold quantity (e.g. milk, curd, eggs, vegetables)
 */
@Serializable
@Entity(
    tableName = "catalog_items",
    indices = [
        Index("name"),
        Index("inventoryType"),
        Index("createdAt"),
        Index("isActive")
    ]
)
data class CatalogItem(
    @PrimaryKey
    val id: String = UuidV7.generate(),

    /** Display name (e.g. "Parle-G", "Amul Milk") */
    val name: String,

    /** Aliases for fuzzy voice matching (Tamil / Tanglish / English) stored as JSON array */
    val aliases: String = "[]",

    /** Price in paise (e.g. ₹5.00 = 500 paise, ₹209.00 = 20900 paise) */
    val pricePaise: Long,

    /** Base unit type: PIECE, GRAM, MILLILITER */
    val baseUnit: BaseUnitType = BaseUnitType.PIECE,

    /** Display unit: PCS, KG, G, L, ML, PACK, DOZEN */
    val displayUnit: DisplayUnit = DisplayUnit.PCS,

    /** STOCK or FLOW */
    val inventoryType: InventoryType = InventoryType.STOCK,

    /** Materialized current count in base units (STOCK only) */
    val stockBaseUnits: Long = 0L,

    /** Reorder alert threshold in base units (STOCK only) */
    val reorderThresholdBaseUnits: Long = 0L,

    /** Units sold today (FLOW only) */
    val flowSoldToday: Double = 0.0,

    /** Device ID where created */
    val deviceId: String = "DEV_01",

    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val deletedAt: Long? = null
) {
    // ── Convenience properties for UI & calculations ──
    val price: Money get() = Money(pricePaise)
    val stockQuantity: Quantity get() = Quantity(stockBaseUnits)
    val reorderThreshold: Quantity get() = Quantity(reorderThresholdBaseUnits)
    val unit: String get() = displayUnit.label
    val priceDouble: Double get() = pricePaise / 100.0
    val stockQty: Double get() = stockBaseUnits / displayUnit.multiplierToBase.toDouble()
}

@Serializable
enum class InventoryType {
    STOCK,
    FLOW
}
