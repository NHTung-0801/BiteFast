package com.bitefast.feature.auth

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.bitefast.core.common.BaseViewModel
import com.bitefast.core.common.extension.isValidEmail
import com.bitefast.core.common.extension.isValidPassword
import com.bitefast.core.domain.auth.EnableGuestModeUseCase
import com.bitefast.core.domain.auth.LoginUseCase
import com.bitefast.core.domain.auth.RegisterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val enableGuestModeUseCase: EnableGuestModeUseCase,
    savedStateHandle: SavedStateHandle
) : BaseViewModel<AuthUiState, AuthUiEvent, AuthUiEffect>(
    initialState = AuthUiState(),
    savedStateHandle = savedStateHandle
) {

    override fun onEvent(event: AuthUiEvent) {
        when (event) {
            is AuthUiEvent.OnNameChanged -> updateState { it.copy(name = event.name, nameError = null) }
            is AuthUiEvent.OnEmailChanged -> updateState { it.copy(email = event.email, emailError = null) }
            is AuthUiEvent.OnPasswordChanged -> updateState { it.copy(password = event.password, passwordError = null) }
            is AuthUiEvent.OnTogglePasswordVisibility -> updateState { it.copy(isPasswordVisible = !it.isPasswordVisible) }
            is AuthUiEvent.OnLoginClicked -> login()
            is AuthUiEvent.OnRegisterClicked -> register()
            is AuthUiEvent.OnGuestModeClicked -> enableGuestMode()
            is AuthUiEvent.OnClearErrors -> updateState { it.copy(nameError = null, emailError = null, passwordError = null) }
        }
    }

    private fun login() {
        val currentState = uiState.value
        val email = currentState.email.trim()
        val password = currentState.password

        var hasError = false
        var emailError: String? = null
        var passwordError: String? = null

        if (!email.isValidEmail()) {
            emailError = "Vui lòng nhập địa chỉ email hợp lệ"
            hasError = true
        }

        if (password.isBlank()) {
            passwordError = "Vui lòng nhập mật khẩu"
            hasError = true
        } else if (password.length < 8) {
            passwordError = "Mật khẩu phải có ít nhất 8 ký tự"
            hasError = true
        }

        if (hasError) {
            updateState { it.copy(emailError = emailError, passwordError = passwordError) }
            return
        }

        updateState { it.copy(isLoading = true, emailError = null, passwordError = null) }

        viewModelScope.launch {
            try {
                loginUseCase(email, password)
                updateState { it.copy(isLoading = false) }
                sendEffect(AuthUiEffect.NavigateToHome)
            } catch (e: Exception) {
                updateState { it.copy(isLoading = false) }
                sendEffect(AuthUiEffect.ShowSnackbar(e.localizedMessage ?: "Đăng nhập thất bại. Vui lòng thử lại."))
            }
        }
    }

    private fun register() {
        val currentState = uiState.value
        val name = currentState.name.trim()
        val email = currentState.email.trim()
        val password = currentState.password

        var hasError = false
        var nameError: String? = null
        var emailError: String? = null
        var passwordError: String? = null

        if (name.isBlank()) {
            nameError = "Vui lòng nhập họ và tên của bạn"
            hasError = true
        }

        if (!email.isValidEmail()) {
            emailError = "Vui lòng nhập địa chỉ email hợp lệ"
            hasError = true
        }

        if (password.isBlank()) {
            passwordError = "Vui lòng nhập mật khẩu"
            hasError = true
        } else if (!password.isValidPassword()) {
            passwordError = "Mật khẩu tối thiểu 8 ký tự, bao gồm cả chữ và số"
            hasError = true
        }

        if (hasError) {
            updateState { it.copy(nameError = nameError, emailError = emailError, passwordError = passwordError) }
            return
        }

        updateState { it.copy(isLoading = true, nameError = null, emailError = null, passwordError = null) }

        viewModelScope.launch {
            try {
                registerUseCase(name, email, password)
                updateState { it.copy(isLoading = false) }
                sendEffect(AuthUiEffect.NavigateToHome)
            } catch (e: Exception) {
                updateState { it.copy(isLoading = false) }
                sendEffect(AuthUiEffect.ShowSnackbar(e.localizedMessage ?: "Đăng ký thất bại. Vui lòng thử lại."))
            }
        }
    }

    private fun enableGuestMode() {
        viewModelScope.launch {
            try {
                enableGuestModeUseCase()
                updateState { it.copy(isGuest = true) }
                sendEffect(AuthUiEffect.NavigateToHome)
            } catch (e: Exception) {
                sendEffect(AuthUiEffect.ShowSnackbar("Không thể kích hoạt chế độ khách."))
            }
        }
    }
}
