package com.bitefast.feature.order

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bitefast.core.designsystem.component.BiteFastButton
import com.bitefast.core.designsystem.component.EmptyState
import com.bitefast.core.designsystem.theme.OrangePrimary
import com.bitefast.core.model.Order
import com.bitefast.core.model.OrderStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ─── Route ───────────────────────────────────────────────────────────────────

@Composable
fun OrderRoute(
    onNavigateToTracking: (String) -> Unit = {},
    onNavigateToDetail: (String) -> Unit = {},
    viewModel: OrderViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is OrderUiEffect.NavigateToTracking -> onNavigateToTracking(effect.orderId)
                is OrderUiEffect.NavigateToDetail -> onNavigateToDetail(effect.restaurantId)
                is OrderUiEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    OrderScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onEvent = viewModel::onEvent,
    )
}

// ─── Screen ──────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderScreen(
    uiState: OrderUiState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onEvent: (OrderUiEvent) -> Unit,
) {
    // Cancel dialog
    if (uiState.showCancelDialog) {
        CancelOrderDialog(
            order = uiState.orderToCancel,
            reason = uiState.cancelReason,
            onReasonChanged = { onEvent(OrderUiEvent.CancelReasonChanged(it)) },
            onConfirm = { onEvent(OrderUiEvent.ConfirmCancelOrder) },
            onDismiss = { onEvent(OrderUiEvent.DismissCancelDialog) },
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Don hang cua toi", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                        if (uiState.activeOrders.isNotEmpty()) {
                            Text(
                                "${uiState.activeOrders.size} don dang xu ly",
                                style = MaterialTheme.typography.labelMedium,
                                color = OrangePrimary,
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // Filter tabs
            ScrollableTabRow(
                selectedTabIndex = OrderFilterTab.entries.indexOf(uiState.selectedTab),
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = OrangePrimary,
                edgePadding = 8.dp,
            ) {
                OrderFilterTab.entries.forEach { tab ->
                    Tab(
                        selected = uiState.selectedTab == tab,
                        onClick = { onEvent(OrderUiEvent.SelectTab(tab)) },
                        text = {
                            val count = when (tab) {
                                OrderFilterTab.ALL -> uiState.allOrders.size
                                OrderFilterTab.ACTIVE -> uiState.activeOrders.size
                                OrderFilterTab.COMPLETED -> uiState.completedOrders.filter { it.status == OrderStatus.DELIVERED }.size
                                OrderFilterTab.CANCELED -> uiState.canceledOrders.size
                            }
                            Text(
                                text = if (count > 0) "${tab.label} ($count)" else tab.label,
                                style = MaterialTheme.typography.labelMedium,
                            )
                        },
                    )
                }
            }

            // Orders list or empty state
            if (uiState.filteredOrders.isEmpty() && !uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState(
                        title = "Chua co don hang nao",
                        subtitle = when (uiState.selectedTab) {
                            OrderFilterTab.ACTIVE -> "Hien tai khong co don hang nao dang giao"
                            OrderFilterTab.COMPLETED -> "Chua co don hang nao hoan thanh"
                            OrderFilterTab.CANCELED -> "Chua co don hang nao bi huy"
                            else -> "Hay kham pha va dat mon an ngon!"
                        },
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    // Active orders callout banner
                    if (uiState.selectedTab == OrderFilterTab.ALL && uiState.activeOrders.isNotEmpty()) {
                        item(key = "active_header") {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(OrangePrimary.copy(alpha = 0.1f))
                                    .padding(12.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(OrangePrimary),
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "${uiState.activeOrders.size} don hang dang duoc xu ly / giao den ban",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = OrangePrimary,
                                )
                            }
                        }
                    }

                    items(uiState.filteredOrders, key = { it.id }) { order ->
                        OrderCard(
                            order = order,
                            onTrack = { onEvent(OrderUiEvent.ClickTrackOrder(order.id)) },
                            onReorder = { onEvent(OrderUiEvent.ClickReorder(order)) },
                            onCancel = { onEvent(OrderUiEvent.RequestCancelOrder(order)) },
                            modifier = Modifier.animateItem(),
                        )
                    }
                }
            }
        }
    }
}

// ─── Order Card ───────────────────────────────────────────────────────────────

@Composable
private fun OrderCard(
    order: Order,
    onTrack: () -> Unit,
    onReorder: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isActive = order.status in setOf(
        OrderStatus.PENDING, OrderStatus.CONFIRMED, OrderStatus.PREPARING,
        OrderStatus.READY, OrderStatus.ON_THE_WAY,
    )
    val isCancellable = order.status == OrderStatus.PENDING

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.fillMaxWidth().animateContentSize(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: restaurant name + status chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = order.restaurantName.ifBlank { "Nha hang" },
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    )
                    Text(
                        text = formatDate(order.orderTime),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                OrderStatusChip(status = order.status)
            }

            Spacer(Modifier.height(10.dp))

            // Items summary
            Text(
                text = order.items.take(2).joinToString(", ") { "${it.quantity}x ${it.name}" }
                    + if (order.items.size > 2) " +${order.items.size - 2} mon khac" else "",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(8.dp))

            // Total
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("${order.items.sumOf { it.quantity }} mon • %,.0f d".format(order.total),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
            }

            Spacer(Modifier.height(12.dp))

            // Action buttons
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (isActive) {
                    BiteFastButton(
                        text = "Theo doi",
                        onClick = onTrack,
                        leadingIcon = Icons.Default.TrackChanges,
                        modifier = Modifier.weight(1f),
                    )
                }
                if (!isActive && order.status == OrderStatus.DELIVERED) {
                    BiteFastButton(
                        text = "Dat lai",
                        onClick = onReorder,
                        leadingIcon = Icons.Default.Replay,
                        modifier = Modifier.weight(1f),
                    )
                }
                if (isCancellable) {
                    TextButton(onClick = onCancel, modifier = if (!isActive) Modifier.weight(1f) else Modifier) {
                        Text("Huy don", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

// ─── Status Chip ─────────────────────────────────────────────────────────────

@Composable
private fun OrderStatusChip(status: OrderStatus) {
    val (label, bgColor, textColor) = when (status) {
        OrderStatus.PENDING -> Triple("Cho xac nhan", OrangePrimary.copy(0.15f), OrangePrimary)
        OrderStatus.CONFIRMED -> Triple("Da xac nhan", OrangePrimary.copy(0.15f), OrangePrimary)
        OrderStatus.PREPARING -> Triple("Dang nau", MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer)
        OrderStatus.READY -> Triple("San sang", MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.onTertiaryContainer)
        OrderStatus.ON_THE_WAY -> Triple("Dang giao", OrangePrimary.copy(0.2f), OrangePrimary)
        OrderStatus.DELIVERED -> Triple("Da giao", androidx.compose.ui.graphics.Color(0xFF1A8A2E).copy(0.15f), androidx.compose.ui.graphics.Color(0xFF1A8A2E))
        OrderStatus.CANCELED -> Triple("Da huy", MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.onErrorContainer)
    }

    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
        color = textColor,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp),
    )
}

// ─── Cancel Dialog ────────────────────────────────────────────────────────────

@Composable
private fun CancelOrderDialog(
    order: Order?,
    reason: String,
    onReasonChanged: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Huy don hang") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Ban co chac muon huy don hang tu ${order?.restaurantName ?: "nha hang nay"}?",
                    style = MaterialTheme.typography.bodyMedium,
                )
                OutlinedTextField(
                    value = reason,
                    onValueChange = onReasonChanged,
                    placeholder = { Text("Ly do huy (khong bat buoc)") },
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrangePrimary),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            BiteFastButton(
                text = "Xac nhan huy",
                onClick = onConfirm,
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Giu nguyen") }
        },
    )
}

// ─── Helpers ─────────────────────────────────────────────────────────────────

private fun formatDate(timestamp: Long): String {
    if (timestamp == 0L) return ""
    return SimpleDateFormat("HH:mm, dd/MM/yyyy", Locale.getDefault()).format(Date(timestamp))
}
