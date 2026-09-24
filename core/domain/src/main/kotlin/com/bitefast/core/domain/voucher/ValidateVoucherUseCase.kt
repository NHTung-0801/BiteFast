package com.bitefast.core.domain.voucher

import com.bitefast.core.model.Voucher
import javax.inject.Inject

enum class VoucherInvalidReason {
    NOT_FOUND,
    INACTIVE,
    NOT_STARTED,
    EXPIRED,
    USAGE_LIMIT_EXCEEDED,
    MIN_ORDER_VALUE_NOT_MET,
    RESTAURANT_NOT_APPLICABLE
}

sealed interface VoucherValidationResult {
    data class Valid(val voucher: Voucher, val calculatedDiscount: Double) : VoucherValidationResult
    data class Invalid(val reason: VoucherInvalidReason, val message: String) : VoucherValidationResult
}

class ValidateVoucherUseCase @Inject constructor() {
    operator fun invoke(
        voucher: Voucher?,
        subtotal: Double,
        restaurantId: String,
        deliveryFee: Double = 0.0,
        currentTimeMillis: Long = System.currentTimeMillis()
    ): VoucherValidationResult {
        if (voucher == null) {
            return VoucherValidationResult.Invalid(
                reason = VoucherInvalidReason.NOT_FOUND,
                message = "Mã giảm giá không tồn tại."
            )
        }

        if (!voucher.isActive) {
            return VoucherValidationResult.Invalid(
                reason = VoucherInvalidReason.INACTIVE,
                message = "Mã giảm giá hiện đang tạm ngưng."
            )
        }

        if (voucher.startDate > 0 && currentTimeMillis < voucher.startDate) {
            return VoucherValidationResult.Invalid(
                reason = VoucherInvalidReason.NOT_STARTED,
                message = "Chương trình ưu đãi chưa diễn ra."
            )
        }

        if (voucher.endDate > 0 && currentTimeMillis > voucher.endDate) {
            return VoucherValidationResult.Invalid(
                reason = VoucherInvalidReason.EXPIRED,
                message = "Mã giảm giá đã hết hạn sử dụng."
            )
        }

        if (voucher.usageLimit in 1..voucher.usedCount) {
            return VoucherValidationResult.Invalid(
                reason = VoucherInvalidReason.USAGE_LIMIT_EXCEEDED,
                message = "Mã ưu đãi đã hết lượt sử dụng."
            )
        }

        if (subtotal < voucher.minOrderValue) {
            val missing = voucher.minOrderValue - subtotal
            return VoucherValidationResult.Invalid(
                reason = VoucherInvalidReason.MIN_ORDER_VALUE_NOT_MET,
                message = "Đơn hàng chưa đạt giá trị tối thiểu ${voucher.minOrderValue.toLong()}đ (cần thêm ${missing.toLong()}đ)."
            )
        }

        if (voucher.applicableRestaurants.isNotEmpty() && !voucher.applicableRestaurants.contains(restaurantId)) {
            return VoucherValidationResult.Invalid(
                reason = VoucherInvalidReason.RESTAURANT_NOT_APPLICABLE,
                message = "Mã ưu đãi không áp dụng cho quán ăn này."
            )
        }

        val discount = when (voucher.type) {
            "percentage" -> {
                val rawDiscount = subtotal * (voucher.value / 100.0)
                if (voucher.maxDiscount > 0) rawDiscount.coerceAtMost(voucher.maxDiscount) else rawDiscount
            }
            "fixed" -> voucher.value.coerceAtMost(subtotal)
            "free_shipping" -> deliveryFee.coerceAtLeast(0.0)
            else -> 0.0
        }

        return VoucherValidationResult.Valid(
            voucher = voucher,
            calculatedDiscount = discount
        )
    }
}
