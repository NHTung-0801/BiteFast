package com.bitefast.feature.discovery.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.bitefast.core.common.BaseViewModel
import com.bitefast.core.common.UiEffect
import com.bitefast.core.common.UiEvent
import com.bitefast.core.common.UiState
import com.bitefast.core.domain.restaurant.GetRestaurantsUseCase
import com.bitefast.core.model.Restaurant
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val isLoading: Boolean = false,
    val recentSearches: List<String> = listOf(
        "Cơm tấm sườn bì", "Trà sữa trân châu", "Bún bò Huế", "Gà rán giòn"
    ),
    val trendingKeywords: List<String> = listOf(
        "Trà sữa", "Cơm tấm", "Gà rán", "Bún bò", "Pizza", "Highlands Coffee", "Bánh mì"
    ),
    val rawResults: List<Restaurant> = emptyList(),
    val filteredResults: List<Restaurant> = emptyList(),
    val onlyOpen: Boolean = false,
    val highRatingOnly: Boolean = false,
    val freeShipOnly: Boolean = false,
    val errorMessage: String? = null,
) : UiState

sealed interface SearchUiEvent : UiEvent {
    data class QueryChanged(val query: String) : SearchUiEvent
    data class SelectTrending(val keyword: String) : SearchUiEvent
    data class SelectRecent(val query: String) : SearchUiEvent
    data class RemoveRecent(val query: String) : SearchUiEvent
    data object ClearAllRecent : SearchUiEvent
    data object ClearQuery : SearchUiEvent
    data object ToggleOnlyOpen : SearchUiEvent
    data object ToggleHighRating : SearchUiEvent
    data object ToggleFreeShip : SearchUiEvent
    data class ClickRestaurant(val restaurantId: String) : SearchUiEvent
    data object ClickBack : SearchUiEvent
}

sealed interface SearchUiEffect : UiEffect {
    data object NavigateBack : SearchUiEffect
    data class NavigateToDetail(val restaurantId: String) : SearchUiEffect
}

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val getRestaurantsUseCase: GetRestaurantsUseCase,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<SearchUiState, SearchUiEvent, SearchUiEffect>(
    initialState = SearchUiState(),
    savedStateHandle = savedStateHandle,
) {
    private val queryTrigger = MutableStateFlow("")

    init {
        queryTrigger
            .debounce(300L)
            .distinctUntilChanged()
            .flatMapLatest { query ->
                val trimmed = query.trim()
                if (trimmed.isEmpty()) {
                    updateState { it.copy(isLoading = false, rawResults = emptyList(), filteredResults = emptyList()) }
                    flowOf(emptyList())
                } else {
                    updateState { it.copy(isLoading = true, errorMessage = null) }
                    getRestaurantsUseCase(query = trimmed, cuisine = null)
                }
            }
            .onEach { list ->
                updateState { state ->
                    val filtered = applyFilters(list, state.onlyOpen, state.highRatingOnly, state.freeShipOnly)
                    state.copy(
                        isLoading = false,
                        rawResults = list,
                        filteredResults = filtered,
                        errorMessage = null
                    )
                }
            }
            .catch { e ->
                updateState {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Không thể kết nối tìm kiếm.",
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    override fun onEvent(event: SearchUiEvent) {
        when (event) {
            is SearchUiEvent.QueryChanged -> {
                updateState { it.copy(query = event.query) }
                queryTrigger.update { event.query }
            }

            is SearchUiEvent.ClearQuery -> {
                updateState { it.copy(query = "", rawResults = emptyList(), filteredResults = emptyList()) }
                queryTrigger.update { "" }
            }

            is SearchUiEvent.SelectTrending -> {
                updateState { it.copy(query = event.keyword) }
                addRecentSearch(event.keyword)
                queryTrigger.update { event.keyword }
            }

            is SearchUiEvent.SelectRecent -> {
                updateState { it.copy(query = event.query) }
                queryTrigger.update { event.query }
            }

            is SearchUiEvent.RemoveRecent -> {
                updateState { state ->
                    state.copy(recentSearches = state.recentSearches.filterNot { it == event.query })
                }
            }

            is SearchUiEvent.ClearAllRecent -> {
                updateState { it.copy(recentSearches = emptyList()) }
            }

            is SearchUiEvent.ToggleOnlyOpen -> {
                val newOnlyOpen = !uiState.value.onlyOpen
                updateState { state ->
                    state.copy(
                        onlyOpen = newOnlyOpen,
                        filteredResults = applyFilters(state.rawResults, newOnlyOpen, state.highRatingOnly, state.freeShipOnly)
                    )
                }
            }

            is SearchUiEvent.ToggleHighRating -> {
                val newHighRating = !uiState.value.highRatingOnly
                updateState { state ->
                    state.copy(
                        highRatingOnly = newHighRating,
                        filteredResults = applyFilters(state.rawResults, state.onlyOpen, newHighRating, state.freeShipOnly)
                    )
                }
            }

            is SearchUiEvent.ToggleFreeShip -> {
                val newFreeShip = !uiState.value.freeShipOnly
                updateState { state ->
                    state.copy(
                        freeShipOnly = newFreeShip,
                        filteredResults = applyFilters(state.rawResults, state.onlyOpen, state.highRatingOnly, newFreeShip)
                    )
                }
            }

            is SearchUiEvent.ClickRestaurant -> {
                val currentQuery = uiState.value.query
                if (currentQuery.isNotBlank()) {
                    addRecentSearch(currentQuery)
                }
                sendEffect(SearchUiEffect.NavigateToDetail(event.restaurantId))
            }

            is SearchUiEvent.ClickBack -> {
                sendEffect(SearchUiEffect.NavigateBack)
            }
        }
    }

    private fun addRecentSearch(query: String) {
        val trimmed = query.trim()
        if (trimmed.isNotBlank()) {
            updateState { state ->
                val updated = listOf(trimmed) + state.recentSearches.filterNot { it.equals(trimmed, ignoreCase = true) }
                state.copy(recentSearches = updated.take(8))
            }
        }
    }

    private fun applyFilters(
        restaurants: List<Restaurant>,
        onlyOpen: Boolean,
        highRatingOnly: Boolean,
        freeShipOnly: Boolean,
    ): List<Restaurant> {
        return restaurants.filter { r ->
            val matchOpen = if (onlyOpen) r.isOpen else true
            val matchRating = if (highRatingOnly) r.rating >= 4.5 else true
            val matchFreeShip = if (freeShipOnly) r.isFreeDelivery else true
            matchOpen && matchRating && matchFreeShip
        }
    }
}
