package com.bitefast.core.domain.voucher

import com.bitefast.core.domain.repository.VoucherRepository
import com.bitefast.core.model.Voucher
import javax.inject.Inject

data class BestVoucherResult(
    val bestVoucher: Voucher? = null,
    val bestDiscount: Double = 0.0,
    val message: String? = null,
    val hasApplicableVouchers: Boolean = bestVoucher != null,
    /** Gợi ý voucher hời hơn nếu mua thêm một ít tiền */
    val upsellVoucher: Voucher? = null,
    val missingAmountForUpsell: Double = 0.0,
    val potentialUpsellDiscount: Double = 0.0,
    val upsellMessage: String? = null
)

class GetBestVoucherUseCase @Inject constructor(
    private val voucherRepository: VoucherRepository,
    private val validateVoucherUseCase: ValidateVoucherUseCase,
) {
    suspend operator fun invoke(
        subtotal: Double,
        restaurantId: String = "",
        deliveryFee: Double = 15_000.0,
        currentTimeMillis: Long = System.currentTimeMillis()
    ): BestVoucherResult {
        val allVouchers = voucherRepository.getVouchers(restaurantId.ifBlank { null })

        var bestVoucher: Voucher? = null
        var maxDiscount = 0.0

        var bestUpsellVoucher: Voucher? = null
        var minMissingAmount = Double.MAX_VALUE
        var potentialDiscount = 0.0

        for (voucher in allVouchers) {
            val result = validateVoucherUseCase(
                voucher = voucher,
                subtotal = subtotal,
                restaurantId = restaurantId,
                deliveryFee = deliveryFee,
                currentTimeMillis = currentTimeMillis
            )

            when (result) {
                is VoucherValidationResult.Valid -> {
                    if (result.calculatedDiscount > maxDiscount) {
                        maxDiscount = result.calculatedDiscount
                        bestVoucher = voucher
                    }
                }
                is VoucherValidationResult.Invalid -> {
                    if (result.reason == VoucherInvalidReason.MIN_ORDER_VALUE_NOT_MET) {
                        val missing = voucher.minOrderValue - subtotal
                        if (missing > 0 && missing < minMissingAmount) {
                            val hypotheticalDiscount = when (voucher.type) {
                                "percentage" -> {
                                    val raw = voucher.minOrderValue * (voucher.value / 100.0)
                                    if (voucher.maxDiscount > 0) raw.coerceAtMost(voucher.maxDiscount) else raw
                                }
                                "fixed" -> voucher.value.coerceAtMost(voucher.minOrderValue)
                                "free_shipping" -> deliveryFee.coerceAtLeast(0.0)
                                else -> 0.0
                            }
                            if (hypotheticalDiscount > maxDiscount) {
                                minMissingAmount = missing
                                bestUpsellVoucher = voucher
                                potentialDiscount = hypotheticalDiscount
                            }
                        }
                    }
                }
            }
        }

        val bestMessage = if (bestVoucher != null) {
            "Áp dụng mã ${bestVoucher.code} tiết kiệm %,.0fđ".format(maxDiscount)
        } else null

        val upsellMsg = if (bestUpsellVoucher != null && minMissingAmount < Double.MAX_VALUE) {
            "Thêm %,.0fđ để áp dụng mã ${bestUpsellVoucher.code} giảm %,.0fđ".format(minMissingAmount, potentialDiscount)
        } else null

        return BestVoucherResult(
            bestVoucher = bestVoucher,
            bestDiscount = maxDiscount,
            message = bestMessage,
            hasApplicableVouchers = bestVoucher != null,
            upsellVoucher = bestUpsellVoucher,
            missingAmountForUpsell = if (bestUpsellVoucher != null) minMissingAmount else 0.0,
            potentialUpsellDiscount = potentialDiscount,
            upsellMessage = upsellMsg
        )
    }
}
