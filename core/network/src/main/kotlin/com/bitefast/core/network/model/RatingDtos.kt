package com.bitefast.core.network.model

import kotlinx.serialization.Serializable

@Serializable
data class DishReviewDto(
    val id: String,
    val dishId: String,
    val authorName: String,
    val ratingStars: Int,
    val comment: String = "",
    val tags: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
data class CreateDishReviewRequestDto(
    val dishId: String,
    val ratingStars: Int,
    val comment: String = "",
    val tags: List<String> = emptyList()
)

@Serializable
data class RestaurantRatingRequestDto(
    val orderId: String,
    val restaurantId: String,
    val restaurantRating: Int,
    val driverRating: Int = 5,
    val tags: List<String> = emptyList(),
    val comment: String = "",
    val isAnonymous: Boolean = false
)
