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
import com.bitefast.core.domain.voucher.GetBestVoucherUseCase
import com.bitefast.core.model.CartItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

// ─── UiState ──────────────────────────────────────────────────────────────────

data class CartUiState(
    val isLoading: Boolean = false,
    val items: List<CartItem> = emptyList(),
    val bestVoucherMessage: String? = null,
    val voucherUpsellMessage: String? = null,
    /** True khi co them item tu nha hang khac vao gio hang */
    val showConflictDialog: Boolean = false,
    val conflictRestaurantName: String = "",
    /** Item dang doi xu ly conflict */
    val pendingConflictItem: CartItem? = null,
) : UiState {
    val subtotal: Double get() = items.sumOf { it.totalPrice }
    val deliveryFee: Double get() = if (items.isNotEmpty()) 15_000.0 else 0.0
    val discount: Double get() = 0.0
    val total: Double get() = subtotal + deliveryFee - discount
    val itemCount: Int get() = items.sumOf { it.quantity }
    val currentRestaurantId: String? get() = items.firstOrNull()?.restaurantId
}

// ─── UiEvent ──────────────────────────────────────────────────────────────────

sealed interface CartUiEvent : UiEvent {
    data class IncreaseQuantity(val itemId: String, val currentQuantity: Int) : CartUiEvent
    data class DecreaseQuantity(val itemId: String, val currentQuantity: Int) : CartUiEvent
    data object ClearCart : CartUiEvent
    data object Checkout : CartUiEvent
    data object AddSampleItem : CartUiEvent
    // Conflict dialog
    data object ConflictConfirmClearAndAdd : CartUiEvent
    data object ConflictDismiss : CartUiEvent
}

// ─── UiEffect ─────────────────────────────────────────────────────────────────

sealed interface CartUiEffect : UiEffect {
    data object NavigateToCheckout : CartUiEffect
    data class ShowSnackbar(val message: String) : CartUiEffect
}

// ─── ViewModel ────────────────────────────────────────────────────────────────

@HiltViewModel
class CartViewModel @Inject constructor(
    private val getCartUseCase: GetCartUseCase,
    private val updateCartQuantityUseCase: UpdateCartQuantityUseCase,
    private val getBestVoucherUseCase: GetBestVoucherUseCase,
    private val cartRepository: CartRepository,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<CartUiState, CartUiEvent, CartUiEffect>(
    initialState = CartUiState(),
    savedStateHandle = savedStateHandle,
) {
    init { observeCart() }

    private fun observeCart() {
        viewModelScope.launch {
            getCartUseCase().collect { items ->
                updateState { it.copy(items = items) }
                checkVouchers(items)
            }
        }
    }

    private fun checkVouchers(items: List<CartItem>) {
        if (items.isEmpty()) {
            updateState { it.copy(bestVoucherMessage = null, voucherUpsellMessage = null) }
            return
        }
        val subtotal = items.sumOf { it.totalPrice }
        val restaurantId = items.first().restaurantId
        viewModelScope.launch {
            val result = getBestVoucherUseCase(
                subtotal = subtotal,
                restaurantId = restaurantId,
                deliveryFee = 15_000.0
            )
            updateState {
                it.copy(
                    bestVoucherMessage = result.message,
                    voucherUpsellMessage = result.upsellMessage
                )
            }
        }
    }

    override fun onEvent(event: CartUiEvent) {
        when (event) {
            is CartUiEvent.IncreaseQuantity -> viewModelScope.launch {
                updateCartQuantityUseCase(event.itemId, event.currentQuantity + 1)
            }

            is CartUiEvent.DecreaseQuantity -> viewModelScope.launch {
                updateCartQuantityUseCase(event.itemId, event.currentQuantity - 1)
            }

            is CartUiEvent.ClearCart -> viewModelScope.launch {
                cartRepository.clearCart()
                sendEffect(CartUiEffect.ShowSnackbar("Đã xóa giỏ hàng"))
            }

            is CartUiEvent.Checkout -> {
                if (uiState.value.items.isNotEmpty()) {
                    sendEffect(CartUiEffect.NavigateToCheckout)
                }
            }

            is CartUiEvent.AddSampleItem -> viewModelScope.launch {
                val sampleItem = CartItem(
                    id = "cart_sample_${System.currentTimeMillis()}",
                    restaurantId = "res_sample_1",
                    name = "Cơm Tấm Sườn Bì Chả Đặc Biệt",
                    price = 65_000.0,
                    quantity = 1,
                )
                cartRepository.addItem(sampleItem)
            }

            is CartUiEvent.ConflictConfirmClearAndAdd -> viewModelScope.launch {
                val pending = uiState.value.pendingConflictItem ?: return@launch
                cartRepository.clearCart()
                cartRepository.addItem(pending)
                updateState { it.copy(showConflictDialog = false, pendingConflictItem = null, conflictRestaurantName = "") }
            }

            is CartUiEvent.ConflictDismiss -> {
                updateState { it.copy(showConflictDialog = false, pendingConflictItem = null, conflictRestaurantName = "") }
            }
        }
    }
}
