package com.bitefast.feature.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.bitefast.core.common.BaseViewModel
import com.bitefast.core.common.UiEffect
import com.bitefast.core.common.UiEvent
import com.bitefast.core.common.UiState
import com.bitefast.core.domain.cart.AddToCartResult
import com.bitefast.core.domain.cart.AddToCartUseCase
import com.bitefast.core.domain.cart.ClearCartUseCase
import com.bitefast.core.domain.cart.GetCartUseCase
import com.bitefast.core.domain.restaurant.GetRestaurantDetailUseCase
import com.bitefast.core.model.CartItem
import com.bitefast.core.model.MenuItem
import com.bitefast.core.model.Restaurant
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── UiState ─────────────────────────────────────────────────────────────────

data class DetailUiState(
    val isLoading: Boolean = true,
    val restaurantId: String = "",
    val restaurant: Restaurant? = null,
    val menuItems: List<MenuItem> = emptyList(),
    val categories: List<String> = emptyList(),
    val selectedCategory: String = "Tat ca",
    val isFavorite: Boolean = false,
    val cartItemCount: Int = 0,
    val cartTotalPrice: Double = 0.0,
    val cartItemsFromThisRestaurant: List<CartItem> = emptyList(),
    val showCustomizationSheet: Boolean = false,
    val selectedMenuItem: MenuItem? = null,
    val customizationQuantity: Int = 1,
    val customizationNote: String = "",
    val showConflictDialog: Boolean = false,
    val pendingConflictItem: CartItem? = null,
    val errorMessage: String? = null
) : UiState {
    val filteredMenuItems: List<MenuItem>
        get() = if (selectedCategory == "Tat ca") {
            menuItems
        } else {
            menuItems.filter { it.category == selectedCategory }
        }

    val hasCartItems: Boolean get() = cartItemCount > 0
}

// ── UiEvent ─────────────────────────────────────────────────────────────────

sealed interface DetailUiEvent : UiEvent {
    data class SelectCategory(val category: String) : DetailUiEvent
    data object ToggleFavorite : DetailUiEvent
    data class ClickMenuItem(val item: MenuItem) : DetailUiEvent
    data class QuickAddToCart(val item: MenuItem) : DetailUiEvent
    data class UpdateQuantity(val delta: Int) : DetailUiEvent
    data class UpdateNote(val note: String) : DetailUiEvent
    data object ConfirmAddToCart : DetailUiEvent
    data object DismissCustomization : DetailUiEvent
    data object ConfirmConflictAndReplace : DetailUiEvent
    data object DismissConflictDialog : DetailUiEvent
    data object ClickViewCart : DetailUiEvent
    data object ClickBack : DetailUiEvent
}

// ── UiEffect ────────────────────────────────────────────────────────────────

sealed interface DetailUiEffect : UiEffect {
    data object NavigateBack : DetailUiEffect
    data object NavigateToCart : DetailUiEffect
    data class ShowSnackbar(val message: String) : DetailUiEffect
}

// ── ViewModel ───────────────────────────────────────────────────────────────

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val getRestaurantDetailUseCase: GetRestaurantDetailUseCase,
    private val addToCartUseCase: AddToCartUseCase,
    private val clearCartUseCase: ClearCartUseCase,
    private val getCartUseCase: GetCartUseCase,
    savedStateHandle: SavedStateHandle
) : BaseViewModel<DetailUiState, DetailUiEvent, DetailUiEffect>(
    initialState = DetailUiState(restaurantId = savedStateHandle.get<String>("restaurantId") ?: "res_1"),
    savedStateHandle = savedStateHandle
) {

    init {
        loadRestaurantDetail()
        observeCart()
    }

    private fun loadRestaurantDetail() {
        val id = uiState.value.restaurantId.ifBlank { "res_1" }
        viewModelScope.launch {
            try {
                val detailResult = getRestaurantDetailUseCase(id)
                val rawCategories = detailResult.menuItems.map { it.category }.filter { it.isNotBlank() }.distinct()
                val categories = listOf("Tat ca") + rawCategories

                updateState {
                    it.copy(
                        isLoading = false,
                        restaurant = detailResult.restaurant,
                        menuItems = detailResult.menuItems,
                        categories = categories,
                        isFavorite = detailResult.restaurant.isFavorite,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                updateState {
                    it.copy(isLoading = false, errorMessage = "Khong the tai thong tin nha hang")
                }
            }
        }
    }

    private fun observeCart() {
        viewModelScope.launch {
            getCartUseCase()
                .catch { emit(emptyList()) }
                .collect { items ->
                    val id = uiState.value.restaurantId
                    val relevant = items.filter { it.restaurantId == id }
                    updateState {
                        it.copy(
                            cartItemCount = items.sumOf { item -> item.quantity },
                            cartTotalPrice = items.sumOf { item -> item.totalPrice },
                            cartItemsFromThisRestaurant = relevant
                        )
                    }
                }
        }
    }

    override fun onEvent(event: DetailUiEvent) {
        when (event) {
            is DetailUiEvent.SelectCategory -> {
                updateState { it.copy(selectedCategory = event.category) }
            }

            is DetailUiEvent.ToggleFavorite -> {
                val newFav = !uiState.value.isFavorite
                updateState { it.copy(isFavorite = newFav) }
                sendEffect(
                    DetailUiEffect.ShowSnackbar(
                        if (newFav) "Da luu nha hang vao muc yeu thich" else "Da go khoi muc yeu thich"
                    )
                )
            }

            is DetailUiEvent.ClickMenuItem -> {
                updateState {
                    it.copy(
                        showCustomizationSheet = true,
                        selectedMenuItem = event.item,
                        customizationQuantity = 1,
                        customizationNote = ""
                    )
                }
            }

            is DetailUiEvent.QuickAddToCart -> {
                addItemToCart(
                    CartItem(
                        id = "ci_${System.currentTimeMillis()}",
                        menuItemId = event.item.id,
                        restaurantId = uiState.value.restaurantId,
                        name = event.item.name,
                        price = event.item.price,
                        quantity = 1,
                        imageUrl = event.item.imageUrl
                    )
                )
            }

            is DetailUiEvent.UpdateQuantity -> {
                val newQty = (uiState.value.customizationQuantity + event.delta).coerceIn(1, 99)
                updateState { it.copy(customizationQuantity = newQty) }
            }

            is DetailUiEvent.UpdateNote -> {
                updateState { it.copy(customizationNote = event.note) }
            }

            is DetailUiEvent.ConfirmAddToCart -> {
                val item = uiState.value.selectedMenuItem ?: return
                val cartItem = CartItem(
                    id = "ci_${System.currentTimeMillis()}",
                    menuItemId = item.id,
                    restaurantId = uiState.value.restaurantId,
                    name = item.name,
                    price = item.price,
                    quantity = uiState.value.customizationQuantity,
                    notes = uiState.value.customizationNote,
                    imageUrl = item.imageUrl
                )
                updateState { it.copy(showCustomizationSheet = false) }
                addItemToCart(cartItem)
            }

            is DetailUiEvent.DismissCustomization -> {
                updateState { it.copy(showCustomizationSheet = false, selectedMenuItem = null) }
            }

            is DetailUiEvent.ConfirmConflictAndReplace -> {
                val pending = uiState.value.pendingConflictItem ?: return
                updateState { it.copy(showConflictDialog = false, pendingConflictItem = null) }
                viewModelScope.launch {
                    clearCartUseCase()
                    when (addToCartUseCase(pending)) {
                        is AddToCartResult.Success -> {
                            sendEffect(DetailUiEffect.ShowSnackbar("Da xoa gio hang cu va them ${pending.name}"))
                        }
                        else -> {}
                    }
                }
            }

            is DetailUiEvent.DismissConflictDialog -> {
                updateState { it.copy(showConflictDialog = false, pendingConflictItem = null) }
            }

            is DetailUiEvent.ClickViewCart -> {
                sendEffect(DetailUiEffect.NavigateToCart)
            }

            is DetailUiEvent.ClickBack -> {
                sendEffect(DetailUiEffect.NavigateBack)
            }
        }
    }

    private fun addItemToCart(cartItem: CartItem) {
        viewModelScope.launch {
            when (val result = addToCartUseCase(cartItem)) {
                is AddToCartResult.Success -> {
                    sendEffect(DetailUiEffect.ShowSnackbar("Da them ${cartItem.quantity}x ${cartItem.name} vao gio"))
                }
                is AddToCartResult.Conflict -> {
                    updateState {
                        it.copy(
                            showConflictDialog = true,
                            pendingConflictItem = cartItem
                        )
                    }
                }
            }
        }
    }
}
