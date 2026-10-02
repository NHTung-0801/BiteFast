package com.bitefast.feature.auth.forgot

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

enum class ForgotStep {
    INPUT_IDENTIFIER,
    ENTER_OTP,
    RESET_PASSWORD,
    SUCCESS
}

data class ForgotPasswordUiState(
    val step: ForgotStep = ForgotStep.INPUT_IDENTIFIER,
    val identifier: String = "",
    val otp: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val isPasswordVisible: Boolean = false,
    val remainingSeconds: Int = 60,
    val canResend: Boolean = false,
    val isLoading: Boolean = false,
    val identifierError: String? = null,
    val otpError: String? = null,
    val passwordError: String? = null,
) : UiState

sealed interface ForgotPasswordUiEvent : UiEvent {
    data class IdentifierChanged(val value: String) : ForgotPasswordUiEvent
    data class OtpChanged(val value: String) : ForgotPasswordUiEvent
    data class NewPasswordChanged(val value: String) : ForgotPasswordUiEvent
    data class ConfirmPasswordChanged(val value: String) : ForgotPasswordUiEvent
    data object TogglePasswordVisibility : ForgotPasswordUiEvent
    data object RequestOtp : ForgotPasswordUiEvent
    data object ResendOtp : ForgotPasswordUiEvent
    data object VerifyOtp : ForgotPasswordUiEvent
    data object ResetPassword : ForgotPasswordUiEvent
    data object ClickBack : ForgotPasswordUiEvent
}

sealed interface ForgotPasswordUiEffect : UiEffect {
    data object NavigateToLogin : ForgotPasswordUiEffect
    data class ShowSnackbar(val message: String) : ForgotPasswordUiEffect
}

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<ForgotPasswordUiState, ForgotPasswordUiEvent, ForgotPasswordUiEffect>(
    initialState = ForgotPasswordUiState(),
    savedStateHandle = savedStateHandle,
) {
    private var countdownJob: Job? = null

    private fun startCountdown() {
        countdownJob?.cancel()
        updateState { it.copy(remainingSeconds = 60, canResend = false) }
        countdownJob = viewModelScope.launch {
            while (uiState.value.remainingSeconds > 0) {
                delay(1000L)
                updateState {
                    val sec = it.remainingSeconds - 1
                    it.copy(
                        remainingSeconds = sec,
                        canResend = sec <= 0
                    )
                }
            }
        }
    }

    override fun onEvent(event: ForgotPasswordUiEvent) {
        when (event) {
            is ForgotPasswordUiEvent.IdentifierChanged -> {
                updateState { it.copy(identifier = event.value, identifierError = null) }
            }

            is ForgotPasswordUiEvent.OtpChanged -> {
                if (event.value.length <= 6 && event.value.all { it.isDigit() }) {
                    updateState { it.copy(otp = event.value, otpError = null) }
                }
            }

            is ForgotPasswordUiEvent.NewPasswordChanged -> {
                updateState { it.copy(newPassword = event.value, passwordError = null) }
            }

            is ForgotPasswordUiEvent.ConfirmPasswordChanged -> {
                updateState { it.copy(confirmPassword = event.value, passwordError = null) }
            }

            is ForgotPasswordUiEvent.TogglePasswordVisibility -> {
                updateState { it.copy(isPasswordVisible = !it.isPasswordVisible) }
            }

            is ForgotPasswordUiEvent.RequestOtp -> {
                val id = uiState.value.identifier.trim()
                if (id.isBlank()) {
                    updateState { it.copy(identifierError = "Vui lòng nhập Email hoặc Số điện thoại") }
                    return
                }
                viewModelScope.launch {
                    updateState { it.copy(isLoading = true) }
                    delay(800L) // Giả lập gửi OTP qua SMS / Email
                    updateState { it.copy(isLoading = false, step = ForgotStep.ENTER_OTP) }
                    startCountdown()
                    sendEffect(ForgotPasswordUiEffect.ShowSnackbar("Mã OTP đã được gửi đến $id"))
                }
            }

            is ForgotPasswordUiEvent.ResendOtp -> {
                if (uiState.value.canResend) {
                    startCountdown()
                    sendEffect(ForgotPasswordUiEffect.ShowSnackbar("Đã gửi lại mã OTP mới"))
                }
            }

            is ForgotPasswordUiEvent.VerifyOtp -> {
                val otp = uiState.value.otp.trim()
                if (otp.length < 6) {
                    updateState { it.copy(otpError = "Vui lòng nhập đủ 6 chữ số OTP") }
                    return
                }
                viewModelScope.launch {
                    updateState { it.copy(isLoading = true) }
                    delay(600L)
                    updateState { it.copy(isLoading = false, step = ForgotStep.RESET_PASSWORD) }
                }
            }

            is ForgotPasswordUiEvent.ResetPassword -> {
                val state = uiState.value
                if (state.newPassword.length < 8) {
                    updateState { it.copy(passwordError = "Mật khẩu phải có tối thiểu 8 ký tự") }
                    return
                }
                if (state.newPassword != state.confirmPassword) {
                    updateState { it.copy(passwordError = "Mật khẩu xác nhận không khớp") }
                    return
                }
                viewModelScope.launch {
                    updateState { it.copy(isLoading = true) }
                    delay(800L)
                    updateState { it.copy(isLoading = false, step = ForgotStep.SUCCESS) }
                    sendEffect(ForgotPasswordUiEffect.ShowSnackbar("Đổi mật khẩu thành công! Vui lòng đăng nhập lại."))
                    delay(1200L)
                    sendEffect(ForgotPasswordUiEffect.NavigateToLogin)
                }
            }

            is ForgotPasswordUiEvent.ClickBack -> {
                when (uiState.value.step) {
                    ForgotStep.INPUT_IDENTIFIER -> sendEffect(ForgotPasswordUiEffect.NavigateToLogin)
                    ForgotStep.ENTER_OTP -> updateState { it.copy(step = ForgotStep.INPUT_IDENTIFIER) }
                    ForgotStep.RESET_PASSWORD -> updateState { it.copy(step = ForgotStep.ENTER_OTP) }
                    ForgotStep.SUCCESS -> sendEffect(ForgotPasswordUiEffect.NavigateToLogin)
                }
            }
        }
    }
}
