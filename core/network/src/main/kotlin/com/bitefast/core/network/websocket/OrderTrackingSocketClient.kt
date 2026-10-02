package com.bitefast.core.network.websocket

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Interface quan ly socket ket noi truc tiep theo doi hanh trinh don hang.
 */
interface OrderTrackingSocketClient {
    /**
     * Lang nghe luong cap nhat GPS va trang thai don hang theo thoi gian thuc.
     */
    fun observeLiveTracking(orderId: String): Flow<OrderLiveTrackingEvent>

    /**
     * Trang thai ket noi hien tai cua socket.
     */
    val connectionState: StateFlow<WebSocketConnectionState>

    /**
     * Dong ket noi chu dong.
     */
    fun close()
}
