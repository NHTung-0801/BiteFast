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
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bitefast.core.designsystem.theme.OrangePrimary
import com.bitefast.feature.auth.LoginRoute
import com.bitefast.feature.auth.RegisterRoute
import com.bitefast.feature.auth.component.LoginGateBottomSheet
import com.bitefast.feature.cart.CartRoute
import com.bitefast.feature.checkout.CheckoutRoute
import com.bitefast.feature.detail.DetailRoute
import com.bitefast.feature.discovery.DiscoveryRoute
import com.bitefast.feature.notification.NotificationRoute
import com.bitefast.feature.order.OrderRoute
import com.bitefast.feature.profile.ProfileRoute
import com.bitefast.feature.profile.edit.EditProfileRoute
import com.bitefast.feature.rating.RatingRoute
import com.bitefast.feature.tracking.TrackingRoute
import com.bitefast.feature.voucher.VoucherWalletRoute

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BiteFastApp(
    /** Số lượng item trong giỏ — dùng cho badge trên tab Giỏ hàng. */
    cartItemCount: Int = 0,
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    var showLoginGate by remember { mutableStateOf(false) }

    // Chỉ hiển thị BottomBar tại 4 TopLevelDestinations (Khám phá, Giỏ hàng, Đơn hàng, Tài khoản)
    val isTopLevelDestination = TopLevelDestination.entries.any { destination ->
        currentDestination?.hierarchy?.any { it.hasRoute(destination.targetClass) } == true
    }

    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = isTopLevelDestination,
                enter = slideInVertically(tween(200)) { it } + fadeIn(tween(200)),
                exit = slideOutVertically(tween(200)) { it } + fadeOut(tween(200)),
            ) {
                BiteFastBottomBar(
                    navBackStackEntry = navBackStackEntry,
                    cartItemCount = cartItemCount,
                    onDestinationSelected = { destination ->
                        val popped = when (destination) {
                            TopLevelDestination.DISCOVERY -> navController.popBackStack<DiscoveryDestination>(inclusive = false)
                            TopLevelDestination.CART -> navController.popBackStack<CartDestination>(inclusive = false)
                            TopLevelDestination.ORDERS -> navController.popBackStack<OrdersDestination>(inclusive = false)
                            TopLevelDestination.PROFILE -> navController.popBackStack<ProfileDestination>(inclusive = false)
                        }
                        if (!popped) {
                            navController.navigate(destination.destination) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    ) { paddingValues ->

        NavHost(
            navController = navController,
            startDestination = DiscoveryDestination,
            modifier = Modifier.padding(paddingValues),
            enterTransition = { fadeIn(tween(220)) },
            exitTransition = { fadeOut(tween(220)) },
            popEnterTransition = { fadeIn(tween(220)) },
            popExitTransition = { fadeOut(tween(220)) },
        ) {

            // ── Tab 1: Discovery (Khám phá) ──────────────────────────────────
            composable<DiscoveryDestination> {
                DiscoveryRoute(
                    onNavigateToDetail = { restaurantId ->
                        navController.navigate(RestaurantDetailDestination(restaurantId))
                    },
                    onNavigateToSearch = {
                        navController.navigate(SearchDestination)
                    }
                )
            }

            // ── Tìm kiếm Chuyên sâu ──────────────────────────────────────────
            composable<SearchDestination> {
                com.bitefast.feature.discovery.search.SearchRoute(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToDetail = { restaurantId ->
                        navController.navigate(RestaurantDetailDestination(restaurantId))
                    }
                )
            }

            // ── Tab 2: Cart (Giỏ hàng) ───────────────────────────────────────
            composable<CartDestination> {
                CartRoute(
                    onNavigateToCheckout = {
                        navController.navigate(CheckoutDestination)
                    },
                    onNavigateBack = {
                        if (!navController.popBackStack()) {
                            navController.navigate(DiscoveryDestination) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }

            // ── Tab 3: Orders (Đơn hàng) ─────────────────────────────────────
            composable<OrdersDestination> {
                OrderRoute(
                    onNavigateToTracking = { orderId ->
                        navController.navigate(TrackingDestination(orderId))
                    },
                    onNavigateToDetail = { restaurantId ->
                        navController.navigate(RestaurantDetailDestination(restaurantId))
                    },
                    onNavigateToOrderDetail = { orderId ->
                        navController.navigate(OrderDetailDestination(orderId))
                    },
                    onNavigateBack = {
                        if (!navController.popBackStack()) {
                            navController.navigate(DiscoveryDestination) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }

            // ── Chi tiết Đơn hàng & Hóa đơn ──────────────────────────────────
            composable<OrderDetailDestination> { backStackEntry ->
                val destination: OrderDetailDestination = backStackEntry.toRoute()
                com.bitefast.feature.order.detail.OrderDetailRoute(
                    orderId = destination.orderId,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToTracking = { orderId ->
                        navController.navigate(TrackingDestination(orderId))
                    },
                    onNavigateToRating = { orderId ->
                        navController.navigate(RatingDestination(orderId))
                    },
                    onNavigateToDetail = { restaurantId ->
                        navController.navigate(RestaurantDetailDestination(restaurantId))
                    },
                    onNavigateToCart = {
                        navController.navigate(CartDestination) {
                            launchSingleTop = true
                        }
                    }
                )
            }

            // ── Tab 4: Profile (Tài khoản) ───────────────────────────────────
            composable<ProfileDestination> { backStackEntry ->
                // Lắng nghe kết quả từ EditProfile: tự động ReloadUser khi quay về
                val profileUpdated = backStackEntry.savedStateHandle
                    .getStateFlow("profile_updated", false)
                    .collectAsStateWithLifecycle()

                ProfileRoute(
                    profileUpdatedSignal = profileUpdated.value,
                    onProfileReloadConsumed = {
                        backStackEntry.savedStateHandle["profile_updated"] = false
                    },
                    onNavigateToLogin = {
                        navController.navigate(LoginDestination)
                    },
                    onNavigateToOrderHistory = {
                        navController.navigate(OrdersDestination) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToVoucherWallet = {
                        navController.navigate(VoucherWalletDestination)
                    },
                    onNavigateToAddresses = {
                        navController.navigate(AddressListDestination)
                    },
                    onNavigateToFavorites = {
                        navController.navigate(WishlistDestination)
                    },
                    onNavigateToEditProfile = {
                        navController.navigate(EditProfileDestination)
                    }
                )
            }

            // ── Chỉnh Sửa Hồ Sơ Cá Nhân ──────────────────────────────────────
            composable<EditProfileDestination> {
                EditProfileRoute(
                    onNavigateBack = {
                        // Báo hiệu ProfileDestination cần reload user sau khi save thành công
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("profile_updated", true)
                        navController.popBackStack()
                    },
                )
            }

            // ── Danh Sách Yêu Thích (Món ăn & Quán yêu thích) ───────────────
            composable<WishlistDestination> {
                com.bitefast.feature.profile.favorites.FavoritesRoute(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToDetail = { restaurantId ->
                        navController.navigate(RestaurantDetailDestination(restaurantId))
                    },
                    onNavigateToHome = {
                        navController.navigate(DiscoveryDestination) {
                            popUpTo(navController.graph.findStartDestination().id) { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                )
            }

            // ── Sổ Địa Chỉ & Ghim Tọa Độ Bản Đồ ─────────────────────────────
            composable<AddressListDestination> {
                com.bitefast.feature.profile.address.AddressListRoute(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToPicker = {
                        navController.navigate(AddressPickerDestination())
                    }
                )
            }

            composable<AddressPickerDestination> { backStackEntry ->
                val destination: AddressPickerDestination = backStackEntry.toRoute()
                com.bitefast.feature.profile.address.AddressPickerMapRoute(
                    initialLat = destination.initialLat,
                    initialLng = destination.initialLng,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // ── Chi tiết Nhà hàng & Thực đơn ─────────────────────────────────
            composable<RestaurantDetailDestination> { backStackEntry ->
                val destination: RestaurantDetailDestination = backStackEntry.toRoute()
                DetailRoute(
                    restaurantId = destination.restaurantId,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToCart = {
                        navController.navigate(CartDestination) {
                            launchSingleTop = true
                        }
                    }
                )
            }

            // ── Kho Voucher Khuyến Mãi ───────────────────────────────────────
            composable<VoucherWalletDestination> {
                VoucherWalletRoute(
                    onNavigateBack = { navController.popBackStack() },
                    onVoucherSelected = { _, _ ->
                        navController.popBackStack()
                    }
                )
            }

            // ── Thanh toán Đơn hàng ──────────────────────────────────────────
            composable<CheckoutDestination> {
                CheckoutRoute(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToTracking = { orderId ->
                        navController.navigate(TrackingDestination(orderId)) {
                            popUpTo<CheckoutDestination> { inclusive = true }
                        }
                    },
                    onNavigateToPaymentResult = { orderId, qrUrl, amount ->
                        navController.navigate(PaymentResultDestination(orderId = orderId, amount = amount, qrPayload = qrUrl)) {
                            popUpTo<CheckoutDestination> { inclusive = true }
                        }
                    }
                )
            }

            // ── Kết quả Thanh toán VietQR Động ──────────────────────────────
            composable<PaymentResultDestination> { backStackEntry ->
                val destination: PaymentResultDestination = backStackEntry.toRoute()
                com.bitefast.feature.checkout.payment.PaymentResultRoute(
                    orderId = destination.orderId,
                    amount = destination.amount,
                    qrPayload = destination.qrPayload,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToTracking = { orderId ->
                        navController.navigate(TrackingDestination(orderId)) {
                            popUpTo<CheckoutDestination> { inclusive = true }
                        }
                    }
                )
            }

            // ── Theo dõi Vận chuyển GPS ──────────────────────────────────────
            composable<TrackingDestination> { backStackEntry ->
                val destination: TrackingDestination = backStackEntry.toRoute()
                TrackingRoute(
                    orderId = destination.orderId,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToHome = {
                        navController.navigate(DiscoveryDestination) {
                            popUpTo(navController.graph.findStartDestination().id) { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                    onNavigateToRating = { id ->
                        navController.navigate(RatingDestination(id))
                    }
                )
            }

            // ── Đánh giá Dịch vụ ─────────────────────────────────────────────
            composable<RatingDestination> { backStackEntry ->
                val destination: RatingDestination = backStackEntry.toRoute()
                RatingRoute(
                    orderId = destination.orderId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // ── Trung tâm Thông báo ──────────────────────────────────────────
            composable<NotificationDestination> {
                NotificationRoute(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToOrder = { orderId ->
                        navController.navigate(TrackingDestination(orderId))
                    }
                )
            }

            // ── Nhóm Màn hình Xác thực (Auth) ────────────────────────────────
            composable<LoginDestination> {
                LoginRoute(
                    onNavigateToHome = {
                        if (!navController.popBackStack()) {
                            navController.navigate(DiscoveryDestination) {
                                popUpTo(navController.graph.findStartDestination().id) { inclusive = false }
                                launchSingleTop = true
                            }
                        }
                    },
                    onNavigateToRegister = {
                        navController.navigate(RegisterDestination)
                    },
                    onNavigateToForgotPassword = {
                        navController.navigate(ForgotPasswordDestination())
                    }
                )
            }

            composable<RegisterDestination> {
                RegisterRoute(
                    onNavigateToHome = {
                        if (!navController.popBackStack()) {
                            navController.navigate(DiscoveryDestination) {
                                popUpTo(navController.graph.findStartDestination().id) { inclusive = false }
                                launchSingleTop = true
                            }
                        }
                    },
                    onNavigateToLogin = {
                        navController.popBackStack()
                    }
                )
            }

            composable<ForgotPasswordDestination> { backStackEntry ->
                val destination: ForgotPasswordDestination = backStackEntry.toRoute()
                com.bitefast.feature.auth.forgot.ForgotPasswordRoute(
                    initialIdentifier = destination.initialEmailOrPhone,
                    onNavigateToLogin = {
                        navController.popBackStack()
                    }
                )
            }
        }

        // ── Login Gate Bottom Sheet Chặn Khách Vãng Lai ──────────────────────
        if (showLoginGate) {
            LoginGateBottomSheet(
                onDismiss = { showLoginGate = false },
                onNavigateToLogin = {
                    showLoginGate = false
                    navController.navigate(LoginDestination)
                },
                onNavigateToRegister = {
                    showLoginGate = false
                    navController.navigate(RegisterDestination)
                },
                cartItemCount = cartItemCount,
            )
        }
    }
}

// ─── Bottom Navigation Bar ───────────────────────────────────────────────────

@Composable
private fun BiteFastBottomBar(
    navBackStackEntry: androidx.navigation.NavBackStackEntry?,
    cartItemCount: Int,
    onDestinationSelected: (TopLevelDestination) -> Unit,
) {
    val currentDestination = navBackStackEntry?.destination

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = androidx.compose.ui.unit.Dp(3f),
    ) {
        TopLevelDestination.entries.forEach { destination ->
            val isSelected = currentDestination?.hierarchy?.any {
                it.hasRoute(destination.targetClass)
            } == true

            NavigationBarItem(
                selected = isSelected,
                onClick = { onDestinationSelected(destination) },
                icon = {
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
