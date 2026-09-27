package com.bitefast.feature.profile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.bitefast.core.common.BaseViewModel
import com.bitefast.core.common.UiEffect
import com.bitefast.core.common.UiEvent
import com.bitefast.core.common.UiState
import com.bitefast.core.domain.auth.LogoutUseCase
import com.bitefast.core.domain.repository.AuthRepository
import com.bitefast.core.domain.user.GetAddressesUseCase
import com.bitefast.core.model.Address
import com.bitefast.core.model.User
import com.bitefast.core.model.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

// ─── UiState ─────────────────────────────────────────────────────────────────

data class ProfileUiState(
    val isLoading: Boolean = true,
    val user: User = User(),
    val addresses: List<Address> = emptyList(),
    val showLogoutDialog: Boolean = false,
    val isBiometricEnabled: Boolean = false,
    val isDarkMode: Boolean = false,
    val isNotificationEnabled: Boolean = true,
    val errorMessage: String? = null,
) : UiState {
    val isGuest: Boolean get() = user.isGuest
    val displayName: String get() = user.displayName
    val memberSince: String get() = if (user.createdAt > 0) {
        val months = ((System.currentTimeMillis() - user.createdAt) / (1000L * 60 * 60 * 24 * 30)).toInt()
        if (months < 1) "Thanh vien moi" else "Thanh vien $months thang"
    } else "Thanh vien"
}

// ─── UiEvent ─────────────────────────────────────────────────────────────────

sealed interface ProfileUiEvent : UiEvent {
    data object ClickEditProfile : ProfileUiEvent
    data object ClickAddresses : ProfileUiEvent
    data object ClickOrderHistory : ProfileUiEvent
    data object ClickFavorites : ProfileUiEvent
    data object ClickSupport : ProfileUiEvent
    data object ClickAbout : ProfileUiEvent
    data object RequestLogout : ProfileUiEvent
    data object ConfirmLogout : ProfileUiEvent
    data object DismissLogoutDialog : ProfileUiEvent
    data class ToggleBiometric(val enabled: Boolean) : ProfileUiEvent
    data class ToggleDarkMode(val enabled: Boolean) : ProfileUiEvent
    data class ToggleNotification(val enabled: Boolean) : ProfileUiEvent
    data object DismissError : ProfileUiEvent
}

// ─── UiEffect ────────────────────────────────────────────────────────────────

sealed interface ProfileUiEffect : UiEffect {
    data object NavigateToLogin : ProfileUiEffect
    data object NavigateToEditProfile : ProfileUiEffect
    data object NavigateToAddresses : ProfileUiEffect
    data object NavigateToOrderHistory : ProfileUiEffect
    data class ShowSnackbar(val message: String) : ProfileUiEffect
}

// ─── ViewModel ───────────────────────────────────────────────────────────────

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val logoutUseCase: LogoutUseCase,
    private val getAddressesUseCase: GetAddressesUseCase,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<ProfileUiState, ProfileUiEvent, ProfileUiEffect>(
    initialState = ProfileUiState(),
    savedStateHandle = savedStateHandle,
) {
    init {
        loadUser()
        loadAddresses()
    }

    private fun loadUser() {
        viewModelScope.launch {
            runCatching { authRepository.getCurrentUser() }
                .onSuccess { user ->
                    updateState {
                        it.copy(
                            isLoading = false,
                            user = user ?: User(),
                            isBiometricEnabled = user?.preferences?.biometricAuth ?: false,
                            isNotificationEnabled = user?.preferences?.notifications ?: true,
                        )
                    }
                }
                .onFailure { updateState { it.copy(isLoading = false) } }
        }
    }

    private fun loadAddresses() {
        viewModelScope.launch {
            runCatching { getAddressesUseCase().collect { addrs -> updateState { it.copy(addresses = addrs) } } }
        }
    }

    override fun onEvent(event: ProfileUiEvent) {
        when (event) {
            is ProfileUiEvent.ClickEditProfile -> sendEffect(ProfileUiEffect.NavigateToEditProfile)
            is ProfileUiEvent.ClickAddresses -> sendEffect(ProfileUiEffect.NavigateToAddresses)
            is ProfileUiEvent.ClickOrderHistory -> sendEffect(ProfileUiEffect.NavigateToOrderHistory)
            is ProfileUiEvent.ClickFavorites -> sendEffect(ProfileUiEffect.ShowSnackbar("Chuc nang Yeu thich dang phat trien"))
            is ProfileUiEvent.ClickSupport -> sendEffect(ProfileUiEffect.ShowSnackbar("Lien he hotline: 1900-2048"))
            is ProfileUiEvent.ClickAbout -> sendEffect(ProfileUiEffect.ShowSnackbar("BiteFast v1.0.0 - Dat do an nhanh hon"))

            is ProfileUiEvent.RequestLogout -> updateState { it.copy(showLogoutDialog = true) }
            is ProfileUiEvent.DismissLogoutDialog -> updateState { it.copy(showLogoutDialog = false) }

            is ProfileUiEvent.ConfirmLogout -> {
                updateState { it.copy(showLogoutDialog = false) }
                viewModelScope.launch {
                    runCatching { logoutUseCase() }
                        .onSuccess { sendEffect(ProfileUiEffect.NavigateToLogin) }
                        .onFailure { sendEffect(ProfileUiEffect.ShowSnackbar("Dang xuat that bai")) }
                }
            }

            is ProfileUiEvent.ToggleBiometric -> {
                updateState { it.copy(isBiometricEnabled = event.enabled) }
                sendEffect(ProfileUiEffect.ShowSnackbar(if (event.enabled) "Da bat xac thuc Biometric" else "Da tat xac thuc Biometric"))
            }

            is ProfileUiEvent.ToggleDarkMode -> {
                updateState { it.copy(isDarkMode = event.enabled) }
            }

            is ProfileUiEvent.ToggleNotification -> {
                updateState { it.copy(isNotificationEnabled = event.enabled) }
                sendEffect(ProfileUiEffect.ShowSnackbar(if (event.enabled) "Da bat thong bao" else "Da tat thong bao"))
            }

            is ProfileUiEvent.DismissError -> updateState { it.copy(errorMessage = null) }
        }
    }
}
