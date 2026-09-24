package com.bitefast.feature.cart

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.bitefast.core.common.BaseViewModel
import com.bitefast.core.common.UiEffect
import com.bitefast.core.common.UiEvent
import com.bitefast.core.common.UiState
import com.bitefast.core.domain.cart.GetCartUseCase
import com.bitefast.core.domain.cart.UpdateCartQuantityUseCase
import com.bitefast.core.domain.repository.CartRepository
import com.bitefast.core.model.CartItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CartUiState(
    val isLoading: Boolean = false,
    val items: List<CartItem> = emptyList(),
    val isConflictDialogOpen: Boolean = false
) : UiState {
    val subtotal: Double get() = items.sumOf { it.totalPrice }
    val deliveryFee: Double get() = if (items.isNotEmpty()) 15000.0 else 0.0
    val total: Double get() = subtotal + deliveryFee
}

sealed interface CartUiEvent : UiEvent {
    data class IncreaseQuantity(val itemId: String, val currentQuantity: Int) : CartUiEvent
    data class DecreaseQuantity(val itemId: String, val currentQuantity: Int) : CartUiEvent
    data object ClearCart : CartUiEvent
    data object Checkout : CartUiEvent
}

sealed interface CartUiEffect : UiEffect {
    data object NavigateToCheckout : CartUiEffect
    data class ShowToast(val message: String) : CartUiEffect
}

@HiltViewModel
class CartViewModel @Inject constructor(
    private val getCartUseCase: GetCartUseCase,
    private val updateCartQuantityUseCase: UpdateCartQuantityUseCase,
    private val cartRepository: CartRepository,
    savedStateHandle: SavedStateHandle
) : BaseViewModel<CartUiState, CartUiEvent, CartUiEffect>(
    initialState = CartUiState(),
    savedStateHandle = savedStateHandle
) {
    init {
        observeCart()
    }

    private fun observeCart() {
        viewModelScope.launch {
            getCartUseCase().collect { items ->
                updateState { it.copy(items = items) }
            }
        }
    }

    override fun onEvent(event: CartUiEvent) {
        when (event) {
            is CartUiEvent.IncreaseQuantity -> {
                viewModelScope.launch {
                    updateCartQuantityUseCase(event.itemId, event.currentQuantity + 1)
                }
            }
            is CartUiEvent.DecreaseQuantity -> {
                viewModelScope.launch {
                    updateCartQuantityUseCase(event.itemId, event.currentQuantity - 1)
                }
            }
            is CartUiEvent.ClearCart -> {
                viewModelScope.launch {
                    cartRepository.clearCart()
                }
            }
            is CartUiEvent.Checkout -> {
                if (uiState.value.items.isNotEmpty()) {
                    sendEffect(CartUiEffect.NavigateToCheckout)
                }
            }
        }
    }
}
