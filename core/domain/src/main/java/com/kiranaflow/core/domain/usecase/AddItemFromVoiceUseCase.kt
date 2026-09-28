package com.kiranaflow.core.domain.usecase

import com.kiranaflow.core.data.CatalogValidator
import com.kiranaflow.core.data.KiranaRepository
import com.kiranaflow.core.model.*
import javax.inject.Inject

class AddItemFromVoiceUseCase @Inject constructor(
    private val repository: KiranaRepository,
    private val catalogValidator: CatalogValidator
) {
    suspend operator fun invoke(
        cart: Cart,
        rawItemName: String,
        quantityDisplayUnits: Double = 1.0,
        catalog: List<CatalogItem>
    ): Cart {
        val matchedItem = catalogValidator.findBestMatch(rawItemName, catalog) ?: return cart
        val baseUnits = (quantityDisplayUnits * matchedItem.displayUnit.multiplierToBase + 0.5).toLong()
        val line = CartLine(
            catalogItemId = matchedItem.id,
            itemName = matchedItem.name,
            quantityBaseUnits = baseUnits,
            unit = matchedItem.displayUnit.name,
            pricePerUnitPaise = matchedItem.pricePaise,
            inventoryType = matchedItem.inventoryType
        )
        val updatedCart = cart.addOrUpdate(line)
        repository.saveDraftCart(updatedCart)
        return updatedCart
    }
}
