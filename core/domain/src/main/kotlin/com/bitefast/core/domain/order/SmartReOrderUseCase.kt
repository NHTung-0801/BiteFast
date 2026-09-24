package com.bitefast.core.domain.order

import com.bitefast.core.domain.cart.ClearCartUseCase
import com.bitefast.core.domain.repository.CartRepository
import com.bitefast.core.domain.repository.RestaurantRepository
import com.bitefast.core.model.CartItem
import com.bitefast.core.model.Order
import javax.inject.Inject

sealed interface SmartReOrderResult {
    data class Success(val reorderedItemsCount: Int, val restaurantName: String) : SmartReOrderResult
    data class RestaurantClosed(val restaurantName: String) : SmartReOrderResult
    data class ItemsUnavailable(val unavailableItemNames: List<String>, val availableItems: List<CartItem>) : SmartReOrderResult
    data class CartConflict(val currentRestaurantId: String, val newRestaurantId: String) : SmartReOrderResult
    data class EmptyOrder(val message: String) : SmartReOrderResult
}

class SmartReOrderUseCase @Inject constructor(
    private val restaurantRepository: RestaurantRepository,
    private val cartRepository: CartRepository,
    private val clearCartUseCase: ClearCartUseCase
) {
    suspend operator fun invoke(
        order: Order,
        forceClearCart: Boolean = false
    ): SmartReOrderResult {
        if (order.items.isEmpty()) {
            return SmartReOrderResult.EmptyOrder("Đơn hàng không có món ăn nào để đặt lại.")
        }

        val restaurant = try {
            restaurantRepository.getRestaurantDetail(order.restaurantId)
        } catch (e: Exception) {
            null
        }

        if (restaurant == null || !restaurant.isOpen) {
            val name = restaurant?.name?.takeIf { it.isNotBlank() } ?: order.restaurantName
            return SmartReOrderResult.RestaurantClosed(name)
        }

        val menu = try {
            restaurantRepository.getRestaurantMenu(order.restaurantId)
        } catch (e: Exception) {
            emptyList()
        }

        val (availableItems, unavailableItems) = if (menu.isNotEmpty()) {
            order.items.partition { cartItem ->
                val menuItem = menu.find { it.id == cartItem.menuItemId }
                menuItem != null && menuItem.isAvailable
            }
        } else {
            Pair(order.items, emptyList())
        }

        if (unavailableItems.isNotEmpty() && !forceClearCart) {
            return SmartReOrderResult.ItemsUnavailable(
                unavailableItemNames = unavailableItems.map { it.name },
                availableItems = availableItems
            )
        }

        val currentRestaurantId = cartRepository.getCurrentRestaurantId()
        if (currentRestaurantId != null && currentRestaurantId != order.restaurantId && !forceClearCart) {
            return SmartReOrderResult.CartConflict(
                currentRestaurantId = currentRestaurantId,
                newRestaurantId = order.restaurantId
            )
        }

        if (forceClearCart) {
            clearCartUseCase()
        }

        val itemsToAdd = if (availableItems.isNotEmpty()) availableItems else order.items
        itemsToAdd.forEach { cartRepository.addItem(it) }

        return SmartReOrderResult.Success(
            reorderedItemsCount = itemsToAdd.sumOf { it.quantity },
            restaurantName = restaurant.name
        )
    }
}
