package com.bitefast.feature.cart

import androidx.lifecycle.SavedStateHandle
import com.bitefast.core.domain.cart.GetCartUseCase
import com.bitefast.core.domain.cart.UpdateCartQuantityUseCase
import com.bitefast.core.domain.repository.CartRepository
import com.bitefast.core.domain.voucher.BestVoucherResult
import com.bitefast.core.domain.voucher.GetBestVoucherUseCase
import com.bitefast.core.model.CartItem
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
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CartViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var getCartUseCase: GetCartUseCase
    private lateinit var updateCartQuantityUseCase: UpdateCartQuantityUseCase
    private lateinit var getBestVoucherUseCase: GetBestVoucherUseCase
    private lateinit var cartRepository: CartRepository

    private val sampleCartItem = CartItem(
        id = "ci_1",
        cartId = "cart_default",
        menuItemId = "menu_1",
        restaurantId = "res_1",
        name = "Cơm Tấm Sườn Bì",
        price = 65000.0,
        quantity = 2
    )

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        getCartUseCase = mockk(relaxed = true)
        updateCartQuantityUseCase = mockk(relaxed = true)
        getBestVoucherUseCase = mockk(relaxed = true)
        cartRepository = mockk(relaxed = true)

        coEvery { getCartUseCase() } returns flowOf(listOf(sampleCartItem))
        coEvery { getBestVoucherUseCase(any(), any(), any(), any()) } returns BestVoucherResult()
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    @DisplayName("Init collects cart items and updates uiState")
    fun init_observesCartItems() = runTest(testDispatcher) {
        val viewModel = CartViewModel(
            getCartUseCase = getCartUseCase,
            updateCartQuantityUseCase = updateCartQuantityUseCase,
            getBestVoucherUseCase = getBestVoucherUseCase,
            cartRepository = cartRepository,
            savedStateHandle = SavedStateHandle()
        )

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.items.size)
        assertEquals("ci_1", state.items[0].id)
        assertEquals(130000.0, state.subtotal)
    }

    @Test
    @DisplayName("IncreaseQuantity invokes updateCartQuantityUseCase with +1")
    fun increaseQuantity_invokesUseCase() = runTest(testDispatcher) {
        val viewModel = CartViewModel(
            getCartUseCase = getCartUseCase,
            updateCartQuantityUseCase = updateCartQuantityUseCase,
            getBestVoucherUseCase = getBestVoucherUseCase,
            cartRepository = cartRepository,
            savedStateHandle = SavedStateHandle()
        )
        advanceUntilIdle()

        viewModel.onEvent(CartUiEvent.IncreaseQuantity("menu_1", 2))
        testDispatcher.scheduler.runCurrent()

        coVerify(exactly = 1) { updateCartQuantityUseCase("menu_1", 3) }
    }

    @Test
    @DisplayName("DecreaseQuantity invokes updateCartQuantityUseCase with -1")
    fun decreaseQuantity_invokesUseCase() = runTest(testDispatcher) {
        val viewModel = CartViewModel(
            getCartUseCase = getCartUseCase,
            updateCartQuantityUseCase = updateCartQuantityUseCase,
            getBestVoucherUseCase = getBestVoucherUseCase,
            cartRepository = cartRepository,
            savedStateHandle = SavedStateHandle()
        )
        advanceUntilIdle()

        viewModel.onEvent(CartUiEvent.DecreaseQuantity("menu_1", 2))
        testDispatcher.scheduler.runCurrent()

        coVerify(exactly = 1) { updateCartQuantityUseCase("menu_1", 1) }
    }

    @Test
    @DisplayName("ClearCart invokes clearCart on repository")
    fun clearCart_invokesRepository() = runTest(testDispatcher) {
        val viewModel = CartViewModel(
            getCartUseCase = getCartUseCase,
            updateCartQuantityUseCase = updateCartQuantityUseCase,
            getBestVoucherUseCase = getBestVoucherUseCase,
            cartRepository = cartRepository,
            savedStateHandle = SavedStateHandle()
        )
        advanceUntilIdle()

        viewModel.onEvent(CartUiEvent.ClearCart)
        testDispatcher.scheduler.runCurrent()

        coVerify(exactly = 1) { cartRepository.clearCart() }
    }
}
