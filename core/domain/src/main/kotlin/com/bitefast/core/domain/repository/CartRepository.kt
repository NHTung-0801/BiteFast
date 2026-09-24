package com.bitefast.core.domain.repository

import com.bitefast.core.model.CartItem
import kotlinx.coroutines.flow.Flow

interface CartRepository {
    fun getCartItems(): Flow<List<CartItem>>
    suspend fun getCurrentRestaurantId(): String?
    suspend fun addItem(item: CartItem)
    suspend fun updateQuantity(itemId: String, quantity: Int)
    suspend fun removeItem(itemId: String)
    suspend fun clearCart()
}
