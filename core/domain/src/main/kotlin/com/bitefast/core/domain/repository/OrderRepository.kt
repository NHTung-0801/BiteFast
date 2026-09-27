package com.bitefast.core.domain.repository

import com.bitefast.core.model.Order
import kotlinx.coroutines.flow.Flow

interface OrderRepository {
    /** Stream toan bo lich su don hang theo thoi gian giam dan. */
    fun getOrderHistory(): Flow<List<Order>>

    /** Stream cac don hang dang hoat dong (chua giao xong / chua huy). */
    fun getActiveOrders(): Flow<List<Order>>

    /** Stream cac don hang da hoan thanh hoac da huy. */
    fun getCompletedOrders(): Flow<List<Order>>

    /** Stream thong tin va trang thai mot don hang theo ID de live tracking. */
    fun getOrderStream(orderId: String): Flow<Order?>

    /** Tao don hang moi va tra ve don sau khi server xac nhan. */
    suspend fun createOrder(order: Order): Order

    /** Lay chi tiet mot don hang theo ID. */
    suspend fun getOrderDetail(orderId: String): Order?

    /** Huy don hang voi ly do. */
    suspend fun cancelOrder(orderId: String, reason: String)
}
