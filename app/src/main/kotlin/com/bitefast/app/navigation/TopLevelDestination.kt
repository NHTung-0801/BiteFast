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

/**
 * Bốn tab chính của BottomNavigationBar.
 */
enum class TopLevelDestination(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val contentDescription: String,
) {
    DISCOVERY(
        route = "discovery",
        label = "Kham pha",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home,
        contentDescription = "Tab Kham pha - Xem danh sach nha hang va mon an",
    ),
    CART(
        route = "cart",
        label = "Gio hang",
        selectedIcon = Icons.Filled.ShoppingCart,
        unselectedIcon = Icons.Outlined.ShoppingCart,
        contentDescription = "Tab Gio hang",
    ),
    ORDERS(
        route = "orders",
        label = "Don hang",
        selectedIcon = Icons.Filled.Receipt,
        unselectedIcon = Icons.Outlined.Receipt,
        contentDescription = "Tab Don hang - Lich su va don dang giao",
    ),
    PROFILE(
        route = "profile",
        label = "Tai khoan",
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person,
        contentDescription = "Tab Tai khoan - Thong tin ca nhan va cai dat",
    ),
}
