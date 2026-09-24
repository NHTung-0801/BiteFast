package com.bitefast.core.domain.cart

import com.bitefast.core.domain.repository.CartRepository
import javax.inject.Inject

class UpdateCartQuantityUseCase @Inject constructor(
    private val cartRepository: CartRepository
) {
    suspend operator fun invoke(itemId: String, quantity: Int) {
        cartRepository.updateQuantity(itemId, quantity)
    }
}
