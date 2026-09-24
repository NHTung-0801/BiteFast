package com.bitefast.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey
    val id: String,
    val cartId: String,
    val menuItemId: String,
    val restaurantId: String,
    val name: String,
    val price: Double,
    val quantity: Int,
    val notes: String,
    val imageUrl: String,
    val timestamp: Long
)
