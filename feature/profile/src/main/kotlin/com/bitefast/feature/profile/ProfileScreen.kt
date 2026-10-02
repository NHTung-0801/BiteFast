package com.bitefast.feature.profile

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bitefast.core.designsystem.component.BiteFastButton
import com.bitefast.core.designsystem.component.shimmerBrush
import com.bitefast.core.designsystem.theme.OrangePrimaryDark
import com.bitefast.core.designsystem.theme.OrangePrimary

// â”€â”€â”€ Route â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

@Composable
fun ProfileRoute(
    profileUpdatedSignal: Boolean = false,
    onProfileReloadConsumed: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
    onNavigateToOrderHistory: () -> Unit = {},
    onNavigateToVoucherWallet: () -> Unit = {},
    onNavigateToAddresses: () -> Unit = {},
    onNavigateToFavorites: () -> Unit = {},
    onNavigateToEditProfile: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Khi quay về từ EditProfile, tự động reload user profile mới nhất từ DataStore
    LaunchedEffect(profileUpdatedSignal) {
        if (profileUpdatedSignal) {
            viewModel.onEvent(ProfileUiEvent.ReloadUser)
            onProfileReloadConsumed()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ProfileUiEffect.NavigateToLogin -> onNavigateToLogin()
                is ProfileUiEffect.NavigateToOrderHistory -> onNavigateToOrderHistory()
                is ProfileUiEffect.NavigateToVoucherWallet -> onNavigateToVoucherWallet()
                is ProfileUiEffect.NavigateToEditProfile -> onNavigateToEditProfile()
                is ProfileUiEffect.NavigateToAddresses -> onNavigateToAddresses()
                is ProfileUiEffect.NavigateToFavorites -> onNavigateToFavorites()
                is ProfileUiEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    ProfileScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onEvent = viewModel::onEvent,
    )
}

// ─── Screen ───────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    uiState: ProfileUiState,
    snackbarHostState: SnackbarHostState,
    onEvent: (ProfileUiEvent) -> Unit,
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Tài khoản", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
    ) { paddingValues ->
        Crossfade(
            targetState = uiState.isLoading,
            animationSpec = tween(300),
            label = "ProfileCrossfade"
        ) { loading ->
            if (loading) {
                ProfileScreenSkeleton(modifier = Modifier.padding(paddingValues))
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState()),
                ) {
            // ── Avatar + Name header ──────────────────────────────────────────
            ProfileHeader(uiState = uiState, onEvent = onEvent)

            Spacer(Modifier.height(16.dp))

            // ── Account Section ───────────────────────────────────────────────
            if (!uiState.isGuest) {
                MenuSection(title = "Tài khoản") {
                    MenuItem(
                        icon = Icons.Default.Person, label = "Chỉnh sửa hồ sơ",
                        onClick = { onEvent(ProfileUiEvent.ClickEditProfile) },
                    )
                    MenuItem(
                        icon = Icons.Default.LocationOn, label = "Sổ địa chỉ giao hàng",
                        subtitle = if (uiState.addresses.isNotEmpty()) "${uiState.addresses.size} địa chỉ đã lưu" else null,
                        onClick = { onEvent(ProfileUiEvent.ClickAddresses) },
                    )
                    MenuItem(
                        icon = Icons.Default.ConfirmationNumber, label = "Kho Voucher & Khuyến mãi",
                        subtitle = "Ưu đãi giảm đến 50k",
                        onClick = { onEvent(ProfileUiEvent.ClickVoucherWallet) },
                    )
                    MenuItem(
                        icon = Icons.Default.Receipt, label = "Lịch sử đơn hàng",
                        onClick = { onEvent(ProfileUiEvent.ClickOrderHistory) },
                    )
                    MenuItem(
                        icon = Icons.Default.FavoriteBorder, label = "Món ăn & Quán yêu thích",
                        subtitle = if (uiState.favoriteCount > 0) "${uiState.favoriteCount} mục đã lưu" else null,
                        onClick = { onEvent(ProfileUiEvent.ClickFavorites) },
                    )
                }

                Spacer(Modifier.height(12.dp))
            }

            // ── Settings Section ──────────────────────────────────────────────
            MenuSection(title = "Cài đặt") {
                SwitchMenuItem(
                    icon = Icons.Default.Notifications,
                    label = "Thông báo",
                    checked = uiState.isNotificationEnabled,
                    onCheckedChange = { onEvent(ProfileUiEvent.ToggleNotification(it)) },
                )
            }

            Spacer(Modifier.height(12.dp))

            // ── Support Section ───────────────────────────────────────────────
            MenuSection(title = "Hỗ trợ") {
                MenuItem(icon = Icons.Default.Info, label = "Về BiteFast",
                    onClick = { onEvent(ProfileUiEvent.ClickAbout) })
            }

            Spacer(Modifier.height(16.dp))

            // ── Logout / Login button ─────────────────────────────────────────
            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                if (uiState.isGuest) {
                    BiteFastButton(
                        text = "Đăng nhập / Đăng ký",
                        onClick = { onEvent(ProfileUiEvent.ClickLogin) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                } else {
                    TextButton(
                        onClick = { onEvent(ProfileUiEvent.RequestLogout) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null,
                            tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Đăng xuất", color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // App version footer
            Text(
                text = "BiteFast v1.0.0",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
            Spacer(Modifier.height(16.dp))
                } // end Column
            } // end else
        } // end Crossfade
    } // end Scaffold

    // ── About Dialog ──────────────────────────────────────────────────────────
    if (uiState.showAboutDialog) {
        AboutAppDialog(onDismiss = { onEvent(ProfileUiEvent.DismissAboutDialog) })
    }

    // ── Logout Confirm Dialog ─────────────────────────────────────────────────
    if (uiState.showLogoutDialog) {
        LogoutConfirmDialog(
            onConfirm = { onEvent(ProfileUiEvent.ConfirmLogout) },
            onDismiss = { onEvent(ProfileUiEvent.DismissLogoutDialog) },
        )
    }
}

// â”€â”€â”€ Profile Header â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

@Composable
private fun ProfileHeader(uiState: ProfileUiState, onEvent: (ProfileUiEvent) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(OrangePrimary, OrangePrimaryDark)))
            .padding(vertical = 24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Avatar circle
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = uiState.displayName.take(1).uppercase(),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        color = Color.White, fontWeight = FontWeight.Bold, fontSize = 32.sp,
                    ),
                )
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = if (uiState.isGuest) "Khach vang lai" else uiState.displayName,
                style = MaterialTheme.typography.titleLarge.copy(color = Color.White, fontWeight = FontWeight.Bold),
            )

            if (!uiState.isGuest) {
                Text(
                    text = uiState.user.email,
                    style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.85f)),
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = uiState.memberSince,
                    style = MaterialTheme.typography.labelSmall.copy(color = Color.White.copy(alpha = 0.7f)),
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White.copy(alpha = 0.2f))
                        .padding(horizontal = 10.dp, vertical = 3.dp),
                )
            }
        }
    }
}

// â”€â”€â”€ Menu Section â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

@Composable
private fun MenuSection(title: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp),
        )
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.animateContentSize()) { content() }
        }
    }
}

@Composable
private fun MenuItem(
    icon: ImageVector,
    label: String,
    subtitle: String? = null,
    showDivider: Boolean = true,
    onClick: () -> Unit,
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(OrangePrimary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = OrangePrimary, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
                if (subtitle != null) {
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null,
                tint = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.size(20.dp))
        }
        if (showDivider) {
            HorizontalDivider(modifier = Modifier.padding(start = 64.dp), thickness = 0.5.dp)
        }
    }
}

@Composable
private fun SwitchMenuItem(
    icon: ImageVector,
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    showDivider: Boolean = true,
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(OrangePrimary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = OrangePrimary, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            Text(label, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), modifier = Modifier.weight(1f))
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(checkedThumbColor = OrangePrimary, checkedTrackColor = OrangePrimary.copy(alpha = 0.4f)),
            )
        }
        if (showDivider) {
            HorizontalDivider(modifier = Modifier.padding(start = 64.dp), thickness = 0.5.dp)
        }
    }
}

// â”€â”€â”€ Logout Dialog â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

@Composable
private fun LogoutConfirmDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
        title = { Text("Đăng xuất") },
        text = { Text("Bạn có chắc muốn đăng xuất khỏi BiteFast?", style = MaterialTheme.typography.bodyMedium) },
        confirmButton = {
            BiteFastButton(text = "Đăng xuất", onClick = onConfirm)
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Hủy") }
        },
    )
}

// ─── About App Dialog ──────────────────────────────────────────────────────────

@Composable
private fun AboutAppDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(OrangePrimary),
                contentAlignment = Alignment.Center,
            ) {
                Text("🍔", style = MaterialTheme.typography.headlineMedium)
            }
        },
        title = {
            Text(
                "BiteFast",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AboutInfoRow(label = "Phiên bản", value = "1.0.0")
                AboutInfoRow(label = "Nền tảng", value = "Android")
                AboutInfoRow(label = "Ngôn ngữ", value = "Tiếng Việt")
                AboutInfoRow(label = "Nhà phát triển", value = "BiteFast Team")
                Spacer(Modifier.height(4.dp))
                Text(
                    "Ứng dụng đặt đồ ăn nhanh, tiện lợi. Kết nối bạn với hàng trăm nhà hàng yêu thích.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                )
            }
        },
        confirmButton = {
            BiteFastButton(text = "Đóng", onClick = onDismiss)
        },
    )
}

@Composable
private fun AboutInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline),
        )
        Text(
            value,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
        )
    }
}

// ─── Profile Screen Skeleton ──────────────────────────────────────────────────

@Composable
private fun ProfileScreenSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar shimmer circle
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .shimmerBrush()
        )
        Spacer(Modifier.height(14.dp))
        // Display name shimmer
        Box(
            modifier = Modifier
                .width(160.dp)
                .height(22.dp)
                .clip(RoundedCornerShape(6.dp))
                .shimmerBrush()
        )
        Spacer(Modifier.height(8.dp))
        // Member badge shimmer
        Box(
            modifier = Modifier
                .width(110.dp)
                .height(16.dp)
                .clip(RoundedCornerShape(4.dp))
                .shimmerBrush()
        )
        Spacer(Modifier.height(24.dp))

        // Menu Section 1 Skeleton
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                repeat(4) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .shimmerBrush()
                        )
                        Spacer(Modifier.width(16.dp))
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(16.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .shimmerBrush()
                        )
                        Spacer(Modifier.width(16.dp))
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .shimmerBrush()
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Menu Section 2 Skeleton
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                repeat(3) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .shimmerBrush()
                        )
                        Spacer(Modifier.width(16.dp))
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(16.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .shimmerBrush()
                        )
                        Spacer(Modifier.width(16.dp))
                        Box(
                            modifier = Modifier
                                .width(36.dp)
                                .height(20.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .shimmerBrush()
                        )
                    }
                }
            }
        }
    }
}
