package com.bitefast.feature.checkout.payment

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Timer
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.bitefast.core.designsystem.theme.OrangePrimary
import java.text.NumberFormat
import java.util.Locale

@Composable
fun PaymentResultRoute(
    orderId: String,
    amount: Long,
    qrPayload: String = "",
    onNavigateBack: () -> Unit,
    onNavigateToTracking: (String) -> Unit,
    viewModel: PaymentResultViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(orderId) {
        viewModel.onEvent(PaymentResultUiEvent.InitPayment(orderId, amount, qrPayload))
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is PaymentResultUiEffect.NavigateBack -> onNavigateBack()
                is PaymentResultUiEffect.NavigateToTracking -> onNavigateToTracking(effect.orderId)
                is PaymentResultUiEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    PaymentResultScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onEvent = viewModel::onEvent,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentResultScreen(
    uiState: PaymentResultUiState,
    snackbarHostState: SnackbarHostState,
    onEvent: (PaymentResultUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

    fun copyToClipboard(label: String, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
        onEvent(PaymentResultUiEvent.CopyToClipboard(label, text))
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Thanh toán VietQR",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onEvent(PaymentResultUiEvent.ClickBack) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── 1. Tổng tiền & Trạng thái ─────────────────────────────────────
            Text(
                text = "Tổng số tiền cần thanh toán",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = currencyFormat.format(uiState.amount),
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                color = OrangePrimary
            )

            // Timer Banner
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (uiState.status == PaymentStatusState.EXPIRED) Color(0xFFFFEBEE)
                else OrangePrimary.copy(alpha = 0.1f),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint = if (uiState.status == PaymentStatusState.EXPIRED) Color(0xFFC62828) else OrangePrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (uiState.status == PaymentStatusState.EXPIRED) "Mã QR đã hết hạn"
                        else "Mã QR tự động hết hạn sau: ${uiState.formattedTimer}",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (uiState.status == PaymentStatusState.EXPIRED) Color(0xFFC62828) else OrangePrimary
                    )
                }
            }

            // ── 2. Thẻ VietQR Code ───────────────────────────────────────────
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(240.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (uiState.status == PaymentStatusState.EXPIRED) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = Color.Red,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Mã hết hiệu lực", fontWeight = FontWeight.Bold, color = Color.Red)
                            }
                        } else if (uiState.status == PaymentStatusState.SUCCESS) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF2E7D32),
                                    modifier = Modifier.size(56.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Đã nhận tiền thành công! 🎉", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                            }
                        } else {
                            if (uiState.qrUrl.isNotBlank()) {
                                AsyncImage(
                                    model = uiState.qrUrl,
                                    contentDescription = "Mã VietQR thanh toán",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.fillMaxSize().padding(8.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.QrCode2,
                                    contentDescription = null,
                                    tint = OrangePrimary,
                                    modifier = Modifier.size(160.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Mở App Ngân hàng bất kỳ để quét mã QR",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // ── 3. Thông tin Chuyển khoản Chi tiết ───────────────────────────
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Thông tin chuyển khoản ngân hàng",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Ngân hàng
                    TransferDetailRow(
                        label = "Ngân hàng thụ hưởng",
                        value = uiState.bankName,
                        onCopy = null
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    // Số tài khoản
                    TransferDetailRow(
                        label = "Số tài khoản",
                        value = uiState.accountNumber,
                        onCopy = { copyToClipboard("Số tài khoản", uiState.accountNumber) }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    // Tên người thụ hưởng
                    TransferDetailRow(
                        label = "Người thụ hưởng",
                        value = uiState.accountName,
                        onCopy = null
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    // Số tiền
                    TransferDetailRow(
                        label = "Số tiền chính xác",
                        value = currencyFormat.format(uiState.amount),
                        onCopy = { copyToClipboard("Số tiền", uiState.amount.toString()) }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    // Nội dung chuyển khoản
                    TransferDetailRow(
                        label = "Nội dung chuyển khoản (bắt buộc)",
                        value = uiState.transferContent,
                        highlight = true,
                        onCopy = { copyToClipboard("Nội dung chuyển khoản", uiState.transferContent) }
                    )
                }
            }

            // ── 4. Action Buttons ────────────────────────────────────────────
            if (uiState.status == PaymentStatusState.EXPIRED) {
                Button(
                    onClick = { onEvent(PaymentResultUiEvent.RetryPayment) },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Icon(Icons.Default.Replay, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Tạo lại mã thanh toán mới", fontWeight = FontWeight.Bold)
                }
            } else if (uiState.status == PaymentStatusState.WAITING) {
                Button(
                    onClick = { onEvent(PaymentResultUiEvent.ConfirmPaidManually) },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text("Tôi đã chuyển khoản xong", fontWeight = FontWeight.Bold)
                }
            }

            OutlinedButton(
                onClick = { onEvent(PaymentResultUiEvent.ClickBack) },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Chọn phương thức thanh toán khác")
            }
        }
    }
}

@Composable
private fun TransferDetailRow(
    label: String,
    value: String,
    highlight: Boolean = false,
    onCopy: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (highlight) FontWeight.Bold else FontWeight.SemiBold
                ),
                color = if (highlight) OrangePrimary else MaterialTheme.colorScheme.onSurface
            )
        }
        if (onCopy != null) {
            IconButton(onClick = onCopy, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Sao chép",
                    tint = OrangePrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
