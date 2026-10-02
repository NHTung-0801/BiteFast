package com.bitefast.core.network.model

import kotlinx.serialization.Serializable

@Serializable
data class VoucherDto(
    val id: String,
    val code: String,
    val name: String,
    val description: String = "",
    val type: String = "percentage",
    val value: Double = 0.0,
    val minOrderValue: Double = 0.0,
    val maxDiscount: Double = 0.0,
    val usageLimit: Int = 1,
    val usedCount: Int = 0,
    val startDate: Long = 0L,
    val endDate: Long = 0L,
    val isActive: Boolean = true,
    val applicableRestaurants: List<String> = emptyList(),
    val imageUrl: String = ""
)

@Serializable
data class ApplyVoucherRequestDto(
    val code: String,
    val orderTotal: Double,
    val restaurantId: String = ""
)

@Serializable
data class VoucherValidationResponseDto(
    val isValid: Boolean,
    val discountAmount: Double = 0.0,
    val voucher: VoucherDto? = null,
    val message: String = ""
)
