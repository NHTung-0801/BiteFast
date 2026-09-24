package com.bitefast.core.data.repository

import com.bitefast.core.data.mapper.asEntity
import com.bitefast.core.data.mapper.asExternalModel
import com.bitefast.core.database.dao.CartDao
import com.bitefast.core.domain.repository.CartRepository
import com.bitefast.core.model.CartItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CartRepositoryImpl @Inject constructor(
    private val cartDao: CartDao
) : CartRepository {

    private val writeMutex = Mutex()

    override fun getCartItems(): Flow<List<CartItem>> {
        return cartDao.getCartItems().map { list ->
            list.map { it.asExternalModel() }
        }
    }

    override suspend fun getCurrentRestaurantId(): String? {
        return cartDao.getCurrentRestaurantId()
    }

    override suspend fun addItem(item: CartItem) {
        writeMutex.withLock {
            cartDao.upsertCartItem(item.asEntity())
        }
    }

    override suspend fun updateQuantity(itemId: String, quantity: Int) {
        writeMutex.withLock {
            if (quantity <= 0) {
                cartDao.deleteItemById(itemId)
            } else {
                val current = cartDao.getCartItemByMenuItemId(itemId)
                if (current != null) {
                    cartDao.updateItem(current.copy(quantity = quantity))
                }
            }
        }
    }

    override suspend fun removeItem(itemId: String) {
        writeMutex.withLock {
            cartDao.deleteItemById(itemId)
        }
    }

    override suspend fun clearCart() {
        writeMutex.withLock {
            cartDao.clearCart()
        }
    }
}
