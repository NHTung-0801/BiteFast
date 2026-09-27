package com.bitefast.feature.profile

import androidx.compose.animation.animateContentSize
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
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
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
import com.bitefast.core.designsystem.theme.OrangePrimaryDark
import com.bitefast.core.designsystem.theme.OrangePrimary

// ─── Route ───────────────────────────────────────────────────────────────────

@Composable
fun ProfileRoute(
    onNavigateToLogin: () -> Unit = {},
    onNavigateToOrderHistory: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ProfileUiEffect.NavigateToLogin -> onNavigateToLogin()
                is ProfileUiEffect.NavigateToOrderHistory -> onNavigateToOrderHistory()
                is ProfileUiEffect.NavigateToEditProfile -> {}
                is ProfileUiEffect.NavigateToAddresses -> {}
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

// ─── Screen ──────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    uiState: ProfileUiState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onEvent: (ProfileUiEvent) -> Unit,
) {
    if (uiState.showLogoutDialog) {
        LogoutConfirmDialog(
            onConfirm = { onEvent(ProfileUiEvent.ConfirmLogout) },
            onDismiss = { onEvent(ProfileUiEvent.DismissLogoutDialog) },
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Tai khoan", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
        ) {
            // ── Avatar + Name header ─────────────────────────────────────────
            ProfileHeader(uiState = uiState, onEvent = onEvent)

            Spacer(Modifier.height(16.dp))

            // ── Account Section ──────────────────────────────────────────────
            if (!uiState.isGuest) {
                MenuSection(title = "Tai khoan") {
                    MenuItem(
                        icon = Icons.Default.Person, label = "Chinh sua ho so",
                        onClick = { onEvent(ProfileUiEvent.ClickEditProfile) },
                    )
                    MenuItem(
                        icon = Icons.Default.LocationOn, label = "So dia chi giao hang",
                        subtitle = if (uiState.addresses.isNotEmpty()) "${uiState.addresses.size} dia chi da luu" else null,
                        onClick = { onEvent(ProfileUiEvent.ClickAddresses) },
                    )
                    MenuItem(
                        icon = Icons.Default.Receipt, label = "Lich su don hang",
                        onClick = { onEvent(ProfileUiEvent.ClickOrderHistory) },
                    )
                    MenuItem(
                        icon = Icons.Default.FavoriteBorder, label = "Nha hang yeu thich",
                        onClick = { onEvent(ProfileUiEvent.ClickFavorites) },
                    )
                }

                Spacer(Modifier.height(12.dp))
            }

            // ── Settings Section ─────────────────────────────────────────────
            MenuSection(title = "Cai dat") {
                SwitchMenuItem(
                    icon = Icons.Default.Fingerprint,
                    label = "Xac thuc Biometric",
                    checked = uiState.isBiometricEnabled,
                    onCheckedChange = { onEvent(ProfileUiEvent.ToggleBiometric(it)) },
                )
                SwitchMenuItem(
                    icon = Icons.Default.Notifications,
                    label = "Thong bao",
                    checked = uiState.isNotificationEnabled,
                    onCheckedChange = { onEvent(ProfileUiEvent.ToggleNotification(it)) },
                )
                SwitchMenuItem(
                    icon = Icons.Default.DarkMode,
                    label = "Giao dien toi",
                    checked = uiState.isDarkMode,
                    onCheckedChange = { onEvent(ProfileUiEvent.ToggleDarkMode(it)) },
                )
            }

            Spacer(Modifier.height(12.dp))

            // ── Support Section ──────────────────────────────────────────────
            MenuSection(title = "Ho tro") {
                MenuItem(icon = Icons.Default.HeadsetMic, label = "Lien he ho tro",
                    onClick = { onEvent(ProfileUiEvent.ClickSupport) })
                MenuItem(icon = Icons.Default.Info, label = "Ve BiteFast",
                    onClick = { onEvent(ProfileUiEvent.ClickAbout) })
            }

            Spacer(Modifier.height(16.dp))

            // ── Logout / Login button ────────────────────────────────────────
            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                if (uiState.isGuest) {
                    BiteFastButton(
                        text = "Dang nhap / Dang ky",
                        onClick = { onEvent(ProfileUiEvent.RequestLogout) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                } else {
                    TextButton(
                        onClick = { onEvent(ProfileUiEvent.RequestLogout) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(Icons.Default.Logout, contentDescription = null,
                            tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Dang xuat", color = MaterialTheme.colorScheme.error,
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
        }
    }
}

// ─── Profile Header ───────────────────────────────────────────────────────────

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

// ─── Menu Section ─────────────────────────────────────────────────────────────

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

// ─── Logout Dialog ────────────────────────────────────────────────────────────

@Composable
private fun LogoutConfirmDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Logout, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
        title = { Text("Dang xuat") },
        text = { Text("Ban co chac muon dang xuat khoi BiteFast?", style = MaterialTheme.typography.bodyMedium) },
        confirmButton = {
            BiteFastButton(text = "Dang xuat", onClick = onConfirm)
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Huy") }
        },
    )
}
