package com.bitefast.feature.discovery

import androidx.lifecycle.SavedStateHandle
import com.bitefast.core.domain.repository.RestaurantRepository
import com.bitefast.core.domain.restaurant.GetRestaurantsUseCase
import com.bitefast.core.model.Restaurant
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DiscoveryViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var getRestaurantsUseCase: GetRestaurantsUseCase
    private lateinit var restaurantRepository: RestaurantRepository

    private val sampleRestaurant = Restaurant(
        id = "res_1",
        name = "Cơm Tấm Phúc Lộc Thọ",
        description = "Cơm tấm ngon",
        cuisine = "Cơm",
        rating = 4.8,
        estimatedTime = 25,
        priceLevel = 2,
        imageUrl = "https://example.com/res1.jpg",
        isOpen = true,
        isFreeDelivery = true
    )

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        getRestaurantsUseCase = mockk(relaxed = true)
        restaurantRepository = mockk(relaxed = true)
        coEvery { getRestaurantsUseCase() } returns flowOf(listOf(sampleRestaurant))
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    @DisplayName("Init loads restaurants and updates uiState")
    fun init_loadsDataSuccessfully() = runTest(testDispatcher) {
        val viewModel = DiscoveryViewModel(
            getRestaurantsUseCase = getRestaurantsUseCase,
            restaurantRepository = restaurantRepository,
            savedStateHandle = SavedStateHandle()
        )

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(1, state.restaurants.size)
        assertEquals("res_1", state.restaurants[0].id)
    }

    @Test
    @DisplayName("SelectCategory updates selectedCategory in uiState")
    fun selectCategory_updatesState() = runTest(testDispatcher) {
        val viewModel = DiscoveryViewModel(
            getRestaurantsUseCase = getRestaurantsUseCase,
            restaurantRepository = restaurantRepository,
            savedStateHandle = SavedStateHandle()
        )

        advanceUntilIdle()

        viewModel.onEvent(DiscoveryUiEvent.SelectCategory("Phở & Bún"))
        testDispatcher.scheduler.runCurrent()

        assertEquals("Phở & Bún", viewModel.uiState.value.selectedCategory)
    }

    @Test
    @DisplayName("SearchQueryChanged updates searchQuery in uiState")
    fun searchQueryChanged_updatesQuery() = runTest(testDispatcher) {
        val viewModel = DiscoveryViewModel(
            getRestaurantsUseCase = getRestaurantsUseCase,
            restaurantRepository = restaurantRepository,
            savedStateHandle = SavedStateHandle()
        )

        advanceUntilIdle()

        viewModel.onEvent(DiscoveryUiEvent.SearchQueryChanged("Cơm sườn"))
        testDispatcher.scheduler.runCurrent()

        assertEquals("Cơm sườn", viewModel.uiState.value.searchQuery)
    }
}
