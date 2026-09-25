package com.bitefast.core.domain.rating

import com.bitefast.core.domain.repository.OrderRepository
import com.bitefast.core.domain.repository.RatingRepository
import com.bitefast.core.model.OrderStatus
import com.bitefast.core.model.Rating
import java.util.UUID
import javax.inject.Inject

sealed interface RatingResult {
    data class Success(val rating: Rating) : RatingResult
    data class InvalidRating(val message: String) : RatingResult
    data object OrderNotFound : RatingResult
    data class OrderNotDelivered(val message: String) : RatingResult
    data class AlreadyRated(val message: String) : RatingResult
}

class SubmitRatingUseCase @Inject constructor(
    private val orderRepository: OrderRepository,
    private val ratingRepository: RatingRepository
) {
    suspend operator fun invoke(
        orderId: String,
        restaurantRating: Int,
        driverRating: Int,
        tags: List<String> = emptyList(),
        comment: String = "",
        isAnonymous: Boolean = false,
        userName: String = ""
    ): RatingResult {
        if (restaurantRating !in 1..5 || driverRating !in 1..5) {
            return RatingResult.InvalidRating("Điểm đánh giá phải từ 1 đến 5 sao.")
        }

        val order = orderRepository.getOrderDetail(orderId)
            ?: return RatingResult.OrderNotFound

        if (order.status != OrderStatus.DELIVERED) {
            return RatingResult.OrderNotDelivered("Chỉ có thể đánh giá đơn hàng khi đã giao thành công.")
        }

        val existingRating = ratingRepository.getRatingForOrder(orderId)
        if (existingRating != null) {
            return RatingResult.AlreadyRated("Đơn hàng này đã được đánh giá trước đó.")
        }

        val displayName = if (isAnonymous) {
            "Người dùng ẩn danh"
        } else {
            userName.takeIf { it.isNotBlank() } ?: "Khách hàng BiteFast"
        }

        val rating = Rating(
            id = UUID.randomUUID().toString(),
            orderId = order.id,
            userId = order.userId,
            userName = displayName,
            restaurantId = order.restaurantId,
            restaurantRating = restaurantRating,
            driverRating = driverRating,
            tags = tags,
            comment = comment.trim(),
            isAnonymous = isAnonymous,
            createdAt = System.currentTimeMillis()
        )

        val savedRating = ratingRepository.submitRating(rating)
        return RatingResult.Success(savedRating)
    }
}
