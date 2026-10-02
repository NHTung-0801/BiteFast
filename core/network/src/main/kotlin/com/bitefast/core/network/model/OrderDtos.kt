package com.bitefast.core.network.model

import kotlinx.serialization.Serializable

@Serializable
data class OrderItemDto(
    val menuItemId: String,
    val name: String,
    val price: Double,
    val quantity: Int,
    val notes: String = "",
    val imageUrl: String = ""
)

@Serializable
data class CreateOrderRequestDto(
    val restaurantId: String,
    val restaurantName: String = "",
    val items: List<OrderItemDto>,
    val subtotal: Double,
    val deliveryFee: Double = 15000.0,
    val discount: Double = 0.0,
    val total: Double,
    val paymentMethod: String = "CASH",
    val deliveryAddressId: String = "",
    val deliveryAddressText: String = "",
    val recipientName: String = "",
    val recipientPhone: String = "",
    val notes: String = ""
)

@Serializable
data class OrderResponseDto(
    val id: String,
    val userId: String,
    val restaurantId: String,
    val restaurantName: String,
    val driverId: String = "",
    val driverName: String = "",
    val driverPhone: String = "",
    val items: List<OrderItemDto> = emptyList(),
    val subtotal: Double,
    val deliveryFee: Double,
    val taxes: Double = 0.0,
    val discount: Double = 0.0,
    val total: Double,
    val status: String,
    val paymentMethod: String,
    val paymentStatus: String,
    val deliveryAddressText: String = "",
    val recipientName: String = "",
    val recipientPhone: String = "",
    val orderTime: Long,
    val estimatedDeliveryTime: Long = 0L
)

@Serializable
data class CancelOrderRequestDto(
    val reason: String = ""
)
