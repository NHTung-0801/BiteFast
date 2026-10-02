package com.bitefast.feature.rating

import androidx.lifecycle.SavedStateHandle
import com.bitefast.core.domain.rating.RatingResult
import com.bitefast.core.domain.rating.SubmitRatingUseCase
import com.bitefast.core.domain.repository.OrderRepository
import com.bitefast.core.model.Address
import com.bitefast.core.model.Order
import com.bitefast.core.model.OrderStatus
import com.bitefast.core.model.PaymentMethod
import com.bitefast.core.model.PaymentStatus
import com.bitefast.core.model.Rating
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
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
class RatingViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var orderRepository: OrderRepository
    private lateinit var submitRatingUseCase: SubmitRatingUseCase

    private val sampleOrder = Order(
        id = "ord_rate_1",
        restaurantId = "res_1",
        restaurantName = "Cơm Tấm Phúc Lộc Thọ",
        items = emptyList(),
        subtotal = 50000.0,
        deliveryFee = 15000.0,
        total = 65000.0,
        status = OrderStatus.DELIVERED,
        paymentMethod = PaymentMethod.CASH,
        paymentStatus = PaymentStatus.PAID,
        address = Address(id = "addr_1", label = "Nhà", recipientName = "A", phoneNumber = "090", streetAddress = "123", city = "SG")
    )

    private val sampleRating = Rating(
        orderId = "ord_rate_1",
        restaurantId = "res_1",
        restaurantRating = 5,
        driverRating = 5,
        comment = "Tuyệt vời"
    )

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        orderRepository = mockk(relaxed = true)
        submitRatingUseCase = mockk(relaxed = true)

        coEvery { orderRepository.getOrderDetail("ord_rate_1") } returns sampleOrder
        coEvery {
            submitRatingUseCase.invoke(
                orderId = any(),
                restaurantRating = any(),
                driverRating = any(),
                tags = any(),
                comment = any(),
                isAnonymous = any(),
                userName = any()
            )
        } returns RatingResult.Success(sampleRating)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    @DisplayName("Init loads order details")
    fun init_loadsOrder() = runTest(testDispatcher) {
        val viewModel = RatingViewModel(
            orderRepository = orderRepository,
            submitRatingUseCase = submitRatingUseCase,
            savedStateHandle = SavedStateHandle(mapOf("orderId" to "ord_rate_1"))
        )

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.order)
        assertEquals("ord_rate_1", state.order?.id)
    }

    @Test
    @DisplayName("SetRestaurantRating updates restaurant rating in state")
    fun setRestaurantRating_updatesRating() = runTest(testDispatcher) {
        val viewModel = RatingViewModel(
            orderRepository = orderRepository,
            submitRatingUseCase = submitRatingUseCase,
            savedStateHandle = SavedStateHandle(mapOf("orderId" to "ord_rate_1"))
        )
        advanceUntilIdle()

        viewModel.onEvent(RatingUiEvent.SetRestaurantRating(5))
        testDispatcher.scheduler.runCurrent()

        assertEquals(5, viewModel.uiState.value.restaurantRating)
    }

    @Test
    @DisplayName("SetDriverRating updates driver rating in state")
    fun setDriverRating_updatesRating() = runTest(testDispatcher) {
        val viewModel = RatingViewModel(
            orderRepository = orderRepository,
            submitRatingUseCase = submitRatingUseCase,
            savedStateHandle = SavedStateHandle(mapOf("orderId" to "ord_rate_1"))
        )
        advanceUntilIdle()

        viewModel.onEvent(RatingUiEvent.SetDriverRating(4))
        testDispatcher.scheduler.runCurrent()

        assertEquals(4, viewModel.uiState.value.driverRating)
    }

    @Test
    @DisplayName("ToggleTag adds and removes tag")
    fun toggleTag_addsAndRemovesTag() = runTest(testDispatcher) {
        val viewModel = RatingViewModel(
            orderRepository = orderRepository,
            submitRatingUseCase = submitRatingUseCase,
            savedStateHandle = SavedStateHandle(mapOf("orderId" to "ord_rate_1"))
        )
        advanceUntilIdle()

        viewModel.onEvent(RatingUiEvent.ToggleTag("Mon an nong hoi"))
        testDispatcher.scheduler.runCurrent()
        assertTrue(viewModel.uiState.value.selectedTags.contains("Mon an nong hoi"))

        viewModel.onEvent(RatingUiEvent.ToggleTag("Mon an nong hoi"))
        testDispatcher.scheduler.runCurrent()
        assertFalse(viewModel.uiState.value.selectedTags.contains("Mon an nong hoi"))
    }

    @Test
    @DisplayName("SubmitRating invokes submitRatingUseCase")
    fun submitRating_invokesUseCase() = runTest(testDispatcher) {
        val viewModel = RatingViewModel(
            orderRepository = orderRepository,
            submitRatingUseCase = submitRatingUseCase,
            savedStateHandle = SavedStateHandle(mapOf("orderId" to "ord_rate_1"))
        )
        advanceUntilIdle()

        viewModel.onEvent(RatingUiEvent.SetRestaurantRating(5))
        viewModel.onEvent(RatingUiEvent.SetDriverRating(5))
        viewModel.onEvent(RatingUiEvent.UpdateComment("Rất tuyệt vời!"))
        viewModel.onEvent(RatingUiEvent.SubmitRating)
        testDispatcher.scheduler.runCurrent()

        coVerify(exactly = 1) {
            submitRatingUseCase.invoke(
                orderId = any(),
                restaurantRating = any(),
                driverRating = any(),
                tags = any(),
                comment = any(),
                isAnonymous = any(),
                userName = any()
            )
        }
    }
}
