package com.bitefast.app.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.bitefast.feature.cart.CartRoute
import com.bitefast.feature.discovery.DiscoveryRoute

sealed class Screen(val route: String, val title: String) {
    data object Discovery : Screen("discovery", "Khám phá")
    data object Cart : Screen("cart", "Giỏ hàng")
}

@Composable
fun BiteFastApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentRoute == Screen.Discovery.route,
                    onClick = {
                        navController.navigate(Screen.Discovery.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Khám phá") },
                    label = { Text(Screen.Discovery.title) }
                )
                NavigationBarItem(
                    selected = currentRoute == Screen.Cart.route,
                    onClick = {
                        navController.navigate(Screen.Cart.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Giỏ hàng") },
                    label = { Text(Screen.Cart.title) }
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Discovery.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Discovery.route) {
                DiscoveryRoute(
                    onNavigateToDetail = { restaurantId ->
                        // navigate to detail
                    }
                )
            }
            composable(Screen.Cart.route) {
                CartRoute(
                    onNavigateToCheckout = {
                        // navigate to checkout
                    }
                )
            }
        }
    }
}
