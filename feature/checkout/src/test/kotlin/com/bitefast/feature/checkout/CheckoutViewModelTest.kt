package com.bitefast.feature.checkout

import androidx.lifecycle.SavedStateHandle
import com.bitefast.core.domain.cart.GetCartUseCase
import com.bitefast.core.domain.order.CheckoutOrderUseCase
import com.bitefast.core.domain.repository.CartRepository
import com.bitefast.core.domain.voucher.ApplyVoucherUseCase
import com.bitefast.core.domain.voucher.BestVoucherResult
import com.bitefast.core.domain.voucher.GetBestVoucherUseCase
import com.bitefast.core.domain.voucher.GetVoucherWalletUseCase
import com.bitefast.core.domain.voucher.VoucherWalletItem
import com.bitefast.core.domain.voucher.VoucherWalletState
import com.bitefast.core.model.Address
import com.bitefast.core.model.CartItem
import com.bitefast.core.model.PaymentMethod
import com.bitefast.core.model.Voucher
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
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CheckoutViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var getCartUseCase: GetCartUseCase
    private lateinit var checkoutOrderUseCase: CheckoutOrderUseCase
    private lateinit var applyVoucherUseCase: ApplyVoucherUseCase
    private lateinit var getBestVoucherUseCase: GetBestVoucherUseCase
    private lateinit var getVoucherWalletUseCase: GetVoucherWalletUseCase
    private lateinit var cartRepository: CartRepository

    private val sampleAddress = Address(
        id = "addr_1",
        label = "Nhà riêng",
        recipientName = "Nguyễn Văn A",
        phoneNumber = "0909123456",
        streetAddress = "456 Lê Văn Việt",
        city = "TP. Thủ Đức"
    )

    private val sampleCartItem = CartItem(
        id = "ci_1",
        cartId = "cart_default",
        menuItemId = "menu_1",
        restaurantId = "res_1",
        name = "Cơm Sườn",
        price = 50000.0,
        quantity = 2
    )

    private val sampleVoucher = Voucher(
        id = "v_1",
        code = "DISCOUNT20",
        name = "Giảm 20k",
        description = "Giảm 20k cho đơn từ 80k",
        type = "fixed",
        value = 20000.0,
        minOrderValue = 80000.0,
        maxDiscount = 20000.0,
        startDate = 0L,
        endDate = 99999999999L,
        isActive = true
    )

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        getCartUseCase = mockk(relaxed = true)
        checkoutOrderUseCase = mockk(relaxed = true)
        applyVoucherUseCase = mockk(relaxed = true)
        getBestVoucherUseCase = mockk(relaxed = true)
        getVoucherWalletUseCase = mockk(relaxed = true)
        cartRepository = mockk(relaxed = true)

        coEvery { getCartUseCase() } returns flowOf(listOf(sampleCartItem))
        coEvery { getVoucherWalletUseCase(any(), any(), any(), any(), any()) } returns VoucherWalletState(
            items = listOf(VoucherWalletItem(voucher = sampleVoucher, isEligible = true, calculatedDiscount = 20000.0)),
            eligibleCount = 1,
            totalCount = 1
        )
        coEvery { getBestVoucherUseCase(any(), any(), any(), any()) } returns BestVoucherResult()
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    @DisplayName("Init loads cart items and calculates subtotal and total")
    fun init_loadsCart() = runTest(testDispatcher) {
        val viewModel = CheckoutViewModel(
            getCartUseCase = getCartUseCase,
            checkoutOrderUseCase = checkoutOrderUseCase,
            applyVoucherUseCase = applyVoucherUseCase,
            getBestVoucherUseCase = getBestVoucherUseCase,
            getVoucherWalletUseCase = getVoucherWalletUseCase,
            cartRepository = cartRepository,
            savedStateHandle = SavedStateHandle()
        )

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.items.size)
        assertEquals(100000.0, state.subtotal)
        assertEquals(115000.0, state.total) // subtotal 100k + 15k delivery
    }

    @Test
    @DisplayName("PaymentMethodSelected updates selectedPayment in state")
    fun paymentMethodSelected_updatesState() = runTest(testDispatcher) {
        val viewModel = CheckoutViewModel(
            getCartUseCase = getCartUseCase,
            checkoutOrderUseCase = checkoutOrderUseCase,
            applyVoucherUseCase = applyVoucherUseCase,
            getBestVoucherUseCase = getBestVoucherUseCase,
            getVoucherWalletUseCase = getVoucherWalletUseCase,
            cartRepository = cartRepository,
            savedStateHandle = SavedStateHandle()
        )
        advanceUntilIdle()

        viewModel.onEvent(CheckoutUiEvent.PaymentMethodSelected(PaymentMethod.E_WALLET))
        testDispatcher.scheduler.runCurrent()

        assertEquals(PaymentMethod.E_WALLET, viewModel.uiState.value.selectedPayment)
    }

    @Test
    @DisplayName("AddressChanged updates deliveryAddress in state")
    fun addressChanged_updatesState() = runTest(testDispatcher) {
        val viewModel = CheckoutViewModel(
            getCartUseCase = getCartUseCase,
            checkoutOrderUseCase = checkoutOrderUseCase,
            applyVoucherUseCase = applyVoucherUseCase,
            getBestVoucherUseCase = getBestVoucherUseCase,
            getVoucherWalletUseCase = getVoucherWalletUseCase,
            cartRepository = cartRepository,
            savedStateHandle = SavedStateHandle()
        )
        advanceUntilIdle()

        viewModel.onEvent(CheckoutUiEvent.AddressChanged(sampleAddress))
        testDispatcher.scheduler.runCurrent()

        assertEquals("addr_1", viewModel.uiState.value.deliveryAddress.id)
    }

    @Test
    @DisplayName("RemoveVoucher clears discount in state")
    fun removeVoucher_clearsDiscount() = runTest(testDispatcher) {
        val viewModel = CheckoutViewModel(
            getCartUseCase = getCartUseCase,
            checkoutOrderUseCase = checkoutOrderUseCase,
            applyVoucherUseCase = applyVoucherUseCase,
            getBestVoucherUseCase = getBestVoucherUseCase,
            getVoucherWalletUseCase = getVoucherWalletUseCase,
            cartRepository = cartRepository,
            savedStateHandle = SavedStateHandle()
        )
        advanceUntilIdle()

        viewModel.onEvent(CheckoutUiEvent.RemoveVoucher)
        testDispatcher.scheduler.runCurrent()

        assertEquals(0.0, viewModel.uiState.value.discount)
    }
}
