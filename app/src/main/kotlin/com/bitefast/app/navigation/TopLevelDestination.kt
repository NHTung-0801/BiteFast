package com.bitefast.app.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.ui.graphics.vector.ImageVector
import kotlin.reflect.KClass

/**
 * Bốn tab chính của BottomNavigationBar tương ứng với Type-Safe Destinations.
 */
enum class TopLevelDestination(
    val destination: Any,
    val targetClass: KClass<*>,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val contentDescription: String,
) {
    DISCOVERY(
        destination = DiscoveryDestination,
        targetClass = DiscoveryDestination::class,
        label = "Khám phá",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home,
        contentDescription = "Tab Khám phá - Xem danh sách nhà hàng và món ăn",
    ),
    CART(
        destination = CartDestination,
        targetClass = CartDestination::class,
        label = "Giỏ hàng",
        selectedIcon = Icons.Filled.ShoppingCart,
        unselectedIcon = Icons.Outlined.ShoppingCart,
        contentDescription = "Tab Giỏ hàng",
    ),
    ORDERS(
        destination = OrdersDestination,
        targetClass = OrdersDestination::class,
        label = "Đơn hàng",
        selectedIcon = Icons.Filled.Receipt,
        unselectedIcon = Icons.Outlined.Receipt,
        contentDescription = "Tab Đơn hàng - Lịch sử và đơn đang giao",
    ),
    PROFILE(
        destination = ProfileDestination,
        targetClass = ProfileDestination::class,
        label = "Tài khoản",
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person,
        contentDescription = "Tab Tài khoản - Thông tin cá nhân và cài đặt",
    ),
}
