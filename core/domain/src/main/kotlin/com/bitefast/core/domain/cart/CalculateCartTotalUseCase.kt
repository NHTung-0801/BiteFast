package com.bitefast.core.domain.cart

import com.bitefast.core.model.CartItem
import javax.inject.Inject
import kotlin.math.ceil
import kotlin.math.max

data class CartBillCalculation(
    val subtotal: Double,
    val deliveryFee: Double,
    val platformFee: Double,
    val taxVat: Double,
    val discount: Double,
    val total: Double,
    val itemCount: Int,
    val isFreeDeliveryQualified: Boolean
)

class CalculateCartTotalUseCase @Inject constructor() {

    companion object {
        const val FREE_DELIVERY_THRESHOLD = 150000.0
        const val BASE_DELIVERY_FEE = 16000.0
        const val BASE_DELIVERY_DISTANCE_KM = 2.0
        const val FEE_PER_EXTRA_KM = 5000.0
        const val FLAT_PLATFORM_FEE = 2000.0
        const val VAT_RATE = 0.08 // 8% VAT
    }

    operator fun invoke(
        items: List<CartItem>,
        distanceKm: Float = 1.0f,
        voucherDiscount: Double = 0.0,
        isRestaurantFreeDelivery: Boolean = false
    ): CartBillCalculation {
        if (items.isEmpty()) {
            return CartBillCalculation(
                subtotal = 0.0,
                deliveryFee = 0.0,
                platformFee = 0.0,
                taxVat = 0.0,
                discount = 0.0,
                total = 0.0,
                itemCount = 0,
                isFreeDeliveryQualified = false
            )
        }

        val subtotal = items.sumOf { item ->
            val customizationPrice = item.customizations.sumOf { it.price }
            (item.price + customizationPrice) * item.quantity
        }

        val itemCount = items.sumOf { it.quantity }
        val isFreeDeliveryQualified = isRestaurantFreeDelivery || subtotal >= FREE_DELIVERY_THRESHOLD

        val deliveryFee = if (isFreeDeliveryQualified) {
            0.0
        } else {
            val extraKm = max(0.0, (distanceKm.toDouble() - BASE_DELIVERY_DISTANCE_KM))
            BASE_DELIVERY_FEE + (ceil(extraKm) * FEE_PER_EXTRA_KM)
        }

        val platformFee = FLAT_PLATFORM_FEE
        val taxVat = subtotal * VAT_RATE
        val discount = voucherDiscount.coerceIn(0.0, subtotal)
        val rawTotal = (subtotal + deliveryFee + platformFee + taxVat) - discount
        val total = max(0.0, rawTotal)

        return CartBillCalculation(
            subtotal = subtotal,
            deliveryFee = deliveryFee,
            platformFee = platformFee,
            taxVat = taxVat,
            discount = discount,
            total = total,
            itemCount = itemCount,
            isFreeDeliveryQualified = isFreeDeliveryQualified
        )
    }
}
