package com.bitefast.core.data.repository

import com.bitefast.core.domain.repository.OrderRepository
import com.bitefast.core.model.Order
import com.bitefast.core.model.OrderStatus
import com.bitefast.core.network.api.BiteFastApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val ACTIVE_STATUSES = setOf(
    OrderStatus.PENDING,
    OrderStatus.CONFIRMED,
    OrderStatus.PREPARING,
    OrderStatus.READY,
    OrderStatus.ON_THE_WAY,
)

private val COMPLETED_STATUSES = setOf(
    OrderStatus.DELIVERED,
    OrderStatus.CANCELED,
)

@Singleton
class OrderRepositoryImpl @Inject constructor(
    private val apiService: BiteFastApiService,
) : OrderRepository {

    override fun getOrderHistory(): Flow<List<Order>> = flow {
        val orders = try {
            apiService.getOrderHistory()
        } catch (e: Exception) {
            emptyList()
        }
        emit(orders)
    }

    override fun getActiveOrders(): Flow<List<Order>> =
        getOrderHistory().map { orders -> orders.filter { it.status in ACTIVE_STATUSES } }

    override fun getCompletedOrders(): Flow<List<Order>> =
        getOrderHistory().map { orders -> orders.filter { it.status in COMPLETED_STATUSES } }

    override suspend fun createOrder(order: Order): Order {
        return try {
            apiService.createOrder(order)
        } catch (e: Exception) {
            order.copy(id = "ord_${System.currentTimeMillis()}")
        }
    }

    override suspend fun getOrderDetail(orderId: String): Order? = null

    override suspend fun cancelOrder(orderId: String, reason: String) {
        // TODO: apiService.cancelOrder(orderId, reason)
    }
}
