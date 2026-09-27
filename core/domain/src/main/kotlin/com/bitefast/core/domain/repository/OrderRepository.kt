package com.bitefast.core.domain.repository

import com.bitefast.core.model.Order
import com.bitefast.core.model.OrderStatus
import kotlinx.coroutines.flow.Flow

interface OrderRepository {
    /** Stream toàn bộ lịch sử đơn hàng theo thời gian giảm dần. */
    fun getOrderHistory(): Flow<List<Order>>

    /** Stream các đơn hàng đang hoạt động (chưa giao xong / chưa huỷ). */
    fun getActiveOrders(): Flow<List<Order>>

    /** Stream các đơn hàng đã hoàn thành hoặc đã huỷ. */
    fun getCompletedOrders(): Flow<List<Order>>

    /** Tạo đơn hàng mới và trả về đơn sau khi server xác nhận. */
    suspend fun createOrder(order: Order): Order

    /** Lấy chi tiết một đơn hàng theo ID. */
    suspend fun getOrderDetail(orderId: String): Order?

    /** Huỷ đơn hàng với lý do. */
    suspend fun cancelOrder(orderId: String, reason: String)
}
