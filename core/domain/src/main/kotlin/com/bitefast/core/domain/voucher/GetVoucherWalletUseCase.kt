package com.bitefast.core.domain.voucher

import com.bitefast.core.domain.repository.VoucherRepository
import com.bitefast.core.model.Voucher
import javax.inject.Inject

enum class VoucherCategory {
    ALL,
    SHIPPING,
    DISCOUNT,
    RESTAURANT
}

data class VoucherWalletItem(
    val voucher: Voucher,
    val isEligible: Boolean,
    val calculatedDiscount: Double = 0.0,
    val missingAmount: Double = 0.0,
    val statusMessage: String = "",
    val ineligibilityReason: VoucherInvalidReason? = null
)

data class VoucherWalletState(
    val items: List<VoucherWalletItem> = emptyList(),
    val eligibleCount: Int = 0,
    val totalCount: Int = 0
)

class GetVoucherWalletUseCase @Inject constructor(
    private val voucherRepository: VoucherRepository,
    private val validateVoucherUseCase: ValidateVoucherUseCase,
) {
    suspend operator fun invoke(
        subtotal: Double = 0.0,
        restaurantId: String = "",
        deliveryFee: Double = 15_000.0,
        category: VoucherCategory = VoucherCategory.ALL,
        currentTimeMillis: Long = System.currentTimeMillis()
    ): VoucherWalletState {
        val allVouchers = voucherRepository.getVouchers(restaurantId.ifBlank { null })

        val filteredByCategory = when (category) {
            VoucherCategory.ALL -> allVouchers
            VoucherCategory.SHIPPING -> allVouchers.filter { it.type == "free_shipping" }
            VoucherCategory.DISCOUNT -> allVouchers.filter { it.type == "percentage" || it.type == "fixed" }
            VoucherCategory.RESTAURANT -> allVouchers.filter { it.applicableRestaurants.isNotEmpty() }
        }

        val items = filteredByCategory.map { voucher ->
            val result = validateVoucherUseCase(
                voucher = voucher,
                subtotal = subtotal,
                restaurantId = restaurantId,
                deliveryFee = deliveryFee,
                currentTimeMillis = currentTimeMillis
            )
            when (result) {
                is VoucherValidationResult.Valid -> {
                    VoucherWalletItem(
                        voucher = voucher,
                        isEligible = true,
                        calculatedDiscount = result.calculatedDiscount,
                        statusMessage = "Có thể áp dụng ngay"
                    )
                }
                is VoucherValidationResult.Invalid -> {
                    val missing = if (result.reason == VoucherInvalidReason.MIN_ORDER_VALUE_NOT_MET) {
                        (voucher.minOrderValue - subtotal).coerceAtLeast(0.0)
                    } else 0.0
                    VoucherWalletItem(
                        voucher = voucher,
                        isEligible = false,
                        calculatedDiscount = 0.0,
                        missingAmount = missing,
                        statusMessage = result.message,
                        ineligibilityReason = result.reason
                    )
                }
            }
        }.sortedWith(
            compareByDescending<VoucherWalletItem> { it.isEligible }
                .thenByDescending { it.calculatedDiscount }
                .thenBy { it.missingAmount }
        )

        return VoucherWalletState(
            items = items,
            eligibleCount = items.count { it.isEligible },
            totalCount = items.size
        )
    }
}
