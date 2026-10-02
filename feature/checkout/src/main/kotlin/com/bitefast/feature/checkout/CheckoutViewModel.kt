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
import com.bitefast.core.domain.voucher.ApplyVoucherUseCase
import com.bitefast.core.domain.voucher.GetBestVoucherUseCase
import com.bitefast.core.domain.voucher.GetVoucherWalletUseCase
import com.bitefast.core.domain.voucher.VoucherValidationResult
import com.bitefast.core.domain.voucher.VoucherWalletItem
import com.bitefast.core.model.Address
import com.bitefast.core.model.CartItem
import com.bitefast.core.model.PaymentMethod
import com.bitefast.core.model.Voucher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

// ─── UiState ──────────────────────────────────────────────────────────────────

data class CheckoutUiState(
    val isLoading: Boolean = false,
    val items: List<CartItem> = emptyList(),
    val deliveryAddress: Address = Address(
        id = "addr_default",
        recipientName = "Nguyễn Văn A",
        phoneNumber = "0901234567",
        streetAddress = "123 Lê Lợi, Phường Bến Nghé, Quận 1",
        city = "TP. Hồ Chí Minh"
    ),
    val selectedPayment: PaymentMethod = PaymentMethod.CASH,
    val voucherCode: String = "",
    val discount: Double = 0.0,
    val note: String = "",
    // Form validation
    val addressError: String? = null,
    val isVoucherLoading: Boolean = false,
    val voucherError: String? = null,
    val showVoucherSheet: Boolean = false,
    val availableVouchers: List<VoucherWalletItem> = emptyList(),
    val bestVoucherSuggestion: String? = null,
    val showBiometricPrompt: Boolean = false,
    val isOrderPlaced: Boolean = false,
    val placedOrderId: String? = null,
    val errorMessage: String? = null,
) : UiState {
    val subtotal: Double get() = items.sumOf { it.totalPrice }
    val deliveryFee: Double get() = if (items.isNotEmpty()) 15_000.0 else 0.0
    val total: Double get() = (subtotal + deliveryFee - discount).coerceAtLeast(0.0)
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
    data class SelectVoucherFromSheet(val voucher: Voucher, val discount: Double) : CheckoutUiEvent
    data object RemoveVoucher : CheckoutUiEvent
    data object OpenVoucherSheet : CheckoutUiEvent
    data object CloseVoucherSheet : CheckoutUiEvent
    data object AutoApplyBestVoucher : CheckoutUiEvent
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
    private val applyVoucherUseCase: ApplyVoucherUseCase,
    private val getBestVoucherUseCase: GetBestVoucherUseCase,
    private val getVoucherWalletUseCase: GetVoucherWalletUseCase,
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
                checkBestVouchers(items)
            }
        }
    }

    private fun checkBestVouchers(items: List<CartItem>) {
        if (items.isEmpty()) return
        val subtotal = items.sumOf { it.totalPrice }
        val restaurantId = items.first().restaurantId
        viewModelScope.launch {
            val bestResult = getBestVoucherUseCase(
                subtotal = subtotal,
                restaurantId = restaurantId,
                deliveryFee = 15_000.0
            )
            val walletState = getVoucherWalletUseCase(
                subtotal = subtotal,
                restaurantId = restaurantId,
                deliveryFee = 15_000.0
            )
            updateState {
                it.copy(
                    availableVouchers = walletState.items,
                    bestVoucherSuggestion = bestResult.message
                )
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

            is CheckoutUiEvent.SelectVoucherFromSheet -> {
                updateState {
                    it.copy(
                        voucherCode = event.voucher.code,
                        discount = event.discount,
                        voucherError = null,
                        showVoucherSheet = false
                    )
                }
                sendEffect(CheckoutUiEffect.ShowSnackbar("Áp dụng mã ${event.voucher.code} (-%,.0fđ)".format(event.discount)))
            }

            is CheckoutUiEvent.AutoApplyBestVoucher -> autoApplyBestVoucher()

            is CheckoutUiEvent.RemoveVoucher -> {
                updateState { it.copy(voucherCode = "", discount = 0.0, voucherError = null) }
            }

            is CheckoutUiEvent.OpenVoucherSheet -> {
                val state = uiState.value
                val firstItem = state.items.firstOrNull()
                viewModelScope.launch {
                    val walletState = getVoucherWalletUseCase(
                        subtotal = state.subtotal,
                        restaurantId = firstItem?.restaurantId ?: "",
                        deliveryFee = state.deliveryFee
                    )
                    updateState { it.copy(showVoucherSheet = true, availableVouchers = walletState.items) }
                }
            }
            is CheckoutUiEvent.CloseVoucherSheet -> updateState { it.copy(showVoucherSheet = false) }

            is CheckoutUiEvent.PlaceOrder -> validateAndPlaceOrder()

            is CheckoutUiEvent.BiometricConfirmed -> placeOrder()

            is CheckoutUiEvent.BiometricDismissed -> {
                updateState { it.copy(showBiometricPrompt = false) }
            }

            is CheckoutUiEvent.DismissError -> updateState { it.copy(errorMessage = null) }
        }
    }

    private fun autoApplyBestVoucher() {
        val state = uiState.value
        val firstItem = state.items.firstOrNull() ?: return
        viewModelScope.launch {
            val bestResult = getBestVoucherUseCase(
                subtotal = state.subtotal,
                restaurantId = firstItem.restaurantId,
                deliveryFee = state.deliveryFee
            )
            val bestVoucher = bestResult.bestVoucher
            if (bestVoucher != null) {
                updateState {
                    it.copy(
                        voucherCode = bestVoucher.code,
                        discount = bestResult.bestDiscount,
                        voucherError = null
                    )
                }
                sendEffect(CheckoutUiEffect.ShowSnackbar("Đã áp dụng mã hời nhất ${bestVoucher.code}!"))
            }
        }
    }

    private fun applyVoucher() {
        val state = uiState.value
        val code = state.voucherCode.trim()
        if (code.isBlank()) {
            updateState { it.copy(voucherError = "Vui lòng nhập mã voucher") }
            return
        }
        val firstItem = state.items.firstOrNull() ?: return
        updateState { it.copy(isVoucherLoading = true, voucherError = null) }
        viewModelScope.launch {
            val result = applyVoucherUseCase(
                code = code,
                subtotal = state.subtotal,
                restaurantId = firstItem.restaurantId,
                deliveryFee = state.deliveryFee
            )
            when (result) {
                is VoucherValidationResult.Valid -> {
                    updateState {
                        it.copy(
                            isVoucherLoading = false,
                            voucherCode = result.voucher.code,
                            discount = result.calculatedDiscount,
                            showVoucherSheet = false,
                        )
                    }
                    sendEffect(CheckoutUiEffect.ShowSnackbar("Áp dụng mã ${result.voucher.code} thành công! Giảm %,.0fđ".format(result.calculatedDiscount)))
                }
                is VoucherValidationResult.Invalid -> {
                    updateState {
                        it.copy(
                            isVoucherLoading = false,
                            voucherError = result.message
                        )
                    }
                }
            }
        }
    }

    private fun validateAndPlaceOrder() {
        val state = uiState.value
        if (!state.isAddressValid) {
            val errorMsg = if (state.deliveryAddress.streetAddress.isBlank()) {
                "Vui lòng nhập địa chỉ nhận hàng"
            } else {
                "Vui lòng nhập số điện thoại liên hệ"
            }
            updateState { it.copy(addressError = errorMsg) }
            sendEffect(CheckoutUiEffect.ShowSnackbar(errorMsg))
            return
        }
        if (state.items.isEmpty()) {
            sendEffect(CheckoutUiEffect.ShowSnackbar("Giỏ hàng của bạn đang trống"))
            return
        }

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
                    updateState { it.copy(isLoading = false, errorMessage = result.exception.message ?: "Đặt hàng thất bại.") }
                }
                CheckoutResult.EmptyCart -> {
                    updateState { it.copy(isLoading = false, errorMessage = "Giỏ hàng trống.") }
                }
            }
        }
    }
}
