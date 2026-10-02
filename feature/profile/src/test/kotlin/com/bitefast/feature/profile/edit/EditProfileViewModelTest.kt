package com.bitefast.feature.profile.edit

import androidx.lifecycle.SavedStateHandle
import com.bitefast.core.domain.user.GetUserProfileUseCase
import com.bitefast.core.domain.user.ProfileValidationResult
import com.bitefast.core.domain.user.UpdateProfileUseCase
import com.bitefast.core.model.User
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EditProfileViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var getUserProfileUseCase: GetUserProfileUseCase
    private lateinit var updateProfileUseCase: UpdateProfileUseCase

    private val sampleUser = User(
        id = "usr_123",
        name = "Nguyễn Văn A",
        email = "nguyenvana@gmail.com",
        phone = "0909123456",
    )

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getUserProfileUseCase = mockk()
        updateProfileUseCase = mockk()
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): EditProfileViewModel {
        coEvery { getUserProfileUseCase() } returns sampleUser
        return EditProfileViewModel(
            getUserProfileUseCase = getUserProfileUseCase,
            updateProfileUseCase = updateProfileUseCase,
            savedStateHandle = SavedStateHandle(),
        )
    }

    // ── Tải hồ sơ ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Khi khởi tạo ViewModel, thông tin hồ sơ phải được tải thành công")
    fun `init - loads user profile successfully`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Nguyễn Văn A", state.name)
        assertEquals("nguyenvana@gmail.com", state.email)
        assertEquals("0909123456", state.phone)
    }

    @Test
    @DisplayName("Khi API tải hồ sơ thất bại, isLoading phải là false và không crash")
    fun `init - handles load failure gracefully`() = runTest {
        coEvery { getUserProfileUseCase() } throws RuntimeException("Network error")
        val viewModel = EditProfileViewModel(
            getUserProfileUseCase = getUserProfileUseCase,
            updateProfileUseCase = updateProfileUseCase,
            savedStateHandle = SavedStateHandle(),
        )
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
    }

    // ── Thay đổi input ────────────────────────────────────────────────────────

    @Test
    @DisplayName("Khi người dùng nhập tên, state.name phải cập nhật ngay lập tức")
    fun `NameChanged - updates name in state`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(EditProfileUiEvent.NameChanged("Lê Thị B"))
        assertEquals("Lê Thị B", viewModel.uiState.value.name)
    }

    @Test
    @DisplayName("Khi người dùng nhập SĐT hợp lệ, phoneError phải là null")
    fun `PhoneChanged - clears phone error when typing`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(EditProfileUiEvent.PhoneChanged("0987654321"))
        assertNull(viewModel.uiState.value.phoneError)
    }

    @Test
    @DisplayName("Khi chọn Avatar, selectedAvatar phải được cập nhật đúng")
    fun `AvatarSelected - updates selectedAvatar in state`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(EditProfileUiEvent.AvatarSelected("🍕"))
        assertEquals("🍕", viewModel.uiState.value.selectedAvatar)
        assertEquals("🍕", viewModel.uiState.value.displayAvatar)
    }

    // ── Validation ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Submit với tên rỗng phải hiển thị lỗi nameError")
    fun `Submit - shows nameError when name is too short`() = runTest {
        coEvery { updateProfileUseCase(any(), any(), any()) } returns ProfileValidationResult.Invalid(
            field = "name",
            message = "Họ và tên phải có ít nhất 2 ký tự."
        )
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(EditProfileUiEvent.NameChanged("A"))
        viewModel.onEvent(EditProfileUiEvent.Submit)
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.nameError)
        assertFalse(viewModel.uiState.value.isSaving)
    }

    @Test
    @DisplayName("Submit với SĐT không hợp lệ phải hiển thị lỗi phoneError")
    fun `Submit - shows phoneError when phone is invalid`() = runTest {
        coEvery { updateProfileUseCase(any(), any(), any()) } returns ProfileValidationResult.Invalid(
            field = "phone",
            message = "Số điện thoại không hợp lệ."
        )
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(EditProfileUiEvent.PhoneChanged("123"))
        viewModel.onEvent(EditProfileUiEvent.Submit)
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.phoneError)
        assertFalse(viewModel.uiState.value.isSaving)
    }

    // ── Lưu thành công ────────────────────────────────────────────────────────

    @Test
    @DisplayName("Submit thành công phải đặt isSuccess = true và phát effect NavigateBack")
    fun `Submit - on success sets isSuccess and emits NavigateBack`() = runTest {
        val updatedUser = sampleUser.copy(name = "Nguyễn Hữu Tùng")
        coEvery { updateProfileUseCase(any(), any(), any()) } returns ProfileValidationResult.Success(updatedUser)

        val viewModel = createViewModel()
        advanceUntilIdle()

        val effects = mutableListOf<EditProfileUiEffect>()
        val effectJob = launch { viewModel.effect.collect { effects.add(it) } }

        viewModel.onEvent(EditProfileUiEvent.Submit)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isSuccess)
        assertFalse(viewModel.uiState.value.isSaving)
        assertTrue(effects.any { it is EditProfileUiEffect.NavigateBack })

        effectJob.cancel()
    }

    @Test
    @DisplayName("Submit thành công phải gọi updateProfileUseCase đúng 1 lần với tham số đúng")
    fun `Submit - calls updateProfileUseCase once with correct params`() = runTest {
        coEvery { updateProfileUseCase("Nguyễn Hữu Tùng", "0987654321", any()) } returns
            ProfileValidationResult.Success(sampleUser)

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(EditProfileUiEvent.NameChanged("Nguyễn Hữu Tùng"))
        viewModel.onEvent(EditProfileUiEvent.PhoneChanged("0987654321"))
        viewModel.onEvent(EditProfileUiEvent.Submit)
        advanceUntilIdle()

        coVerify(exactly = 1) { updateProfileUseCase("Nguyễn Hữu Tùng", "0987654321", any()) }
    }

    // ── BackClicked ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("BackClicked phải phát effect NavigateBack ngay lập tức")
    fun `BackClicked - emits NavigateBack effect`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val effects = mutableListOf<EditProfileUiEffect>()
        val effectJob = launch { viewModel.effect.collect { effects.add(it) } }

        viewModel.onEvent(EditProfileUiEvent.BackClicked)
        advanceUntilIdle()

        assertTrue(effects.any { it is EditProfileUiEffect.NavigateBack })
        effectJob.cancel()
    }
}
