package com.bitefast.feature.checkout

import androidx.biometric.BiometricPrompt
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Note
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Receipt
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bitefast.core.designsystem.component.BiteFastButton
import com.bitefast.core.designsystem.component.VoucherCard
import com.bitefast.core.designsystem.component.shimmerBrush
import com.bitefast.core.designsystem.theme.OrangePrimary
import com.bitefast.core.designsystem.theme.SuccessGreen
import com.bitefast.core.domain.voucher.VoucherWalletItem
import com.bitefast.core.model.Address
import com.bitefast.core.model.PaymentMethod
import com.bitefast.core.model.Voucher
import com.bitefast.feature.checkout.security.BiometricAuthStatus
import com.bitefast.feature.checkout.security.BiometricCryptoManager

@Composable
fun CheckoutRoute(
    onNavigateBack: () -> Unit = {},
    onNavigateToTracking: (String) -> Unit = {},
    onNavigateToPaymentResult: (orderId: String, qrUrl: String, amount: Long) -> Unit = { _, _, _ -> },
    viewModel: CheckoutViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val biometricCryptoManager = remember(context) { BiometricCryptoManager(context) }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is CheckoutUiEffect.NavigateToTracking -> onNavigateToTracking(effect.orderId)
                is CheckoutUiEffect.NavigateBack -> onNavigateBack()
                is CheckoutUiEffect.TriggerBiometric -> {
                    val fragmentActivity = context as? FragmentActivity
                    val status = biometricCryptoManager.checkBiometricAvailability()

                    if (fragmentActivity != null && status == BiometricAuthStatus.READY) {
                        val cryptoObject = biometricCryptoManager.createCryptoObject()
                        val promptInfo = biometricCryptoManager.createPromptInfo(
                            title = "Xác thực đơn hàng BiteFast",
                            subtitle = "Quét vân tay hoặc khuôn mặt để phê duyệt thanh toán an toàn"
                        )
                        val executor = ContextCompat.getMainExecutor(context)
                        val biometricPrompt = BiometricPrompt(
                            fragmentActivity,
                            executor,
                            object : BiometricPrompt.AuthenticationCallback() {
                                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                                    super.onAuthenticationSucceeded(result)
                                    viewModel.onEvent(CheckoutUiEvent.BiometricConfirmed)
                                }

                                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                                    super.onAuthenticationError(errorCode, errString)
                                    viewModel.onEvent(CheckoutUiEvent.BiometricDismissed)
                                }

                                override fun onAuthenticationFailed() {
                                    super.onAuthenticationFailed()
                                }
                            }
                        )

                        if (cryptoObject != null) {
                            biometricPrompt.authenticate(promptInfo, cryptoObject)
                        } else {
                            biometricPrompt.authenticate(promptInfo)
                        }
                    } else {
                        // Fallback: nếu thiết bị không hỗ trợ vân tay thì xác nhận thành công
                        viewModel.onEvent(CheckoutUiEvent.BiometricConfirmed)
                    }
                }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    uiState: CheckoutUiState,
    snackbarHostState: SnackbarHostState,
    onEvent: (CheckoutUiEvent) -> Unit,
    onNavigateBack: () -> Unit = {},
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Thanh toán", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        bottomBar = {
            CheckoutBottomBar(
                total = uiState.total,
                isLoading = uiState.isLoading,
                isEnabled = uiState.items.isNotEmpty() && !uiState.isLoading,
                onPlaceOrder = { onEvent(CheckoutUiEvent.PlaceOrder) },
            )
        },
    ) { padding ->
        Crossfade(
            targetState = uiState.isLoading && uiState.items.isEmpty(),
            animationSpec = tween(300),
            label = "CheckoutLoadingCrossfade"
        ) { loading ->
            if (loading) {
                CheckoutScreenSkeleton(modifier = Modifier.padding(padding))
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
            // 1. Địa chỉ giao hàng
            SectionCard(title = "Địa chỉ giao hàng", icon = Icons.Default.LocationOn) {
                DeliveryAddressSection(
                    address = uiState.deliveryAddress,
                    error = uiState.addressError,
                    onAddressChanged = { onEvent(CheckoutUiEvent.AddressChanged(it)) },
                )
            }

            // 2. Phương thức thanh toán
            SectionCard(title = "Phương thức thanh toán", icon = Icons.Default.Payment) {
                PaymentMethodSelector(
                    selected = uiState.selectedPayment,
                    onSelect = { onEvent(CheckoutUiEvent.PaymentMethodSelected(it)) },
                )
            }

            // 3. Ưu đãi & Voucher
            SectionCard(title = "Mã khuyến mãi & Ưu đãi", icon = Icons.Default.LocalOffer) {
                VoucherSection(
                    voucherCode = uiState.voucherCode,
                    discount = uiState.discount,
                    bestSuggestion = uiState.bestVoucherSuggestion,
                    error = uiState.voucherError,
                    onOpenSheet = { onEvent(CheckoutUiEvent.OpenVoucherSheet) },
                    onAutoApply = { onEvent(CheckoutUiEvent.AutoApplyBestVoucher) },
                    onRemove = { onEvent(CheckoutUiEvent.RemoveVoucher) },
                )
            }

            // 4. Ghi chú cho tài xế / nhà hàng
            SectionCard(title = "Ghi chú đơn hàng", icon = Icons.AutoMirrored.Filled.Note) {
                OutlinedTextField(
                    value = uiState.note,
                    onValueChange = { onEvent(CheckoutUiEvent.NoteChanged(it)) },
                    placeholder = { Text("Ví dụ: Ít cay, giao lên tầng 3...") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OrangePrimary,
                    ),
                    singleLine = true,
                )
            }

            // 5. Chi tiết hóa đơn
            SectionCard(title = "Chi tiết thanh toán", icon = Icons.Default.Receipt) {
                OrderSummarySection(uiState = uiState)
            }

            Spacer(Modifier.height(16.dp))
                }
            }
        }
    }

    // Voucher Bottom Sheet
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
}

// --- Delivery Address Section -------------------------------------------------

@Composable
private fun DeliveryAddressSection(
    address: Address,
    error: String?,
    onAddressChanged: (Address) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
            value = address.streetAddress,
            onValueChange = { onAddressChanged(address.copy(streetAddress = it)) },
            label = { Text("Địa chỉ nhận hàng *") },
            placeholder = { Text("Số nhà, tên đường, phường/xã...") },
            isError = error != null && address.streetAddress.isBlank(),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrangePrimary),
            singleLine = true,
        )
        OutlinedTextField(
            value = address.phoneNumber,
            onValueChange = { onAddressChanged(address.copy(phoneNumber = it)) },
            label = { Text("Số điện thoại liên hệ *") },
            placeholder = { Text("09xxxxxxxx") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            isError = error != null && address.phoneNumber.isBlank(),
            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(18.dp)) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrangePrimary),
            singleLine = true,
        )
        if (error != null) {
            Text(
                text = error,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
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
        Triple(PaymentMethod.CASH, "Tiền mặt khi nhận hàng (COD)", Icons.Default.Money),
        Triple(PaymentMethod.CARD, "Thẻ ATM / Visa / Mastercard", Icons.Default.CreditCard),
        Triple(PaymentMethod.E_WALLET, "Ví điện tử MoMo / ZaloPay", Icons.Default.AccountBalanceWallet),
        Triple(PaymentMethod.WALLET, "Ví BiteFast Wallet", Icons.Default.AccountBalance),
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { (method, label, icon) ->
            val isChosen = selected == method
            Card(
                onClick = { onSelect(method) },
                shape = RoundedCornerShape(12.dp),
                border = if (isChosen) androidx.compose.foundation.BorderStroke(2.dp, OrangePrimary) else null,
                colors = CardDefaults.cardColors(
                    containerColor = if (isChosen) OrangePrimary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                ),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isChosen) OrangePrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        if (method != PaymentMethod.CASH) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Fingerprint,
                                    contentDescription = null,
                                    tint = OrangePrimary,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = "Bảo mật sinh trắc học Keystore",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = OrangePrimary
                                )
                            }
                        }
                    }
                    if (isChosen) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Đã chọn",
                            tint = OrangePrimary,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
        }
    }
}

// --- Voucher Section ----------------------------------------------------------

@Composable
private fun VoucherSection(
    voucherCode: String,
    discount: Double,
    bestSuggestion: String?,
    error: String?,
    onOpenSheet: () -> Unit,
    onAutoApply: () -> Unit,
    onRemove: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (voucherCode.isNotBlank() && discount > 0) {
            Surface(
                color = SuccessGreen.copy(alpha = 0.1f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            text = "Đã áp dụng mã: $voucherCode",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = SuccessGreen,
                        )
                        Text(
                            text = "Tiết kiệm %,.0fđ".format(discount),
                            style = MaterialTheme.typography.bodySmall,
                            color = SuccessGreen,
                        )
                    }
                    OutlinedButton(
                        onClick = onRemove,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                        modifier = Modifier.height(32.dp),
                    ) {
                        Text("Gỡ", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                BiteFastButton(
                    text = "Chọn hoặc nhập Voucher",
                    onClick = onOpenSheet,
                    modifier = Modifier.weight(1f),
                )
                if (!bestSuggestion.isNullOrBlank()) {
                    OutlinedButton(
                        onClick = onAutoApply,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = OrangePrimary),
                    ) {
                        Text("Mã tốt nhất", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
        if (error != null) {
            Text(text = error, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
        }
    }
}

// --- Order Summary Section ----------------------------------------------------

@Composable
private fun OrderSummarySection(uiState: CheckoutUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        uiState.items.forEach { item ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "${item.quantity}x ${item.name}",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f),
                    maxLines = 1
                )
                Text(
                    text = "%,.0fđ".format(item.totalPrice),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
        SummaryRow("Tạm tính", uiState.subtotal)
        SummaryRow("Phí giao hàng", uiState.deliveryFee)
        if (uiState.discount > 0) SummaryRow("Giảm giá voucher", -uiState.discount, highlight = true)
        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("Tổng cộng", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
            Text(
                text = "%,.0fđ".format(uiState.total),
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
            text = "${if (amount < 0) "-%,.0f" else "%,.0f"}đ".format(kotlin.math.abs(amount)),
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
                Text("Tổng thanh toán", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    text = "%,.0fđ".format(total),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = OrangePrimary,
                )
            }
            BiteFastButton(
                text = if (isLoading) "Đang xử lý..." else "Đặt hàng",
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
                text = "Ưu đãi BiteFast",
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
                    label = { Text("Nhập mã ưu đãi") },
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
                        Text("Áp dụng", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Text(
                text = "Mã giảm giá khả dụng:",
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

// --- Checkout Screen Skeleton -------------------------------------------------

@Composable
private fun CheckoutScreenSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Address Card Skeleton
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Box(
                    modifier = Modifier
                        .width(140.dp)
                        .height(20.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmerBrush()
                )
                Spacer(Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmerBrush()
                )
                Spacer(Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(14.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmerBrush()
                )
            }
        }

        // Payment Method Card Skeleton
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Box(
                    modifier = Modifier
                        .width(180.dp)
                        .height(20.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmerBrush()
                )
                Spacer(Modifier.height(12.dp))
                repeat(3) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .shimmerBrush()
                        )
                        Spacer(Modifier.width(12.dp))
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(16.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .shimmerBrush()
                        )
                    }
                }
            }
        }

        // Voucher Card Skeleton
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Box(
                    modifier = Modifier
                        .width(160.dp)
                        .height(20.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmerBrush()
                )
                Spacer(Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .shimmerBrush()
                )
            }
        }

        // Order Summary Card Skeleton
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Box(
                    modifier = Modifier
                        .width(150.dp)
                        .height(20.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmerBrush()
                )
                Spacer(Modifier.height(12.dp))
                repeat(2) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .width(120.dp)
                                .height(16.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .shimmerBrush()
                        )
                        Box(
                            modifier = Modifier
                                .width(70.dp)
                                .height(16.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .shimmerBrush()
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .width(80.dp)
                            .height(20.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .shimmerBrush()
                    )
                    Box(
                        modifier = Modifier
                            .width(100.dp)
                            .height(20.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .shimmerBrush()
                    )
                }
            }
        }
    }
}
