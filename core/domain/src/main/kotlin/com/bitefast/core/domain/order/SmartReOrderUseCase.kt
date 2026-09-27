package com.bitefast.core.domain.order

import com.bitefast.core.domain.cart.ClearCartUseCase
import com.bitefast.core.domain.repository.CartRepository
import com.bitefast.core.domain.repository.RestaurantRepository
import com.bitefast.core.model.CartItem
import com.bitefast.core.model.Order
import javax.inject.Inject

// ─── Result ───────────────────────────────────────────────────────────────────

sealed interface SmartReOrderResult {
    /** Tái đặt hàng thành công — toàn bộ món đã được thêm vào giỏ. */
    data class Success(val reorderedItemsCount: Int, val restaurantName: String) : SmartReOrderResult

    /** Nhà hàng đóng cửa — không thể đặt lại. */
    data class RestaurantClosed(val restaurantName: String) : SmartReOrderResult

    /**
     * Có một số món không còn hàng.
     * - [unavailableItemNames]: tên các món hết hàng.
     * - [availableItems]: các món vẫn còn — người dùng có thể chọn đặt phần còn lại.
     */
    data class ItemsUnavailable(
        val unavailableItemNames: List<String>,
        val availableItems: List<CartItem>,
    ) : SmartReOrderResult

    /**
     * Giỏ hàng hiện tại thuộc nhà hàng khác.
     * UI hiển thị dialog cho phép người dùng "Xóa giỏ & đặt lại" hoặc "Giữ nguyên giỏ".
     */
    data class CartConflict(
        val currentRestaurantId: String,
        val newRestaurantId: String,
    ) : SmartReOrderResult

    /** Đơn hàng gốc không có món ăn nào để đặt lại. */
    data class EmptyOrder(val message: String) : SmartReOrderResult
}

// ─── Use Case ─────────────────────────────────────────────────────────────────

/**
 * Smart Re-Order — Tái đặt hàng thông minh (1 chạm).
 *
 * Luồng xử lý:
 * 1. Kiểm tra đơn gốc có món ăn không.
 * 2. Kiểm tra nhà hàng vẫn mở cửa.
 * 3. So sánh món đơn gốc với thực đơn hiện tại → xác định món còn hàng / hết hàng.
 * 4. Nếu có xung đột giỏ hàng (khác nhà hàng) → yêu cầu xác nhận từ người dùng qua [forceClearCart].
 * 5. Thêm các món còn hàng vào giỏ hàng.
 *
 * @param order Đơn hàng gốc cần tái đặt.
 * @param forceClearCart Khi `true`: bỏ qua cảnh báo xung đột & hết hàng, xóa giỏ cũ rồi thêm món.
 */
class SmartReOrderUseCase @Inject constructor(
    private val restaurantRepository: RestaurantRepository,
    private val cartRepository: CartRepository,
    private val clearCartUseCase: ClearCartUseCase,
) {
    suspend operator fun invoke(
        order: Order,
        forceClearCart: Boolean = false,
    ): SmartReOrderResult {
        // 1. Đơn không có món ăn
        if (order.items.isEmpty()) {
            return SmartReOrderResult.EmptyOrder("Đơn hàng không có món ăn nào để đặt lại.")
        }

        // 2. Kiểm tra nhà hàng còn mở cửa
        val restaurant = runCatching {
            restaurantRepository.getRestaurantDetail(order.restaurantId)
        }.getOrNull()

        if (restaurant == null || !restaurant.isOpen) {
            val name = restaurant?.name?.takeIf { it.isNotBlank() } ?: order.restaurantName
            return SmartReOrderResult.RestaurantClosed(name)
        }

        // 3. Kiểm tra tính khả dụng của từng món
        val menu = runCatching {
            restaurantRepository.getRestaurantMenu(order.restaurantId)
        }.getOrElse { emptyList() }

        val (availableItems, unavailableItems) = if (menu.isNotEmpty()) {
            order.items.partition { cartItem ->
                val menuItem = menu.find { it.id == cartItem.menuItemId }
                menuItem != null && menuItem.isAvailable
            }
        } else {
            // Không lấy được thực đơn → coi như tất cả còn hàng
            Pair(order.items, emptyList())
        }

        // 4. Cảnh báo hết hàng (nếu chưa force)
        if (unavailableItems.isNotEmpty() && !forceClearCart) {
            return SmartReOrderResult.ItemsUnavailable(
                unavailableItemNames = unavailableItems.map { it.name },
                availableItems = availableItems,
            )
        }

        // 5. Xung đột giỏ hàng (khác nhà hàng)
        val currentRestaurantId = cartRepository.getCurrentRestaurantId()
        if (currentRestaurantId != null &&
            currentRestaurantId != order.restaurantId &&
            !forceClearCart
        ) {
            return SmartReOrderResult.CartConflict(
                currentRestaurantId = currentRestaurantId,
                newRestaurantId = order.restaurantId,
            )
        }

        // 6. Xóa giỏ cũ nếu force hoặc có xung đột đã được xác nhận
        if (forceClearCart) {
            clearCartUseCase()
        }

        // 7. Thêm các món còn hàng (hoặc toàn bộ nếu không lấy được menu)
        val itemsToAdd = availableItems.ifEmpty { order.items }
        itemsToAdd.forEach { cartRepository.addItem(it) }

        return SmartReOrderResult.Success(
            reorderedItemsCount = itemsToAdd.sumOf { it.quantity },
            restaurantName = restaurant.name,
        )
    }
}
