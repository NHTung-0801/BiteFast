package com.bitefast.core.network.websocket

import kotlinx.serialization.Serializable

/**
 * Su kien cap nhat hanh trinh va toa do GPS thoi gian thuc cua tai xe.
 */
@Serializable
data class OrderLiveTrackingEvent(
    val orderId: String,
    val status: String,
    val driverId: String = "",
    val driverName: String = "",
    val driverPhone: String = "",
    val driverLat: Double = 0.0,
    val driverLng: Double = 0.0,
    val etaMinutes: Int = 15,
    val progressPercent: Float = 0.35f,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Trang thai ket noi socket.
 */
sealed interface WebSocketConnectionState {
    data object Idle : WebSocketConnectionState
    data object Connecting : WebSocketConnectionState
    data object Connected : WebSocketConnectionState
    data class Disconnected(val code: Int = 1000, val reason: String? = null) : WebSocketConnectionState
    data class Error(val throwable: Throwable) : WebSocketConnectionState
}
