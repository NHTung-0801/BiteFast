package com.bitefast.core.domain.voucher

import com.bitefast.core.domain.repository.VoucherRepository
import com.bitefast.core.model.Voucher
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class VoucherUseCasesTest {

    private val voucherRepository: VoucherRepository = mockk()
    private lateinit var validateVoucherUseCase: ValidateVoucherUseCase
    private lateinit var applyVoucherUseCase: ApplyVoucherUseCase

    private val baseTime = 1700000000000L

    @BeforeEach
    fun setUp() {
        validateVoucherUseCase = ValidateVoucherUseCase()
        applyVoucherUseCase = ApplyVoucherUseCase(voucherRepository, validateVoucherUseCase)
    }

    @Test
    @DisplayName("ValidateVoucher: returns NOT_FOUND when voucher is null")
    fun validate_nullVoucher_returnsNotFound() {
        val result = validateVoucherUseCase(
            voucher = null,
            subtotal = 100000.0,
            restaurantId = "res_1",
            currentTimeMillis = baseTime
        )

        assertTrue(result is VoucherValidationResult.Invalid)
        assertEquals(VoucherInvalidReason.NOT_FOUND, (result as VoucherValidationResult.Invalid).reason)
    }

    @Test
    @DisplayName("ValidateVoucher: returns INACTIVE when isActive is false")
    fun validate_inactiveVoucher_returnsInactive() {
        val voucher = createVoucher(isActive = false)
        val result = validateVoucherUseCase(voucher, 100000.0, "res_1", currentTimeMillis = baseTime)

        assertTrue(result is VoucherValidationResult.Invalid)
        assertEquals(VoucherInvalidReason.INACTIVE, (result as VoucherValidationResult.Invalid).reason)
    }

    @Test
    @DisplayName("ValidateVoucher: returns NOT_STARTED when before startDate")
    fun validate_notStarted_returnsNotStarted() {
        val voucher = createVoucher(startDate = baseTime + 10000L, endDate = baseTime + 50000L)
        val result = validateVoucherUseCase(voucher, 100000.0, "res_1", currentTimeMillis = baseTime)

        assertTrue(result is VoucherValidationResult.Invalid)
        assertEquals(VoucherInvalidReason.NOT_STARTED, (result as VoucherValidationResult.Invalid).reason)
    }

    @Test
    @DisplayName("ValidateVoucher: returns EXPIRED when after endDate")
    fun validate_expired_returnsExpired() {
        val voucher = createVoucher(startDate = baseTime - 50000L, endDate = baseTime - 10000L)
        val result = validateVoucherUseCase(voucher, 100000.0, "res_1", currentTimeMillis = baseTime)

        assertTrue(result is VoucherValidationResult.Invalid)
        assertEquals(VoucherInvalidReason.EXPIRED, (result as VoucherValidationResult.Invalid).reason)
    }

    @Test
    @DisplayName("ValidateVoucher: returns USAGE_LIMIT_EXCEEDED when usedCount >= usageLimit")
    fun validate_usageLimitExceeded_returnsLimitExceeded() {
        val voucher = createVoucher(usageLimit = 5, usedCount = 5)
        val result = validateVoucherUseCase(voucher, 100000.0, "res_1", currentTimeMillis = baseTime)

        assertTrue(result is VoucherValidationResult.Invalid)
        assertEquals(VoucherInvalidReason.USAGE_LIMIT_EXCEEDED, (result as VoucherValidationResult.Invalid).reason)
    }

    @Test
    @DisplayName("ValidateVoucher: returns MIN_ORDER_VALUE_NOT_MET when subtotal < minOrderValue")
    fun validate_minOrderNotMet_returnsMinOrderNotMet() {
        val voucher = createVoucher(minOrderValue = 150000.0)
        val result = validateVoucherUseCase(voucher, 100000.0, "res_1", currentTimeMillis = baseTime)

        assertTrue(result is VoucherValidationResult.Invalid)
        assertEquals(VoucherInvalidReason.MIN_ORDER_VALUE_NOT_MET, (result as VoucherValidationResult.Invalid).reason)
    }

    @Test
    @DisplayName("ValidateVoucher: returns RESTAURANT_NOT_APPLICABLE when restaurant not in list")
    fun validate_restaurantNotApplicable_returnsNotApplicable() {
        val voucher = createVoucher(applicableRestaurants = listOf("res_other"))
        val result = validateVoucherUseCase(voucher, 100000.0, "res_1", currentTimeMillis = baseTime)

        assertTrue(result is VoucherValidationResult.Invalid)
        assertEquals(VoucherInvalidReason.RESTAURANT_NOT_APPLICABLE, (result as VoucherValidationResult.Invalid).reason)
    }

    @Test
    @DisplayName("ValidateVoucher: calculates percentage discount capped at maxDiscount")
    fun validate_percentageDiscount_capsAtMaxDiscount() {
        val voucher = createVoucher(
            type = "percentage",
            value = 20.0, // 20%
            maxDiscount = 30000.0,
            minOrderValue = 50000.0
        )
        // 20% of 200,000 = 40,000 -> capped at 30,000
        val result = validateVoucherUseCase(voucher, 200000.0, "res_1", currentTimeMillis = baseTime)

        assertTrue(result is VoucherValidationResult.Valid)
        assertEquals(30000.0, (result as VoucherValidationResult.Valid).calculatedDiscount)
    }

    @Test
    @DisplayName("ValidateVoucher: calculates fixed discount capped at subtotal")
    fun validate_fixedDiscount_capsAtSubtotal() {
        val voucher = createVoucher(type = "fixed", value = 50000.0, minOrderValue = 30000.0)
        val result = validateVoucherUseCase(voucher, 40000.0, "res_1", currentTimeMillis = baseTime)

        assertTrue(result is VoucherValidationResult.Valid)
        assertEquals(40000.0, (result as VoucherValidationResult.Valid).calculatedDiscount)
    }

    @Test
    @DisplayName("ValidateVoucher: calculates free shipping discount")
    fun validate_freeShippingDiscount_appliesDeliveryFee() {
        val voucher = createVoucher(type = "free_shipping", minOrderValue = 50000.0)
        val result = validateVoucherUseCase(
            voucher = voucher,
            subtotal = 80000.0,
            restaurantId = "res_1",
            deliveryFee = 25000.0,
            currentTimeMillis = baseTime
        )

        assertTrue(result is VoucherValidationResult.Valid)
        assertEquals(25000.0, (result as VoucherValidationResult.Valid).calculatedDiscount)
    }

    @Test
    @DisplayName("ApplyVoucher: normalizes code to uppercase and applies successfully")
    fun applyVoucher_normalizesCode_success() = runTest {
        val voucher = createVoucher(code = "BITEFAST20", type = "fixed", value = 20000.0)
        coEvery { voucherRepository.getVoucherByCode("BITEFAST20") } returns voucher

        val result = applyVoucherUseCase(
            code = "  bitefast20  ",
            subtotal = 100000.0,
            restaurantId = "res_1",
            currentTimeMillis = baseTime
        )

        assertTrue(result is VoucherValidationResult.Valid)
        assertEquals(20000.0, (result as VoucherValidationResult.Valid).calculatedDiscount)
    }

    private fun createVoucher(
        code: String = "TEST_CODE",
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
        id = "v_1",
        code = code,
        name = "Test Voucher",
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
