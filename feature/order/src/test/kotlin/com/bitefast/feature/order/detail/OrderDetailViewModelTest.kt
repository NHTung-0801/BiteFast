package com.bitefast.feature.order.detail

import androidx.lifecycle.SavedStateHandle
import com.bitefast.core.domain.order.GetOrderTrackingUseCase
import com.bitefast.core.domain.order.SmartReOrderResult
import com.bitefast.core.domain.order.SmartReOrderUseCase
import com.bitefast.core.model.Order
import com.bitefast.core.model.OrderStatus
import com.bitefast.core.model.PaymentMethod
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OrderDetailViewModelTest {

    private val getOrderTrackingUseCase: GetOrderTrackingUseCase = mockk()
    private val smartReOrderUseCase: SmartReOrderUseCase = mockk()
    private val testDispatcher = StandardTestDispatcher()

    private val sampleOrder = Order(
        id = "ORD_8899",
        restaurantId = "res_101",
        restaurantName = "Bún Chả Hà Nội Phố",
        items = emptyList(),
        total = 85000.0,
        status = OrderStatus.DELIVERED,
        paymentMethod = PaymentMethod.CASH,
        orderTime = 1711800000000L
    )

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { getOrderTrackingUseCase.stream("ORD_8899") } returns flowOf(sampleOrder)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    @DisplayName("LoadOrder should fetch order details and update state")
    fun loadOrder_updatesStateWithOrder() = runTest(testDispatcher) {
        val savedStateHandle = SavedStateHandle(mapOf("orderId" to "ORD_8899"))
        val viewModel = OrderDetailViewModel(getOrderTrackingUseCase, smartReOrderUseCase, savedStateHandle)

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.order)
        assertEquals("ORD_8899", state.order?.id)
        assertEquals("Bún Chả Hà Nội Phố", state.order?.restaurantName)
    }

    @Test
    @DisplayName("OpenSupport and DismissSupport should toggle showSupportDialog")
    fun supportDialog_togglesState() = runTest(testDispatcher) {
        val savedStateHandle = SavedStateHandle(mapOf("orderId" to "ORD_8899"))
        val viewModel = OrderDetailViewModel(getOrderTrackingUseCase, smartReOrderUseCase, savedStateHandle)

        viewModel.onEvent(OrderDetailUiEvent.OpenSupport)
        assertTrue(viewModel.uiState.value.showSupportDialog)

        viewModel.onEvent(OrderDetailUiEvent.DismissSupport)
        assertFalse(viewModel.uiState.value.showSupportDialog)
    }

    @Test
    @DisplayName("ReOrder should call smartReOrderUseCase")
    fun reorder_invokesUseCase() = runTest(testDispatcher) {
        coEvery { smartReOrderUseCase(sampleOrder) } returns SmartReOrderResult.Success(reorderedItemsCount = 2, restaurantName = "Bún Chả Hà Nội Phố")

        val savedStateHandle = SavedStateHandle(mapOf("orderId" to "ORD_8899"))
        val viewModel = OrderDetailViewModel(getOrderTrackingUseCase, smartReOrderUseCase, savedStateHandle)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(OrderDetailUiEvent.ReOrder)
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isReordering)
    }
}
