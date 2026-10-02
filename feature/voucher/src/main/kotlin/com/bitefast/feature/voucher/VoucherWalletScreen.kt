package com.bitefast.feature.voucher

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.draw.clip
import com.bitefast.core.designsystem.component.shimmerBrush
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bitefast.core.designsystem.component.EmptyState
import com.bitefast.core.designsystem.component.VoucherCard
import com.bitefast.core.designsystem.theme.OrangePrimary
import com.bitefast.core.domain.voucher.VoucherCategory

@Composable
fun VoucherWalletRoute(
    onNavigateBack: () -> Unit,
    onVoucherSelected: (String, Double) -> Unit = { _, _ -> },
    viewModel: VoucherViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is VoucherUiEffect.NavigateBack -> onNavigateBack()
                is VoucherUiEffect.VoucherSelected -> {
                    onVoucherSelected(effect.voucherCode, effect.discount)
                }
                is VoucherUiEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is VoucherUiEffect.CopyToClipboard -> {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Voucher Code", effect.code)
                    clipboard.setPrimaryClip(clip)
                }
            }
        }
    }

    VoucherWalletScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onEvent = viewModel::onEvent
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoucherWalletScreen(
    uiState: VoucherUiState,
    snackbarHostState: SnackbarHostState,
    onEvent: (VoucherUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Kho Voucher & Khuyến mãi",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onEvent(VoucherUiEvent.ClickBack) }) {
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
        ) {
            // ── Input Promo Code Box ─────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = uiState.customVoucherCode,
                        onValueChange = { onEvent(VoucherUiEvent.CustomCodeChanged(it.uppercase())) },
                        placeholder = { Text("Nhập mã ưu đãi (VD: WELCOME50)") },
                        singleLine = true,
                        isError = uiState.customCodeError != null,
                        supportingText = uiState.customCodeError?.let { { Text(it) } },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OrangePrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = { onEvent(VoucherUiEvent.ApplyCustomCode) },
                        enabled = !uiState.isValidatingCustomCode && uiState.customVoucherCode.isNotBlank(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                        modifier = Modifier.height(54.dp)
                    ) {
                        if (uiState.isValidatingCustomCode) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Áp dụng", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // ── Category Tabs ────────────────────────────────────────────────
            TabRow(
                selectedTabIndex = VoucherCategory.entries.indexOf(uiState.selectedCategory),
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = OrangePrimary
            ) {
                VoucherCategory.entries.forEach { category ->
                    val isSelected = uiState.selectedCategory == category
                    val title = when (category) {
                        VoucherCategory.ALL -> "Tất cả"
                        VoucherCategory.SHIPPING -> "Freeship"
                        VoucherCategory.DISCOUNT -> "Giảm món"
                        VoucherCategory.RESTAURANT -> "Quán ruột"
                    }
                    Tab(
                        selected = isSelected,
                        onClick = { onEvent(VoucherUiEvent.SelectCategory(category)) },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        }
                    )
                }
            }

            // ── Voucher List ─────────────────────────────────────────────────
            if (uiState.isLoading) {
                VoucherWalletSkeleton()
            } else if (uiState.currentItems.isEmpty()) {
                EmptyState(
                    title = "Chưa có voucher phù hợp",
                    subtitle = "Các mã giảm giá mới sẽ được cập nhật liên tục.",
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = uiState.currentItems,
                        key = { it.voucher.id.ifBlank { it.voucher.code } }
                    ) { item ->
                        VoucherCard(
                            voucher = item.voucher,
                            isEligible = item.isEligible,
                            calculatedDiscount = item.calculatedDiscount,
                            missingAmount = item.missingAmount,
                            statusMessage = item.statusMessage,
                            isSelected = item.voucher.code == uiState.selectedCode,
                            onApply = {
                                onEvent(VoucherUiEvent.SelectVoucher(item.voucher, item.calculatedDiscount))
                            },
                            onCopyCode = { onEvent(VoucherUiEvent.CopyCode(it)) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VoucherWalletSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(4) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(108.dp)
            ) {
                Row(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .width(84.dp)
                            .fillMaxHeight()
                            .shimmerBrush()
                    )
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(80.dp)
                                .height(18.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .shimmerBrush()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .height(16.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .shimmerBrush()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.6f)
                                .height(12.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .shimmerBrush()
                        )
                    }
                }
            }
        }
    }
}

