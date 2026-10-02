package com.bitefast.feature.profile.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.bitefast.core.common.BaseViewModel
import com.bitefast.core.common.UiEffect
import com.bitefast.core.common.UiEvent
import com.bitefast.core.common.UiState
import com.bitefast.core.domain.user.GetUserProfileUseCase
import com.bitefast.core.domain.user.ProfileValidationResult
import com.bitefast.core.domain.user.UpdateProfileUseCase
import com.bitefast.core.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

// ─── Preset Avatar IDs ────────────────────────────────────────────────────────

/**
 * Danh sách ký tự Emoji đại diện phong cách Foodie cho phần chọn Avatar nhanh.
 * Mỗi phần tử là một preset ID (emoji string) được lưu vào DataStore.
 */
val PRESET_AVATARS = listOf("🍕", "🍔", "☕", "🍜", "🍣", "🌮", "🍰", "🥗", "👨‍🍳", "🍩")

// ─── UiState ──────────────────────────────────────────────────────────────────

data class EditProfileUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val currentAvatar: String? = null,   // Avatar hiện tại (null = dùng chữ cái đầu)
    val selectedAvatar: String? = null,  // Avatar đang được chọn trong session này
    val nameError: String? = null,
    val phoneError: String? = null,
    val isSuccess: Boolean = false,
) : UiState {
    /** Avatar hiển thị: ưu tiên selectedAvatar trong session, rồi currentAvatar từ DB */
    val displayAvatar: String? get() = selectedAvatar ?: currentAvatar
}

// ─── UiEvent ──────────────────────────────────────────────────────────────────

sealed interface EditProfileUiEvent : UiEvent {
    data class NameChanged(val value: String) : EditProfileUiEvent
    data class PhoneChanged(val value: String) : EditProfileUiEvent
    data class AvatarSelected(val avatar: String) : EditProfileUiEvent
    data object Submit : EditProfileUiEvent
    data object BackClicked : EditProfileUiEvent
}

// ─── UiEffect ─────────────────────────────────────────────────────────────────

sealed interface EditProfileUiEffect : UiEffect {
    data object NavigateBack : EditProfileUiEffect
    data class ShowSnackbar(val message: String) : EditProfileUiEffect
}

// ─── ViewModel ────────────────────────────────────────────────────────────────

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<EditProfileUiState, EditProfileUiEvent, EditProfileUiEffect>(
    initialState = EditProfileUiState(),
    savedStateHandle = savedStateHandle,
) {

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            updateState { it.copy(isLoading = true) }
            runCatching { getUserProfileUseCase() }
                .onSuccess { user ->
                    val u = user ?: User()
                    updateState {
                        it.copy(
                            isLoading = false,
                            name = u.name,
                            phone = u.phone,
                            email = u.email,
                            currentAvatar = u.avatar,
                        )
                    }
                }
                .onFailure {
                    updateState { it.copy(isLoading = false) }
                }
        }
    }

    override fun onEvent(event: EditProfileUiEvent) {
        when (event) {
            is EditProfileUiEvent.NameChanged -> {
                updateState {
                    it.copy(
                        name = event.value,
                        // Xóa lỗi ngay khi người dùng bắt đầu gõ lại
                        nameError = if (event.value.trim().length >= 2) null else it.nameError,
                    )
                }
            }

            is EditProfileUiEvent.PhoneChanged -> {
                updateState {
                    it.copy(
                        phone = event.value,
                        phoneError = null,
                    )
                }
            }

            is EditProfileUiEvent.AvatarSelected -> {
                updateState { it.copy(selectedAvatar = event.avatar) }
            }

            is EditProfileUiEvent.Submit -> saveProfile()

            is EditProfileUiEvent.BackClicked -> sendEffect(EditProfileUiEffect.NavigateBack)
        }
    }

    private fun saveProfile() {
        val state = uiState.value
        if (state.isSaving) return

        updateState { it.copy(isSaving = true, nameError = null, phoneError = null) }

        viewModelScope.launch {
            when (val result = updateProfileUseCase(
                name = state.name,
                phone = state.phone,
                avatar = state.selectedAvatar ?: state.currentAvatar,
            )) {
                is ProfileValidationResult.Invalid -> {
                    updateState { current ->
                        when (result.field) {
                            "name" -> current.copy(isSaving = false, nameError = result.message)
                            "phone" -> current.copy(isSaving = false, phoneError = result.message)
                            else -> current.copy(isSaving = false)
                        }
                    }
                }

                is ProfileValidationResult.Success -> {
                    updateState { it.copy(isSaving = false, isSuccess = true) }
                    sendEffect(EditProfileUiEffect.ShowSnackbar("Cập nhật hồ sơ thành công!"))
                    sendEffect(EditProfileUiEffect.NavigateBack)
                }
            }
        }
    }
}
