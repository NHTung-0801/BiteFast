package com.bitefast.feature.auth

import com.bitefast.core.common.UiEffect
import com.bitefast.core.common.UiEvent
import com.bitefast.core.common.UiState

data class AuthUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val nameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val isLoading: Boolean = false,
    val isGuest: Boolean = false
) : UiState

sealed interface AuthUiEvent : UiEvent {
    data class OnNameChanged(val name: String) : AuthUiEvent
    data class OnEmailChanged(val email: String) : AuthUiEvent
    data class OnPasswordChanged(val password: String) : AuthUiEvent
    data object OnTogglePasswordVisibility : AuthUiEvent
    data object OnLoginClicked : AuthUiEvent
    data object OnRegisterClicked : AuthUiEvent
    data object OnGuestModeClicked : AuthUiEvent
    data object OnClearErrors : AuthUiEvent
}

sealed interface AuthUiEffect : UiEffect {
    data object NavigateToHome : AuthUiEffect
    data object NavigateToRegister : AuthUiEffect
    data object NavigateToLogin : AuthUiEffect
    data class ShowSnackbar(val message: String) : AuthUiEffect
}
