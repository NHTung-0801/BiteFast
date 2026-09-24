package com.bitefast.feature.discovery

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.bitefast.core.common.BaseViewModel
import com.bitefast.core.common.UiEffect
import com.bitefast.core.common.UiEvent
import com.bitefast.core.common.UiState
import com.bitefast.core.domain.restaurant.GetRestaurantsUseCase
import com.bitefast.core.model.Restaurant
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DiscoveryUiState(
    val isLoading: Boolean = true,
    val restaurants: List<Restaurant> = emptyList(),
    val selectedCategory: String = "Tất cả",
    val searchQuery: String = "",
    val errorMessage: String? = null
) : UiState

sealed interface DiscoveryUiEvent : UiEvent {
    data class SelectCategory(val category: String) : DiscoveryUiEvent
    data class Search(val query: String) : DiscoveryUiEvent
    data object Refresh : DiscoveryUiEvent
    data class ClickRestaurant(val restaurantId: String) : DiscoveryUiEvent
}

sealed interface DiscoveryUiEffect : UiEffect {
    data class NavigateToDetail(val restaurantId: String) : DiscoveryUiEffect
    data class ShowToast(val message: String) : DiscoveryUiEffect
}

@HiltViewModel
class DiscoveryViewModel @Inject constructor(
    private val getRestaurantsUseCase: GetRestaurantsUseCase,
    savedStateHandle: SavedStateHandle
) : BaseViewModel<DiscoveryUiState, DiscoveryUiEvent, DiscoveryUiEffect>(
    initialState = DiscoveryUiState(),
    savedStateHandle = savedStateHandle
) {
    init {
        loadRestaurants()
    }

    override fun onEvent(event: DiscoveryUiEvent) {
        when (event) {
            is DiscoveryUiEvent.SelectCategory -> {
                updateState { it.copy(selectedCategory = event.category) }
                loadRestaurants(category = event.category, query = uiState.value.searchQuery)
            }
            is DiscoveryUiEvent.Search -> {
                updateState { it.copy(searchQuery = event.query) }
                loadRestaurants(category = uiState.value.selectedCategory, query = event.query)
            }
            is DiscoveryUiEvent.Refresh -> {
                loadRestaurants()
            }
            is DiscoveryUiEvent.ClickRestaurant -> {
                sendEffect(DiscoveryUiEffect.NavigateToDetail(event.restaurantId))
            }
        }
    }

    private fun loadRestaurants(category: String = "Tất cả", query: String = "") {
        updateState { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val cuisineParam = if (category == "Tất cả") null else category
            val queryParam = query.ifBlank { null }

            getRestaurantsUseCase(queryParam, cuisineParam)
                .catch { e ->
                    updateState { it.copy(isLoading = false, errorMessage = e.message ?: "Lỗi tải danh sách") }
                }
                .collect { list ->
                    updateState { it.copy(isLoading = false, restaurants = list) }
                }
        }
    }
}
