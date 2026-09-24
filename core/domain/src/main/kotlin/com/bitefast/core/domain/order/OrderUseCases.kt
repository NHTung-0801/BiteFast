package com.bitefast.core.domain.order

import com.bitefast.core.domain.repository.CartRepository
import com.bitefast.core.domain.repository.OrderRepository
import com.bitefast.core.model.Order
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CreateOrderUseCase @Inject constructor(
    private val orderRepository: OrderRepository,
    private val cartRepository: CartRepository
) {
    suspend operator fun invoke(order: Order): Order {
        val created = orderRepository.createOrder(order)
        cartRepository.clearCart()
        return created
    }
}

class GetOrderHistoryUseCase @Inject constructor(
    private val orderRepository: OrderRepository
) {
    operator fun invoke(): Flow<List<Order>> {
        return orderRepository.getOrderHistory()
    }
}
