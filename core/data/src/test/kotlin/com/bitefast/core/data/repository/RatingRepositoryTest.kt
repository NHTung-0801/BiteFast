package com.bitefast.core.data.repository

import com.bitefast.core.model.Rating
import com.bitefast.core.network.api.BiteFastApiService
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class RatingRepositoryTest {

    private lateinit var apiService: BiteFastApiService
    private lateinit var repository: RatingRepositoryImpl

    private val sampleRating = Rating(
        orderId = "ord_rate_123",
        restaurantId = "res_1",
        restaurantRating = 5,
        driverRating = 5,
        comment = "Món ăn ngon, giao hàng siêu nhanh!",
        tags = listOf("Ngon miệng", "Nhanh chóng"),
        createdAt = System.currentTimeMillis()
    )

    @BeforeEach
    fun setUp() {
        apiService = mockk(relaxed = true)
        repository = RatingRepositoryImpl(apiService)
    }

    @Test
    @DisplayName("submitRating caches rating and calls apiService")
    fun submitRating_savesAndCallsApi() = runTest {
        val result = repository.submitRating(sampleRating)

        assertEquals("ord_rate_123", result.orderId)
        coVerify(exactly = 1) {
            apiService.submitRestaurantRating(
                orderId = "ord_rate_123",
                body = any()
            )
        }

        val cached = repository.getRatingForOrder("ord_rate_123")
        assertNotNull(cached)
        assertEquals(5, cached?.restaurantRating)
        assertEquals("Món ăn ngon, giao hàng siêu nhanh!", cached?.comment)
    }

    @Test
    @DisplayName("getRatingForOrder returns null when not rated")
    fun getRatingForOrder_unratedReturnsNull() = runTest {
        val rating = repository.getRatingForOrder("ord_unrated_999")
        assertNull(rating)
    }
}
