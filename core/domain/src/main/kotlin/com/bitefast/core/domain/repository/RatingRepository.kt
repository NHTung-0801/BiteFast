package com.bitefast.core.domain.repository

import com.bitefast.core.model.Rating

interface RatingRepository {
    suspend fun submitRating(rating: Rating): Rating
    suspend fun getRatingForOrder(orderId: String): Rating?
}
