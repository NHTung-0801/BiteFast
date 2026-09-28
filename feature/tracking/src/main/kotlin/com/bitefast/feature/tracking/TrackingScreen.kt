package com.bitefast.feature.tracking

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bitefast.core.designsystem.component.BiteFastButton
import com.bitefast.core.designsystem.theme.ErrorRed
import com.bitefast.core.designsystem.theme.OrangePrimary
import com.bitefast.core.designsystem.theme.OrangePrimaryDark
import com.bitefast.core.designsystem.theme.SuccessGreen
import com.bitefast.core.model.Order
import com.bitefast.core.model.OrderStatus
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import java.text.DecimalFormat

@Composable
fun TrackingRoute(
    orderId: String,
    onNavigateBack: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onNavigateToRating: (String) -> Unit = {},
    viewModel: TrackingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is TrackingUiEffect.DialPhone -> {
                    try {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${effect.phoneNumber}"))
                        context.startActivity(intent)
                    } catch (_: Exception) {
                        snackbarHostState.showSnackbar("Goi so: ${effect.phoneNumber}")
                    }
                }
                is TrackingUiEffect.SendSms -> {
                    try {
                        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${effect.phoneNumber}"))
                        context.startActivity(intent)
                    } catch (_: Exception) {
                        snackbarHostState.showSnackbar("Nhan tin den: ${effect.phoneNumber}")
                    }
                }
                is TrackingUiEffect.NavigateToRating -> onNavigateToRating(effect.orderId)
                is TrackingUiEffect.NavigateBack -> onNavigateBack()
                is TrackingUiEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    TrackingScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackingScreen(
    uiState: TrackingUiState,
    snackbarHostState: SnackbarHostState,
    onEvent: (TrackingUiEvent) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.semantics { contentDescription = "Man hinh theo doi don hang" },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Theo doi don hang",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "#${uiState.order?.id ?: uiState.orderId}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Quay lai"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = OrangePrimary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // 1. Live Map View / Visual GPS
                item {
                    TrackingMapCard(
                        restaurantLat = uiState.restaurantLat,
                        restaurantLng = uiState.restaurantLng,
                        customerLat = uiState.customerLat,
                        customerLng = uiState.customerLng,
                        driverLat = uiState.driverLat,
                        driverLng = uiState.driverLng,
                        progressPercent = uiState.progressPercent
                    )
                }

                // 2. Status Banner & Estimated Arrival
                item {
                    EtaStatusCard(
                        orderStatus = uiState.orderStatus,
                        etaMinutes = uiState.etaMinutes
                    )
                }

                // 3. Vertical Step Progress
                item {
                    OrderStatusStepper(currentStepIndex = uiState.currentStepIndex)
                }

                // 4. Driver Information Card
                item {
                    val order = uiState.order
                    if (order != null && order.status != OrderStatus.CANCELED) {
                        DriverInfoCard(
                            driverName = order.driverName.ifBlank { "Nguyen Van Hung" },
                            driverPhone = order.driverPhone.ifBlank { "0901234567" },
                            onCallDriver = { onEvent(TrackingUiEvent.CallDriver) },
                            onMessageDriver = { onEvent(TrackingUiEvent.MessageDriver) }
                        )
                    }
                }

                // 5. Order Summary Breakdown
                item {
                    val order = uiState.order
                    if (order != null) {
                        OrderSummaryCard(order = order)
                    }
                }

                // 6. Action CTAs
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (uiState.orderStatus == OrderStatus.ON_THE_WAY) {
                            BiteFastButton(
                                text = "Toi da nhan duoc hang",
                                onClick = { onEvent(TrackingUiEvent.ConfirmDelivered) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else if (uiState.orderStatus == OrderStatus.DELIVERED) {
                            BiteFastButton(
                                text = "Danh gia don hang 5 sao",
                                onClick = { onEvent(TrackingUiEvent.ClickRateOrder) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        if (uiState.orderStatus in setOf(OrderStatus.PENDING, OrderStatus.CONFIRMED)) {
                            OutlinedButton(
                                onClick = { onEvent(TrackingUiEvent.RequestCancel) },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Huy don hang nay")
                            }
                        }
                    }
                }
            }
        }

        // Cancel Confirmation Dialog
        if (uiState.showCancelDialog) {
            AlertDialog(
                onDismissRequest = { onEvent(TrackingUiEvent.DismissCancelDialog) },
                title = { Text("Xac nhan huy don hang") },
                text = { Text("Ban co chac chan muon huy don hang nay khong? Thao tac nay khong the hoan tac.") },
                confirmButton = {
                    Button(
                        onClick = { onEvent(TrackingUiEvent.ConfirmCancel) },
                        colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                    ) {
                        Text("Xac nhan huy")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { onEvent(TrackingUiEvent.DismissCancelDialog) }) {
                        Text("Giu lai")
                    }
                }
            )
        }
    }
}

// ── 1. Map Card with fallback ────────────────────────────────────────────────

@Composable
private fun TrackingMapCard(
    restaurantLat: Double,
    restaurantLng: Double,
    customerLat: Double,
    customerLng: Double,
    driverLat: Double,
    driverLng: Double,
    progressPercent: Float,
    modifier: Modifier = Modifier
) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(driverLat, driverLng), 14.5f)
    }

    LaunchedEffect(driverLat, driverLng) {
        cameraPositionState.position = CameraPosition.fromLatLngZoom(LatLng(driverLat, driverLng), 14.5f)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(240.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = false,
                    myLocationButtonEnabled = false,
                    mapToolbarEnabled = false
                )
            ) {
                Marker(
                    state = MarkerState(position = LatLng(restaurantLat, restaurantLng)),
                    title = "Nha hang"
                )
                Marker(
                    state = MarkerState(position = LatLng(customerLat, customerLng)),
                    title = "Diem giao hang"
                )
                Marker(
                    state = MarkerState(position = LatLng(driverLat, driverLng)),
                    title = "Tai xe BiteFast"
                )
                Polyline(
                    points = listOf(
                        LatLng(restaurantLat, restaurantLng),
                        LatLng(driverLat, driverLng),
                        LatLng(customerLat, customerLng)
                    ),
                    color = OrangePrimary,
                    width = 10f
                )
            }

            // Fallback & Live Overlay Route progress
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.15f))
                    .padding(12.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                    shadowElevation = 3.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.DirectionsBike,
                                contentDescription = null,
                                tint = OrangePrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Shipper dang cach ban ${(1.8f * (1f - progressPercent)).coerceAtLeast(0.1f).formatOneDecimal()} km",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Text(
                            text = "${(progressPercent * 100).toInt()}% tuyen duong",
                            style = MaterialTheme.typography.labelSmall,
                            color = OrangePrimaryDark,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// ── 2. ETA & Status Card ────────────────────────────────────────────────────

@Composable
private fun EtaStatusCard(
    orderStatus: OrderStatus,
    etaMinutes: Int,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = OrangePrimary.copy(alpha = 0.08f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = when (orderStatus) {
                        OrderStatus.PENDING -> "Dang cho nha hang xac nhan"
                        OrderStatus.CONFIRMED -> "Nha hang da nhan don"
                        OrderStatus.PREPARING -> "Nha hang dang che bien mon"
                        OrderStatus.READY -> "Mon an da san sang"
                        OrderStatus.ON_THE_WAY -> "Tai xe dang giao den ban"
                        OrderStatus.DELIVERED -> "Giao hang thanh cong!"
                        OrderStatus.CANCELED -> "Don hang da bi huy"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (orderStatus == OrderStatus.CANCELED) ErrorRed else OrangePrimaryDark
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (orderStatus == OrderStatus.DELIVERED) {
                            "Da giao luc: Vua xong"
                        } else if (orderStatus == OrderStatus.CANCELED) {
                            "Don da ngung xu ly"
                        } else {
                            "Du kien giao trong: $etaMinutes phut"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (orderStatus == OrderStatus.ON_THE_WAY || orderStatus == OrderStatus.PREPARING) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .scale(pulseScale)
                        .background(SuccessGreen.copy(alpha = 0.25f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(SuccessGreen, CircleShape)
                    )
                }
            }
        }
    }
}

// ── 3. Order Stepper Timeline ───────────────────────────────────────────────

@Composable
private fun OrderStatusStepper(
    currentStepIndex: Int,
    modifier: Modifier = Modifier
) {
    val steps = listOf(
        "Xac nhan don" to "Nha hang tiep nhan",
        "Chuan bi mon" to "Bep dang che bien nong hoi",
        "Dang giao hang" to "Tai xe dang di chuyen den ban",
        "Giao thanh cong" to "Chuc ban ngon mieng"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Tien trinh giao hang",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(14.dp))

            steps.forEachIndexed { index, pair ->
                val (title, subtitle) = pair
                val isCompleted = currentStepIndex > index
                val isCurrent = currentStepIndex == index

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(32.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isCompleted -> SuccessGreen
                                        isCurrent -> OrangePrimary
                                        else -> MaterialTheme.colorScheme.surfaceVariant
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isCompleted) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            } else {
                                Text(
                                    text = "${index + 1}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (isCurrent) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (index < steps.size - 1) {
                            Box(
                                modifier = Modifier
                                    .width(2.dp)
                                    .height(28.dp)
                                    .background(
                                        if (isCompleted) SuccessGreen else MaterialTheme.colorScheme.surfaceVariant
                                    )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.padding(bottom = if (index < steps.size - 1) 16.dp else 0.dp)) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                            color = if (isCurrent) OrangePrimaryDark else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

// ── 4. Driver Info Card ─────────────────────────────────────────────────────

@Composable
private fun DriverInfoCard(
    driverName: String,
    driverPhone: String,
    onCallDriver: () -> Unit,
    onMessageDriver: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(OrangePrimary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.TwoWheeler,
                        contentDescription = null,
                        tint = OrangePrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = driverName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Honda Air Blade • 59-X1 234.56",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFB300),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "4.9 (1.2k+ chuyen)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Row {
                IconButton(
                    onClick = onMessageDriver,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Message,
                            contentDescription = "Nhắn tin cho tài xế",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = onCallDriver,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(SuccessGreen, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "Gọi điện cho tài xế",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

// ── 5. Order Summary Card ───────────────────────────────────────────────────

@Composable
private fun OrderSummaryCard(
    order: Order,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Restaurant,
                    contentDescription = null,
                    tint = OrangePrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = order.restaurantName.ifBlank { "Nha hang doi tac BiteFast" },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = order.address.streetAddress.ifBlank { "Dia chi giao hang mac dinh" },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Divider(modifier = Modifier.padding(vertical = 12.dp))

            order.items.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${item.quantity}x ${item.name}",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "${item.totalPrice.formatVnd()} d",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Divider(modifier = Modifier.padding(vertical = 12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tong thanh toan",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${order.total.formatVnd()} d",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = OrangePrimaryDark
                )
            }
        }
    }
}

private fun Float.formatOneDecimal(): String = DecimalFormat("#.#").format(this)
private fun Double.formatVnd(): String = DecimalFormat("#,###").format(this)
