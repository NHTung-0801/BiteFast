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
import androidx.compose.material.icons.filled.ConfirmationNumber
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
import androidx.compose.material3.Surface
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
import com.bitefast.core.designsystem.theme.WarningAmber
import com.bitefast.core.model.CartItem

// --- Route --------------------------------------------------------------------

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

// --- Screen -------------------------------------------------------------------

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
                        Text("Gi? hàng c?a b?n", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                        AnimatedVisibility(visible = uiState.itemCount > 0) {
                            Text(
                                "${uiState.itemCount} món",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
                actions = {
                    if (uiState.items.isNotEmpty()) {
                        IconButton(onClick = { onEvent(CartUiEvent.ClearCart) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Xóa t?t c?", tint = MaterialTheme.colorScheme.error)
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
                CartBottomBar(
                    uiState = uiState,
                    onEvent = onEvent,
                )
            }
        },
    ) { paddingValues ->
        if (uiState.items.isEmpty()) {
            EmptyCartView(
                onAddSample = { onEvent(CartUiEvent.AddSampleItem) },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            )
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
                            text = "T?: ${first.restaurantId}",
                            style = MaterialTheme.typography.labelMedium,
                            color = OrangePrimary,
                            modifier = Modifier.padding(bottom = 4.dp),
                        )
                    }
                }

                // Smart voucher suggestion / upsell banner
                if (!uiState.bestVoucherMessage.isNullOrBlank() || !uiState.voucherUpsellMessage.isNullOrBlank()) {
                    item(key = "voucher_banner") {
                        VoucherCartBanner(
                            bestVoucher = uiState.bestVoucherMessage,
                            upsell = uiState.voucherUpsellMessage
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

                item(key = "price_breakdown") {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            PriceRow("T?m tính", uiState.subtotal)
                            PriceRow("Phí giao hàng", uiState.deliveryFee)
                            if (uiState.discount > 0) {
                                PriceRow("Gi?m giá", -uiState.discount, isHighlight = true)
                            }
                            HorizontalDivider()
                            PriceRow("T?ng c?ng", uiState.total, isHighlight = true)
                        }
                    }
                }
            }
        }
    }
}

// --- Voucher Cart Banner ------------------------------------------------------

@Composable
private fun VoucherCartBanner(
    bestVoucher: String?,
    upsell: String?
) {
    if (!bestVoucher.isNullOrBlank()) {
        Surface(
            color = OrangePrimary.copy(alpha = 0.1f),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ConfirmationNumber,
                    contentDescription = null,
                    tint = OrangePrimary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = bestVoucher,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = OrangePrimary
                )
            }
        }
    } else if (!upsell.isNullOrBlank()) {
        Surface(
            color = WarningAmber.copy(alpha = 0.12f),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("??", modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = upsell,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                    color = WarningAmber
                )
            }
        }
    }
}

// --- Cart Item Card -----------------------------------------------------------

@Composable
private fun CartItemCard(
    item: CartItem,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Food image
            AsyncImage(
                model = item.imageUrl.ifBlank { "https://images.unsplash.com/photo-1546069901-ba9599a7e63c" },
                contentDescription = item.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(68.dp)
                    .clip(RoundedCornerShape(10.dp)),
            )

            Spacer(Modifier.width(12.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (item.notes.isNotBlank()) {
                    Text(
                        text = item.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "%,.0fd".format(item.totalPrice),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = OrangePrimary,
                )
            }

            Spacer(Modifier.width(8.dp))

            // Quantity selector
            QuantitySelector(
                quantity = item.quantity,
                onIncrease = onIncrease,
                onDecrease = onDecrease,
                
            )
        }
    }
}

// --- Empty State --------------------------------------------------------------

@Composable
private fun EmptyCartView(
    onAddSample: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(24.dp),
        ) {
            EmptyState(
                title = "Gi? hàng tr?ng",
                subtitle = "B?n chua có món an nào trong gi? hàng. Hãy khám phá th?c don h?p d?n nhé!",
            )
            BiteFastButton(
                text = "Thêm món an m?u",
                onClick = onAddSample,
                modifier = Modifier
                    .width(200.dp)
                    .semantics { contentDescription = "Them mon mau" },
            )
        }
    }
}

// --- Cart Bottom Bar ----------------------------------------------------------

@Composable
private fun CartBottomBar(
    uiState: CartUiState,
    onEvent: (CartUiEvent) -> Unit,
) {
    Card(
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("T?ng thanh toán", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Text(
                    text = "%,.0fd".format(uiState.total),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = OrangePrimary,
                )
            }
            Spacer(Modifier.height(12.dp))
            BiteFastButton(
                text = "Ti?n hành thanh toán",
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
            text = "%,.0fd".format(amount),
            style = MaterialTheme.typography.bodyMedium,
            color = if (isHighlight) OrangePrimary else MaterialTheme.colorScheme.onSurface,
        )
    }
}

// --- Restaurant Conflict Dialog -----------------------------------------------

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
            Text("Gi? hàng t? nhà hàng khác", style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Text(
                "B?n dang có món an t? \"$currentRestaurantName\" trong gi? hàng. " +
                    "Thêm món t? nhà hàng khác s? xóa gi? hàng hi?n t?i. B?n có mu?n ti?p t?c?",
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        confirmButton = {
            BiteFastButton(
                text = "Xóa và thêm m?i",
                onClick = onConfirm,
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Gi? nguyên")
            }
        },
    )
}
