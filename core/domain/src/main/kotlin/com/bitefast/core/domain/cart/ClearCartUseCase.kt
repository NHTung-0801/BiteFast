package com.bitefast.core.domain.cart

import com.bitefast.core.domain.repository.CartRepository
import javax.inject.Inject

class ClearCartUseCase @Inject constructor(
    private val cartRepository: CartRepository
) {
    suspend operator fun invoke() {
        cartRepository.clearCart()
    }
}
