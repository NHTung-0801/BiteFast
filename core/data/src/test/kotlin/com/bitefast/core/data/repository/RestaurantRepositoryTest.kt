package com.bitefast.core.data.repository

import com.bitefast.core.database.dao.RestaurantDao
import com.bitefast.core.database.entity.RestaurantEntity
import com.bitefast.core.network.api.BiteFastApiService
import com.bitefast.core.network.model.ApiResponse
import com.bitefast.core.network.model.RestaurantDetailDto
import com.bitefast.core.network.model.RestaurantDto
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RestaurantRepositoryTest {

    private lateinit var restaurantDao: RestaurantDao
    private lateinit var apiService: BiteFastApiService
    private lateinit var repository: RestaurantRepositoryImpl

    @BeforeEach
    fun setUp() {
        restaurantDao = mockk(relaxed = true)
        apiService = mockk(relaxed = true)
        repository = RestaurantRepositoryImpl(restaurantDao, apiService)
    }

    @Test
    fun `getRestaurants returns mapped restaurants from Room database`() = runTest {
        val sampleEntity = RestaurantEntity(
            id = "res_local_1",
            name = "Phúc Lộc Thọ Local",
            description = "Cơm tấm ngon chuẩn vị Sài Gòn",
            cuisine = "Cơm",
            rating = 4.8,
            distance = 1.0f,
            estimatedTime = 20,
            priceLevel = 2,
            imageUrl = "",
            coverImageUrl = "",
            latitude = 0.0,
            longitude = 0.0,
            address = "Địa chỉ local",
            phoneNumber = "",
            openingHours = "",
            isOpen = true,
            isFreeDelivery = true,
            isFavorite = false
        )

        coEvery { restaurantDao.getAllRestaurants() } returns flowOf(listOf(sampleEntity))

        val result = repository.getRestaurants(null, null).first()

        assertEquals(1, result.size)
        assertEquals("res_local_1", result.first().id)
        assertEquals("Phúc Lộc Thọ Local", result.first().name)
    }

    @Test
    fun `getRestaurantDetail returns remote detail when local is not found`() = runTest {
        coEvery { restaurantDao.getRestaurantById("res_remote_1") } returns null
        coEvery { apiService.getRestaurantDetail("res_remote_1") } returns ApiResponse(
            data = RestaurantDetailDto(
                restaurant = RestaurantDto(
                    id = "res_remote_1",
                    name = "Nhà hàng Remote",
                    cuisine = "Phở"
                )
            )
        )

        val restaurant = repository.getRestaurantDetail("res_remote_1")

        assertNotNull(restaurant)
        assertEquals("res_remote_1", restaurant.id)
        assertEquals("Nhà hàng Remote", restaurant.name)
    }
}
