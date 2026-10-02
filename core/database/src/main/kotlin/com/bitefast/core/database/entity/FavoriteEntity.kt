package com.bitefast.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_items")
data class FavoriteEntity(
    @PrimaryKey
    val id: String,
    val type: String, // "DISH" or "RESTAURANT"
    val targetId: String,
    val name: String,
    val description: String,
    val price: Double,
    val imageUrl: String,
    val rating: Double,
    val restaurantId: String,
    val restaurantName: String,
    val category: String,
    val createdAt: Long = System.currentTimeMillis()
)
