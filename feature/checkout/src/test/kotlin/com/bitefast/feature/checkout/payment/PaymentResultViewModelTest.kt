package com.bitefast.feature.checkout.payment

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
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PaymentResultViewModelTest {

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
    @DisplayName("InitPayment should set orderId, amount, QR payload and start timer")
    fun initPayment_setsStateAndStartsTimer() = runTest(testDispatcher) {
        val viewModel = PaymentResultViewModel(SavedStateHandle())

        viewModel.onEvent(PaymentResultUiEvent.InitPayment(orderId = "1001", amount = 150000L, qrPayload = "https://vietqr.net/sample.png"))
        testDispatcher.scheduler.runCurrent()

        val state = viewModel.uiState.value
        assertEquals("1001", state.orderId)
        assertEquals(150000L, state.amount)
        assertEquals(PaymentStatusState.WAITING, state.status)
        assertEquals("BF1001", state.transferContent)
        assertEquals("10:00", state.formattedTimer)
    }

    @Test
    @DisplayName("Timer should tick down every second")
    fun timer_ticksDown() = runTest(testDispatcher) {
        val viewModel = PaymentResultViewModel(SavedStateHandle())
        viewModel.onEvent(PaymentResultUiEvent.InitPayment(orderId = "1001", amount = 150000L, qrPayload = "url"))
        testDispatcher.scheduler.runCurrent()

        testDispatcher.scheduler.advanceTimeBy(3050)

        val state = viewModel.uiState.value
        assertTrue(state.remainingSeconds <= 597)
    }


    @Test
    @DisplayName("ConfirmPaidManually should mark status as SUCCESS")
    fun confirmPaidManually_updatesStatusToSuccess() = runTest(testDispatcher) {
        val viewModel = PaymentResultViewModel(SavedStateHandle())
        viewModel.onEvent(PaymentResultUiEvent.InitPayment(orderId = "BF_1001", amount = 150000L, qrPayload = "url"))

        viewModel.onEvent(PaymentResultUiEvent.ConfirmPaidManually)

        val state = viewModel.uiState.value
        assertEquals(PaymentStatusState.SUCCESS, state.status)
    }

    @Test
    @DisplayName("RetryPayment should reset remaining time to 600s and state to WAITING")
    fun retryPayment_resetsState() = runTest(testDispatcher) {
        val viewModel = PaymentResultViewModel(SavedStateHandle())
        viewModel.onEvent(PaymentResultUiEvent.InitPayment(orderId = "BF_1001", amount = 150000L, qrPayload = "url"))

        advanceTimeBy(5000)
        viewModel.onEvent(PaymentResultUiEvent.RetryPayment)

        val state = viewModel.uiState.value
        assertEquals(600, state.remainingSeconds)
        assertEquals(PaymentStatusState.WAITING, state.status)
    }
}
