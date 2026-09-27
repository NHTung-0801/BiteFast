package com.bitefast.feature.checkout

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.bitefast.core.common.BaseViewModel
import com.bitefast.core.common.UiEffect
import com.bitefast.core.common.UiEvent
import com.bitefast.core.common.UiState
import com.bitefast.core.domain.cart.GetCartUseCase
import com.bitefast.core.domain.order.CheckoutOrderUseCase
import com.bitefast.core.domain.order.CheckoutResult
import com.bitefast.core.domain.repository.CartRepository
import com.bitefast.core.model.Address
import com.bitefast.core.model.CartItem
import com.bitefast.core.model.Order
import com.bitefast.core.model.PaymentMethod
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

// ─── UiState ──────────────────────────────────────────────────────────────────

data class CheckoutUiState(
    val isLoading: Boolean = false,
    val items: List<CartItem> = emptyList(),
    val deliveryAddress: Address = Address(),
    val selectedPayment: PaymentMethod = PaymentMethod.CASH,
    val voucherCode: String = "",
    val discount: Double = 0.0,
    val note: String = "",
    // Form validation
    val addressError: String? = null,
    val isVoucherLoading: Boolean = false,
    val voucherError: String? = null,
    val showVoucherSheet: Boolean = false,
    val showBiometricPrompt: Boolean = false,
    val isOrderPlaced: Boolean = false,
    val placedOrderId: String? = null,
    val errorMessage: String? = null,
) : UiState {
    val subtotal: Double get() = items.sumOf { it.totalPrice }
    val deliveryFee: Double get() = if (items.isNotEmpty()) 15_000.0 else 0.0
    val total: Double get() = subtotal + deliveryFee - discount
    val isAddressValid: Boolean get() = deliveryAddress.streetAddress.isNotBlank()
        && deliveryAddress.phoneNumber.isNotBlank()
}

// ─── UiEvent ──────────────────────────────────────────────────────────────────

sealed interface CheckoutUiEvent : UiEvent {
    data class AddressChanged(val address: Address) : CheckoutUiEvent
    data class PaymentMethodSelected(val method: PaymentMethod) : CheckoutUiEvent
    data class VoucherCodeChanged(val code: String) : CheckoutUiEvent
    data class NoteChanged(val note: String) : CheckoutUiEvent
    data object ApplyVoucher : CheckoutUiEvent
    data object RemoveVoucher : CheckoutUiEvent
    data object OpenVoucherSheet : CheckoutUiEvent
    data object CloseVoucherSheet : CheckoutUiEvent
    data object PlaceOrder : CheckoutUiEvent
    data object BiometricConfirmed : CheckoutUiEvent
    data object BiometricDismissed : CheckoutUiEvent
    data object DismissError : CheckoutUiEvent
}

// ─── UiEffect ─────────────────────────────────────────────────────────────────

sealed interface CheckoutUiEffect : UiEffect {
    data class NavigateToTracking(val orderId: String) : CheckoutUiEffect
    data object NavigateBack : CheckoutUiEffect
    data object TriggerBiometric : CheckoutUiEffect
    data class ShowSnackbar(val message: String) : CheckoutUiEffect
}

// ─── ViewModel ────────────────────────────────────────────────────────────────

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val getCartUseCase: GetCartUseCase,
    private val checkoutOrderUseCase: CheckoutOrderUseCase,
    private val cartRepository: CartRepository,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<CheckoutUiState, CheckoutUiEvent, CheckoutUiEffect>(
    initialState = CheckoutUiState(),
    savedStateHandle = savedStateHandle,
) {
    init { observeCart() }

    private fun observeCart() {
        viewModelScope.launch {
            getCartUseCase().collect { items ->
                updateState { it.copy(items = items) }
            }
        }
    }

    override fun onEvent(event: CheckoutUiEvent) {
        when (event) {
            is CheckoutUiEvent.AddressChanged -> {
                updateState { it.copy(deliveryAddress = event.address, addressError = null) }
            }

            is CheckoutUiEvent.PaymentMethodSelected -> {
                updateState { it.copy(selectedPayment = event.method) }
            }

            is CheckoutUiEvent.VoucherCodeChanged -> {
                updateState { it.copy(voucherCode = event.code, voucherError = null) }
            }

            is CheckoutUiEvent.NoteChanged -> {
                updateState { it.copy(note = event.note) }
            }

            is CheckoutUiEvent.ApplyVoucher -> applyVoucher()

            is CheckoutUiEvent.RemoveVoucher -> {
                updateState { it.copy(voucherCode = "", discount = 0.0, voucherError = null) }
            }

            is CheckoutUiEvent.OpenVoucherSheet -> updateState { it.copy(showVoucherSheet = true) }
            is CheckoutUiEvent.CloseVoucherSheet -> updateState { it.copy(showVoucherSheet = false) }

            is CheckoutUiEvent.PlaceOrder -> validateAndPlaceOrder()

            is CheckoutUiEvent.BiometricConfirmed -> placeOrder()

            is CheckoutUiEvent.BiometricDismissed -> {
                updateState { it.copy(showBiometricPrompt = false) }
            }

            is CheckoutUiEvent.DismissError -> updateState { it.copy(errorMessage = null) }
        }
    }

    private fun applyVoucher() {
        val code = uiState.value.voucherCode.trim()
        if (code.isBlank()) {
            updateState { it.copy(voucherError = "Vui long nhap ma voucher") }
            return
        }
        updateState { it.copy(isVoucherLoading = true, voucherError = null) }
        viewModelScope.launch {
            // Simulate voucher check — thay bang ValidateVoucherUseCase sau
            kotlinx.coroutines.delay(600)
            val discountAmount = when (code.uppercase()) {
                "BITE10" -> uiState.value.subtotal * 0.10
                "BITE20" -> uiState.value.subtotal * 0.20
                "FREESHIP" -> 15_000.0
                else -> null
            }
            if (discountAmount != null) {
                updateState {
                    it.copy(
                        isVoucherLoading = false,
                        discount = discountAmount,
                        showVoucherSheet = false,
                    )
                }
                sendEffect(CheckoutUiEffect.ShowSnackbar("Ap dung voucher thanh cong! Giam ${"%,.0f".format(discountAmount)}d"))
            } else {
                updateState { it.copy(isVoucherLoading = false, voucherError = "Ma voucher khong hop le hoac da het han") }
            }
        }
    }

    private fun validateAndPlaceOrder() {
        val state = uiState.value
        if (!state.isAddressValid) {
            updateState { it.copy(addressError = "Vui long nhap dia chi giao hang hop le") }
            return
        }
        if (state.items.isEmpty()) return

        // Trigger biometric for CARD / E_WALLET payments
        if (state.selectedPayment in listOf(PaymentMethod.CARD, PaymentMethod.E_WALLET)) {
            updateState { it.copy(showBiometricPrompt = true) }
            sendEffect(CheckoutUiEffect.TriggerBiometric)
        } else {
            placeOrder()
        }
    }

    private fun placeOrder() {
        val state = uiState.value
        val firstItem = state.items.firstOrNull() ?: return
        updateState { it.copy(isLoading = true, showBiometricPrompt = false) }
        viewModelScope.launch {
            val request = CheckoutOrderUseCase.CheckoutRequest(
                userId = "user_current",
                restaurantId = firstItem.restaurantId,
                restaurantName = firstItem.restaurantId,
                deliveryAddress = state.deliveryAddress,
                paymentMethod = state.selectedPayment,
                voucherCode = state.voucherCode.ifBlank { null },
            )
            when (val result = checkoutOrderUseCase(request)) {
                is CheckoutResult.Success -> {
                    updateState { it.copy(isLoading = false, isOrderPlaced = true, placedOrderId = result.order.id) }
                    sendEffect(CheckoutUiEffect.NavigateToTracking(result.order.id))
                }
                is CheckoutResult.InvalidAddress -> {
                    updateState { it.copy(isLoading = false, addressError = result.reason) }
                }
                is CheckoutResult.VoucherError -> {
                    updateState { it.copy(isLoading = false, voucherError = result.message) }
                }
                is CheckoutResult.Failure -> {
                    updateState { it.copy(isLoading = false, errorMessage = result.exception.message ?: "Dat hang that bai.") }
                }
                CheckoutResult.EmptyCart -> {
                    updateState { it.copy(isLoading = false, errorMessage = "Gio hang trong.") }
                }
            }
        }
    }
}
