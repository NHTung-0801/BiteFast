package com.bitefast.core.domain.order

import com.bitefast.core.domain.cart.ClearCartUseCase
import com.bitefast.core.domain.repository.CartRepository
import com.bitefast.core.domain.repository.RestaurantRepository
import com.bitefast.core.model.*
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("SmartReOrderUseCase")
class SmartReOrderUseCaseTest {

    @MockK lateinit var restaurantRepository: RestaurantRepository
    @MockK lateinit var cartRepository: CartRepository
    @MockK lateinit var clearCartUseCase: ClearCartUseCase

    private lateinit var useCase: SmartReOrderUseCase

    private val openRestaurant = Restaurant(
        id = "rest-1",
        name = "Pho Ha Noi",
        isOpen = true,
    )

    private val availableMenuItem = MenuItem(
        id = "menu-1",
        restaurantId = "rest-1",
        name = "Pho Bo",
        isAvailable = true,
    )

    private val unavailableMenuItem = MenuItem(
        id = "menu-2",
        restaurantId = "rest-1",
        name = "Bun Bo",
        isAvailable = false,
    )

    private val cartItemAvailable = CartItem(
        id = "ci-1", menuItemId = "menu-1", restaurantId = "rest-1",
        name = "Pho Bo", price = 50_000.0, quantity = 2,
    )

    private val cartItemUnavailable = CartItem(
        id = "ci-2", menuItemId = "menu-2", restaurantId = "rest-1",
        name = "Bun Bo", price = 45_000.0, quantity = 1,
    )

    private val sampleOrder = Order(
        id = "order-1",
        restaurantId = "rest-1",
        restaurantName = "Pho Ha Noi",
        items = listOf(cartItemAvailable),
    )

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        useCase = SmartReOrderUseCase(restaurantRepository, cartRepository, clearCartUseCase)
    }

    @Nested
    @DisplayName("Đơn rỗng")
    inner class EmptyOrder {
        @Test
        fun `trả về EmptyOrder khi không có món`() = runTest {
            val result = useCase(sampleOrder.copy(items = emptyList()))
            assertTrue(result is SmartReOrderResult.EmptyOrder)
        }
    }

    @Nested
    @DisplayName("Nhà hàng đóng cửa")
    inner class ClosedRestaurant {
        @Test
        fun `trả về RestaurantClosed khi nhà hàng đóng`() = runTest {
            coEvery { restaurantRepository.getRestaurantDetail(any()) } returns openRestaurant.copy(isOpen = false)

            val result = useCase(sampleOrder)

            assertTrue(result is SmartReOrderResult.RestaurantClosed)
        }

        @Test
        fun `trả về RestaurantClosed khi không tìm thấy nhà hàng`() = runTest {
            coEvery { restaurantRepository.getRestaurantDetail(any()) } throws RuntimeException("Not found")

            val result = useCase(sampleOrder)

            assertTrue(result is SmartReOrderResult.RestaurantClosed)
        }
    }

    @Nested
    @DisplayName("Món hết hàng")
    inner class ItemsUnavailable {
        @Test
        fun `trả về ItemsUnavailable khi có món hết hàng`() = runTest {
            coEvery { restaurantRepository.getRestaurantDetail(any()) } returns openRestaurant
            coEvery { restaurantRepository.getRestaurantMenu(any()) } returns listOf(availableMenuItem, unavailableMenuItem)

            val orderWithBothItems = sampleOrder.copy(
                items = listOf(cartItemAvailable, cartItemUnavailable)
            )
            val result = useCase(orderWithBothItems)

            assertTrue(result is SmartReOrderResult.ItemsUnavailable)
            val typed = result as SmartReOrderResult.ItemsUnavailable
            assertEquals(1, typed.unavailableItemNames.size)
            assertEquals("Bun Bo", typed.unavailableItemNames.first())
            assertEquals(1, typed.availableItems.size)
        }
    }

    @Nested
    @DisplayName("Xung đột giỏ hàng")
    inner class CartConflict {
        @Test
        fun `trả về CartConflict khi giỏ đang có đồ từ nhà hàng khác`() = runTest {
            coEvery { restaurantRepository.getRestaurantDetail(any()) } returns openRestaurant
            coEvery { restaurantRepository.getRestaurantMenu(any()) } returns listOf(availableMenuItem)
            coEvery { cartRepository.getCurrentRestaurantId() } returns "rest-OTHER"

            val result = useCase(sampleOrder)

            assertTrue(result is SmartReOrderResult.CartConflict)
            val typed = result as SmartReOrderResult.CartConflict
            assertEquals("rest-OTHER", typed.currentRestaurantId)
            assertEquals("rest-1", typed.newRestaurantId)
        }
    }

    @Nested
    @DisplayName("Tái đặt hàng thành công")
    inner class Success {
        @Test
        fun `trả về Success và thêm đúng số lượng món vào giỏ`() = runTest {
            coEvery { restaurantRepository.getRestaurantDetail(any()) } returns openRestaurant
            coEvery { restaurantRepository.getRestaurantMenu(any()) } returns listOf(availableMenuItem)
            coEvery { cartRepository.getCurrentRestaurantId() } returns null
            coEvery { cartRepository.addItem(any()) } just Runs

            val result = useCase(sampleOrder)

            assertTrue(result is SmartReOrderResult.Success)
            val typed = result as SmartReOrderResult.Success
            assertEquals(2, typed.reorderedItemsCount) // quantity = 2
            assertEquals("Pho Ha Noi", typed.restaurantName)
        }

        @Test
        fun `forceClearCart=true xóa giỏ cũ trước khi thêm món`() = runTest {
            coEvery { restaurantRepository.getRestaurantDetail(any()) } returns openRestaurant
            coEvery { restaurantRepository.getRestaurantMenu(any()) } returns listOf(availableMenuItem)
            coEvery { cartRepository.getCurrentRestaurantId() } returns "rest-OTHER"
            coEvery { clearCartUseCase.invoke() } just Runs
            coEvery { cartRepository.addItem(any()) } just Runs

            val result = useCase(sampleOrder, forceClearCart = true)

            assertTrue(result is SmartReOrderResult.Success)
            coVerify(exactly = 1) { clearCartUseCase.invoke() }
        }
    }
}
