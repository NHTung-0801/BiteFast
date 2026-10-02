package com.bitefast.core.data.repository

import com.bitefast.core.database.dao.CartDao
import com.bitefast.core.database.entity.CartItemEntity
import com.bitefast.core.model.CartItem
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class CartRepositoryTest {

    private lateinit var cartDao: CartDao
    private lateinit var repository: CartRepositoryImpl

    private val sampleEntity = CartItemEntity(
        id = "ci_1",
        cartId = "cart_default",
        menuItemId = "menu_1",
        restaurantId = "res_1",
        name = "Phở Bò",
        price = 60000.0,
        quantity = 2,
        notes = "Nhiều hành",
        imageUrl = "https://example.com/pho.jpg",
        timestamp = System.currentTimeMillis()
    )

    private val sampleItem = CartItem(
        id = "ci_1",
        cartId = "cart_default",
        menuItemId = "menu_1",
        restaurantId = "res_1",
        name = "Phở Bò",
        price = 60000.0,
        quantity = 2,
        notes = "Nhiều hành",
        imageUrl = "https://example.com/pho.jpg"
    )

    @BeforeEach
    fun setUp() {
        cartDao = mockk(relaxed = true)
        repository = CartRepositoryImpl(cartDao)
    }

    @Test
    @DisplayName("getCartItems maps entity flow to domain model flow")
    fun getCartItems_returnsDomainItems() = runTest {
        coEvery { cartDao.getCartItems() } returns flowOf(listOf(sampleEntity))

        val items = repository.getCartItems().first()

        assertEquals(1, items.size)
        assertEquals("ci_1", items[0].id)
        assertEquals("Phở Bò", items[0].name)
        assertEquals(2, items[0].quantity)
    }

    @Test
    @DisplayName("getCurrentRestaurantId delegates to cartDao")
    fun getCurrentRestaurantId_delegatesToDao() = runTest {
        coEvery { cartDao.getCurrentRestaurantId() } returns "res_1"

        val resId = repository.getCurrentRestaurantId()

        assertEquals("res_1", resId)
        coVerify(exactly = 1) { cartDao.getCurrentRestaurantId() }
    }

    @Test
    @DisplayName("addItem converts to entity and calls upsertCartItem")
    fun addItem_callsUpsert() = runTest {
        repository.addItem(sampleItem)

        coVerify(exactly = 1) {
            cartDao.upsertCartItem(match {
                it.id == "ci_1" && it.name == "Phở Bò" && it.quantity == 2
            })
        }
    }

    @Test
    @DisplayName("updateQuantity with 0 or negative deletes item")
    fun updateQuantity_zeroDeletesItem() = runTest {
        repository.updateQuantity("ci_1", 0)

        coVerify(exactly = 1) { cartDao.deleteItemById("ci_1") }
    }

    @Test
    @DisplayName("updateQuantity with positive updates item")
    fun updateQuantity_positiveUpdatesItem() = runTest {
        coEvery { cartDao.getCartItemByMenuItemId("menu_1") } returns sampleEntity

        repository.updateQuantity("menu_1", 5)

        coVerify(exactly = 1) {
            cartDao.updateItem(match {
                it.quantity == 5
            })
        }
    }

    @Test
    @DisplayName("removeItem deletes item by id")
    fun removeItem_deletesById() = runTest {
        repository.removeItem("ci_1")

        coVerify(exactly = 1) { cartDao.deleteItemById("ci_1") }
    }

    @Test
    @DisplayName("clearCart calls clearCart on dao")
    fun clearCart_clearsAll() = runTest {
        repository.clearCart()

        coVerify(exactly = 1) { cartDao.clearCart() }
    }
}
