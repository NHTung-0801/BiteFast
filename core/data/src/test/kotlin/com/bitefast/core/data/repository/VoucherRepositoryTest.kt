package com.bitefast.core.data.repository

import com.bitefast.core.network.api.BiteFastApiService
import com.bitefast.core.network.model.ApiResponse
import com.bitefast.core.network.model.VoucherDto
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class VoucherRepositoryTest {

    private lateinit var apiService: BiteFastApiService
    private lateinit var repository: VoucherRepositoryImpl

    @BeforeEach
    fun setUp() {
        apiService = mockk(relaxed = true)
        repository = VoucherRepositoryImpl(apiService)
    }

    @Test
    @DisplayName("getVouchers returns active vouchers from cache or remote")
    fun getVouchers_returnsActiveVouchers() = runTest {
        val vouchers = repository.getVouchers(null)

        assertTrue(vouchers.isNotEmpty())
        assertTrue(vouchers.all { it.isActive })
        assertTrue(vouchers.any { it.code == "WELCOME50" })
    }

    @Test
    @DisplayName("getVouchers updates cache with remote vouchers when api succeeds")
    fun getVouchers_updatesWithRemote() = runTest {
        val remoteDto = VoucherDto(
            id = "v_remote_1",
            code = "REMOTE99",
            name = "Remote Discount 99k",
            description = "Remote voucher test",
            type = "fixed",
            value = 99000.0,
            minOrderValue = 200000.0,
            maxDiscount = 99000.0,
            startDate = 0L,
            endDate = System.currentTimeMillis() + 86400000L,
            isActive = true
        )

        coEvery { apiService.getVouchers() } returns ApiResponse(
            success = true,
            data = listOf(remoteDto)
        )

        val vouchers = repository.getVouchers(null)

        assertTrue(vouchers.any { it.code == "REMOTE99" })
    }

    @Test
    @DisplayName("getVoucherByCode finds voucher case-insensitively")
    fun getVoucherByCode_findsVoucher() = runTest {
        val voucher = repository.getVoucherByCode("welcome50")

        assertNotNull(voucher)
        assertEquals("WELCOME50", voucher?.code)
        assertEquals(50000.0, voucher?.value)
    }

    @Test
    @DisplayName("getVoucherByCode returns null for nonexistent code")
    fun getVoucherByCode_notFound() = runTest {
        coEvery { apiService.getVouchers() } returns ApiResponse(success = true, data = emptyList())

        val voucher = repository.getVoucherByCode("NON_EXISTING_CODE_XYZ")

        assertNull(voucher)
    }
}
