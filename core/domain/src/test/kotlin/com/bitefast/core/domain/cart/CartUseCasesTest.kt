package com.bitefast.core.domain.cart

import com.bitefast.core.domain.repository.CartRepository
import com.bitefast.core.model.CartItem
import com.bitefast.core.model.Customization
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class CartUseCasesTest {

    private val cartRepository: CartRepository = mockk(relaxed = true)
    private lateinit var addToCartUseCase: AddToCartUseCase
    private lateinit var clearCartUseCase: ClearCartUseCase
    private lateinit var calculateCartTotalUseCase: CalculateCartTotalUseCase

    @BeforeEach
    fun setUp() {
        addToCartUseCase = AddToCartUseCase(cartRepository)
        clearCartUseCase = ClearCartUseCase(cartRepository)
        calculateCartTotalUseCase = CalculateCartTotalUseCase()
    }

    @Test
    @DisplayName("AddToCart: successfully adds item when cart is empty")
    fun addToCart_emptyCart_success() = runTest {
        coEvery { cartRepository.getCurrentRestaurantId() } returns null

        val item = createCartItem(restaurantId = "res_1", price = 50000.0, quantity = 2)
        val result = addToCartUseCase(item)

        assertTrue(result is AddToCartResult.Success)
        coVerify(exactly = 1) { cartRepository.addItem(item) }
    }

    @Test
    @DisplayName("AddToCart: successfully adds item when same restaurant")
    fun addToCart_sameRestaurant_success() = runTest {
        coEvery { cartRepository.getCurrentRestaurantId() } returns "res_1"

        val item = createCartItem(restaurantId = "res_1", price = 30000.0, quantity = 1)
        val result = addToCartUseCase(item)

        assertTrue(result is AddToCartResult.Success)
        coVerify(exactly = 1) { cartRepository.addItem(item) }
    }

    @Test
    @DisplayName("AddToCart: returns Conflict when item is from different restaurant")
    fun addToCart_differentRestaurant_returnsConflict() = runTest {
        coEvery { cartRepository.getCurrentRestaurantId() } returns "res_1"

        val item = createCartItem(restaurantId = "res_2", price = 40000.0, quantity = 1)
        val result = addToCartUseCase(item)

        assertTrue(result is AddToCartResult.Conflict)
        val conflict = result as AddToCartResult.Conflict
        assertEquals("res_1", conflict.currentRestaurantId)
        assertEquals("res_2", conflict.newRestaurantId)
        coVerify(exactly = 0) { cartRepository.addItem(any()) }
    }

    @Test
    @DisplayName("ClearCart: calls cartRepository.clearCart()")
    fun clearCart_callsRepository() = runTest {
        clearCartUseCase()
        coVerify(exactly = 1) { cartRepository.clearCart() }
    }

    @Test
    @DisplayName("CalculateCartTotal: empty items returns all zero values")
    fun calculateTotal_emptyItems_returnsZeros() {
        val calculation = calculateCartTotalUseCase(emptyList())

        assertEquals(0.0, calculation.subtotal)
        assertEquals(0.0, calculation.deliveryFee)
        assertEquals(0.0, calculation.platformFee)
        assertEquals(0.0, calculation.taxVat)
        assertEquals(0.0, calculation.discount)
        assertEquals(0.0, calculation.total)
        assertEquals(0, calculation.itemCount)
        assertFalse(calculation.isFreeDeliveryQualified)
    }

    @Test
    @DisplayName("CalculateCartTotal: subtotal includes item customizations")
    fun calculateTotal_withCustomizations_calculatesCorrectSubtotal() {
        val item = createCartItem(
            price = 50000.0,
            quantity = 2,
            customizations = listOf(
                Customization(name = "Egg", price = 7000.0),
                Customization(name = "Pork", price = 10000.0)
            )
        )
        // (50000 + 7000 + 10000) * 2 = 134000
        val calculation = calculateCartTotalUseCase(listOf(item), distanceKm = 1.5f)

        assertEquals(134000.0, calculation.subtotal)
        assertEquals(2, calculation.itemCount)
        assertEquals(16000.0, calculation.deliveryFee) // <= 2km base fee
        assertEquals(2000.0, calculation.platformFee)
        assertEquals(134000.0 * 0.08, calculation.taxVat)
        assertFalse(calculation.isFreeDeliveryQualified)
    }

    @Test
    @DisplayName("CalculateCartTotal: qualifies for free delivery when subtotal >= 150,000 VND")
    fun calculateTotal_subtotalOverThreshold_freeDelivery() {
        val item = createCartItem(price = 160000.0, quantity = 1)
        val calculation = calculateCartTotalUseCase(listOf(item), distanceKm = 5.0f)

        assertEquals(160000.0, calculation.subtotal)
        assertEquals(0.0, calculation.deliveryFee)
        assertTrue(calculation.isFreeDeliveryQualified)
        assertEquals(160000.0 + 0.0 + 2000.0 + (160000.0 * 0.08), calculation.total)
    }

    @Test
    @DisplayName("CalculateCartTotal: charges extra delivery fee for distance > 2km")
    fun calculateTotal_extraDistance_chargesExtraFee() {
        val item = createCartItem(price = 80000.0, quantity = 1)
        // 4.2 km -> extra 2.2 km -> ceil is 3 extra km -> 16000 + 3 * 5000 = 31000
        val calculation = calculateCartTotalUseCase(listOf(item), distanceKm = 4.2f)

        assertEquals(31000.0, calculation.deliveryFee)
    }

    @Test
    @DisplayName("CalculateCartTotal: applies voucher discount and clamps total at zero")
    fun calculateTotal_voucherDiscount_appliesCorrectly() {
        val item = createCartItem(price = 50000.0, quantity = 1)
        val calculation = calculateCartTotalUseCase(
            items = listOf(item),
            distanceKm = 1.0f,
            voucherDiscount = 20000.0
        )

        assertEquals(20000.0, calculation.discount)
        val expected = (50000.0 + 16000.0 + 2000.0 + (50000.0 * 0.08)) - 20000.0
        assertEquals(expected, calculation.total)
    }

    private fun createCartItem(
        restaurantId: String = "res_default",
        price: Double = 50000.0,
        quantity: Int = 1,
        customizations: List<Customization> = emptyList()
    ) = CartItem(
        id = "item_test",
        cartId = "cart_1",
        menuItemId = "menu_1",
        restaurantId = restaurantId,
        name = "Test Item",
        price = price,
        quantity = quantity,
        customizations = customizations
    )
}
