package com.bitefast.core.domain.voucher

import com.bitefast.core.domain.repository.VoucherRepository
import javax.inject.Inject

class ApplyVoucherUseCase @Inject constructor(
    private val voucherRepository: VoucherRepository,
    private val validateVoucherUseCase: ValidateVoucherUseCase
) {
    suspend operator fun invoke(
        code: String,
        subtotal: Double,
        restaurantId: String,
        deliveryFee: Double = 0.0,
        currentTimeMillis: Long = System.currentTimeMillis()
    ): VoucherValidationResult {
        val normalizedCode = code.trim().uppercase()
        val voucher = voucherRepository.getVoucherByCode(normalizedCode)
        return validateVoucherUseCase(
            voucher = voucher,
            subtotal = subtotal,
            restaurantId = restaurantId,
            deliveryFee = deliveryFee,
            currentTimeMillis = currentTimeMillis
        )
    }
}
