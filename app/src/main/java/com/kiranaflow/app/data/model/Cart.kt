package com.kiranaflow.app.data.model

import kotlinx.serialization.Serializable

/**
 * A single cart line, held in-memory until "Bill potru".
 * This is NOT a Room entity — it lives only in ViewModel state.
 */
@Serializable
data class CartLine(
    val catalogItemId: Long,
    val itemName: String,
    val quantity: Double,
    val unit: String,
    val pricePerUnit: Double,
    val inventoryType: InventoryType = InventoryType.STOCK,
) {
    val lineTotal: Double get() = quantity * pricePerUnit
}

/**
 * In-memory cart.
 */
data class Cart(
    val lines: List<CartLine> = emptyList(),
    val isListening: Boolean = false,
    val lastVoiceText: String = ""
) {
    val totalAmount: Double get() = lines.sumOf { it.lineTotal }
    val itemCount: Int get() = lines.size

    fun addOrUpdate(line: CartLine): Cart {
        val existing = lines.indexOfFirst { it.catalogItemId == line.catalogItemId }
        return if (existing >= 0) {
            copy(lines = lines.toMutableList().also {
                it[existing] = it[existing].copy(quantity = it[existing].quantity + line.quantity)
            })
        } else {
            copy(lines = lines + line)
        }
    }

    fun removeLast(): Cart = if (lines.isEmpty()) this else copy(lines = lines.dropLast(1))

    fun clear(): Cart = copy(lines = emptyList(), lastVoiceText = "")
}
