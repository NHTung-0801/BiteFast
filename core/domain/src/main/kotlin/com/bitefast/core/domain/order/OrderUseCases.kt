package com.bitefast.core.domain.order

import com.bitefast.core.domain.repository.CartRepository
import com.bitefast.core.domain.repository.OrderRepository
import com.bitefast.core.model.Order
import com.bitefast.core.model.OrderStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

// ─── Order History ─────────────────────────────────────────────────────────────

/** Trạng thái đơn hàng đang hoạt động (chưa kết thúc). */
private val ACTIVE_STATUSES = setOf(
    OrderStatus.PENDING,
    OrderStatus.CONFIRMED,
    OrderStatus.PREPARING,
    OrderStatus.READY,
    OrderStatus.ON_THE_WAY,
)

/** Trạng thái đơn hàng đã kết thúc. */
private val COMPLETED_STATUSES = setOf(
    OrderStatus.DELIVERED,
    OrderStatus.CANCELED,
)

/**
 * Toàn bộ lịch sử đơn hàng, được phân loại thành hai nhóm:
 * - [active]: đơn đang xử lý / đang giao.
 * - [completed]: đơn đã giao hoặc đã huỷ.
 */
data class CategorizedOrders(
    val active: List<Order>,
    val completed: List<Order>,
)

/**
 * Lấy toàn bộ lịch sử đơn hàng dưới dạng Flow realtime, tự động phân loại.
 */
class GetOrderHistoryUseCase @Inject constructor(
    private val orderRepository: OrderRepository,
) {
    /** Flow danh sách thô toàn bộ đơn hàng (không phân loại). */
    operator fun invoke(): Flow<List<Order>> = orderRepository.getOrderHistory()

    /** Flow tự động phân loại đơn đang hoạt động và đã hoàn thành. */
    fun categorized(): Flow<CategorizedOrders> =
        orderRepository.getOrderHistory().map { orders ->
            CategorizedOrders(
                active = orders.filter { it.status in ACTIVE_STATUSES }
                    .sortedByDescending { it.orderTime },
                completed = orders.filter { it.status in COMPLETED_STATUSES }
                    .sortedByDescending { it.orderTime },
            )
        }

    /** Flow chỉ các đơn đang hoạt động (stream realtime từ repository). */
    fun activeOrders(): Flow<List<Order>> = orderRepository.getActiveOrders()

    /** Flow chỉ các đơn đã hoàn thành (stream realtime từ repository). */
    fun completedOrders(): Flow<List<Order>> = orderRepository.getCompletedOrders()
}

// ─── Create Order ──────────────────────────────────────────────────────────────

/**
 * Tạo đơn hàng và xóa giỏ hàng. Dùng trong trường hợp không cần validate voucher/checkout.
 * (Use case đơn giản; checkout đầy đủ → dùng [CheckoutOrderUseCase]).
 */
class CreateOrderUseCase @Inject constructor(
    private val orderRepository: OrderRepository,
    private val cartRepository: CartRepository,
) {
    suspend operator fun invoke(order: Order): Order {
        val created = orderRepository.createOrder(order)
        cartRepository.clearCart()
        return created
    }
}

// ─── Cancel Order ──────────────────────────────────────────────────────────────

/** Huỷ đơn hàng với lý do. */
class CancelOrderUseCase @Inject constructor(
    private val orderRepository: OrderRepository,
) {
    suspend operator fun invoke(orderId: String, reason: String) {
        orderRepository.cancelOrder(orderId, reason)
    }
}
