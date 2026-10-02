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
import dagger.hilt.android.lifecycle.HiltViewModel
import com.bitefast.core.domain.favorite.GetFavoritesUseCase
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

// ─── UiState ──────────────────────────────────────────────────────────────────

data class ProfileUiState(
    val isLoading: Boolean = true,
    val user: User = User(),
    val addresses: List<Address> = emptyList(),
    val showLogoutDialog: Boolean = false,
    val showAboutDialog: Boolean = false,
    val isNotificationEnabled: Boolean = true,
    val favoriteCount: Int = 0,
    val errorMessage: String? = null,
) : UiState {
    val isGuest: Boolean get() = user.isGuest
    val displayName: String get() = user.displayName
    val memberSince: String get() = if (user.createdAt > 0) {
        val months = ((System.currentTimeMillis() - user.createdAt) / (1000L * 60 * 60 * 24 * 30)).toInt()
        if (months < 1) "Thành viên mới" else "Thành viên $months tháng"
    } else "Thành viên"
}

// ─── UiEvent ──────────────────────────────────────────────────────────────────

sealed interface ProfileUiEvent : UiEvent {
    data object ClickLogin : ProfileUiEvent
    data object ClickEditProfile : ProfileUiEvent
    data object ClickAddresses : ProfileUiEvent
    data object ClickVoucherWallet : ProfileUiEvent
    data object ClickOrderHistory : ProfileUiEvent
    data object ClickFavorites : ProfileUiEvent
    data object ClickAbout : ProfileUiEvent
    data object DismissAboutDialog : ProfileUiEvent
    data object RequestLogout : ProfileUiEvent
    data object ConfirmLogout : ProfileUiEvent
    data object DismissLogoutDialog : ProfileUiEvent
    data object ReloadUser : ProfileUiEvent
    data class ToggleNotification(val enabled: Boolean) : ProfileUiEvent
    data object DismissError : ProfileUiEvent
}

// ─── UiEffect ─────────────────────────────────────────────────────────────────

sealed interface ProfileUiEffect : UiEffect {
    data object NavigateToLogin : ProfileUiEffect
    data object NavigateToEditProfile : ProfileUiEffect
    data object NavigateToAddresses : ProfileUiEffect
    data object NavigateToVoucherWallet : ProfileUiEffect
    data object NavigateToOrderHistory : ProfileUiEffect
    data object NavigateToFavorites : ProfileUiEffect
    data class ShowSnackbar(val message: String) : ProfileUiEffect
}

// ─── ViewModel ────────────────────────────────────────────────────────────────

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val logoutUseCase: LogoutUseCase,
    private val getAddressesUseCase: GetAddressesUseCase,
    private val getFavoritesUseCase: GetFavoritesUseCase,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<ProfileUiState, ProfileUiEvent, ProfileUiEffect>(
    initialState = ProfileUiState(),
    savedStateHandle = savedStateHandle,
) {
    init {
        loadUser()
        loadAddresses()
        observeFavorites()
    }

    private fun observeFavorites() {
        viewModelScope.launch {
            getFavoritesUseCase.getCount()
                .catch { emit(0) }
                .collect { count -> updateState { it.copy(favoriteCount = count) } }
        }
    }

    private fun loadUser() {
        viewModelScope.launch {
            runCatching { authRepository.getCurrentUser() }
                .onSuccess { user ->
                    updateState {
                        it.copy(
                            isLoading = false,
                            user = user ?: User(),
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
            is ProfileUiEvent.ClickLogin -> sendEffect(ProfileUiEffect.NavigateToLogin)
            is ProfileUiEvent.ClickEditProfile -> sendEffect(ProfileUiEffect.NavigateToEditProfile)
            is ProfileUiEvent.ReloadUser -> loadUser()
            is ProfileUiEvent.ClickAddresses -> sendEffect(ProfileUiEffect.NavigateToAddresses)
            is ProfileUiEvent.ClickVoucherWallet -> sendEffect(ProfileUiEffect.NavigateToVoucherWallet)
            is ProfileUiEvent.ClickOrderHistory -> sendEffect(ProfileUiEffect.NavigateToOrderHistory)
            is ProfileUiEvent.ClickFavorites -> sendEffect(ProfileUiEffect.NavigateToFavorites)
            is ProfileUiEvent.ClickAbout -> updateState { it.copy(showAboutDialog = true) }
            is ProfileUiEvent.DismissAboutDialog -> updateState { it.copy(showAboutDialog = false) }

            is ProfileUiEvent.RequestLogout -> updateState { it.copy(showLogoutDialog = true) }
            is ProfileUiEvent.DismissLogoutDialog -> updateState { it.copy(showLogoutDialog = false) }

            is ProfileUiEvent.ConfirmLogout -> {
                updateState { it.copy(showLogoutDialog = false) }
                viewModelScope.launch {
                    runCatching { logoutUseCase() }
                        .onSuccess { sendEffect(ProfileUiEffect.NavigateToLogin) }
                        .onFailure { sendEffect(ProfileUiEffect.ShowSnackbar("Đăng xuất thất bại")) }
                }
            }

            is ProfileUiEvent.ToggleNotification -> {
                updateState { it.copy(isNotificationEnabled = event.enabled) }
                sendEffect(ProfileUiEffect.ShowSnackbar(if (event.enabled) "Đã bật thông báo" else "Đã tắt thông báo"))
            }

            is ProfileUiEvent.DismissError -> updateState { it.copy(errorMessage = null) }
        }
    }
}

