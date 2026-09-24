package com.bitefast.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "restaurants")
data class RestaurantEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String,
    val cuisine: String,
    val rating: Double,
    val distance: Float,
    val estimatedTime: Int,
    val priceLevel: Int,
    val imageUrl: String,
    val coverImageUrl: String,
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val phoneNumber: String,
    val openingHours: String,
    val isOpen: Boolean,
    val isFreeDelivery: Boolean,
    val isFavorite: Boolean
)
