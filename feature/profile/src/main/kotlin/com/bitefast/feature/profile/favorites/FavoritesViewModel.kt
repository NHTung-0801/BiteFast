package com.bitefast.feature.profile.favorites

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.bitefast.core.common.BaseViewModel
import com.bitefast.core.common.UiEffect
import com.bitefast.core.common.UiEvent
import com.bitefast.core.common.UiState
import com.bitefast.core.domain.cart.AddToCartResult
import com.bitefast.core.domain.cart.AddToCartUseCase
import com.bitefast.core.domain.favorite.GetFavoritesUseCase
import com.bitefast.core.domain.favorite.ToggleFavoriteUseCase
import com.bitefast.core.model.CartItem
import com.bitefast.core.model.FavoriteItem
import com.bitefast.core.model.FavoriteType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── UiState ─────────────────────────────────────────────────────────────────

data class FavoritesUiState(
    val selectedTab: FavoriteType = FavoriteType.DISH,
    val dishes: List<FavoriteItem> = emptyList(),
    val restaurants: List<FavoriteItem> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
) : UiState {
    val currentItems: List<FavoriteItem>
        get() = when (selectedTab) {
            FavoriteType.DISH -> dishes
            FavoriteType.RESTAURANT -> restaurants
        }
    val isEmpty: Boolean
        get() = currentItems.isEmpty()
}

// ── UiEvent ─────────────────────────────────────────────────────────────────

sealed interface FavoritesUiEvent : UiEvent {
    data class SelectTab(val tab: FavoriteType) : FavoritesUiEvent
    data class RemoveFavorite(val item: FavoriteItem) : FavoritesUiEvent
    data class QuickAddToCart(val item: FavoriteItem) : FavoritesUiEvent
    data class ClickItem(val item: FavoriteItem) : FavoritesUiEvent
    data object ClickBack : FavoritesUiEvent
    data object ClickExplore : FavoritesUiEvent
}

// ── UiEffect ────────────────────────────────────────────────────────────────

sealed interface FavoritesUiEffect : UiEffect {
    data object NavigateBack : FavoritesUiEffect
    data class NavigateToDetail(val restaurantId: String) : FavoritesUiEffect
    data object NavigateToHome : FavoritesUiEffect
    data class ShowSnackbar(val message: String) : FavoritesUiEffect
}

// ── ViewModel ───────────────────────────────────────────────────────────────

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val getFavoritesUseCase: GetFavoritesUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val addToCartUseCase: AddToCartUseCase,
    savedStateHandle: SavedStateHandle
) : BaseViewModel<FavoritesUiState, FavoritesUiEvent, FavoritesUiEffect>(
    initialState = FavoritesUiState(),
    savedStateHandle = savedStateHandle
) {

    init {
        observeFavorites()
    }

    private fun observeFavorites() {
        viewModelScope.launch {
            getFavoritesUseCase(FavoriteType.DISH)
                .catch { updateState { it.copy(isLoading = false, errorMessage = "Không thể tải danh sách món ăn") } }
                .collect { items ->
                    updateState { it.copy(dishes = items, isLoading = false) }
                }
        }
        viewModelScope.launch {
            getFavoritesUseCase(FavoriteType.RESTAURANT)
                .catch { updateState { it.copy(isLoading = false, errorMessage = "Không thể tải danh sách quán") } }
                .collect { items ->
                    updateState { it.copy(restaurants = items, isLoading = false) }
                }
        }
    }

    override fun onEvent(event: FavoritesUiEvent) {
        when (event) {
            is FavoritesUiEvent.SelectTab -> {
                updateState { it.copy(selectedTab = event.tab) }
            }

            is FavoritesUiEvent.RemoveFavorite -> {
                viewModelScope.launch {
                    toggleFavoriteUseCase.remove(event.item.targetId)
                    sendEffect(FavoritesUiEffect.ShowSnackbar("Đã gỡ ${event.item.name} khỏi yêu thích"))
                }
            }

            is FavoritesUiEvent.QuickAddToCart -> {
                viewModelScope.launch {
                    val cartItem = CartItem(
                        id = "${event.item.targetId}_${System.currentTimeMillis()}",
                        menuItemId = event.item.targetId,
                        restaurantId = event.item.restaurantId.ifBlank { "res_1" },
                        name = event.item.name,
                        price = event.item.price,
                        imageUrl = event.item.imageUrl,
                        quantity = 1,
                        notes = ""
                    )
                    when (addToCartUseCase(cartItem)) {
                        is AddToCartResult.Success -> {
                            sendEffect(FavoritesUiEffect.ShowSnackbar("Đã thêm ${event.item.name} vào giỏ hàng"))
                        }
                        is AddToCartResult.Conflict -> {
                            sendEffect(FavoritesUiEffect.ShowSnackbar("Giỏ hàng đang có món của quán khác"))
                        }
                    }
                }
            }

            is FavoritesUiEvent.ClickItem -> {
                val restId = event.item.restaurantId.ifBlank {
                    if (event.item.type == FavoriteType.RESTAURANT) event.item.targetId else "res_1"
                }
                sendEffect(FavoritesUiEffect.NavigateToDetail(restId))
            }

            is FavoritesUiEvent.ClickBack -> {
                sendEffect(FavoritesUiEffect.NavigateBack)
            }

            is FavoritesUiEvent.ClickExplore -> {
                sendEffect(FavoritesUiEffect.NavigateToHome)
            }
        }
    }
}
