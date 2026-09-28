package com.bitefast.feature.checkout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bitefast.core.designsystem.component.BiteFastButton
import com.bitefast.core.designsystem.component.VoucherCard
import com.bitefast.core.designsystem.theme.OrangePrimary
import com.bitefast.core.designsystem.theme.SuccessGreen
import com.bitefast.core.domain.voucher.VoucherWalletItem
import com.bitefast.core.model.Address
import com.bitefast.core.model.CartItem
import com.bitefast.core.model.PaymentMethod
import com.bitefast.core.model.Voucher

// --- Route --------------------------------------------------------------------

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

// --- Screen -------------------------------------------------------------------

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
            availableVouchers = uiState.availableVouchers,
            isLoading = uiState.isVoucherLoading,
            error = uiState.voucherError,
            onCodeChanged = { onEvent(CheckoutUiEvent.VoucherCodeChanged(it)) },
            onApply = { onEvent(CheckoutUiEvent.ApplyVoucher) },
            onSelectVoucher = { voucher, discount ->
                onEvent(CheckoutUiEvent.SelectVoucherFromSheet(voucher, discount))
            },
            onDismiss = { onEvent(CheckoutUiEvent.CloseVoucherSheet) },
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Xác nh?n don hàng", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay l?i")
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
            // -- Delivery Address section ---------------------------------------
            SectionCard(title = "Ð?a ch? giao hàng", icon = Icons.Default.LocationOn) {
                AddressForm(
                    address = uiState.deliveryAddress,
                    error = uiState.addressError,
                    onAddressChanged = { onEvent(CheckoutUiEvent.AddressChanged(it)) },
                )
            }

            // -- Payment Method section -----------------------------------------
            SectionCard(title = "Phuong th?c thanh toán", icon = Icons.Default.CreditCard) {
                PaymentMethodSelector(
                    selected = uiState.selectedPayment,
                    onSelect = { onEvent(CheckoutUiEvent.PaymentMethodSelected(it)) },
                )
            }

            // -- Voucher section -----------------------------------------------
            SectionCard(title = "Mã gi?m giá", icon = Icons.Default.ConfirmationNumber) {
                if (uiState.discount > 0) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column {
                                Surface(
                                    color = SuccessGreen.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "Mã: ${uiState.voucherCode}",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = SuccessGreen,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                                Text(
                                    text = "Ti?t ki?m: %,.0fd".format(uiState.discount),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = SuccessGreen,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                            Row {
                                TextButton(onClick = { onEvent(CheckoutUiEvent.OpenVoucherSheet) }) {
                                    Text("Ð?i mã", color = OrangePrimary)
                                }
                                TextButton(onClick = { onEvent(CheckoutUiEvent.RemoveVoucher) }) {
                                    Text("Xóa", color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Smart auto-suggestion banner if available
                        if (!uiState.bestVoucherSuggestion.isNullOrBlank()) {
                            Surface(
                                color = OrangePrimary.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.LocalOffer,
                                            contentDescription = null,
                                            tint = OrangePrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            text = uiState.bestVoucherSuggestion,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                            color = OrangePrimary
                                        )
                                    }
                                    Button(
                                        onClick = { onEvent(CheckoutUiEvent.AutoApplyBestVoucher) },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                                        modifier = Modifier.height(32.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                                    ) {
                                        Text("Áp d?ng", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                                    }
                                }
                            }
                        }

                        TextButton(
                            onClick = { onEvent(CheckoutUiEvent.OpenVoucherSheet) },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Icon(Icons.Default.ConfirmationNumber, contentDescription = null, tint = OrangePrimary, modifier = Modifier.size(18.dp))
                            Text("  Ch?n ho?c nh?p mã voucher", color = OrangePrimary)
                        }
                    }
                }
            }

            // -- Note section --------------------------------------------------
            SectionCard(title = "Ghi chú", icon = Icons.Default.MonetizationOn) {
                OutlinedTextField(
                    value = uiState.note,
                    onValueChange = { onEvent(CheckoutUiEvent.NoteChanged(it)) },
                    placeholder = { Text("Ví d?: ít cay, không hành...") },
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrangePrimary),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            // -- Order Summary -------------------------------------------------
            SectionCard(title = "Tóm t?t don hàng", icon = Icons.Default.LocationOn) {
                OrderSummarySection(uiState = uiState)
            }
        }
    }
}

// --- Address Form -------------------------------------------------------------

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
            label = { Text("H? và tên ngu?i nh?n") },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrangePrimary),
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = address.phoneNumber,
            onValueChange = { onAddressChanged(address.copy(phoneNumber = it)) },
            label = { Text("S? di?n tho?i") },
            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrangePrimary),
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = address.streetAddress,
            onValueChange = { onAddressChanged(address.copy(streetAddress = it)) },
            label = { Text("Ð?a ch? chi ti?t (s? nhà, tên du?ng)") },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrangePrimary),
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = address.city,
            onValueChange = { onAddressChanged(address.copy(city = it)) },
            label = { Text("T?nh / Thành ph?") },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrangePrimary),
            modifier = Modifier.fillMaxWidth(),
        )
        if (error != null) {
            Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
    }
}

// --- Payment Method Selector --------------------------------------------------

@Composable
private fun PaymentMethodSelector(
    selected: PaymentMethod,
    onSelect: (PaymentMethod) -> Unit,
) {
    val options = listOf(
        PaymentMethod.CASH to "Ti?n m?t khi nh?n hàng (COD)",
        PaymentMethod.CARD to "Th? ngân hàng (Yêu c?u Biometric)",
        PaymentMethod.E_WALLET to "Ví di?n t? MoMo/ZaloPay (Yêu c?u Biometric)",
    )
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { (method, label) ->
            Card(
                onClick = { onSelect(method) },
                shape = RoundedCornerShape(10.dp),
                border = if (selected == method)
                    androidx.compose.foundation.BorderStroke(2.dp, OrangePrimary)
                else null,
                colors = CardDefaults.cardColors(
                    containerColor = if (selected == method)
                        OrangePrimary.copy(alpha = 0.08f)
                    else MaterialTheme.colorScheme.surfaceVariant,
                ),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val icon = when (method) {
                        PaymentMethod.CASH -> Icons.Default.MonetizationOn
                        PaymentMethod.CARD -> Icons.Default.CreditCard
                        else -> Icons.Default.Phone
                    }
                    Icon(icon, contentDescription = null, tint = OrangePrimary)
                    Spacer(Modifier.width(12.dp))
                    Text(label, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

// --- Order Summary ------------------------------------------------------------

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
                Text("%,.0fd".format(item.totalPrice), style = MaterialTheme.typography.bodySmall)
            }
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
        SummaryRow("T?m tính", uiState.subtotal)
        SummaryRow("Phí giao hàng", uiState.deliveryFee)
        if (uiState.discount > 0) SummaryRow("Gi?m giá", -uiState.discount, highlight = true)
        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("T?ng c?ng", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
            Text(
                "%,.0fd".format(uiState.total),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = OrangePrimary,
            )
        }
    }
}

@Composable
private fun SummaryRow(label: String, amount: Double, highlight: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            "${if (amount < 0) "-%,.0f" else "%,.0f"}d".format(kotlin.math.abs(amount)),
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = if (highlight) FontWeight.Bold else FontWeight.Normal,
            ),
            color = if (highlight) SuccessGreen else MaterialTheme.colorScheme.onSurface,
        )
    }
}

// --- Checkout Bottom Bar ------------------------------------------------------

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
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text("T?ng thanh toán", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    "%,.0fd".format(total),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = OrangePrimary,
                )
            }
            BiteFastButton(
                text = if (isLoading) "Ðang x? lý..." else "Ð?t hàng",
                onClick = onPlaceOrder,
                enabled = isEnabled,
                modifier = Modifier.width(160.dp),
            )
        }
    }
}

// --- Voucher Bottom Sheet -----------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VoucherBottomSheet(
    voucherCode: String,
    availableVouchers: List<VoucherWalletItem>,
    isLoading: Boolean,
    error: String?,
    onCodeChanged: (String) -> Unit,
    onApply: () -> Unit,
    onSelectVoucher: (Voucher, Double) -> Unit,
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
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Uu dãi BiteFast",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )

            // Input Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = voucherCode,
                    onValueChange = { onCodeChanged(it.uppercase()) },
                    label = { Text("Nh?p mã uu dãi") },
                    isError = error != null,
                    supportingText = error?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrangePrimary),
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = onApply,
                    enabled = !isLoading && voucherCode.isNotBlank(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                    modifier = Modifier.height(54.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("Áp d?ng", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Text(
                text = "Mã gi?m giá kh? d?ng:",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
            )

            // Scrollable list of vouchers
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 380.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(availableVouchers, key = { it.voucher.id.ifBlank { it.voucher.code } }) { item ->
                    VoucherCard(
                        voucher = item.voucher,
                        isEligible = item.isEligible,
                        calculatedDiscount = item.calculatedDiscount,
                        missingAmount = item.missingAmount,
                        statusMessage = item.statusMessage,
                        isSelected = item.voucher.code == voucherCode,
                        onApply = { onSelectVoucher(item.voucher, item.calculatedDiscount) }
                    )
                }
            }
        }
    }
}

// --- Helpers ------------------------------------------------------------------

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
