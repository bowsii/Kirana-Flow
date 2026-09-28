package com.kiranaflow.app.data.model

import kotlinx.serialization.Serializable

/**
 * A single cart line, held in-memory or persisted in draft_cart table.
 */
@Serializable
data class CartLine(
    val catalogItemId: String,
    val itemName: String,
    val quantityBaseUnits: Long,
    val unit: String,
    val pricePerUnitPaise: Long,
    val inventoryType: InventoryType = InventoryType.STOCK,
) {
    val quantity: Double get() = when (unit.lowercase()) {
        "kg", "l" -> quantityBaseUnits / 1000.0
        "dozen" -> quantityBaseUnits / 12.0
        else -> quantityBaseUnits.toDouble()
    }

    val pricePerUnit: Double get() = pricePerUnitPaise / 100.0

    val lineTotalPaise: Long get() = (quantity * pricePerUnitPaise + 0.5).toLong()
    val lineTotal: Double get() = lineTotalPaise / 100.0
    val totalMoney: Money get() = Money(lineTotalPaise)
}

/**
 * In-memory / active draft cart.
 */
@Serializable
data class Cart(
    val lines: List<CartLine> = emptyList(),
    val isListening: Boolean = false,
    val lastVoiceText: String = ""
) {
    val totalPaise: Long get() = lines.sumOf { it.lineTotalPaise }
    val totalAmount: Double get() = totalPaise / 100.0
    val totalMoney: Money get() = Money(totalPaise)
    val itemCount: Int get() = lines.size

    fun addOrUpdate(line: CartLine): Cart {
        val existing = lines.indexOfFirst { it.catalogItemId == line.catalogItemId }
        return if (existing >= 0) {
            copy(lines = lines.toMutableList().also {
                val current = it[existing]
                it[existing] = current.copy(
                    quantityBaseUnits = current.quantityBaseUnits + line.quantityBaseUnits
                )
            })
        } else {
            copy(lines = lines + line)
        }
    }

    fun removeLast(): Cart = if (lines.isEmpty()) this else copy(lines = lines.dropLast(1))

    fun clear(): Cart = copy(lines = emptyList(), lastVoiceText = "")
}
