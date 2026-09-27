package com.bitefast.core.domain.order

import app.cash.turbine.test
import com.bitefast.core.domain.repository.OrderRepository
import com.bitefast.core.model.Order
import com.bitefast.core.model.OrderStatus
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("GetOrderHistoryUseCase")
class GetOrderHistoryUseCaseTest {

    @MockK lateinit var orderRepository: OrderRepository
    private lateinit var useCase: GetOrderHistoryUseCase

    private val activeOrders = listOf(
        Order(id = "o1", status = OrderStatus.PENDING, orderTime = 3000L),
        Order(id = "o2", status = OrderStatus.ON_THE_WAY, orderTime = 1000L),
        Order(id = "o3", status = OrderStatus.PREPARING, orderTime = 2000L),
    )

    private val completedOrders = listOf(
        Order(id = "o4", status = OrderStatus.DELIVERED, orderTime = 5000L),
        Order(id = "o5", status = OrderStatus.CANCELED, orderTime = 4000L),
    )

    private val allOrders = activeOrders + completedOrders

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        useCase = GetOrderHistoryUseCase(orderRepository)
    }

    @Test
    @DisplayName("invoke() trả về toàn bộ đơn hàng từ repository")
    fun `invoke returns all orders`() = runTest {
        every { orderRepository.getOrderHistory() } returns flowOf(allOrders)

        useCase().test {
            val result = awaitItem()
            assertEquals(5, result.size)
            awaitComplete()
        }
    }

    @Test
    @DisplayName("categorized() phân loại đúng active vs completed")
    fun `categorized splits orders correctly`() = runTest {
        every { orderRepository.getOrderHistory() } returns flowOf(allOrders)

        useCase.categorized().test {
            val result = awaitItem()
            assertEquals(3, result.active.size, "Phải có 3 đơn đang hoạt động")
            assertEquals(2, result.completed.size, "Phải có 2 đơn đã hoàn thành")
            awaitComplete()
        }
    }

    @Test
    @DisplayName("categorized() active sắp xếp theo orderTime giảm dần")
    fun `active orders sorted descending by orderTime`() = runTest {
        every { orderRepository.getOrderHistory() } returns flowOf(allOrders)

        useCase.categorized().test {
            val result = awaitItem()
            val activeTimes = result.active.map { it.orderTime }
            assertEquals(activeTimes.sortedDescending(), activeTimes)
            awaitComplete()
        }
    }

    @Test
    @DisplayName("categorized() completed sắp xếp theo orderTime giảm dần")
    fun `completed orders sorted descending by orderTime`() = runTest {
        every { orderRepository.getOrderHistory() } returns flowOf(allOrders)

        useCase.categorized().test {
            val result = awaitItem()
            val completedTimes = result.completed.map { it.orderTime }
            assertEquals(completedTimes.sortedDescending(), completedTimes)
            awaitComplete()
        }
    }

    @Test
    @DisplayName("categorized() với danh sách rỗng trả về hai nhóm rỗng")
    fun `empty history returns empty categories`() = runTest {
        every { orderRepository.getOrderHistory() } returns flowOf(emptyList())

        useCase.categorized().test {
            val result = awaitItem()
            assertTrue(result.active.isEmpty())
            assertTrue(result.completed.isEmpty())
            awaitComplete()
        }
    }

    @Test
    @DisplayName("DELIVERED và CANCELED đều nằm trong completed")
    fun `both DELIVERED and CANCELED are in completed group`() = runTest {
        val orders = listOf(
            Order(id = "d1", status = OrderStatus.DELIVERED),
            Order(id = "d2", status = OrderStatus.CANCELED),
        )
        every { orderRepository.getOrderHistory() } returns flowOf(orders)

        useCase.categorized().test {
            val result = awaitItem()
            val ids = result.completed.map { it.id }
            assertTrue("d1" in ids)
            assertTrue("d2" in ids)
            assertTrue(result.active.isEmpty())
            awaitComplete()
        }
    }
}
