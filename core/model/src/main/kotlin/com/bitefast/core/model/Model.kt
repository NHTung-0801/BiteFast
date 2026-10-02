package com.bitefast.core.model

import kotlinx.serialization.Serializable
import kotlin.math.min

@Serializable
data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val avatar: String? = null,
    val isGuest: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val lastLoginAt: Long = System.currentTimeMillis(),
    val preferences: UserPreferences = UserPreferences()
) {
    val displayName: String get() = if (name.isNotEmpty()) name else email.takeIf { it.isNotEmpty() }?.split("@")?.firstOrNull() ?: "Guest"
}

@Serializable
data class UserPreferences(
    val language: String = "vi",
    val theme: String = "system",
    val notifications: Boolean = true,
    val pushNotifications: Boolean = true,
    val soundEnabled: Boolean = true,
    val hapticFeedback: Boolean = true,
    val autoRefresh: Boolean = true,
    val biometricAuth: Boolean = false
)

@Serializable
data class Address(
    val id: String = "",
    val userId: String = "",
    val label: String = "",
    val recipientName: String = "",
    val phoneNumber: String = "",
    val streetAddress: String = "",
    val city: String = "",
    val state: String = "",
    val postalCode: String = "",
    val country: String = "Vietnam",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val isDefault: Boolean = false,
    val isSelected: Boolean = false
)

@Serializable
data class Restaurant(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val cuisine: String = "",
    val rating: Double = 0.0,
    val distance: Float = 0f,
    val estimatedTime: Int = 0,
    val priceLevel: Int = 1,
    val imageUrl: String = "",
    val coverImageUrl: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val address: String = "",
    val phoneNumber: String = "",
    val openingHours: String = "",
    val isOpen: Boolean = false,
    val isFreeDelivery: Boolean = false,
    val tags: List<String> = emptyList(),
    val isFavorite: Boolean = false,
    val totalOrders: Int = 0,
    val acceptTime: Int = 0
)

@Serializable
data class MenuItem(
    val id: String = "",
    val restaurantId: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val currency: String = "VND",
    val imageUrl: String = "",
    val category: String = "",
    val isAvailable: Boolean = true,
    val isVegetarian: Boolean = false,
    val isSpicy: Boolean = false,
    val isPopular: Boolean = false,
    val preparationTime: Int = 0,
    val tags: List<String> = emptyList(),
    val rating: Double = 4.8,
    val reviewCount: Int = 50,
    val isFavorite: Boolean = false
)

@Serializable
data class CartItem(
    val id: String = "",
    val cartId: String = "",
    val menuItemId: String = "",
    val restaurantId: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val quantity: Int = 1,
    val notes: String = "",
    val imageUrl: String = "",
    val customizations: List<Customization> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
) {
    val totalPrice: Double get() = price * quantity
}

@Serializable
data class Customization(
    val id: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val selectedOptions: List<String> = emptyList()
)

@Serializable
data class Order(
    val id: String = "",
    val userId: String = "",
    val restaurantId: String = "",
    val restaurantName: String = "",
    val driverId: String = "",
    val driverName: String = "",
    val driverPhone: String = "",
    val items: List<CartItem> = emptyList(),
    val subtotal: Double = 0.0,
    val deliveryFee: Double = 0.0,
    val taxes: Double = 0.0,
    val discount: Double = 0.0,
    val total: Double = 0.0,
    val status: OrderStatus = OrderStatus.PENDING,
    val paymentMethod: PaymentMethod = PaymentMethod.CASH,
    val paymentStatus: PaymentStatus = PaymentStatus.PENDING,
    val address: Address = Address(),
    val orderTime: Long = System.currentTimeMillis(),
    val preparedTime: Long = 0L,
    val deliveredTime: Long = 0L,
    val estimatedDeliveryTime: Long = 0L,
    val rating: Int = 0,
    val feedback: String = "",
    val cancellationReason: String = "",
    val isCanceled: Boolean = false
)

@Serializable
enum class OrderStatus {
    PENDING,
    CONFIRMED,
    PREPARING,
    READY,
    ON_THE_WAY,
    DELIVERED,
    CANCELED
}

@Serializable
enum class PaymentMethod {
    CASH,
    CARD,
    E_WALLET,
    WALLET
}

@Serializable
enum class PaymentStatus {
    PENDING,
    PAID,
    FAILED,
    REFUNDED
}

@Serializable
data class Voucher(
    val id: String = "",
    val code: String = "",
    val name: String = "",
    val description: String = "",
    val type: String = "",
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
) {
    fun isValid(): Boolean {
        val currentTime = System.currentTimeMillis()
        return isActive && usedCount < usageLimit && currentTime in startDate..endDate
    }

    fun getDiscountAmount(orderTotal: Double): Double {
        return when (type) {
            "percentage" -> min((value / 100) * orderTotal, maxDiscount.takeIf { it > 0 } ?: Double.MAX_VALUE)
            "fixed" -> min(value, orderTotal)
            "free_shipping" -> if (orderTotal >= minOrderValue) 15000.0 else 0.0
            else -> 0.0
        }
    }
}

@Serializable
data class Rating(
    val id: String = "",
    val orderId: String = "",
    val userId: String = "",
    val userName: String = "",
    val restaurantId: String = "",
    val restaurantRating: Int = 0,
    val driverRating: Int = 0,
    val tags: List<String> = emptyList(),
    val comment: String = "",
    val isAnonymous: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
data class Notification(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val message: String = "",
    val type: String = "",
    val data: Map<String, String> = emptyMap(),
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
enum class FavoriteType {
    DISH,
    RESTAURANT
}

@Serializable
data class FavoriteItem(
    val id: String = "",
    val type: FavoriteType = FavoriteType.DISH,
    val targetId: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val imageUrl: String = "",
    val rating: Double = 4.8,
    val restaurantId: String = "",
    val restaurantName: String = "",
    val category: String = "",
    val createdAt: Long = System.currentTimeMillis()
)