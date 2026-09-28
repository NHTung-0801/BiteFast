package com.bitefast.core.domain.restaurant

import app.cash.turbine.test
import com.bitefast.core.domain.repository.RestaurantRepository
import com.bitefast.core.model.MenuItem
import com.bitefast.core.model.Restaurant
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RestaurantUseCasesTest {

    private lateinit var restaurantRepository: RestaurantRepository
    private val testRestaurant = Restaurant(
        id = "rest_1",
        name = "Phở Thìn 13 Lò Đúc",
        rating = 4.8,
        distance = 1.2f
    )
    private val testMenuItem = MenuItem(
        id = "menu_1",
        restaurantId = "rest_1",
        name = "Phở Tái Lăn",
        price = 65000.0
    )

    @BeforeEach
    fun setUp() {
        restaurantRepository = mockk(relaxed = true)
    }

    @Test
    fun getRestaurantsUseCase_returnsFlowOfRestaurants() = runTest {
        every { restaurantRepository.getRestaurants("Phở", "Vietnamese") } returns flowOf(listOf(testRestaurant))

        val useCase = GetRestaurantsUseCase(restaurantRepository)
        useCase("Phở", "Vietnamese").test {
            val list = awaitItem()
            assertEquals(1, list.size)
            assertEquals("rest_1", list[0].id)
            awaitComplete()
        }
    }

    @Test
    fun getRestaurantDetailUseCase_returnsCombinedDetailResult() = runTest {
        coEvery { restaurantRepository.getRestaurantDetail("rest_1") } returns testRestaurant
        coEvery { restaurantRepository.getRestaurantMenu("rest_1") } returns listOf(testMenuItem)

        val useCase = GetRestaurantDetailUseCase(restaurantRepository)
        val result = useCase("rest_1")

        assertEquals(testRestaurant, result.restaurant)
        assertEquals(1, result.menuItems.size)
        assertEquals("menu_1", result.menuItems[0].id)
    }
}
