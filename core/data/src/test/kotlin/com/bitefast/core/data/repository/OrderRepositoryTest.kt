package com.bitefast.core.data.repository

import com.bitefast.core.model.Address
import com.bitefast.core.model.CartItem
import com.bitefast.core.model.Order
import com.bitefast.core.model.OrderStatus
import com.bitefast.core.model.PaymentMethod
import com.bitefast.core.model.PaymentStatus
import com.bitefast.core.network.api.BiteFastApiService
import com.bitefast.core.network.model.ApiResponse
import com.bitefast.core.network.model.OrderResponseDto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class OrderRepositoryTest {

    private lateinit var apiService: BiteFastApiService
    private lateinit var repository: OrderRepositoryImpl

    private val sampleAddress = Address(
        id = "addr_test",
        label = "Nhà",
        recipientName = "Nguyễn Văn Test",
        phoneNumber = "0900000000",
        streetAddress = "123 Đường Test",
        city = "TP. Thủ Đức"
    )

    private val sampleCartItem = CartItem(
        id = "ci_test",
        cartId = "cart_1",
        menuItemId = "menu_1",
        restaurantId = "res_1",
        name = "Cơm Tấm Sườn Bì",
        price = 50000.0,
        quantity = 2
    )

    @BeforeEach
    fun setUp() {
        apiService = mockk(relaxed = true)
        repository = OrderRepositoryImpl(apiService)
    }

    @Test
    @DisplayName("getOrderHistory returns initial list of orders")
    fun getOrderHistory_returnsOrders() = runTest {
        val orders = repository.getOrderHistory().first()
        assertTrue(orders.isNotEmpty())
        assertTrue(orders.any { it.id == "ord_101" })
    }

    @Test
    @DisplayName("getActiveOrders filters orders with active status")
    fun getActiveOrders_filtersActiveOnly() = runTest {
        val activeOrders = repository.getActiveOrders().first()
        assertTrue(activeOrders.all {
            it.status in setOf(
                OrderStatus.PENDING,
                OrderStatus.CONFIRMED,
                OrderStatus.PREPARING,
                OrderStatus.READY,
                OrderStatus.ON_THE_WAY
            )
        })
    }

    @Test
    @DisplayName("getCompletedOrders filters orders with completed status")
    fun getCompletedOrders_filtersCompletedOnly() = runTest {
        val completedOrders = repository.getCompletedOrders().first()
        assertTrue(completedOrders.all {
            it.status in setOf(
                OrderStatus.DELIVERED,
                OrderStatus.CANCELED
            )
        })
    }

    @Test
    @DisplayName("createOrder calls apiService and adds new order to flow")
    fun createOrder_success() = runTest {
        val newOrder = Order(
            id = "ord_new_999",
            restaurantId = "res_1",
            restaurantName = "Cơm Tấm Phúc Lộc Thọ",
            items = listOf(sampleCartItem),
            subtotal = 100000.0,
            deliveryFee = 15000.0,
            discount = 10000.0,
            total = 105000.0,
            status = OrderStatus.CONFIRMED,
            paymentMethod = PaymentMethod.CASH,
            paymentStatus = PaymentStatus.PENDING,
            address = sampleAddress
        )

        val responseDto = OrderResponseDto(
            id = "ord_new_999",
            userId = "usr_123",
            restaurantId = "res_1",
            restaurantName = "Cơm Tấm Phúc Lộc Thọ",
            subtotal = 100000.0,
            deliveryFee = 15000.0,
            total = 105000.0,
            status = "CONFIRMED",
            paymentMethod = "CASH",
            paymentStatus = "PENDING",
            orderTime = System.currentTimeMillis()
        )

        coEvery { apiService.createOrder(any()) } returns ApiResponse(
            success = true,
            data = responseDto
        )

        val result = repository.createOrder(newOrder)

        assertEquals("ord_new_999", result.id)
        coVerify(exactly = 1) { apiService.createOrder(any()) }

        val orders = repository.getOrderHistory().first()
        assertTrue(orders.any { it.id == "ord_new_999" })
    }

    @Test
    @DisplayName("createOrder handles API failure gracefully by creating local order")
    fun createOrder_fallbackOnApiError() = runTest {
        val newOrder = Order(
            id = "ord_fallback_123",
            restaurantId = "res_1",
            restaurantName = "Cơm Tấm",
            items = listOf(sampleCartItem),
            subtotal = 100000.0,
            deliveryFee = 15000.0,
            discount = 0.0,
            total = 115000.0,
            status = OrderStatus.PENDING,
            paymentMethod = PaymentMethod.CASH,
            paymentStatus = PaymentStatus.PENDING,
            address = sampleAddress
        )

        coEvery { apiService.createOrder(any()) } throws RuntimeException("Network timeout")

        val result = repository.createOrder(newOrder)

        assertEquals("ord_fallback_123", result.id)
        assertEquals(OrderStatus.CONFIRMED, result.status)

        val history = repository.getOrderHistory().first()
        assertTrue(history.any { it.id == "ord_fallback_123" })
    }

    @Test
    @DisplayName("cancelOrder updates order status to CANCELED and invokes API")
    fun cancelOrder_cancelsSuccessfully() = runTest {
        repository.cancelOrder("ord_101", "Changed my mind")

        coVerify(exactly = 1) { apiService.cancelOrder("ord_101", any()) }

        val orders = repository.getOrderHistory().first()
        val canceledOrder = orders.find { it.id == "ord_101" }

        assertNotNull(canceledOrder)
        assertEquals(OrderStatus.CANCELED, canceledOrder?.status)
        assertEquals("Changed my mind", canceledOrder?.cancellationReason)
        assertTrue(canceledOrder?.isCanceled == true)
    }

    @Test
    @DisplayName("getOrderStream with socketClient streams live updated order")
    fun getOrderStream_withSocketClient_streamsUpdatedLiveOrder() = runTest {
        val mockSocketClient = io.mockk.mockk<com.bitefast.core.network.websocket.OrderTrackingSocketClient>()
        val liveEvent = com.bitefast.core.network.websocket.OrderLiveTrackingEvent(
            orderId = "ord_101",
            status = "ON_THE_WAY",
            driverName = "Nguyễn Văn Shipper",
            driverPhone = "0987654321",
            driverLat = 10.8520,
            driverLng = 106.7780
        )
        io.mockk.every { mockSocketClient.observeLiveTracking("ord_101") } returns kotlinx.coroutines.flow.flowOf(liveEvent)

        val repoWithSocket = OrderRepositoryImpl(apiService, mockSocketClient)
        val emissions = repoWithSocket.getOrderStream("ord_101").take(2).toList()

        assertTrue(emissions.isNotEmpty())
        val latest = emissions.last()
        assertNotNull(latest)
        assertEquals(OrderStatus.ON_THE_WAY, latest?.status)
        assertEquals("Nguyễn Văn Shipper", latest?.driverName)
        assertEquals("0987654321", latest?.driverPhone)
    }
}
