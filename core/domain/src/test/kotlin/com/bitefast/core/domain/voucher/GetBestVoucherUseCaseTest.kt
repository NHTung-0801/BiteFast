package com.bitefast.core.domain.voucher

import com.bitefast.core.domain.repository.VoucherRepository
import com.bitefast.core.model.Voucher
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class GetBestVoucherUseCaseTest {

    private val voucherRepository: VoucherRepository = mockk()
    private val validateVoucherUseCase = ValidateVoucherUseCase()
    private lateinit var getBestVoucherUseCase: GetBestVoucherUseCase
    private lateinit var getVoucherWalletUseCase: GetVoucherWalletUseCase

    private val baseTime = 1700000000000L

    @BeforeEach
    fun setUp() {
        getBestVoucherUseCase = GetBestVoucherUseCase(voucherRepository, validateVoucherUseCase)
        getVoucherWalletUseCase = GetVoucherWalletUseCase(voucherRepository, validateVoucherUseCase)
    }

    @Test
    @DisplayName("GetBestVoucher: selects voucher with highest discount among applicable")
    fun getBestVoucher_selectsHighestDiscount() = runTest {
        val voucher10k = createVoucher("V10K", type = "fixed", value = 10000.0, minOrderValue = 50000.0)
        val voucher50k = createVoucher("V50K", type = "fixed", value = 50000.0, minOrderValue = 100000.0)
        val voucher20Percent = createVoucher("V20P", type = "percentage", value = 20.0, maxDiscount = 30000.0, minOrderValue = 60000.0)

        coEvery { voucherRepository.getVouchers(any()) } returns listOf(voucher10k, voucher50k, voucher20Percent)

        // Subtotal = 120,000 -> V10k = 10k, V50k = 50k, V20P = 24k -> Best is V50K (50,000đ)
        val result = getBestVoucherUseCase(
            subtotal = 120000.0,
            restaurantId = "res_1",
            deliveryFee = 15000.0,
            currentTimeMillis = baseTime
        )

        assertTrue(result.hasApplicableVouchers)
        assertNotNull(result.bestVoucher)
        assertEquals("V50K", result.bestVoucher?.code)
        assertEquals(50000.0, result.bestDiscount)
    }

    @Test
    @DisplayName("GetBestVoucher: when order value is low, suggests upsell voucher")
    fun getBestVoucher_suggestsUpsellWhenCloseToMinOrder() = runTest {
        val voucher10k = createVoucher("V10K", type = "fixed", value = 10000.0, minOrderValue = 50000.0)
        val voucher50k = createVoucher("V50K", type = "fixed", value = 50000.0, minOrderValue = 100000.0)

        coEvery { voucherRepository.getVouchers(any()) } returns listOf(voucher10k, voucher50k)

        // Subtotal = 85,000 -> V10k is applicable (10k). V50K needs 15,000 more for 50,000 discount.
        val result = getBestVoucherUseCase(
            subtotal = 85000.0,
            restaurantId = "res_1",
            deliveryFee = 15000.0,
            currentTimeMillis = baseTime
        )

        assertEquals("V10K", result.bestVoucher?.code)
        assertEquals(10000.0, result.bestDiscount)

        // Upsell check
        assertNotNull(result.upsellVoucher)
        assertEquals("V50K", result.upsellVoucher?.code)
        assertEquals(15000.0, result.missingAmountForUpsell)
        assertEquals(50000.0, result.potentialUpsellDiscount)
    }

    @Test
    @DisplayName("GetBestVoucher: returns empty result when no vouchers applicable")
    fun getBestVoucher_noVouchersApplicable() = runTest {
        val voucher50k = createVoucher("V50K", type = "fixed", value = 50000.0, minOrderValue = 100000.0)
        coEvery { voucherRepository.getVouchers(any()) } returns listOf(voucher50k)

        val result = getBestVoucherUseCase(
            subtotal = 30000.0,
            restaurantId = "res_1",
            deliveryFee = 15000.0,
            currentTimeMillis = baseTime
        )

        assertFalse(result.hasApplicableVouchers)
        assertNull(result.bestVoucher)
        assertEquals(0.0, result.bestDiscount)
    }

    @Test
    @DisplayName("GetVoucherWallet: categorizes items into eligible and ineligible correctly")
    fun getVoucherWallet_categorizesCorrectly() = runTest {
        val eligibleVoucher = createVoucher("ELIGIBLE", type = "fixed", value = 20000.0, minOrderValue = 50000.0)
        val expiredVoucher = createVoucher("EXPIRED", endDate = baseTime - 1000L)
        val nearVoucher = createVoucher("NEAR", type = "fixed", value = 40000.0, minOrderValue = 120000.0)

        coEvery { voucherRepository.getVouchers(any()) } returns listOf(eligibleVoucher, expiredVoucher, nearVoucher)

        val wallet = getVoucherWalletUseCase(
            subtotal = 80000.0,
            restaurantId = "res_1",
            deliveryFee = 15000.0,
            category = VoucherCategory.ALL,
            currentTimeMillis = baseTime
        )

        assertEquals(3, wallet.totalCount)
        assertEquals(1, wallet.eligibleCount)

        val firstItem = wallet.items.first()
        assertTrue(firstItem.isEligible)
        assertEquals("ELIGIBLE", firstItem.voucher.code)

        val nearItem = wallet.items.first { it.voucher.code == "NEAR" }
        assertFalse(nearItem.isEligible)
        assertEquals(40000.0, nearItem.missingAmount) // 120,000 - 80,000 = 40,000
    }

    private fun createVoucher(
        code: String,
        type: String = "fixed",
        value: Double = 10000.0,
        minOrderValue: Double = 0.0,
        maxDiscount: Double = 0.0,
        usageLimit: Int = 100,
        usedCount: Int = 0,
        startDate: Long = baseTime - 10000L,
        endDate: Long = baseTime + 100000L,
        isActive: Boolean = true,
        applicableRestaurants: List<String> = emptyList()
    ) = Voucher(
        id = "v_$code",
        code = code,
        name = "Voucher $code",
        type = type,
        value = value,
        minOrderValue = minOrderValue,
        maxDiscount = maxDiscount,
        usageLimit = usageLimit,
        usedCount = usedCount,
        startDate = startDate,
        endDate = endDate,
        isActive = isActive,
        applicableRestaurants = applicableRestaurants
    )
}
