package com.bitefast.feature.auth.forgot

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ForgotPasswordViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    @DisplayName("Initial state should be INPUT_IDENTIFIER")
    fun initialState_isInputIdentifier() = runTest(testDispatcher) {
        val viewModel = ForgotPasswordViewModel(SavedStateHandle())
        val state = viewModel.uiState.value

        assertEquals(ForgotStep.INPUT_IDENTIFIER, state.step)
        assertEquals("", state.identifier)
        assertFalse(state.isLoading)
    }

    @Test
    @DisplayName("RequestOtp with valid email advances to ENTER_OTP step")
    fun requestOtp_validEmail_advancesStep() = runTest(testDispatcher) {
        val viewModel = ForgotPasswordViewModel(SavedStateHandle())

        viewModel.onEvent(ForgotPasswordUiEvent.IdentifierChanged("user@example.com"))
        viewModel.onEvent(ForgotPasswordUiEvent.RequestOtp)

        advanceTimeBy(1000)

        val state = viewModel.uiState.value
        assertEquals(ForgotStep.ENTER_OTP, state.step)
        assertEquals(60, state.remainingSeconds)
        assertFalse(state.canResend)
    }

    @Test
    @DisplayName("VerifyOtp with valid 6-digit code advances to RESET_PASSWORD step")
    fun verifyOtp_validCode_advancesStep() = runTest(testDispatcher) {
        val viewModel = ForgotPasswordViewModel(SavedStateHandle())

        viewModel.onEvent(ForgotPasswordUiEvent.IdentifierChanged("0901234567"))
        viewModel.onEvent(ForgotPasswordUiEvent.RequestOtp)
        advanceTimeBy(1000)

        viewModel.onEvent(ForgotPasswordUiEvent.OtpChanged("123456"))
        viewModel.onEvent(ForgotPasswordUiEvent.VerifyOtp)
        advanceTimeBy(1000)

        val state = viewModel.uiState.value
        assertEquals(ForgotStep.RESET_PASSWORD, state.step)
    }

    @Test
    @DisplayName("ResetPassword with matching passwords advances to SUCCESS step")
    fun resetPassword_matchingPasswords_advancesToSuccess() = runTest(testDispatcher) {
        val viewModel = ForgotPasswordViewModel(SavedStateHandle())

        viewModel.onEvent(ForgotPasswordUiEvent.IdentifierChanged("0901234567"))
        viewModel.onEvent(ForgotPasswordUiEvent.RequestOtp)
        advanceTimeBy(1000)

        viewModel.onEvent(ForgotPasswordUiEvent.OtpChanged("123456"))
        viewModel.onEvent(ForgotPasswordUiEvent.VerifyOtp)
        advanceTimeBy(1000)

        viewModel.onEvent(ForgotPasswordUiEvent.NewPasswordChanged("Password123"))
        viewModel.onEvent(ForgotPasswordUiEvent.ConfirmPasswordChanged("Password123"))
        viewModel.onEvent(ForgotPasswordUiEvent.ResetPassword)
        advanceTimeBy(1000)

        val state = viewModel.uiState.value
        assertEquals(ForgotStep.SUCCESS, state.step)
    }
}
