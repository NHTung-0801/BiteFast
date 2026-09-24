package com.bitefast.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.bitefast.core.database.entity.CartItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CartDao {

    @Query("SELECT * FROM cart_items ORDER BY timestamp DESC")
    fun getCartItems(): Flow<List<CartItemEntity>>

    @Query("SELECT restaurantId FROM cart_items LIMIT 1")
    suspend fun getCurrentRestaurantId(): String?

    @Query("SELECT * FROM cart_items WHERE menuItemId = :menuItemId LIMIT 1")
    suspend fun getCartItemByMenuItemId(menuItemId: String): CartItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: CartItemEntity)

    @Update
    suspend fun updateItem(item: CartItemEntity)

    @Query("DELETE FROM cart_items WHERE id = :id")
    suspend fun deleteItemById(id: String)

    @Query("DELETE FROM cart_items")
    suspend fun clearCart()

    @Transaction
    suspend fun upsertCartItem(item: CartItemEntity) {
        val existing = getCartItemByMenuItemId(item.menuItemId)
        if (existing != null) {
            updateItem(existing.copy(quantity = existing.quantity + item.quantity))
        } else {
            insertItem(item)
        }
    }
}
