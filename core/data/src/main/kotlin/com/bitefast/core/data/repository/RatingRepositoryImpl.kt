package com.bitefast.core.data.repository

import com.bitefast.core.domain.repository.RatingRepository
import com.bitefast.core.model.Rating
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RatingRepositoryImpl @Inject constructor() : RatingRepository {

    private val ratings = ConcurrentHashMap<String, Rating>()

    override suspend fun submitRating(rating: Rating): Rating {
        ratings[rating.orderId] = rating
        return rating
    }

    override suspend fun getRatingForOrder(orderId: String): Rating? {
        return ratings[orderId]
    }
}
