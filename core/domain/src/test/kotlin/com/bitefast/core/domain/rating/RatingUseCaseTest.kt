package com.bitefast.core.domain.rating

import com.bitefast.core.domain.repository.OrderRepository
import com.bitefast.core.domain.repository.RatingRepository
import com.bitefast.core.model.Order
import com.bitefast.core.model.OrderStatus
import com.bitefast.core.model.Rating
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class RatingUseCaseTest {

    private val orderRepository: OrderRepository = mockk()
    private val ratingRepository: RatingRepository = mockk()
    private lateinit var submitRatingUseCase: SubmitRatingUseCase

    @BeforeEach
    fun setUp() {
        submitRatingUseCase = SubmitRatingUseCase(orderRepository, ratingRepository)
    }

    @Test
    @DisplayName("SubmitRating: returns InvalidRating when stars outside 1..5")
    fun submitRating_invalidStars_returnsInvalidRating() = runTest {
        val result = submitRatingUseCase(
            orderId = "order_1",
            restaurantRating = 0,
            driverRating = 5
        )

        assertTrue(result is RatingResult.InvalidRating)
    }

    @Test
    @DisplayName("SubmitRating: returns OrderNotFound when order does not exist")
    fun submitRating_orderNotFound_returnsOrderNotFound() = runTest {
        coEvery { orderRepository.getOrderDetail("order_missing") } returns null

        val result = submitRatingUseCase(
            orderId = "order_missing",
            restaurantRating = 5,
            driverRating = 5
        )

        assertTrue(result is RatingResult.OrderNotFound)
    }

    @Test
    @DisplayName("SubmitRating: returns OrderNotDelivered when status is not DELIVERED")
    fun submitRating_orderNotDelivered_returnsOrderNotDelivered() = runTest {
        val order = createOrder(id = "order_1", status = OrderStatus.ON_THE_WAY)
        coEvery { orderRepository.getOrderDetail("order_1") } returns order

        val result = submitRatingUseCase(
            orderId = "order_1",
            restaurantRating = 5,
            driverRating = 5
        )

        assertTrue(result is RatingResult.OrderNotDelivered)
    }

    @Test
    @DisplayName("SubmitRating: returns AlreadyRated when order was already reviewed")
    fun submitRating_alreadyRated_returnsAlreadyRated() = runTest {
        val order = createOrder(id = "order_1", status = OrderStatus.DELIVERED)
        val existingRating = Rating(id = "r_1", orderId = "order_1")

        coEvery { orderRepository.getOrderDetail("order_1") } returns order
        coEvery { ratingRepository.getRatingForOrder("order_1") } returns existingRating

        val result = submitRatingUseCase(
            orderId = "order_1",
            restaurantRating = 5,
            driverRating = 5
        )

        assertTrue(result is RatingResult.AlreadyRated)
    }

    @Test
    @DisplayName("SubmitRating: masks username when isAnonymous is true")
    fun submitRating_anonymous_masksUserName() = runTest {
        val order = createOrder(id = "order_1", status = OrderStatus.DELIVERED)
        coEvery { orderRepository.getOrderDetail("order_1") } returns order
        coEvery { ratingRepository.getRatingForOrder("order_1") } returns null
        coEvery { ratingRepository.submitRating(any()) } answers { firstArg() }

        val result = submitRatingUseCase(
            orderId = "order_1",
            restaurantRating = 5,
            driverRating = 5,
            userName = "Nguyễn Văn A",
            isAnonymous = true
        )

        assertTrue(result is RatingResult.Success)
        val rating = (result as RatingResult.Success).rating
        assertEquals("Người dùng ẩn danh", rating.userName)
        assertTrue(rating.isAnonymous)
        coVerify(exactly = 1) { ratingRepository.submitRating(any()) }
    }

    @Test
    @DisplayName("SubmitRating: successfully submits rating with user name and tags")
    fun submitRating_valid_submitsSuccessfully() = runTest {
        val order = createOrder(id = "order_1", status = OrderStatus.DELIVERED)
        coEvery { orderRepository.getOrderDetail("order_1") } returns order
        coEvery { ratingRepository.getRatingForOrder("order_1") } returns null
        coEvery { ratingRepository.submitRating(any()) } answers { firstArg() }

        val result = submitRatingUseCase(
            orderId = "order_1",
            restaurantRating = 5,
            driverRating = 4,
            tags = listOf("Nóng hổi", "Giao nhanh"),
            comment = "Món ăn rất ngon, phục vụ chu đáo",
            userName = "Tùng Nguyễn",
            isAnonymous = false
        )

        assertTrue(result is RatingResult.Success)
        val rating = (result as RatingResult.Success).rating
        assertEquals("Tùng Nguyễn", rating.userName)
        assertEquals(5, rating.restaurantRating)
        assertEquals(4, rating.driverRating)
        assertEquals(2, rating.tags.size)
        assertEquals("Món ăn rất ngon, phục vụ chu đáo", rating.comment)
    }

    private fun createOrder(id: String, status: OrderStatus) = Order(
        id = id,
        userId = "user_101",
        restaurantId = "res_202",
        restaurantName = "Phúc Lộc Thọ",
        status = status
    )
}
