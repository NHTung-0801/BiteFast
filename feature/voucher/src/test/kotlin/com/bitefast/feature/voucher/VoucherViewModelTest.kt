package com.bitefast.feature.voucher

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.bitefast.core.domain.voucher.ApplyVoucherUseCase
import com.bitefast.core.domain.voucher.GetVoucherWalletUseCase
import com.bitefast.core.domain.voucher.VoucherCategory
import com.bitefast.core.domain.voucher.VoucherInvalidReason
import com.bitefast.core.domain.voucher.VoucherValidationResult
import com.bitefast.core.domain.voucher.VoucherWalletItem
import com.bitefast.core.domain.voucher.VoucherWalletState
import com.bitefast.core.model.Voucher
import io.mockk.coEvery
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
class VoucherViewModelTest {

    private val getVoucherWalletUseCase: GetVoucherWalletUseCase = mockk()
    private val applyVoucherUseCase: ApplyVoucherUseCase = mockk()
    private val testDispatcher: TestDispatcher = StandardTestDispatcher()

    private val sampleVoucher = Voucher(
        id = "v_1",
        code = "WELCOME50",
        name = "Giảm 50k",
        type = "fixed",
        value = 50000.0,
        minOrderValue = 100000.0,
        maxDiscount = 50000.0,
        isActive = true
    )

    private val sampleWallet = VoucherWalletState(
        items = listOf(
            VoucherWalletItem(
                voucher = sampleVoucher,
                isEligible = true,
                calculatedDiscount = 50000.0
            )
        ),
        eligibleCount = 1,
        totalCount = 1
    )

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery { getVoucherWalletUseCase(any(), any(), any(), any(), any()) } returns sampleWallet
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(savedStateHandle: SavedStateHandle = SavedStateHandle()): VoucherViewModel {
        return VoucherViewModel(
            getVoucherWalletUseCase = getVoucherWalletUseCase,
            applyVoucherUseCase = applyVoucherUseCase,
            savedStateHandle = savedStateHandle
        )
    }

    @Test
    @DisplayName("init: loads vouchers and updates state")
    fun init_loadsVouchers() = runTest {
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.walletState.items.size)
        assertEquals("WELCOME50", viewModel.uiState.value.walletState.items.first().voucher.code)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    @DisplayName("onEvent SelectCategory: reloads with new category")
    fun selectCategory_reloads() = runTest {
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(VoucherUiEvent.SelectCategory(VoucherCategory.SHIPPING))
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(VoucherCategory.SHIPPING, viewModel.uiState.value.selectedCategory)
    }

    @Test
    @DisplayName("onEvent CustomCodeChanged: updates state and clears error")
    fun customCodeChanged_updatesState() = runTest {
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(VoucherUiEvent.CustomCodeChanged("TESTCODE"))
        assertEquals("TESTCODE", viewModel.uiState.value.customVoucherCode)
        assertNull(viewModel.uiState.value.customCodeError)
    }

    @Test
    @DisplayName("onEvent ApplyCustomCode: when valid emits VoucherSelected effect")
    fun applyCustomCode_valid_emitsEffect() = runTest {
        coEvery { applyVoucherUseCase("DISCOUNT20", any(), any(), any(), any()) } returns
            VoucherValidationResult.Valid(sampleVoucher, 50000.0)

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.effect.test {
            viewModel.onEvent(VoucherUiEvent.CustomCodeChanged("DISCOUNT20"))
            viewModel.onEvent(VoucherUiEvent.ApplyCustomCode)
            testDispatcher.scheduler.advanceUntilIdle()

            val effect1 = awaitItem()
            assertTrue(effect1 is VoucherUiEffect.VoucherSelected)
            assertEquals("WELCOME50", (effect1 as VoucherUiEffect.VoucherSelected).voucherCode)

            val effect2 = awaitItem()
            assertTrue(effect2 is VoucherUiEffect.ShowSnackbar)
        }
    }

    @Test
    @DisplayName("onEvent ApplyCustomCode: when invalid sets customCodeError")
    fun applyCustomCode_invalid_setsError() = runTest {
        coEvery { applyVoucherUseCase("WRONG", any(), any(), any(), any()) } returns
            VoucherValidationResult.Invalid(VoucherInvalidReason.NOT_FOUND, "Mã không hợp lệ")

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(VoucherUiEvent.CustomCodeChanged("WRONG"))
        viewModel.onEvent(VoucherUiEvent.ApplyCustomCode)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("Mã không hợp lệ", viewModel.uiState.value.customCodeError)
        assertFalse(viewModel.uiState.value.isValidatingCustomCode)
    }

    @Test
    @DisplayName("onEvent SelectVoucher: emits VoucherSelected effect")
    fun selectVoucher_emitsEffect() = runTest {
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.effect.test {
            viewModel.onEvent(VoucherUiEvent.SelectVoucher(sampleVoucher, 50000.0))

            val effect = awaitItem()
            assertTrue(effect is VoucherUiEffect.VoucherSelected)
            assertEquals("WELCOME50", (effect as VoucherUiEffect.VoucherSelected).voucherCode)
            assertEquals(50000.0, (effect as VoucherUiEffect.VoucherSelected).discount)
        }
    }
}
