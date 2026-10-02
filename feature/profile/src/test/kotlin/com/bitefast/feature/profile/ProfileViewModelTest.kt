package com.bitefast.feature.profile

import androidx.lifecycle.SavedStateHandle
import com.bitefast.core.domain.auth.LogoutUseCase
import com.bitefast.core.domain.favorite.GetFavoritesUseCase
import com.bitefast.core.domain.repository.AuthRepository
import com.bitefast.core.domain.user.GetAddressesUseCase
import com.bitefast.core.model.User
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var authRepository: AuthRepository
    private lateinit var logoutUseCase: LogoutUseCase
    private lateinit var getAddressesUseCase: GetAddressesUseCase
    private lateinit var getFavoritesUseCase: GetFavoritesUseCase

    private val sampleUser = User(
        id = "usr_123",
        name = "Nguyễn Văn A",
        email = "vana@example.com",
        phone = "0909123456"
    )

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        authRepository = mockk(relaxed = true)
        logoutUseCase = mockk(relaxed = true)
        getAddressesUseCase = mockk(relaxed = true)
        getFavoritesUseCase = mockk(relaxed = true)

        coEvery { authRepository.getCurrentUser() } returns sampleUser
        coEvery { getAddressesUseCase() } returns flowOf(emptyList())
        coEvery { getFavoritesUseCase.getCount() } returns flowOf(5)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): ProfileViewModel {
        return ProfileViewModel(
            authRepository = authRepository,
            logoutUseCase = logoutUseCase,
            getAddressesUseCase = getAddressesUseCase,
            getFavoritesUseCase = getFavoritesUseCase,
            savedStateHandle = SavedStateHandle()
        )
    }

    @Test
    @DisplayName("Init loads current user profile and favorite count")
    fun init_loadsUser() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("usr_123", state.user.id)
        assertEquals("Nguyễn Văn A", state.user.name)
        assertEquals("vana@example.com", state.user.email)
        assertEquals(5, state.favoriteCount)
    }

    @Test
    @DisplayName("ConfirmLogout calls logoutUseCase")
    fun logout_callsUseCase() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(ProfileUiEvent.ConfirmLogout)
        testDispatcher.scheduler.runCurrent()

        coVerify(exactly = 1) { logoutUseCase() }
    }

    @Test
    @DisplayName("ToggleNotification updates state")
    fun toggleNotification_updatesState() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(ProfileUiEvent.ToggleNotification(false))
        testDispatcher.scheduler.runCurrent()

        assertEquals(false, viewModel.uiState.value.isNotificationEnabled)
    }

    @Test
    @DisplayName("ClickAbout and DismissAboutDialog toggle about dialog state")
    fun aboutDialog_updatesState() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(ProfileUiEvent.ClickAbout)
        testDispatcher.scheduler.runCurrent()
        assertEquals(true, viewModel.uiState.value.showAboutDialog)

        viewModel.onEvent(ProfileUiEvent.DismissAboutDialog)
        testDispatcher.scheduler.runCurrent()
        assertEquals(false, viewModel.uiState.value.showAboutDialog)
    }
}
