package com.bitefast.core.data.repository

import com.bitefast.core.data.mapper.asExternalModel
import com.bitefast.core.data.mapper.asOrderItemDto
import com.bitefast.core.domain.repository.OrderRepository
import com.bitefast.core.model.Address
import com.bitefast.core.model.CartItem
import com.bitefast.core.model.Order
import com.bitefast.core.model.OrderStatus
import com.bitefast.core.model.PaymentMethod
import com.bitefast.core.model.PaymentStatus
import com.bitefast.core.network.api.BiteFastApiService
import com.bitefast.core.network.model.CancelOrderRequestDto
import com.bitefast.core.network.model.CreateOrderRequestDto
import com.bitefast.core.network.websocket.OrderTrackingSocketClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
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
    private val socketClient: OrderTrackingSocketClient? = null
) : OrderRepository {

    private val initialSampleOrders = listOf(
        Order(
            id = "ord_101",
            userId = "usr_demo",
            restaurantId = "res_1",
            restaurantName = "Com Tam Phuc Loc Tho - Le Van Viet",
            driverId = "drv_01",
            driverName = "Nguyen Van Hung",
            driverPhone = "0901234567",
            items = listOf(
                CartItem(
                    id = "ci_1",
                    cartId = "cart_demo",
                    menuItemId = "menu_1_2",
                    restaurantId = "res_1",
                    name = "Com Suon Bi Cha Dac Biet",
                    price = 65000.0,
                    quantity = 1,
                    notes = "Nhieu mo hanh",
                    imageUrl = "https://images.unsplash.com/photo-1544025162-d76694265947?w=500"
                ),
                CartItem(
                    id = "ci_2",
                    cartId = "cart_demo",
                    menuItemId = "menu_1_7",
                    restaurantId = "res_1",
                    name = "Sam Bi Dao Hat Chia",
                    price = 18000.0,
                    quantity = 1,
                    imageUrl = "https://images.unsplash.com/photo-1558857563-b371033873b8?w=500"
                )
            ),
            subtotal = 83000.0,
            deliveryFee = 15000.0,
            taxes = 0.0,
            discount = 15000.0,
            total = 83000.0,
            status = OrderStatus.ON_THE_WAY,
            paymentMethod = PaymentMethod.CASH,
            paymentStatus = PaymentStatus.PENDING,
            address = Address(
                id = "addr_1",
                label = "Nha rieng",
                recipientName = "Nguyen Van A",
                phoneNumber = "0909123456",
                streetAddress = "456 Le Van Viet, Tang Nhon Phu A",
                city = "TP. Thu Duc",
                latitude = 10.8499,
                longitude = 106.7719
            ),
            orderTime = System.currentTimeMillis() - 15 * 60 * 1000L,
            estimatedDeliveryTime = System.currentTimeMillis() + 15 * 60 * 1000L
        ),
        Order(
            id = "ord_102",
            userId = "usr_demo",
            restaurantId = "res_2",
            restaurantName = "Pho Thin Lo Duc - Bo Tai Lan",
            driverId = "drv_02",
            driverName = "Tran Minh Duc",
            driverPhone = "0912345678",
            items = listOf(
                CartItem(
                    id = "ci_3",
                    cartId = "cart_demo",
                    menuItemId = "menu_2_1",
                    restaurantId = "res_2",
                    name = "Pho Bo Tai Lan Truyen Thong",
                    price = 75000.0,
                    quantity = 1,
                    notes = "Nhieu hanh hoa, it banh pho",
                    imageUrl = "https://images.unsplash.com/photo-1582878826629-29b7ad1cdc43?w=500"
                )
            ),
            subtotal = 75000.0,
            deliveryFee = 15000.0,
            taxes = 0.0,
            discount = 0.0,
            total = 90000.0,
            status = OrderStatus.PREPARING,
            paymentMethod = PaymentMethod.E_WALLET,
            paymentStatus = PaymentStatus.PAID,
            address = Address(
                id = "addr_2",
                label = "Cong ty",
                recipientName = "Nguyen Van A",
                phoneNumber = "0909123456",
                streetAddress = "Tòa nhà Bitexco, Q.1",
                city = "TP. Ho Chi Minh",
                latitude = 10.7718,
                longitude = 106.7044
            ),
            orderTime = System.currentTimeMillis() - 5 * 60 * 1000L,
            estimatedDeliveryTime = System.currentTimeMillis() + 25 * 60 * 1000L
        ),
        Order(
            id = "ord_103",
            userId = "usr_demo",
            restaurantId = "res_3",
            restaurantName = "Pizza 4P's - Hai Ba Trung",
            driverId = "drv_03",
            driverName = "Le Quoc Bao",
            driverPhone = "0987654321",
            items = listOf(
                CartItem(
                    id = "ci_4",
                    cartId = "cart_demo",
                    menuItemId = "menu_3_1",
                    restaurantId = "res_3",
                    name = "Pizza Burrata Thit Nguoi Parma Ham",
                    price = 290000.0,
                    quantity = 1,
                    imageUrl = "https://images.unsplash.com/photo-1513104890138-7c749659a591?w=500"
                )
            ),
            subtotal = 290000.0,
            deliveryFee = 20000.0,
            taxes = 0.0,
            discount = 20000.0,
            total = 290000.0,
            status = OrderStatus.DELIVERED,
            paymentMethod = PaymentMethod.CARD,
            paymentStatus = PaymentStatus.PAID,
            address = Address(
                id = "addr_1",
                label = "Nha rieng",
                recipientName = "Nguyen Van A",
                phoneNumber = "0909123456",
                streetAddress = "151 Hai Ba Trung, Q.3",
                city = "TP. Ho Chi Minh"
            ),
            orderTime = System.currentTimeMillis() - 2 * 24 * 60 * 60 * 1000L,
            deliveredTime = System.currentTimeMillis() - 2 * 24 * 60 * 60 * 1000L + 35 * 60 * 1000L
        )
    )

    private val ordersFlow = MutableStateFlow<List<Order>>(initialSampleOrders)

    override fun getOrderHistory(): Flow<List<Order>> = ordersFlow

    override fun getActiveOrders(): Flow<List<Order>> =
        ordersFlow.map { orders -> orders.filter { it.status in ACTIVE_STATUSES } }

    override fun getCompletedOrders(): Flow<List<Order>> =
        ordersFlow.map { orders -> orders.filter { it.status in COMPLETED_STATUSES } }

    override fun getOrderStream(orderId: String): Flow<Order?> = flow {
        val initialOrder = ordersFlow.value.find { it.id == orderId } ?: buildFallbackOrder(orderId)
        emit(initialOrder)

        val socket = socketClient
        if (socket != null) {
            socket.observeLiveTracking(orderId).collect { event ->
                val statusEnum = when (event.status.uppercase()) {
                    "PREPARING" -> OrderStatus.PREPARING
                    "READY" -> OrderStatus.READY
                    "ON_THE_WAY" -> OrderStatus.ON_THE_WAY
                    "DELIVERED" -> OrderStatus.DELIVERED
                    "CANCELED" -> OrderStatus.CANCELED
                    else -> initialOrder.status
                }

                val updatedOrder = (ordersFlow.value.find { it.id == orderId } ?: initialOrder).copy(
                    status = statusEnum,
                    driverId = event.driverId.ifBlank { initialOrder.driverId },
                    driverName = event.driverName.ifBlank { initialOrder.driverName },
                    driverPhone = event.driverPhone.ifBlank { initialOrder.driverPhone }
                )

                ordersFlow.update { current ->
                    listOf(updatedOrder) + current.filter { it.id != orderId }
                }
                emit(updatedOrder)
            }
        } else {
            ordersFlow.collect { list ->
                emit(list.find { it.id == orderId } ?: initialOrder)
            }
        }
    }

    override suspend fun createOrder(order: Order): Order {
        val requestDto = CreateOrderRequestDto(
            restaurantId = order.restaurantId,
            restaurantName = order.restaurantName,
            items = order.items.map { it.asOrderItemDto() },
            subtotal = order.subtotal,
            deliveryFee = order.deliveryFee,
            discount = order.discount,
            total = order.total,
            paymentMethod = order.paymentMethod.name,
            deliveryAddressText = order.address.streetAddress,
            recipientName = order.address.recipientName,
            recipientPhone = order.address.phoneNumber
        )

        val created = try {
            val response = apiService.createOrder(requestDto)
            response.data?.asExternalModel() ?: order.copy(
                id = if (order.id.isNotBlank()) order.id else "ord_${System.currentTimeMillis()}",
                status = OrderStatus.CONFIRMED,
                driverName = "Nguyễn Văn Hùng",
                driverPhone = "0901234567",
                estimatedDeliveryTime = System.currentTimeMillis() + 25 * 60 * 1000L
            )
        } catch (e: Exception) {
            order.copy(
                id = if (order.id.isNotBlank()) order.id else "ord_${System.currentTimeMillis()}",
                status = OrderStatus.CONFIRMED,
                driverName = "Nguyễn Văn Hùng",
                driverPhone = "0901234567",
                estimatedDeliveryTime = System.currentTimeMillis() + 25 * 60 * 1000L
            )
        }
        ordersFlow.update { current ->
            listOf(created) + current.filter { it.id != created.id }
        }
        return created
    }

    private fun buildFallbackOrder(orderId: String): Order = Order(
        id = orderId,
        restaurantId = "res_1",
        restaurantName = "Cơm Tấm Phúc Lộc Thọ - Lê Văn Việt",
        driverId = "drv_01",
        driverName = "Nguyễn Văn Hùng",
        driverPhone = "0901234567",
        status = OrderStatus.ON_THE_WAY,
        items = listOf(
            CartItem(
                id = "ci_mock",
                cartId = "cart_mock",
                menuItemId = "menu_1_2",
                restaurantId = "res_1",
                name = "Cơm Sườn Bì Chả Đặc Biệt",
                price = 65000.0,
                quantity = 1,
                notes = "Kèm canh rong biển",
                imageUrl = "https://images.unsplash.com/photo-1544025162-d76694265947?w=500"
            )
        ),
        subtotal = 65000.0,
        deliveryFee = 15000.0,
        total = 80000.0,
        paymentMethod = PaymentMethod.CASH,
        paymentStatus = PaymentStatus.PENDING,
        address = Address(
            recipientName = "Nguyễn Văn A",
            phoneNumber = "0909123456",
            streetAddress = "456 Lê Văn Việt, Tăng Nhơn Phú A, TP. Thủ Đức"
        ),
        orderTime = System.currentTimeMillis() - 10 * 60 * 1000L,
        estimatedDeliveryTime = System.currentTimeMillis() + 15 * 60 * 1000L
    )

    override suspend fun getOrderDetail(orderId: String): Order? {
        val existing = ordersFlow.value.find { it.id == orderId }
        if (existing != null) return existing
        return try {
            val response = apiService.getOrderDetail(orderId)
            val remoteOrder = response.data?.asExternalModel()
            if (remoteOrder != null) {
                ordersFlow.update { current -> listOf(remoteOrder) + current.filter { it.id != orderId } }
                remoteOrder
            } else {
                val mockOrder = buildFallbackOrder(orderId)
                ordersFlow.update { current -> listOf(mockOrder) + current }
                mockOrder
            }
        } catch (e: Exception) {
            val mockOrder = buildFallbackOrder(orderId)
            ordersFlow.update { current -> listOf(mockOrder) + current }
            mockOrder
        }
    }

    override suspend fun cancelOrder(orderId: String, reason: String) {
        try {
            apiService.cancelOrder(orderId, CancelOrderRequestDto(reason))
        } catch (_: Exception) {}

        ordersFlow.update { list ->
            list.map {
                if (it.id == orderId) {
                    it.copy(
                        status = OrderStatus.CANCELED,
                        cancellationReason = reason,
                        isCanceled = true
                    )
                } else it
            }
        }
    }
}
