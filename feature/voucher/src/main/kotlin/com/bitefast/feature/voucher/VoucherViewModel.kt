package com.bitefast.feature.voucher

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.bitefast.core.common.BaseViewModel
import com.bitefast.core.common.UiEffect
import com.bitefast.core.common.UiEvent
import com.bitefast.core.common.UiState
import com.bitefast.core.domain.voucher.ApplyVoucherUseCase
import com.bitefast.core.domain.voucher.GetVoucherWalletUseCase
import com.bitefast.core.domain.voucher.VoucherCategory
import com.bitefast.core.domain.voucher.VoucherValidationResult
import com.bitefast.core.domain.voucher.VoucherWalletItem
import com.bitefast.core.domain.voucher.VoucherWalletState
import com.bitefast.core.model.Voucher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VoucherUiState(
    val isLoading: Boolean = false,
    val selectedCategory: VoucherCategory = VoucherCategory.ALL,
    val walletState: VoucherWalletState = VoucherWalletState(),
    val customVoucherCode: String = "",
    val isValidatingCustomCode: Boolean = false,
    val customCodeError: String? = null,
    val subtotal: Double = 0.0,
    val restaurantId: String = "",
    val selectedCode: String? = null,
    val snackbarMessage: String? = null
) : UiState {
    val currentItems: List<VoucherWalletItem> get() = walletState.items
}

sealed interface VoucherUiEvent : UiEvent {
    data class SelectCategory(val category: VoucherCategory) : VoucherUiEvent
    data class CustomCodeChanged(val code: String) : VoucherUiEvent
    data object ApplyCustomCode : VoucherUiEvent
    data class SelectVoucher(val voucher: Voucher, val discount: Double) : VoucherUiEvent
    data class CopyCode(val code: String) : VoucherUiEvent
    data object DismissMessage : VoucherUiEvent
    data object ClickBack : VoucherUiEvent
}

sealed interface VoucherUiEffect : UiEffect {
    data class VoucherSelected(val voucherCode: String, val discount: Double) : VoucherUiEffect
    data class ShowSnackbar(val message: String) : VoucherUiEffect
    data class CopyToClipboard(val code: String) : VoucherUiEffect
    data object NavigateBack : VoucherUiEffect
}

@HiltViewModel
class VoucherViewModel @Inject constructor(
    private val getVoucherWalletUseCase: GetVoucherWalletUseCase,
    private val applyVoucherUseCase: ApplyVoucherUseCase,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<VoucherUiState, VoucherUiEvent, VoucherUiEffect>(
    initialState = VoucherUiState(
        subtotal = savedStateHandle.get<Double>("subtotal") ?: 0.0,
        restaurantId = savedStateHandle.get<String>("restaurantId") ?: "",
        selectedCode = savedStateHandle.get<String>("selectedCode")
    ),
    savedStateHandle = savedStateHandle,
) {
    init {
        loadVouchers()
    }

    override fun onEvent(event: VoucherUiEvent) {
        when (event) {
            is VoucherUiEvent.SelectCategory -> {
                updateState { it.copy(selectedCategory = event.category) }
                loadVouchers()
            }
            is VoucherUiEvent.CustomCodeChanged -> {
                updateState { it.copy(customVoucherCode = event.code, customCodeError = null) }
            }
            is VoucherUiEvent.ApplyCustomCode -> validateAndApplyCustomCode()
            is VoucherUiEvent.SelectVoucher -> {
                updateState { it.copy(selectedCode = event.voucher.code) }
                sendEffect(VoucherUiEffect.VoucherSelected(event.voucher.code, event.discount))
            }
            is VoucherUiEvent.CopyCode -> {
                sendEffect(VoucherUiEffect.CopyToClipboard(event.code))
                sendEffect(VoucherUiEffect.ShowSnackbar("Đã sao chép mã ${event.code}"))
            }
            is VoucherUiEvent.DismissMessage -> {
                updateState { it.copy(snackbarMessage = null, customCodeError = null) }
            }
            is VoucherUiEvent.ClickBack -> sendEffect(VoucherUiEffect.NavigateBack)
        }
    }

    private fun loadVouchers() {
        updateState { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                val currentState = uiState.value
                val wallet = getVoucherWalletUseCase(
                    subtotal = currentState.subtotal,
                    restaurantId = currentState.restaurantId,
                    category = currentState.selectedCategory
                )
                updateState { it.copy(isLoading = false, walletState = wallet) }
            } catch (e: Exception) {
                updateState { it.copy(isLoading = false) }
                sendEffect(VoucherUiEffect.ShowSnackbar("Không thể tải danh sách voucher: ${e.message}"))
            }
        }
    }

    private fun validateAndApplyCustomCode() {
        val currentState = uiState.value
        val code = currentState.customVoucherCode.trim()
        if (code.isBlank()) {
            updateState { it.copy(customCodeError = "Vui lòng nhập mã voucher") }
            return
        }

        updateState { it.copy(isValidatingCustomCode = true, customCodeError = null) }
        viewModelScope.launch {
            val result = applyVoucherUseCase(
                code = code,
                subtotal = currentState.subtotal,
                restaurantId = currentState.restaurantId
            )
            when (result) {
                is VoucherValidationResult.Valid -> {
                    updateState {
                        it.copy(
                            isValidatingCustomCode = false,
                            selectedCode = result.voucher.code
                        )
                    }
                    sendEffect(VoucherUiEffect.VoucherSelected(result.voucher.code, result.calculatedDiscount))
                    sendEffect(VoucherUiEffect.ShowSnackbar("Áp dụng mã ${result.voucher.code} thành công!"))
                }
                is VoucherValidationResult.Invalid -> {
                    updateState {
                        it.copy(
                            isValidatingCustomCode = false,
                            customCodeError = result.message
                        )
                    }
                }
            }
        }
    }
}
