package com.bitefast.core.domain.cart

import com.bitefast.core.domain.repository.CartRepository
import com.bitefast.core.model.CartItem
import javax.inject.Inject

sealed interface AddToCartResult {
    data object Success : AddToCartResult
    data class Conflict(val currentRestaurantId: String, val newRestaurantId: String) : AddToCartResult
}

class AddToCartUseCase @Inject constructor(
    private val cartRepository: CartRepository
) {
    suspend operator fun invoke(item: CartItem): AddToCartResult {
        val currentRestaurantId = cartRepository.getCurrentRestaurantId()
        if (currentRestaurantId != null && currentRestaurantId != item.restaurantId) {
            return AddToCartResult.Conflict(currentRestaurantId, item.restaurantId)
        }
        cartRepository.addItem(item)
        return AddToCartResult.Success
    }
}
