package com.bitefast.app.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.bitefast.core.designsystem.theme.OrangePrimary
import com.bitefast.feature.auth.component.LoginGateBottomSheet
import com.bitefast.feature.auth.navigation.loginScreen
import com.bitefast.feature.auth.navigation.navigateToLogin
import com.bitefast.feature.auth.navigation.navigateToRegister
import com.bitefast.feature.auth.navigation.registerScreen
import com.bitefast.feature.cart.CartRoute
import com.bitefast.feature.checkout.CheckoutRoute
import com.bitefast.feature.detail.DetailRoute
import com.bitefast.feature.discovery.DiscoveryRoute
import com.bitefast.feature.order.OrderRoute
import com.bitefast.feature.profile.ProfileRoute
import com.bitefast.feature.rating.RatingRoute
import com.bitefast.feature.tracking.TrackingRoute

// ─── Route constants ──────────────────────────────────────────────────────────
const val DETAIL_ROUTE = "detail/{restaurantId}"
const val CHECKOUT_ROUTE = "checkout"
const val TRACKING_ROUTE = "tracking/{orderId}"
const val RATING_ROUTE = "rating/{orderId}"

/** Route destinations có bottom bar ẩn đi. */
private val ROUTES_WITHOUT_BOTTOM_BAR = setOf(
    "login", "register",
    "checkout",
    "detail/{restaurantId}",
    "tracking/{orderId}",
    "rating/{orderId}",
)

// ─── Top-level destinations ───────────────────────────────────────────────────
private val TOP_LEVEL_ROUTES = TopLevelDestination.entries.map { it.route }.toSet()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BiteFastApp(
    /** Số lượng item trong giỏ — dùng cho badge trên tab Giỏ hàng. */
    cartItemCount: Int = 0,
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    var showLoginGate by remember { mutableStateOf(false) }

    val showBottomBar = currentRoute !in ROUTES_WITHOUT_BOTTOM_BAR

    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically(tween(200)) { it } + fadeIn(tween(200)),
                exit = slideOutVertically(tween(200)) { it } + fadeOut(tween(200)),
            ) {
                BiteFastBottomBar(
                    currentRoute = currentRoute,
                    cartItemCount = cartItemCount,
                    onDestinationSelected = { destination ->
                        navController.navigate(destination.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { paddingValues ->

        NavHost(
            navController = navController,
            startDestination = TopLevelDestination.DISCOVERY.route,
            modifier = Modifier.padding(paddingValues),
            enterTransition = { fadeIn(tween(220)) },
            exitTransition = { fadeOut(tween(220)) },
            popEnterTransition = { fadeIn(tween(220)) },
            popExitTransition = { fadeOut(tween(220)) },
        ) {

            // ── Tab 1: Discovery ──────────────────────────────────────────────
            composable(TopLevelDestination.DISCOVERY.route) {
                DiscoveryRoute(
                    onNavigateToDetail = { restaurantId ->
                        navController.navigate("detail/$restaurantId")
                    }
                )
            }

            // ── Tab 2: Cart ───────────────────────────────────────────────────
            composable(TopLevelDestination.CART.route) {
                CartRoute(
                    onNavigateToCheckout = {
                        showLoginGate = true      // Triggers LoginGate for guests
                    }
                )
            }

            // ── Tab 3: Orders ─────────────────────────────────────────────────
            composable(TopLevelDestination.ORDERS.route) {
                OrderRoute(
                    onNavigateToTracking = { orderId ->
                        navController.navigate("tracking/$orderId")
                    },
                    onNavigateToDetail = { restaurantId ->
                        navController.navigate("detail/$restaurantId")
                    }
                )
            }

            // ── Tab 4: Profile ────────────────────────────────────────────────
            composable(TopLevelDestination.PROFILE.route) {
                ProfileRoute(
                    onNavigateToLogin = {
                        navController.navigateToLogin()
                    },
                    onNavigateToOrderHistory = {
                        navController.navigate(TopLevelDestination.ORDERS.route) {
                            launchSingleTop = true
                        }
                    }
                )
            }

            // ── Detail ────────────────────────────────────────────────────────
            composable(
                route = DETAIL_ROUTE,
                arguments = listOf(navArgument("restaurantId") { type = NavType.StringType })
            ) { backStackEntry ->
                val restaurantId = backStackEntry.arguments?.getString("restaurantId") ?: ""
                DetailRoute(restaurantId = restaurantId)
            }

            // ── Checkout ──────────────────────────────────────────────────────
            composable(CHECKOUT_ROUTE) {
                CheckoutRoute(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToTracking = { orderId ->
                        navController.navigate("tracking/orderId") {
                            popUpTo(CHECKOUT_ROUTE) { inclusive = true }
                        }
                    }
                )
            }

            // ── Tracking ──────────────────────────────────────────────────────
            composable(
                route = TRACKING_ROUTE,
                arguments = listOf(navArgument("orderId") { type = NavType.StringType })
            ) { backStackEntry ->
                val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
                TrackingRoute(orderId = orderId)
            }

            // ── Rating ────────────────────────────────────────────────────────
            composable(
                route = RATING_ROUTE,
                arguments = listOf(navArgument("orderId") { type = NavType.StringType })
            ) { backStackEntry ->
                val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
                RatingRoute(orderId = orderId)
            }

            // ── Auth ──────────────────────────────────────────────────────────
            loginScreen(
                onNavigateToHome = {
                    navController.navigate(TopLevelDestination.DISCOVERY.route) {
                        popUpTo(navController.graph.findStartDestination().id) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onNavigateToRegister = { navController.navigateToRegister() }
            )

            registerScreen(
                onNavigateToHome = {
                    navController.navigate(TopLevelDestination.DISCOVERY.route) {
                        popUpTo(navController.graph.findStartDestination().id) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }

        // ── Login Gate Bottom Sheet ───────────────────────────────────────────
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
                cartItemCount = cartItemCount,
            )
        }
    }
}

// ─── Bottom Navigation Bar ────────────────────────────────────────────────────

@Composable
private fun BiteFastBottomBar(
    currentRoute: String?,
    cartItemCount: Int,
    onDestinationSelected: (TopLevelDestination) -> Unit,
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = androidx.compose.ui.unit.Dp(3f),
    ) {
        TopLevelDestination.entries.forEach { destination ->
            val isSelected = currentRoute == destination.route
            NavigationBarItem(
                selected = isSelected,
                onClick = { onDestinationSelected(destination) },
                icon = {
                    // Badge số lượng giỏ hàng
                    if (destination == TopLevelDestination.CART && cartItemCount > 0) {
                        BadgedBox(
                            badge = {
                                Badge {
                                    Text(
                                        text = if (cartItemCount > 99) "99+" else cartItemCount.toString(),
                                        style = MaterialTheme.typography.labelSmall,
                                    )
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (isSelected) destination.selectedIcon else destination.unselectedIcon,
                                contentDescription = null,
                            )
                        }
                    } else {
                        Icon(
                            imageVector = if (isSelected) destination.selectedIcon else destination.unselectedIcon,
                            contentDescription = null,
                        )
                    }
                },
                label = { Text(destination.label, style = MaterialTheme.typography.labelSmall) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = OrangePrimary,
                    selectedTextColor = OrangePrimary,
                    indicatorColor = OrangePrimary.copy(alpha = 0.12f),
                ),
                modifier = Modifier.semantics {
                    contentDescription = destination.contentDescription
                },
            )
        }
    }
}
