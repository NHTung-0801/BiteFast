package com.bitefast.feature.profile.favorites

import androidx.lifecycle.SavedStateHandle
import com.bitefast.core.domain.cart.AddToCartResult
import com.bitefast.core.domain.cart.AddToCartUseCase
import com.bitefast.core.domain.favorite.GetFavoritesUseCase
import com.bitefast.core.domain.favorite.ToggleFavoriteUseCase
import com.bitefast.core.model.FavoriteItem
import com.bitefast.core.model.FavoriteType
import io.mockk.coEvery
import io.mockk.coVerify
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
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FavoritesViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var getFavoritesUseCase: GetFavoritesUseCase
    private lateinit var toggleFavoriteUseCase: ToggleFavoriteUseCase
    private lateinit var addToCartUseCase: AddToCartUseCase

    private val sampleDishFavorite = FavoriteItem(
        id = "fav_dish_1",
        type = FavoriteType.DISH,
        targetId = "menu_1",
        name = "Trà Đào Cam Sả",
        price = 45000.0,
        imageUrl = "https://example.com/tea.jpg",
        restaurantId = "res_1",
        restaurantName = "Phúc Long Coffee & Tea"
    )

    private val sampleRestaurantFavorite = FavoriteItem(
        id = "fav_res_1",
        type = FavoriteType.RESTAURANT,
        targetId = "res_1",
        name = "Phúc Long Coffee & Tea",
        imageUrl = "https://example.com/res.jpg",
        category = "Trà sữa & Đồ uống"
    )

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        getFavoritesUseCase = mockk(relaxed = true)
        toggleFavoriteUseCase = mockk(relaxed = true)
        addToCartUseCase = mockk(relaxed = true)

        coEvery { getFavoritesUseCase(FavoriteType.DISH) } returns flowOf(listOf(sampleDishFavorite))
        coEvery { getFavoritesUseCase(FavoriteType.RESTAURANT) } returns flowOf(listOf(sampleRestaurantFavorite))
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): FavoritesViewModel {
        return FavoritesViewModel(
            getFavoritesUseCase = getFavoritesUseCase,
            toggleFavoriteUseCase = toggleFavoriteUseCase,
            addToCartUseCase = addToCartUseCase,
            savedStateHandle = SavedStateHandle()
        )
    }

    @Test
    @DisplayName("Init loads favorite dishes and restaurants")
    fun init_loadsFavorites() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(1, state.dishes.size)
        assertEquals("Trà Đào Cam Sả", state.dishes[0].name)
        assertEquals(1, state.restaurants.size)
        assertEquals("Phúc Long Coffee & Tea", state.restaurants[0].name)
        assertEquals(FavoriteType.DISH, state.selectedTab)
    }

    @Test
    @DisplayName("SelectTab switches between dishes and restaurants")
    fun selectTab_switchesTab() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(FavoritesUiEvent.SelectTab(FavoriteType.RESTAURANT))
        testDispatcher.scheduler.runCurrent()

        assertEquals(FavoriteType.RESTAURANT, viewModel.uiState.value.selectedTab)
        assertEquals(1, viewModel.uiState.value.currentItems.size)
        assertEquals("fav_res_1", viewModel.uiState.value.currentItems[0].id)
    }

    @Test
    @DisplayName("RemoveFavorite calls toggleFavoriteUseCase.remove")
    fun removeFavorite_callsUseCase() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(FavoritesUiEvent.RemoveFavorite(sampleDishFavorite))
        advanceUntilIdle()

        coVerify { toggleFavoriteUseCase.remove(sampleDishFavorite.targetId) }
    }

    @Test
    @DisplayName("QuickAddToCart adds item to cart")
    fun quickAddToCart_callsAddToCartUseCase() = runTest(testDispatcher) {
        coEvery { addToCartUseCase(any()) } returns AddToCartResult.Success

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(FavoritesUiEvent.QuickAddToCart(sampleDishFavorite))
        advanceUntilIdle()

        coVerify { addToCartUseCase(match { it.menuItemId == sampleDishFavorite.targetId }) }
    }
}
