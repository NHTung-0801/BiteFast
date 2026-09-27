package com.bitefast.feature.auth

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.bitefast.core.domain.auth.EnableGuestModeUseCase
import com.bitefast.core.domain.auth.LoginUseCase
import com.bitefast.core.domain.auth.RegisterUseCase
import com.bitefast.core.model.User
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
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
class AuthViewModelTest {

    private val loginUseCase: LoginUseCase = mockk()
    private val registerUseCase: RegisterUseCase = mockk()
    private val enableGuestModeUseCase: EnableGuestModeUseCase = mockk(relaxed = true)
    private val savedStateHandle: SavedStateHandle = SavedStateHandle()

    private val testDispatcher: TestDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: AuthViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = AuthViewModel(
            loginUseCase = loginUseCase,
            registerUseCase = registerUseCase,
            enableGuestModeUseCase = enableGuestModeUseCase,
            savedStateHandle = savedStateHandle
        )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    @DisplayName("Initial state has empty fields and not loading")
    fun initialState_isDefault() {
        val state = viewModel.uiState.value
        assertEquals("", state.email)
        assertEquals("", state.password)
        assertEquals("", state.name)
        assertFalse(state.isLoading)
        assertFalse(state.isGuest)
        assertFalse(state.isPasswordVisible)
        assertNull(state.emailError)
        assertNull(state.passwordError)
        assertNull(state.nameError)
    }

    @Test
    @DisplayName("Input events update state values and clear errors")
    fun onFieldChanged_updatesState() {
        viewModel.onEvent(AuthUiEvent.OnNameChanged("Nguyen Van A"))
        assertEquals("Nguyen Van A", viewModel.uiState.value.name)

        viewModel.onEvent(AuthUiEvent.OnEmailChanged("test@bitefast.vn"))
        assertEquals("test@bitefast.vn", viewModel.uiState.value.email)

        viewModel.onEvent(AuthUiEvent.OnPasswordChanged("SecurePass123"))
        assertEquals("SecurePass123", viewModel.uiState.value.password)

        viewModel.onEvent(AuthUiEvent.OnTogglePasswordVisibility)
        assertTrue(viewModel.uiState.value.isPasswordVisible)

        viewModel.onEvent(AuthUiEvent.OnTogglePasswordVisibility)
        assertFalse(viewModel.uiState.value.isPasswordVisible)
    }

    @Test
    @DisplayName("Login with invalid email sets emailError")
    fun login_invalidEmail_setsEmailError() = runTest(testDispatcher) {
        viewModel.onEvent(AuthUiEvent.OnEmailChanged("not-an-email"))
        viewModel.onEvent(AuthUiEvent.OnPasswordChanged("password123"))
        viewModel.onEvent(AuthUiEvent.OnLoginClicked)

        val state = viewModel.uiState.value
        assertNotNull(state.emailError)
        assertFalse(state.isLoading)
    }

    @Test
    @DisplayName("Login with short password sets passwordError")
    fun login_shortPassword_setsPasswordError() = runTest(testDispatcher) {
        viewModel.onEvent(AuthUiEvent.OnEmailChanged("valid@bitefast.vn"))
        viewModel.onEvent(AuthUiEvent.OnPasswordChanged("short"))
        viewModel.onEvent(AuthUiEvent.OnLoginClicked)

        val state = viewModel.uiState.value
        assertNotNull(state.passwordError)
        assertFalse(state.isLoading)
    }

    @Test
    @DisplayName("Login success emits NavigateToHome effect")
    fun login_success_emitsNavigateToHome() = runTest(testDispatcher) {
        val user = User(id = "user_1", email = "test@bitefast.vn", name = "Test User")
        coEvery { loginUseCase("test@bitefast.vn", "Password123") } returns user

        viewModel.effect.test {
            viewModel.onEvent(AuthUiEvent.OnEmailChanged("test@bitefast.vn"))
            viewModel.onEvent(AuthUiEvent.OnPasswordChanged("Password123"))
            viewModel.onEvent(AuthUiEvent.OnLoginClicked)

            testScheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertTrue(effect is AuthUiEffect.NavigateToHome)
            assertFalse(viewModel.uiState.value.isLoading)
        }
    }

    @Test
    @DisplayName("Login failure emits ShowSnackbar effect")
    fun login_failure_emitsShowSnackbar() = runTest(testDispatcher) {
        coEvery { loginUseCase("test@bitefast.vn", "Password123") } throws RuntimeException("Sai thông tin đăng nhập")

        viewModel.effect.test {
            viewModel.onEvent(AuthUiEvent.OnEmailChanged("test@bitefast.vn"))
            viewModel.onEvent(AuthUiEvent.OnPasswordChanged("Password123"))
            viewModel.onEvent(AuthUiEvent.OnLoginClicked)

            testScheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertTrue(effect is AuthUiEffect.ShowSnackbar)
            assertEquals("Sai thông tin đăng nhập", (effect as AuthUiEffect.ShowSnackbar).message)
            assertFalse(viewModel.uiState.value.isLoading)
        }
    }

    @Test
    @DisplayName("Register validation checks empty name, email and password strength")
    fun register_validationChecks() = runTest(testDispatcher) {
        viewModel.onEvent(AuthUiEvent.OnNameChanged(""))
        viewModel.onEvent(AuthUiEvent.OnEmailChanged("invalid-email"))
        viewModel.onEvent(AuthUiEvent.OnPasswordChanged("weak"))
        viewModel.onEvent(AuthUiEvent.OnRegisterClicked)

        val state = viewModel.uiState.value
        assertNotNull(state.nameError)
        assertNotNull(state.emailError)
        assertNotNull(state.passwordError)
        assertFalse(state.isLoading)
    }

    @Test
    @DisplayName("Register success triggers usecase and emits NavigateToHome")
    fun register_success_emitsNavigateToHome() = runTest(testDispatcher) {
        val user = User(id = "user_2", email = "reg@bitefast.vn", name = "Nguyen Van B")
        coEvery { registerUseCase("Nguyen Van B", "reg@bitefast.vn", "Secure123") } returns user

        viewModel.effect.test {
            viewModel.onEvent(AuthUiEvent.OnNameChanged("Nguyen Van B"))
            viewModel.onEvent(AuthUiEvent.OnEmailChanged("reg@bitefast.vn"))
            viewModel.onEvent(AuthUiEvent.OnPasswordChanged("Secure123"))
            viewModel.onEvent(AuthUiEvent.OnRegisterClicked)

            testScheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertTrue(effect is AuthUiEffect.NavigateToHome)
            assertFalse(viewModel.uiState.value.isLoading)
        }
    }

    @Test
    @DisplayName("Guest mode enables guest in usecase and navigates to Home")
    fun guestMode_success_emitsNavigateToHome() = runTest(testDispatcher) {
        viewModel.effect.test {
            viewModel.onEvent(AuthUiEvent.OnGuestModeClicked)

            testScheduler.advanceUntilIdle()

            coVerify(exactly = 1) { enableGuestModeUseCase() }
            assertTrue(viewModel.uiState.value.isGuest)

            val effect = awaitItem()
            assertTrue(effect is AuthUiEffect.NavigateToHome)
        }
    }
}
