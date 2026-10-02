package com.bitefast.feature.discovery.search

import androidx.lifecycle.SavedStateHandle
import com.bitefast.core.domain.restaurant.GetRestaurantsUseCase
import com.bitefast.core.model.Restaurant
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
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
class SearchViewModelTest {

    private val getRestaurantsUseCase: GetRestaurantsUseCase = mockk()
    private val testDispatcher = StandardTestDispatcher()

    private val mockRestaurants = listOf(
        Restaurant(
            id = "res_1",
            name = "Cơm Tấm Phúc Lộc Thọ",
            imageUrl = "https://example.com/comtam.jpg",
            rating = 4.8,
            estimatedTime = 20,
            isFreeDelivery = true,
            isOpen = true
        ),
        Restaurant(
            id = "res_2",
            name = "Trà Sữa Gong Cha",
            imageUrl = "https://example.com/gongcha.jpg",
            rating = 4.2,
            estimatedTime = 35,
            isFreeDelivery = false,
            isOpen = false
        )
    )

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { getRestaurantsUseCase(query = any(), cuisine = any()) } answers {
            val q = firstArg<String?>()
            val list = if (q.isNullOrBlank()) {
                mockRestaurants
            } else {
                mockRestaurants.filter { it.name.contains(q, ignoreCase = true) }
            }
            flowOf(list)
        }
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    @DisplayName("Initial state should have default recent searches and trending keywords")
    fun initialState_hasDefaults() = runTest(testDispatcher) {
        val viewModel = SearchViewModel(getRestaurantsUseCase, SavedStateHandle())
        val state = viewModel.uiState.value

        assertEquals("", state.query)
        assertFalse(state.isLoading)
        assertTrue(state.recentSearches.isNotEmpty())
        assertTrue(state.trendingKeywords.isNotEmpty())
    }

    @Test
    @DisplayName("QueryChanged should debounce and filter restaurants by keyword")
    fun queryChanged_debouncesAndFilters() = runTest(testDispatcher) {
        val viewModel = SearchViewModel(getRestaurantsUseCase, SavedStateHandle())

        viewModel.onEvent(SearchUiEvent.QueryChanged("Cơm Tấm"))
        testDispatcher.scheduler.advanceTimeBy(350)
        testDispatcher.scheduler.runCurrent()

        val state = viewModel.uiState.value
        assertEquals("Cơm Tấm", state.query)
        assertEquals(1, state.filteredResults.size)
        assertEquals("res_1", state.filteredResults.first().id)
    }

    @Test
    @DisplayName("ToggleOnlyOpen should filter out closed restaurants")
    fun toggleOnlyOpen_filtersClosed() = runTest(testDispatcher) {
        val viewModel = SearchViewModel(getRestaurantsUseCase, SavedStateHandle())

        viewModel.onEvent(SearchUiEvent.ToggleOnlyOpen)
        val state = viewModel.uiState.value

        assertTrue(state.onlyOpen)
    }

    @Test
    @DisplayName("ClearAllRecent should empty the recent searches list")
    fun clearAllRecent_clearsList() = runTest(testDispatcher) {
        val viewModel = SearchViewModel(getRestaurantsUseCase, SavedStateHandle())

        viewModel.onEvent(SearchUiEvent.ClearAllRecent)
        val state = viewModel.uiState.value

        assertTrue(state.recentSearches.isEmpty())
    }
}
