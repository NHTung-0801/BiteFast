package com.bitefast.core.data.repository

import com.bitefast.core.domain.repository.RatingRepository
import com.bitefast.core.model.Rating
import com.bitefast.core.network.api.BiteFastApiService
import com.bitefast.core.network.model.RestaurantRatingRequestDto
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RatingRepositoryImpl @Inject constructor(
    private val apiService: BiteFastApiService
) : RatingRepository {

    private val ratings = ConcurrentHashMap<String, Rating>()

    override suspend fun submitRating(rating: Rating): Rating {
        ratings[rating.orderId] = rating

        try {
            apiService.submitRestaurantRating(
                orderId = rating.orderId,
                body = RestaurantRatingRequestDto(
                    orderId = rating.orderId,
                    restaurantId = rating.restaurantId,
                    restaurantRating = rating.restaurantRating,
                    driverRating = rating.driverRating,
                    tags = rating.tags,
                    comment = rating.comment,
                    isAnonymous = rating.isAnonymous
                )
            )
        } catch (_: Exception) {}

        return rating
    }

    override suspend fun getRatingForOrder(orderId: String): Rating? {
        return ratings[orderId]
    }
}
