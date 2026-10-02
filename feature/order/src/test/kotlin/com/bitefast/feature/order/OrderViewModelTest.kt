package com.bitefast.feature.order

import androidx.lifecycle.SavedStateHandle
import com.bitefast.core.domain.order.CancelOrderUseCase
import com.bitefast.core.domain.order.CategorizedOrders
import com.bitefast.core.domain.order.GetOrderHistoryUseCase
import com.bitefast.core.model.Address
import com.bitefast.core.model.Order
import com.bitefast.core.model.OrderStatus
import com.bitefast.core.model.PaymentMethod
import com.bitefast.core.model.PaymentStatus
import io.mockk.coEvery
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
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OrderViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var getOrderHistoryUseCase: GetOrderHistoryUseCase
    private lateinit var cancelOrderUseCase: CancelOrderUseCase

    private val sampleActiveOrder = Order(
        id = "ord_active_1",
        restaurantId = "res_1",
        restaurantName = "Cơm Tấm Phúc Lộc Thọ",
        items = emptyList(),
        subtotal = 50000.0,
        deliveryFee = 15000.0,
        total = 65000.0,
        status = OrderStatus.ON_THE_WAY,
        paymentMethod = PaymentMethod.CASH,
        paymentStatus = PaymentStatus.PENDING,
        address = Address(id = "addr_1", label = "Nhà", recipientName = "A", phoneNumber = "090", streetAddress = "123", city = "SG")
    )

    private val sampleCompletedOrder = Order(
        id = "ord_done_1",
        restaurantId = "res_2",
        restaurantName = "Phở Thìn",
        items = emptyList(),
        subtotal = 75000.0,
        deliveryFee = 15000.0,
        total = 90000.0,
        status = OrderStatus.DELIVERED,
        paymentMethod = PaymentMethod.CASH,
        paymentStatus = PaymentStatus.PAID,
        address = Address(id = "addr_1", label = "Nhà", recipientName = "A", phoneNumber = "090", streetAddress = "123", city = "SG")
    )

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        getOrderHistoryUseCase = mockk(relaxed = true)
        cancelOrderUseCase = mockk(relaxed = true)

        coEvery { getOrderHistoryUseCase.categorized() } returns flowOf(
            CategorizedOrders(
                active = listOf(sampleActiveOrder),
                completed = listOf(sampleCompletedOrder)
            )
        )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    @DisplayName("Init loads active and completed orders")
    fun init_loadsOrders() = runTest(testDispatcher) {
        val viewModel = OrderViewModel(
            getOrderHistoryUseCase = getOrderHistoryUseCase,
            cancelOrderUseCase = cancelOrderUseCase,
            savedStateHandle = SavedStateHandle()
        )

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(1, state.activeOrders.size)
        assertEquals(1, state.completedOrders.size)
        assertEquals("ord_active_1", state.activeOrders[0].id)
    }

    @Test
    @DisplayName("SelectTab updates selectedTab in uiState")
    fun selectTab_updatesState() = runTest(testDispatcher) {
        val viewModel = OrderViewModel(
            getOrderHistoryUseCase = getOrderHistoryUseCase,
            cancelOrderUseCase = cancelOrderUseCase,
            savedStateHandle = SavedStateHandle()
        )
        advanceUntilIdle()

        viewModel.onEvent(OrderUiEvent.SelectTab(OrderFilterTab.COMPLETED))
        testDispatcher.scheduler.runCurrent()

        assertEquals(OrderFilterTab.COMPLETED, viewModel.uiState.value.selectedTab)
    }

    @Test
    @DisplayName("RequestCancelOrder sets orderToCancel and opens dialog")
    fun requestCancelOrder_setsState() = runTest(testDispatcher) {
        val viewModel = OrderViewModel(
            getOrderHistoryUseCase = getOrderHistoryUseCase,
            cancelOrderUseCase = cancelOrderUseCase,
            savedStateHandle = SavedStateHandle()
        )
        advanceUntilIdle()

        viewModel.onEvent(OrderUiEvent.RequestCancelOrder(sampleActiveOrder))
        testDispatcher.scheduler.runCurrent()

        assertEquals("ord_active_1", viewModel.uiState.value.orderToCancel?.id)
    }

    @Test
    @DisplayName("DismissCancelDialog resets cancel state")
    fun dismissCancelDialog_resetsState() = runTest(testDispatcher) {
        val viewModel = OrderViewModel(
            getOrderHistoryUseCase = getOrderHistoryUseCase,
            cancelOrderUseCase = cancelOrderUseCase,
            savedStateHandle = SavedStateHandle()
        )
        advanceUntilIdle()

        viewModel.onEvent(OrderUiEvent.RequestCancelOrder(sampleActiveOrder))
        testDispatcher.scheduler.runCurrent()

        viewModel.onEvent(OrderUiEvent.DismissCancelDialog)
        testDispatcher.scheduler.runCurrent()

        assertNull(viewModel.uiState.value.orderToCancel)
    }
}
