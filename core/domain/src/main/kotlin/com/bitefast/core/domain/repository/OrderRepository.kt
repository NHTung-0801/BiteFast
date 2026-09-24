package com.bitefast.core.domain.repository

import com.bitefast.core.model.Order
import kotlinx.coroutines.flow.Flow

interface OrderRepository {
    fun getOrderHistory(): Flow<List<Order>>
    suspend fun createOrder(order: Order): Order
    suspend fun getOrderDetail(orderId: String): Order?
}
