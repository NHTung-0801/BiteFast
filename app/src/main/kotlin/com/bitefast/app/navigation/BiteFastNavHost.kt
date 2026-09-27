package com.bitefast.app.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.bitefast.feature.auth.component.LoginGateBottomSheet
import com.bitefast.feature.auth.navigation.LOGIN_ROUTE
import com.bitefast.feature.auth.navigation.REGISTER_ROUTE
import com.bitefast.feature.auth.navigation.loginScreen
import com.bitefast.feature.auth.navigation.navigateToLogin
import com.bitefast.feature.auth.navigation.navigateToRegister
import com.bitefast.feature.auth.navigation.registerScreen
import com.bitefast.feature.cart.CartRoute
import com.bitefast.feature.discovery.DiscoveryRoute

sealed class Screen(val route: String, val title: String) {
    data object Discovery : Screen("discovery", "Khám phá")
    data object Cart : Screen("cart", "Giỏ hàng")
    data object Auth : Screen(LOGIN_ROUTE, "Tài khoản")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BiteFastApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    var showLoginGate by remember { mutableStateOf(false) }

    val isTopLevelDestination = currentRoute in listOf(
        Screen.Discovery.route,
        Screen.Cart.route
    )

    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = isTopLevelDestination,
                enter = slideInVertically { it },
                exit = slideOutVertically { it }
            ) {
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
                    NavigationBarItem(
                        selected = currentRoute == LOGIN_ROUTE || currentRoute == REGISTER_ROUTE,
                        onClick = {
                            navController.navigateToLogin()
                        },
                        icon = { Icon(Icons.Default.Person, contentDescription = "Tài khoản") },
                        label = { Text("Tài khoản") }
                    )
                }
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
                        // Triggers Login Gate when guest user tries to check out
                        showLoginGate = true
                    }
                )
            }

            loginScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.Discovery.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            inclusive = false
                        }
                        launchSingleTop = true
                    }
                },
                onNavigateToRegister = {
                    navController.navigateToRegister()
                }
            )

            registerScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.Discovery.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            inclusive = false
                        }
                        launchSingleTop = true
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        if (showLoginGate) {
            LoginGateBottomSheet(
                onDismiss = { showLoginGate = false },
                onNavigateToLogin = {
                    showLoginGate = false
                    navController.navigateToLogin()
                },
                onNavigateToRegister = {
                    showLoginGate = false
                    navController.navigateToRegister()
                },
                cartItemCount = 2
            )
        }
    }
}
