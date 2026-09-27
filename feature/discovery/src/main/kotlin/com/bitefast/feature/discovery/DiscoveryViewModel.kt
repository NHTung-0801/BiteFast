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
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// ─── UiState ──────────────────────────────────────────────────────────────────

data class DiscoveryUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val restaurants: List<Restaurant> = emptyList(),
    val selectedCategory: String = CATEGORY_ALL,
    val searchQuery: String = "",
    val errorMessage: String? = null,
) : UiState

// ─── UiEvent ──────────────────────────────────────────────────────────────────

sealed interface DiscoveryUiEvent : UiEvent {
    data class SelectCategory(val category: String) : DiscoveryUiEvent
    data class SearchQueryChanged(val query: String) : DiscoveryUiEvent
    data object Refresh : DiscoveryUiEvent
    data class ClickRestaurant(val restaurantId: String) : DiscoveryUiEvent
    data object DismissError : DiscoveryUiEvent
}

// ─── UiEffect ─────────────────────────────────────────────────────────────────

sealed interface DiscoveryUiEffect : UiEffect {
    data class NavigateToDetail(val restaurantId: String) : DiscoveryUiEffect
    data class ShowSnackbar(val message: String) : DiscoveryUiEffect
}

// ─── Constants ────────────────────────────────────────────────────────────────

const val CATEGORY_ALL = "Tất cả"
val FOOD_CATEGORIES = listOf(
    CATEGORY_ALL, "Cơm", "Phở & Bún", "Trà sữa", "Bánh mì", "Gà rán", "Pizza", "Đồ uống"
)

// ─── ViewModel ────────────────────────────────────────────────────────────────

@OptIn(FlowPreview::class)
@HiltViewModel
class DiscoveryViewModel @Inject constructor(
    private val getRestaurantsUseCase: GetRestaurantsUseCase,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<DiscoveryUiState, DiscoveryUiEvent, DiscoveryUiEffect>(
    initialState = DiscoveryUiState(),
    savedStateHandle = savedStateHandle,
) {
    /** Search query Flow riêng để debounce 300ms trước khi trigger search. */
    private val searchTrigger = MutableStateFlow(Pair(CATEGORY_ALL, ""))

    init {
        // Lắng nghe thay đổi query/category với debounce 300ms — tránh gọi API liên tục
        searchTrigger
            .debounce(300L)
            .distinctUntilChanged()
            .flatMapLatest { (category, query) ->
                updateState { it.copy(isLoading = true, errorMessage = null) }
                val cuisineParam = if (category == CATEGORY_ALL) null else category
                val queryParam = query.ifBlank { null }
                getRestaurantsUseCase(queryParam, cuisineParam)
            }
            .onEach { restaurants ->
                updateState {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        restaurants = restaurants,
                        errorMessage = null,
                    )
                }
            }
            .catch { e ->
                updateState {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        errorMessage = e.message ?: "Lỗi tải danh sách nhà hàng.",
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    override fun onEvent(event: DiscoveryUiEvent) {
        when (event) {
            is DiscoveryUiEvent.SelectCategory -> {
                updateState { it.copy(selectedCategory = event.category) }
                searchTrigger.update { Pair(event.category, uiState.value.searchQuery) }
            }

            is DiscoveryUiEvent.SearchQueryChanged -> {
                updateState { it.copy(searchQuery = event.query) }
                searchTrigger.update { Pair(uiState.value.selectedCategory, event.query) }
            }

            is DiscoveryUiEvent.Refresh -> {
                updateState { it.copy(isRefreshing = true, errorMessage = null) }
                searchTrigger.update { it } // re-emit cùng value để trigger lại collect
                viewModelScope.launch {
                    searchTrigger.emit(
                        Pair(uiState.value.selectedCategory, uiState.value.searchQuery)
                    )
                }
            }

            is DiscoveryUiEvent.ClickRestaurant -> {
                sendEffect(DiscoveryUiEffect.NavigateToDetail(event.restaurantId))
            }

            is DiscoveryUiEvent.DismissError -> {
                updateState { it.copy(errorMessage = null) }
            }
        }
    }
}
