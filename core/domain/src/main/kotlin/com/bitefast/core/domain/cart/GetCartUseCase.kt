package com.bitefast.core.domain.cart

import com.bitefast.core.domain.repository.CartRepository
import com.bitefast.core.model.CartItem
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCartUseCase @Inject constructor(
    private val cartRepository: CartRepository
) {
    operator fun invoke(): Flow<List<CartItem>> = cartRepository.getCartItems()
}
