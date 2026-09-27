package com.bitefast.core.domain.order

import com.bitefast.core.domain.repository.OrderRepository
import com.bitefast.core.model.Order
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * UseCase theo doi tien trinh don hang realtime theo orderId.
 */
class GetOrderTrackingUseCase @Inject constructor(
    private val orderRepository: OrderRepository
) {
    /** Lay chi tiet don hang ngay lap tuc. */
    suspend operator fun invoke(orderId: String): Order? {
        return orderRepository.getOrderDetail(orderId)
    }

    /** Stream realtime trang thai don hang. */
    fun stream(orderId: String): Flow<Order?> {
        return orderRepository.getOrderStream(orderId)
    }
}
