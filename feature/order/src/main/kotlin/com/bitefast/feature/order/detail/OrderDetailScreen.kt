package com.bitefast.feature.order.detail

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
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
import com.bitefast.core.designsystem.component.shimmerBrush
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bitefast.core.designsystem.theme.OrangePrimary
import com.bitefast.core.model.Order
import com.bitefast.core.model.OrderStatus
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun OrderDetailRoute(
    orderId: String,
    onNavigateBack: () -> Unit,
    onNavigateToTracking: (String) -> Unit,
    onNavigateToRating: (String) -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToCart: () -> Unit,
    viewModel: OrderDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(orderId) {
        viewModel.onEvent(OrderDetailUiEvent.LoadOrder(orderId))
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is OrderDetailUiEffect.NavigateBack -> onNavigateBack()
                is OrderDetailUiEffect.NavigateToTracking -> onNavigateToTracking(effect.orderId)
                is OrderDetailUiEffect.NavigateToRating -> onNavigateToRating(effect.orderId)
                is OrderDetailUiEffect.NavigateToDetail -> onNavigateToDetail(effect.restaurantId)
                is OrderDetailUiEffect.NavigateToCart -> onNavigateToCart()
                is OrderDetailUiEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
            }
        }
    }

    OrderDetailScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onEvent = viewModel::onEvent,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    uiState: OrderDetailUiState,
    snackbarHostState: SnackbarHostState,
    onEvent: (OrderDetailUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Chi tiết đơn hàng",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onEvent(OrderDetailUiEvent.ClickBack) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { onEvent(OrderDetailUiEvent.OpenSupport) }) {
                        Icon(
                            imageVector = Icons.Default.Headphones,
                            contentDescription = "Trợ giúp & Khiếu nại",
                            tint = OrangePrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            uiState.order?.let { order ->
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        when (order.status) {
                            OrderStatus.PENDING,
                            OrderStatus.CONFIRMED,
                            OrderStatus.PREPARING,
                            OrderStatus.READY,
                            OrderStatus.ON_THE_WAY -> {
                                Button(
                                    onClick = { onEvent(OrderDetailUiEvent.ClickTrack) },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.DeliveryDining, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Theo dõi lộ trình Shipper", fontWeight = FontWeight.Bold)
                                }
                            }
                            OrderStatus.DELIVERED -> {
                                OutlinedButton(
                                    onClick = { onEvent(OrderDetailUiEvent.ClickRate) },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFA000))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Đánh giá")
                                }
                                Button(
                                    onClick = { onEvent(OrderDetailUiEvent.ReOrder) },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                                    enabled = !uiState.isReordering,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    if (uiState.isReordering) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Icon(Icons.Default.Replay, contentDescription = null)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Đặt lại đơn", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            OrderStatus.CANCELED -> {
                                Button(
                                    onClick = { onEvent(OrderDetailUiEvent.ReOrder) },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Replay, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Đặt lại các món này", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                OrderDetailSkeleton(modifier = Modifier.padding(paddingValues))
            }
            uiState.order == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(uiState.errorMessage ?: "Không tìm thấy thông tin đơn hàng")
                }
            }
            else -> {
                val order = uiState.order
                val currencyFormat = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
                val dateFormat = SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale.getDefault())

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(scrollState)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // ── 1. Thẻ Trạng thái Đơn hàng ───────────────────────────
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = when (order.status) {
                                OrderStatus.DELIVERED -> Color(0xFFE8F5E9)
                                OrderStatus.CANCELED -> Color(0xFFFFEBEE)
                                else -> OrangePrimary.copy(alpha = 0.1f)
                            }
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = when (order.status) {
                                    OrderStatus.DELIVERED -> Icons.Default.CheckCircle
                                    OrderStatus.CANCELED -> Icons.Default.Schedule
                                    else -> Icons.Default.DeliveryDining
                                },
                                contentDescription = null,
                                tint = when (order.status) {
                                    OrderStatus.DELIVERED -> Color(0xFF2E7D32)
                                    OrderStatus.CANCELED -> Color(0xFFC62828)
                                    else -> OrangePrimary
                                },
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = when (order.status) {
                                        OrderStatus.PENDING -> "Chờ xác nhận đơn"
                                        OrderStatus.CONFIRMED -> "Quán đã xác nhận"
                                        OrderStatus.PREPARING -> "Quán đang chuẩn bị món"
                                        OrderStatus.READY -> "Món đã sẵn sàng giao"
                                        OrderStatus.ON_THE_WAY -> "Tài xế đang trên đường giao"
                                        OrderStatus.DELIVERED -> "Giao hàng thành công"
                                        OrderStatus.CANCELED -> "Đơn hàng đã hủy"
                                    },
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = when (order.status) {
                                        OrderStatus.DELIVERED -> Color(0xFF2E7D32)
                                        OrderStatus.CANCELED -> Color(0xFFC62828)
                                        else -> OrangePrimary
                                    }
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = dateFormat.format(Date(order.orderTime)),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // ── 2. Mã Đơn & Thông tin Nhà hàng ───────────────────────
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Mã đơn: #${order.id.takeLast(8).uppercase()}",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                IconButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("Mã đơn hàng", order.id))
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Sao chép mã đơn",
                                        tint = OrangePrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onEvent(OrderDetailUiEvent.ClickRestaurant) },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(OrangePrimary.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Storefront,
                                        contentDescription = null,
                                        tint = OrangePrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = order.restaurantName.ifBlank { "Nhà hàng đối tác" },
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "Nhấn để xem thực đơn quán",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = OrangePrimary
                                    )
                                }
                            }
                        }
                    }

                    // ── 3. Địa chỉ nhận hàng & Tài xế ────────────────────────
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = OrangePrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Địa chỉ nhận hàng",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "${order.address.recipientName.ifBlank { "Khách hàng" }} • ${order.address.phoneNumber.ifBlank { "09xxxxxxxx" }}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                            )
                            Text(
                                text = order.address.streetAddress.ifBlank { "Địa chỉ giao hàng mặc định" },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (order.driverName.isNotBlank()) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Tài xế: ${order.driverName}",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = "SĐT: ${order.driverPhone}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${order.driverPhone}"))
                                            context.startActivity(dialIntent)
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Call,
                                            contentDescription = "Gọi tài xế",
                                            tint = OrangePrimary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // ── 4. Danh sách Món ăn đã đặt ───────────────────────────
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Món đã chọn (${order.items.size})",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            order.items.forEachIndexed { index, item ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = OrangePrimary.copy(alpha = 0.1f),
                                        ) {
                                            Text(
                                                text = "${item.quantity}x",
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                color = OrangePrimary,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = item.name,
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                                            )
                                            if (item.customizations.isNotEmpty()) {
                                                Text(
                                                    text = item.customizations.joinToString { c ->
                                                        if (c.selectedOptions.isNotEmpty()) "${c.name}: ${c.selectedOptions.joinToString()}" else c.name
                                                    },
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = currencyFormat.format(item.price * item.quantity),
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                                if (index < order.items.size - 1) {
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))
                                }
                            }
                        }
                    }

                    // ── 5. Bảng Kê Khai Chi Phí & Hóa Đơn (Billing Summary) ───
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Chi tiết thanh toán",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Tạm tính món", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(currencyFormat.format(order.subtotal), style = MaterialTheme.typography.bodyMedium)
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Phí giao hàng", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(currencyFormat.format(order.deliveryFee), style = MaterialTheme.typography.bodyMedium)
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                            if (order.discount > 0) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Khuyến mãi voucher", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF2E7D32))
                                    Text("-${currencyFormat.format(order.discount)}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = Color(0xFF2E7D32))
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Tổng thanh toán",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = currencyFormat.format(order.total),
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = OrangePrimary
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Phương thức thanh toán", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = order.paymentMethod.name,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // ── Dialog Khiếu Nại & CSKH ───────────────────────────────────────────────
    if (uiState.showSupportDialog) {
        AlertDialog(
            onDismissRequest = { onEvent(OrderDetailUiEvent.DismissSupport) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Headphones,
                    contentDescription = null,
                    tint = OrangePrimary,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text("Hỗ trợ đơn hàng & Khiếu nại", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Bạn cần hỗ trợ về đơn hàng này? Đội ngũ CSKH BiteFast luôn túc trực 24/7.")
                    Text("• Hotline khẩn cấp: 1900 8888 (Miễn phí)")
                    Text("• Email khiếu nại / hoàn tiền: support@bitefast.vn")
                    Text("• Đơn hàng được đảm bảo bồi hoàn 100% nếu giao trễ quá 30 phút hoặc giao sai món.")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:19008888"))
                        context.startActivity(dialIntent)
                        onEvent(OrderDetailUiEvent.DismissSupport)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                ) {
                    Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Gọi tổng đài")
                }
            },
            dismissButton = {
                TextButton(onClick = { onEvent(OrderDetailUiEvent.DismissSupport) }) {
                    Text("Đóng")
                }
            }
        )
    }
}

@Composable
private fun OrderDetailSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Status header card shimmer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .clip(RoundedCornerShape(16.dp))
                .shimmerBrush()
        )
        // Restaurant header shimmer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .clip(RoundedCornerShape(16.dp))
                .shimmerBrush()
        )
        // Order items shimmer card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(modifier = Modifier.width(120.dp).height(20.dp).clip(RoundedCornerShape(4.dp)).shimmerBrush())
                repeat(3) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(modifier = Modifier.width(140.dp).height(16.dp).clip(RoundedCornerShape(4.dp)).shimmerBrush())
                            Box(modifier = Modifier.width(80.dp).height(12.dp).clip(RoundedCornerShape(4.dp)).shimmerBrush())
                        }
                        Box(modifier = Modifier.width(60.dp).height(16.dp).clip(RoundedCornerShape(4.dp)).shimmerBrush())
                    }
                }
            }
        }
        // Billing summary shimmer card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(modifier = Modifier.width(130.dp).height(20.dp).clip(RoundedCornerShape(4.dp)).shimmerBrush())
                repeat(4) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Box(modifier = Modifier.width(90.dp).height(14.dp).clip(RoundedCornerShape(4.dp)).shimmerBrush())
                        Box(modifier = Modifier.width(70.dp).height(14.dp).clip(RoundedCornerShape(4.dp)).shimmerBrush())
                    }
                }
            }
        }
    }
}

