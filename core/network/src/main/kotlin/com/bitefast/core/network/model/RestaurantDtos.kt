package com.bitefast.core.network.model

import kotlinx.serialization.Serializable

@Serializable
data class RestaurantDto(
    val id: String,
    val name: String,
    val description: String = "",
    val cuisine: String = "",
    val rating: Double = 0.0,
    val distance: Float = 0f,
    val estimatedTime: Int = 0,
    val priceLevel: Int = 1,
    val imageUrl: String = "",
    val coverImageUrl: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val address: String = "",
    val phoneNumber: String = "",
    val openingHours: String = "",
    val isOpen: Boolean = true,
    val isFreeDelivery: Boolean = false,
    val tags: List<String> = emptyList(),
    val totalOrders: Int = 0
)

@Serializable
data class MenuItemDto(
    val id: String,
    val restaurantId: String,
    val name: String,
    val description: String = "",
    val price: Double,
    val currency: String = "VND",
    val imageUrl: String = "",
    val category: String = "",
    val isAvailable: Boolean = true,
    val isVegetarian: Boolean = false,
    val isSpicy: Boolean = false,
    val isPopular: Boolean = false,
    val preparationTime: Int = 0,
    val tags: List<String> = emptyList(),
    val rating: Double = 4.8,
    val reviewCount: Int = 50
)

@Serializable
data class RestaurantDetailDto(
    val restaurant: RestaurantDto,
    val menu: List<MenuItemDto> = emptyList(),
    val categories: List<String> = emptyList()
)
