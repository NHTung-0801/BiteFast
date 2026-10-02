package com.bitefast.feature.checkout.payment

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.bitefast.core.common.BaseViewModel
import com.bitefast.core.common.UiEffect
import com.bitefast.core.common.UiEvent
import com.bitefast.core.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class PaymentStatusState {
    WAITING,
    SUCCESS,
    EXPIRED,
    FAILED
}

data class PaymentResultUiState(
    val orderId: String = "",
    val amount: Long = 0L,
    val remainingSeconds: Int = 600,
    val status: PaymentStatusState = PaymentStatusState.WAITING,
    val bankName: String = "MB Bank (Ngân hàng Quân Đội)",
    val bankCode: String = "970422",
    val accountNumber: String = "0987654321",
    val accountName: String = "BITEFAST VIETNAM",
    val transferContent: String = "",
    val qrUrl: String = "",
    val errorMessage: String? = null,
) : UiState {
    val formattedTimer: String
        get() {
            val minutes = remainingSeconds / 60
            val seconds = remainingSeconds % 60
            return "%02d:%02d".format(minutes, seconds)
        }
}

sealed interface PaymentResultUiEvent : UiEvent {
    data class InitPayment(val orderId: String, val amount: Long, val qrPayload: String) : PaymentResultUiEvent
    data object ConfirmPaidManually : PaymentResultUiEvent
    data object RetryPayment : PaymentResultUiEvent
    data class CopyToClipboard(val label: String, val text: String) : PaymentResultUiEvent
    data object ClickBack : PaymentResultUiEvent
}

sealed interface PaymentResultUiEffect : UiEffect {
    data class NavigateToTracking(val orderId: String) : PaymentResultUiEffect
    data object NavigateBack : PaymentResultUiEffect
    data class ShowSnackbar(val message: String) : PaymentResultUiEffect
}

@HiltViewModel
class PaymentResultViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<PaymentResultUiState, PaymentResultUiEvent, PaymentResultUiEffect>(
    initialState = PaymentResultUiState(),
    savedStateHandle = savedStateHandle,
) {
    private var timerJob: Job? = null

    fun initialize(orderId: String, amount: Long, qrPayload: String) {
        val content = "BF${orderId.takeLast(6).uppercase()}"
        val qrUrl = if (qrPayload.isNotBlank()) qrPayload
        else "https://img.vietqr.io/image/970422-0987654321-compact2.png?amount=$amount&addInfo=$content&accountName=BITEFAST%20VIETNAM"

        updateState {
            it.copy(
                orderId = orderId,
                amount = amount,
                transferContent = content,
                qrUrl = qrUrl,
                remainingSeconds = 600,
                status = PaymentStatusState.WAITING
            )
        }

        startTimer()
        simulateBankWebhook()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (uiState.value.remainingSeconds > 0 && uiState.value.status == PaymentStatusState.WAITING) {
                delay(1000L)
                updateState {
                    val nextSec = it.remainingSeconds - 1
                    it.copy(
                        remainingSeconds = nextSec,
                        status = if (nextSec <= 0) PaymentStatusState.EXPIRED else it.status
                    )
                }
            }
        }
    }

    private fun simulateBankWebhook() {
        viewModelScope.launch {
            // Giả lập webhook ngân hàng tự động báo nhận tiền sau 8 giây
            delay(8000L)
            if (uiState.value.status == PaymentStatusState.WAITING) {
                triggerPaymentSuccess()
            }
        }
    }

    private fun triggerPaymentSuccess() {
        timerJob?.cancel()
        updateState { it.copy(status = PaymentStatusState.SUCCESS) }
        viewModelScope.launch {
            delay(1500L)
            sendEffect(PaymentResultUiEffect.NavigateToTracking(uiState.value.orderId))
        }
    }

    override fun onEvent(event: PaymentResultUiEvent) {
        when (event) {
            is PaymentResultUiEvent.InitPayment -> {
                initialize(event.orderId, event.amount, event.qrPayload)
            }

            is PaymentResultUiEvent.ConfirmPaidManually -> {
                triggerPaymentSuccess()
            }

            is PaymentResultUiEvent.RetryPayment -> {
                updateState { it.copy(remainingSeconds = 600, status = PaymentStatusState.WAITING) }
                startTimer()
                simulateBankWebhook()
            }

            is PaymentResultUiEvent.CopyToClipboard -> {
                sendEffect(PaymentResultUiEffect.ShowSnackbar("Đã sao chép ${event.label}"))
            }

            is PaymentResultUiEvent.ClickBack -> {
                sendEffect(PaymentResultUiEffect.NavigateBack)
            }
        }
    }
}
