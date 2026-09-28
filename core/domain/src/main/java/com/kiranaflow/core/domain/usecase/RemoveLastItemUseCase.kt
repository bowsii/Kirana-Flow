package com.kiranaflow.core.domain.usecase

import com.kiranaflow.core.data.KiranaRepository
import com.kiranaflow.core.model.Cart
import javax.inject.Inject

class RemoveLastItemUseCase @Inject constructor(
    private val repository: KiranaRepository
) {
    suspend operator fun invoke(cart: Cart): Cart {
        val updatedCart = cart.removeLast()
        repository.saveDraftCart(updatedCart)
        return updatedCart
    }
}
