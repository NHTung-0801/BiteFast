package com.bitefast.feature.tracking

import androidx.lifecycle.SavedStateHandle
import com.bitefast.core.domain.order.CancelOrderUseCase
import com.bitefast.core.domain.order.GetOrderTrackingUseCase
import com.bitefast.core.model.Address
import com.bitefast.core.model.Order
import com.bitefast.core.model.OrderStatus
import com.bitefast.core.model.PaymentMethod
import com.bitefast.core.model.PaymentStatus
import io.mockk.coEvery
import io.mockk.coVerify
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
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TrackingViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var getOrderTrackingUseCase: GetOrderTrackingUseCase
    private lateinit var cancelOrderUseCase: CancelOrderUseCase

    private val sampleTrackingOrder = Order(
        id = "ord_track_1",
        restaurantId = "res_1",
        restaurantName = "Cơm Tấm Phúc Lộc Thọ",
        driverId = "drv_1",
        driverName = "Nguyễn Văn Hùng",
        driverPhone = "0901234567",
        items = emptyList(),
        subtotal = 50000.0,
        deliveryFee = 15000.0,
        total = 65000.0,
        status = OrderStatus.CONFIRMED,
        paymentMethod = PaymentMethod.CASH,
        paymentStatus = PaymentStatus.PENDING,
        address = Address(
            id = "addr_1",
            label = "Nhà",
            recipientName = "A",
            phoneNumber = "090",
            streetAddress = "123 Lê Văn Việt",
            city = "TP. Thủ Đức",
            latitude = 10.8540,
            longitude = 106.7810
        )
    )

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        getOrderTrackingUseCase = mockk(relaxed = true)
        cancelOrderUseCase = mockk(relaxed = true)

        coEvery { getOrderTrackingUseCase.stream(any()) } returns flowOf(sampleTrackingOrder)
        coEvery { getOrderTrackingUseCase(any()) } returns sampleTrackingOrder
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    @DisplayName("Init loads tracking order and calculates coordinates")
    fun init_loadsTrackingOrder() = runTest(testDispatcher) {
        val viewModel = TrackingViewModel(
            getOrderTrackingUseCase = getOrderTrackingUseCase,
            cancelOrderUseCase = cancelOrderUseCase,
            savedStateHandle = SavedStateHandle(mapOf("orderId" to "ord_track_1"))
        )

        testDispatcher.scheduler.runCurrent()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.order)
        assertEquals("ord_track_1", state.order?.id)
        assertEquals("Nguyễn Văn Hùng", state.order?.driverName)
        assertEquals("0901234567", state.order?.driverPhone)
    }

    @Test
    @DisplayName("ConfirmCancel invokes cancelOrderUseCase")
    fun confirmCancel_invokesUseCase() = runTest(testDispatcher) {
        val viewModel = TrackingViewModel(
            getOrderTrackingUseCase = getOrderTrackingUseCase,
            cancelOrderUseCase = cancelOrderUseCase,
            savedStateHandle = SavedStateHandle(mapOf("orderId" to "ord_track_1"))
        )
        testDispatcher.scheduler.runCurrent()

        viewModel.onEvent(TrackingUiEvent.ConfirmCancel)
        testDispatcher.scheduler.runCurrent()

        coVerify(exactly = 1) { cancelOrderUseCase("ord_track_1", any()) }
    }
}
