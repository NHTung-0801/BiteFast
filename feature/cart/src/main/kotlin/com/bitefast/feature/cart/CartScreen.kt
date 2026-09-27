package com.bitefast.feature.cart

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.bitefast.core.designsystem.component.BiteFastButton
import com.bitefast.core.designsystem.component.EmptyState
import com.bitefast.core.designsystem.component.QuantitySelector
import com.bitefast.core.designsystem.theme.OrangePrimary
import com.bitefast.core.model.CartItem

// ─── Route ────────────────────────────────────────────────────────────────────

@Composable
fun CartRoute(
    onNavigateToCheckout: () -> Unit,
    viewModel: CartViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is CartUiEffect.NavigateToCheckout -> onNavigateToCheckout()
                is CartUiEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    CartScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onEvent = viewModel::onEvent,
    )
}

// ─── Screen ───────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    uiState: CartUiState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onEvent: (CartUiEvent) -> Unit,
) {
    // RestaurantConflict Dialog
    if (uiState.showConflictDialog) {
        RestaurantConflictDialog(
            currentRestaurantName = uiState.conflictRestaurantName,
            onConfirm = { onEvent(CartUiEvent.ConflictConfirmClearAndAdd) },
            onDismiss = { onEvent(CartUiEvent.ConflictDismiss) },
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Gio hang cua ban", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                        AnimatedVisibility(visible = uiState.itemCount > 0) {
                            Text(
                                "${uiState.itemCount} mon",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
                actions = {
                    AnimatedVisibility(visible = uiState.items.isNotEmpty()) {
                        IconButton(
                            onClick = { onEvent(CartUiEvent.ClearCart) },
                            modifier = Modifier.semantics { contentDescription = "Xoa toan bo gio hang" }
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
        bottomBar = {
            AnimatedVisibility(
                visible = uiState.items.isNotEmpty(),
                enter = fadeIn(tween(200)),
                exit = fadeOut(tween(200)),
            ) {
                CartSummaryBar(uiState = uiState, onEvent = onEvent)
            }
        },
    ) { paddingValues ->
        if (uiState.items.isEmpty()) {
            // Empty state
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.size(80.dp),
                )
                Spacer(Modifier.height(16.dp))
                EmptyState(
                    title = "Gio hang dang trong",
                    subtitle = "Hay chon nhung mon an ngon tu BiteFast nhe!",
                )
                Spacer(Modifier.height(20.dp))
                BiteFastButton(
                    text = "Them mon mau vao gio hang",
                    onClick = { onEvent(CartUiEvent.AddSampleItem) },
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Restaurant header
                item(key = "restaurant_header") {
                    uiState.items.firstOrNull()?.let { first ->
                        Text(
                            text = "Tu: ${first.restaurantId}",
                            style = MaterialTheme.typography.labelMedium,
                            color = OrangePrimary,
                            modifier = Modifier.padding(bottom = 4.dp),
                        )
                    }
                }

                items(uiState.items, key = { it.id }) { item ->
                    CartItemCard(
                        item = item,
                        onIncrease = { onEvent(CartUiEvent.IncreaseQuantity(item.id, item.quantity)) },
                        onDecrease = { onEvent(CartUiEvent.DecreaseQuantity(item.id, item.quantity)) },
                        modifier = Modifier.animateItem(),
                    )
                }
            }
        }
    }
}

// ─── Cart Item Card ───────────────────────────────────────────────────────────

@Composable
private fun CartItemCard(
    item: CartItem,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
            .semantics { contentDescription = "${item.name}, so luong ${item.quantity}, ${"%,.0f".format(item.totalPrice)} dong" },
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
        ) {
            // Thumbnail
            if (item.imageUrl.isNotEmpty()) {
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                )
                Spacer(Modifier.width(12.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (item.notes.isNotEmpty()) {
                    Text(
                        text = "Ghi chu: ${item.notes}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "%,.0f d".format(item.totalPrice),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = OrangePrimary,
                )
            }

            Spacer(Modifier.width(8.dp))

            QuantitySelector(
                quantity = item.quantity,
                onIncrease = onIncrease,
                onDecrease = onDecrease,
            )
        }
    }
}

// ─── Cart Summary BottomBar ───────────────────────────────────────────────────

@Composable
private fun CartSummaryBar(
    uiState: CartUiState,
    onEvent: (CartUiEvent) -> Unit,
) {
    Card(
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
            PriceRow("Tam tinh", uiState.subtotal)
            Spacer(Modifier.height(4.dp))
            PriceRow("Phi giao hang", uiState.deliveryFee)
            if (uiState.discount > 0) {
                Spacer(Modifier.height(4.dp))
                PriceRow("Giam gia", -uiState.discount, isHighlight = true)
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Tong thanh toan", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Text(
                    text = "%,.0f d".format(uiState.total),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = OrangePrimary,
                )
            }
            Spacer(Modifier.height(12.dp))
            BiteFastButton(
                text = "Tien hanh thanh toan",
                onClick = { onEvent(CartUiEvent.Checkout) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun PriceRow(label: String, amount: Double, isHighlight: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            text = "%,.0f d".format(amount),
            style = MaterialTheme.typography.bodyMedium,
            color = if (isHighlight) OrangePrimary else MaterialTheme.colorScheme.onSurface,
        )
    }
}

// ─── Restaurant Conflict Dialog ───────────────────────────────────────────────

@Composable
fun RestaurantConflictDialog(
    currentRestaurantName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
        },
        title = {
            Text("Gio hang tu nha hang khac", style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Text(
                "Ban dang co mon an tu \"$currentRestaurantName\" trong gio hang. " +
                    "Them mon tu nha hang khac se xoa gio hang hien tai. Ban co muon tiep tuc?",
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        confirmButton = {
            BiteFastButton(
                text = "Xoa va them moi",
                onClick = onConfirm,
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Giu nguyen")
            }
        },
    )
}
