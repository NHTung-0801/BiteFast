package com.bitefast.feature.detail

import androidx.lifecycle.SavedStateHandle
import com.bitefast.core.domain.cart.AddToCartUseCase
import com.bitefast.core.domain.cart.ClearCartUseCase
import com.bitefast.core.domain.cart.GetCartUseCase
import com.bitefast.core.domain.favorite.GetFavoritesUseCase
import com.bitefast.core.domain.favorite.ToggleFavoriteUseCase
import com.bitefast.core.domain.restaurant.GetRestaurantDetailUseCase
import com.bitefast.core.domain.restaurant.RestaurantDetailResult
import com.bitefast.core.model.MenuItem
import com.bitefast.core.model.Restaurant
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
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var getRestaurantDetailUseCase: GetRestaurantDetailUseCase
    private lateinit var addToCartUseCase: AddToCartUseCase
    private lateinit var clearCartUseCase: ClearCartUseCase
    private lateinit var getCartUseCase: GetCartUseCase
    private lateinit var getFavoritesUseCase: GetFavoritesUseCase
    private lateinit var toggleFavoriteUseCase: ToggleFavoriteUseCase

    private val sampleMenuItem = MenuItem(
        id = "menu_1_1",
        restaurantId = "res_1",
        name = "Cơm Sườn Nướng Than Hoa",
        description = "Sườn nướng mật ong",
        price = 48000.0,
        imageUrl = "https://example.com/suon.jpg",
        category = "Món chính",
        isPopular = true,
        rating = 4.9,
        reviewCount = 68
    )

    private val sampleRestaurant = Restaurant(
        id = "res_1",
        name = "Cơm Tấm Phúc Lộc Thọ",
        description = "Chuẩn vị Sài Gòn",
        cuisine = "Cơm",
        rating = 4.8,
        imageUrl = "https://example.com/res1.jpg",
        coverImageUrl = "https://example.com/cover1.jpg",
        address = "123 Lê Văn Việt",
        phoneNumber = "02873002060",
        isOpen = true,
        isFreeDelivery = true
    )

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        getRestaurantDetailUseCase = mockk(relaxed = true)
        addToCartUseCase = mockk(relaxed = true)
        clearCartUseCase = mockk(relaxed = true)
        getCartUseCase = mockk(relaxed = true)
        getFavoritesUseCase = mockk(relaxed = true)
        toggleFavoriteUseCase = mockk(relaxed = true)

        coEvery { getRestaurantDetailUseCase("res_1") } returns RestaurantDetailResult(
            restaurant = sampleRestaurant,
            menuItems = listOf(sampleMenuItem)
        )
        coEvery { getCartUseCase() } returns flowOf(emptyList())
        coEvery { getFavoritesUseCase.isFavorite(any()) } returns flowOf(false)
        coEvery { getFavoritesUseCase(any()) } returns flowOf(emptyList())
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(restaurantId: String = "res_1"): DetailViewModel {
        return DetailViewModel(
            getRestaurantDetailUseCase = getRestaurantDetailUseCase,
            addToCartUseCase = addToCartUseCase,
            clearCartUseCase = clearCartUseCase,
            getCartUseCase = getCartUseCase,
            getFavoritesUseCase = getFavoritesUseCase,
            toggleFavoriteUseCase = toggleFavoriteUseCase,
            savedStateHandle = SavedStateHandle(mapOf("restaurantId" to restaurantId))
        )
    }

    @Test
    @DisplayName("Init loads restaurant detail")
    fun init_loadsRestaurantDetail() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.restaurant)
        assertEquals("res_1", state.restaurant?.id)
        assertEquals("Cơm Tấm Phúc Lộc Thọ", state.restaurant?.name)
        assertEquals(1, state.menuItems.size)
    }

    @Test
    @DisplayName("ClickMenuItem opens customization sheet")
    fun clickMenuItem_opensSheet() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(DetailUiEvent.ClickMenuItem(sampleMenuItem))
        testDispatcher.scheduler.runCurrent()

        val state = viewModel.uiState.value
        assertEquals("menu_1_1", state.selectedMenuItem?.id)
        assertTrue(state.showCustomizationSheet)
    }

    @Test
    @DisplayName("DismissCustomization closes customization sheet")
    fun dismissCustomization_closesSheet() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(DetailUiEvent.ClickMenuItem(sampleMenuItem))
        testDispatcher.scheduler.runCurrent()
        assertTrue(viewModel.uiState.value.showCustomizationSheet)

        viewModel.onEvent(DetailUiEvent.DismissCustomization)
        testDispatcher.scheduler.runCurrent()

        assertFalse(viewModel.uiState.value.showCustomizationSheet)
        assertNull(viewModel.uiState.value.selectedMenuItem)
    }

    @Test
    @DisplayName("OpenDishRating opens dish rating sheet with item")
    fun openDishRating_opensRatingSheet() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(DetailUiEvent.OpenDishRating(sampleMenuItem))
        testDispatcher.scheduler.runCurrent()

        assertTrue(viewModel.uiState.value.showDishRatingSheet)
        assertEquals("menu_1_1", viewModel.uiState.value.ratingMenuItem?.id)
        assertEquals(5, viewModel.uiState.value.selectedRatingStars)
    }

    @Test
    @DisplayName("SelectTab updates selectedTab in uiState")
    fun selectTab_updatesState() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(DetailUiEvent.SelectTab(DetailTab.REVIEWS))
        testDispatcher.scheduler.runCurrent()

        assertEquals(DetailTab.REVIEWS, viewModel.uiState.value.selectedTab)
    }

    @Test
    @DisplayName("SelectSortOption updates selectedSortOption and sorts items")
    fun selectSortOption_updatesStateAndSorts() = runTest(testDispatcher) {
        val expensiveItem = sampleMenuItem.copy(id = "expensive", price = 100000.0)
        val cheapItem = sampleMenuItem.copy(id = "cheap", price = 20000.0)
        coEvery { getRestaurantDetailUseCase("res_1") } returns RestaurantDetailResult(
            restaurant = sampleRestaurant,
            menuItems = listOf(expensiveItem, cheapItem)
        )

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(DetailUiEvent.SelectSortOption(DishSortOption.PRICE_LOW_TO_HIGH))
        testDispatcher.scheduler.runCurrent()

        val filtered = viewModel.uiState.value.filteredMenuItems
        assertEquals("cheap", filtered[0].id)
        assertEquals("expensive", filtered[1].id)
    }

    @Test
    @DisplayName("SelectCategory with Vietnamese accents filters items accurately")
    fun selectCategory_filtersItemsAccurately() = runTest(testDispatcher) {
        val drinkItem = sampleMenuItem.copy(id = "drink", category = "Đồ uống")
        val mainItem = sampleMenuItem.copy(id = "main", category = "Món chính")
        coEvery { getRestaurantDetailUseCase("res_1") } returns RestaurantDetailResult(
            restaurant = sampleRestaurant,
            menuItems = listOf(drinkItem, mainItem)
        )

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(DetailUiEvent.SelectCategory("Đồ uống"))
        testDispatcher.scheduler.runCurrent()

        val filtered = viewModel.uiState.value.filteredMenuItems
        assertEquals(1, filtered.size)
        assertEquals("drink", filtered[0].id)
    }

    @Test
    @DisplayName("SelectReviewFilter filters reviews properly")
    fun selectReviewFilter_filtersReviewsProperly() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(DetailUiEvent.SelectReviewFilter("5★"))
        testDispatcher.scheduler.runCurrent()

        assertEquals("5★", viewModel.uiState.value.selectedReviewFilter)
        val filtered = viewModel.uiState.value.filteredReviews
        assertTrue(filtered.all { it.ratingStars >= 5 })
    }

    @Test
    @DisplayName("ToggleDishFavorite calls ToggleFavoriteUseCase")
    fun toggleDishFavorite_callsUseCase() = runTest(testDispatcher) {
        coEvery { toggleFavoriteUseCase.toggleDish(sampleMenuItem, sampleRestaurant.name) } returns true

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(DetailUiEvent.ToggleDishFavorite(sampleMenuItem))
        advanceUntilIdle()

        coVerify { toggleFavoriteUseCase.toggleDish(sampleMenuItem, sampleRestaurant.name) }
        assertTrue(viewModel.uiState.value.favoriteDishIds.contains(sampleMenuItem.id))
    }
}
