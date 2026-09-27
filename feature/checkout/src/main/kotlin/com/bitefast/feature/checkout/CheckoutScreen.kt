package com.bitefast.feature.checkout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bitefast.core.designsystem.component.BiteFastButton
import com.bitefast.core.designsystem.theme.OrangePrimary
import com.bitefast.core.designsystem.theme.SuccessGreen
import com.bitefast.core.model.Address
import com.bitefast.core.model.CartItem
import com.bitefast.core.model.PaymentMethod

// ─── Route ────────────────────────────────────────────────────────────────────

@Composable
fun CheckoutRoute(
    onNavigateBack: () -> Unit,
    onNavigateToTracking: (String) -> Unit = {},
    viewModel: CheckoutViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is CheckoutUiEffect.NavigateToTracking -> onNavigateToTracking(effect.orderId)
                is CheckoutUiEffect.NavigateBack -> onNavigateBack()
                is CheckoutUiEffect.TriggerBiometric -> { /* Handled at Activity level */ }
                is CheckoutUiEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    CheckoutScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack,
    )
}

// ─── Screen ───────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    uiState: CheckoutUiState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onEvent: (CheckoutUiEvent) -> Unit,
    onNavigateBack: () -> Unit,
) {
    // Voucher BottomSheet
    if (uiState.showVoucherSheet) {
        VoucherBottomSheet(
            voucherCode = uiState.voucherCode,
            isLoading = uiState.isVoucherLoading,
            error = uiState.voucherError,
            onCodeChanged = { onEvent(CheckoutUiEvent.VoucherCodeChanged(it)) },
            onApply = { onEvent(CheckoutUiEvent.ApplyVoucher) },
            onDismiss = { onEvent(CheckoutUiEvent.CloseVoucherSheet) },
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Xac nhan don hang", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lai")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
        bottomBar = {
            CheckoutBottomBar(
                total = uiState.total,
                isLoading = uiState.isLoading,
                isEnabled = !uiState.isLoading && uiState.items.isNotEmpty(),
                onPlaceOrder = { onEvent(CheckoutUiEvent.PlaceOrder) },
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // ── Delivery Address section ──────────────────────────────────────
            SectionCard(title = "Dia chi giao hang", icon = Icons.Default.LocationOn) {
                AddressForm(
                    address = uiState.deliveryAddress,
                    error = uiState.addressError,
                    onAddressChanged = { onEvent(CheckoutUiEvent.AddressChanged(it)) },
                )
            }

            // ── Payment Method section ────────────────────────────────────────
            SectionCard(title = "Phuong thuc thanh toan", icon = Icons.Default.CreditCard) {
                PaymentMethodSelector(
                    selected = uiState.selectedPayment,
                    onSelect = { onEvent(CheckoutUiEvent.PaymentMethodSelected(it)) },
                )
            }

            // ── Voucher section ───────────────────────────────────────────────
            SectionCard(title = "Ma giam gia", icon = Icons.Default.ConfirmationNumber) {
                if (uiState.discount > 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Giam: %,.0f d".format(uiState.discount),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = SuccessGreen,
                        )
                        TextButton(onClick = { onEvent(CheckoutUiEvent.RemoveVoucher) }) {
                            Text("Xoa voucher", color = MaterialTheme.colorScheme.error)
                        }
                    }
                } else {
                    TextButton(
                        onClick = { onEvent(CheckoutUiEvent.OpenVoucherSheet) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(Icons.Default.ConfirmationNumber, contentDescription = null, tint = OrangePrimary, modifier = Modifier.size(18.dp))
                        Text("  Nhap ma voucher", color = OrangePrimary)
                    }
                }
            }

            // ── Note section ──────────────────────────────────────────────────
            SectionCard(title = "Ghi chu", icon = Icons.Default.MonetizationOn) {
                OutlinedTextField(
                    value = uiState.note,
                    onValueChange = { onEvent(CheckoutUiEvent.NoteChanged(it)) },
                    placeholder = { Text("Vi du: it cay, khong hanh...") },
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrangePrimary),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            // ── Order Summary ─────────────────────────────────────────────────
            SectionCard(title = "Tom tat don hang", icon = Icons.Default.LocationOn) {
                OrderSummarySection(uiState = uiState)
            }
        }
    }
}

// ─── Address Form ─────────────────────────────────────────────────────────────

@Composable
private fun AddressForm(
    address: Address,
    error: String?,
    onAddressChanged: (Address) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = address.recipientName,
            onValueChange = { onAddressChanged(address.copy(recipientName = it)) },
            label = { Text("Ho va ten nguoi nhan") },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrangePrimary),
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = address.phoneNumber,
            onValueChange = { onAddressChanged(address.copy(phoneNumber = it)) },
            label = { Text("So dien thoai") },
            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true,
            isError = error != null,
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrangePrimary),
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = address.streetAddress,
            onValueChange = { onAddressChanged(address.copy(streetAddress = it)) },
            label = { Text("Dia chi cu the (so nha, duong, phuong/xa)") },
            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
            maxLines = 2,
            isError = error != null,
            supportingText = error?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrangePrimary),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

// ─── Payment Method Selector ──────────────────────────────────────────────────

@Composable
private fun PaymentMethodSelector(
    selected: PaymentMethod,
    onSelect: (PaymentMethod) -> Unit,
) {
    val options = listOf(
        PaymentMethod.CASH to "Tien mat khi nhan hang",
        PaymentMethod.CARD to "The ngân hang (Yeu cau Biometric)",
        PaymentMethod.E_WALLET to "Vi dien tu MoMo/ZaloPay (Yeu cau Biometric)",
    )
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { (method, label) ->
            Card(
                onClick = { onSelect(method) },
                shape = RoundedCornerShape(10.dp),
                border = if (selected == method)
                    androidx.compose.foundation.BorderStroke(2.dp, OrangePrimary)
                else
                    androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                colors = CardDefaults.cardColors(
                    containerColor = if (selected == method) OrangePrimary.copy(alpha = 0.07f) else MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(12.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.CreditCard,
                        contentDescription = null,
                        tint = if (selected == method) OrangePrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.size(8.dp))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (selected == method) OrangePrimary else MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}

// ─── Order Summary ────────────────────────────────────────────────────────────

@Composable
private fun OrderSummarySection(uiState: CheckoutUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        uiState.items.forEach { item ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("${item.quantity}x ${item.name}", style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f), maxLines = 1)
                Text("%,.0f d".format(item.totalPrice), style = MaterialTheme.typography.bodySmall)
            }
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
        SummaryRow("Tam tinh", uiState.subtotal)
        SummaryRow("Phi giao hang", uiState.deliveryFee)
        if (uiState.discount > 0) SummaryRow("Giam gia", -uiState.discount, highlight = true)
        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("Tong cong", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
            Text(
                "%,.0f d".format(uiState.total),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = OrangePrimary,
            )
        }
    }
}

@Composable
private fun SummaryRow(label: String, amount: Double, highlight: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            "%,.0f d".format(amount),
            style = MaterialTheme.typography.bodySmall,
            color = if (highlight) SuccessGreen else MaterialTheme.colorScheme.onSurface,
        )
    }
}

// ─── Bottom CTA ───────────────────────────────────────────────────────────────

@Composable
private fun CheckoutBottomBar(
    total: Double,
    isLoading: Boolean,
    isEnabled: Boolean,
    onPlaceOrder: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text("Tong thanh toan", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    "%,.0f d".format(total),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = OrangePrimary,
                )
            }
            if (isLoading) {
                CircularProgressIndicator(color = OrangePrimary, modifier = Modifier.size(40.dp))
            } else {
                BiteFastButton(
                    text = "Dat hang",
                    onClick = onPlaceOrder,
                    enabled = isEnabled,
                )
            }
        }
    }
}

// ─── Voucher Bottom Sheet ─────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VoucherBottomSheet(
    voucherCode: String,
    isLoading: Boolean,
    error: String?,
    onCodeChanged: (String) -> Unit,
    onApply: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Nhap ma voucher", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
            OutlinedTextField(
                value = voucherCode,
                onValueChange = onCodeChanged,
                label = { Text("Ma voucher (VD: BITE10)") },
                isError = error != null,
                supportingText = error?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrangePrimary),
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                "Thu: BITE10 (giam 10%), BITE20 (giam 20%), FREESHIP (mien phi giao hang)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            BiteFastButton(
                text = if (isLoading) "Dang kiem tra..." else "Ap dung",
                onClick = onApply,
                enabled = !isLoading && voucherCode.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}

// ─── Helpers ──────────────────────────────────────────────────────────────────

@Composable
private fun SectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = OrangePrimary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.size(8.dp))
                Text(title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold))
            }
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}
