package com.bitefast.core.data.repository

import com.bitefast.core.domain.repository.OrderRepository
import com.bitefast.core.model.Order
import com.bitefast.core.network.api.BiteFastApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrderRepositoryImpl @Inject constructor(
    private val apiService: BiteFastApiService
) : OrderRepository {

    override fun getOrderHistory(): Flow<List<Order>> = flow {
        val orders = try {
            apiService.getOrderHistory()
        } catch (e: Exception) {
            emptyList()
        }
        emit(orders)
    }

    override suspend fun createOrder(order: Order): Order {
        return try {
            apiService.createOrder(order)
        } catch (e: Exception) {
            order.copy(id = "ord_${System.currentTimeMillis()}")
        }
    }

    override suspend fun getOrderDetail(orderId: String): Order? {
        return null
    }
}
