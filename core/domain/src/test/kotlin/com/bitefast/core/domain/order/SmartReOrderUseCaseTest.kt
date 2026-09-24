package com.bitefast.core.domain.order

import com.bitefast.core.domain.cart.ClearCartUseCase
import com.bitefast.core.domain.repository.CartRepository
import com.bitefast.core.domain.repository.RestaurantRepository
import com.bitefast.core.model.CartItem
import com.bitefast.core.model.MenuItem
import com.bitefast.core.model.Order
import com.bitefast.core.model.Restaurant
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class SmartReOrderUseCaseTest {

    private val restaurantRepository: RestaurantRepository = mockk()
    private val cartRepository: CartRepository = mockk(relaxed = true)
    private val clearCartUseCase: ClearCartUseCase = mockk(relaxed = true)

    private lateinit var smartReOrderUseCase: SmartReOrderUseCase

    @BeforeEach
    fun setUp() {
        smartReOrderUseCase = SmartReOrderUseCase(
            restaurantRepository = restaurantRepository,
            cartRepository = cartRepository,
            clearCartUseCase = clearCartUseCase
        )
    }

    @Test
    @DisplayName("SmartReOrder: returns EmptyOrder when order has no items")
    fun reorder_emptyItems_returnsEmptyOrder() = runTest {
        val order = Order(id = "order_1", restaurantId = "res_1", items = emptyList())
        val result = smartReOrderUseCase(order)

        assertTrue(result is SmartReOrderResult.EmptyOrder)
    }

    @Test
    @DisplayName("SmartReOrder: returns RestaurantClosed when restaurant is not open")
    fun reorder_restaurantClosed_returnsRestaurantClosed() = runTest {
        val order = createOrder(restaurantId = "res_1", restaurantName = "Cơm Tấm")
        val restaurant = Restaurant(id = "res_1", name = "Cơm Tấm", isOpen = false)

        coEvery { restaurantRepository.getRestaurantDetail("res_1") } returns restaurant

        val result = smartReOrderUseCase(order)

        assertTrue(result is SmartReOrderResult.RestaurantClosed)
        assertEquals("Cơm Tấm", (result as SmartReOrderResult.RestaurantClosed).restaurantName)
        coVerify(exactly = 0) { cartRepository.addItem(any()) }
    }

    @Test
    @DisplayName("SmartReOrder: returns ItemsUnavailable when some items are out of stock")
    fun reorder_itemsUnavailable_returnsUnavailable() = runTest {
        val item1 = CartItem(id = "item_1", menuItemId = "m_1", name = "Cơm Sườn", restaurantId = "res_1", quantity = 1)
        val item2 = CartItem(id = "item_2", menuItemId = "m_2", name = "Canh Khổ Qua", restaurantId = "res_1", quantity = 1)
        val order = createOrder(restaurantId = "res_1", items = listOf(item1, item2))

        val restaurant = Restaurant(id = "res_1", name = "Cơm Tấm", isOpen = true)
        val menu = listOf(
            MenuItem(id = "m_1", restaurantId = "res_1", name = "Cơm Sườn", isAvailable = true),
            MenuItem(id = "m_2", restaurantId = "res_1", name = "Canh Khổ Qua", isAvailable = false)
        )

        coEvery { restaurantRepository.getRestaurantDetail("res_1") } returns restaurant
        coEvery { restaurantRepository.getRestaurantMenu("res_1") } returns menu

        val result = smartReOrderUseCase(order, forceClearCart = false)

        assertTrue(result is SmartReOrderResult.ItemsUnavailable)
        val unavailable = result as SmartReOrderResult.ItemsUnavailable
        assertEquals(listOf("Canh Khổ Qua"), unavailable.unavailableItemNames)
        assertEquals(1, unavailable.availableItems.size)
        assertEquals("Cơm Sườn", unavailable.availableItems.first().name)
    }

    @Test
    @DisplayName("SmartReOrder: returns CartConflict when cart has items from different restaurant")
    fun reorder_cartConflict_returnsConflict() = runTest {
        val item = CartItem(id = "item_1", menuItemId = "m_1", name = "Cơm Sườn", restaurantId = "res_1", quantity = 1)
        val order = createOrder(restaurantId = "res_1", items = listOf(item))

        val restaurant = Restaurant(id = "res_1", name = "Cơm Tấm", isOpen = true)
        val menu = listOf(MenuItem(id = "m_1", restaurantId = "res_1", name = "Cơm Sườn", isAvailable = true))

        coEvery { restaurantRepository.getRestaurantDetail("res_1") } returns restaurant
        coEvery { restaurantRepository.getRestaurantMenu("res_1") } returns menu
        coEvery { cartRepository.getCurrentRestaurantId() } returns "res_other"

        val result = smartReOrderUseCase(order, forceClearCart = false)

        assertTrue(result is SmartReOrderResult.CartConflict)
        val conflict = result as SmartReOrderResult.CartConflict
        assertEquals("res_other", conflict.currentRestaurantId)
        assertEquals("res_1", conflict.newRestaurantId)
    }

    @Test
    @DisplayName("SmartReOrder: clears cart and adds items when forceClearCart is true")
    fun reorder_forceClearCart_clearsAndAddsItems() = runTest {
        val item = CartItem(id = "item_1", menuItemId = "m_1", name = "Cơm Sườn", restaurantId = "res_1", quantity = 2)
        val order = createOrder(restaurantId = "res_1", items = listOf(item))

        val restaurant = Restaurant(id = "res_1", name = "Cơm Tấm", isOpen = true)
        val menu = listOf(MenuItem(id = "m_1", restaurantId = "res_1", name = "Cơm Sườn", isAvailable = true))

        coEvery { restaurantRepository.getRestaurantDetail("res_1") } returns restaurant
        coEvery { restaurantRepository.getRestaurantMenu("res_1") } returns menu
        coEvery { cartRepository.getCurrentRestaurantId() } returns "res_other"

        val result = smartReOrderUseCase(order, forceClearCart = true)

        assertTrue(result is SmartReOrderResult.Success)
        val success = result as SmartReOrderResult.Success
        assertEquals(2, success.reorderedItemsCount)
        assertEquals("Cơm Tấm", success.restaurantName)

        coVerify(exactly = 1) { clearCartUseCase() }
        coVerify(exactly = 1) { cartRepository.addItem(item) }
    }

    private fun createOrder(
        restaurantId: String = "res_1",
        restaurantName: String = "Quán Ngon",
        items: List<CartItem> = listOf(
            CartItem(id = "item_1", menuItemId = "m_1", name = "Món 1", restaurantId = restaurantId, quantity = 1)
        )
    ) = Order(
        id = "order_123",
        restaurantId = restaurantId,
        restaurantName = restaurantName,
        items = items
    )
}
