package com.bitefast.feature.auth.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bitefast.core.designsystem.component.BiteFastButton
import com.bitefast.core.designsystem.component.BiteFastOutlinedButton
import com.bitefast.core.designsystem.theme.BiteFastTheme
import com.bitefast.core.designsystem.theme.OrangePrimary
import com.bitefast.core.designsystem.theme.SuccessGreen

/**
 * Reusable LoginGateBottomSheet protecting sensitive actions (Checkout, Order History, Rating).
 * Implements Zero-Data-Loss guarantee for guest users.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginGateBottomSheet(
    onDismiss: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    title: String = "Đăng nhập để tiếp tục",
    description: String = "Vui lòng đăng nhập để lưu trữ thông tin đơn hàng và tiến hành thanh toán.",
    cartItemCount: Int = 0
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Lock Icon Badge
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(OrangePrimary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = OrangePrimary,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Zero-Data-Loss Guarantee Badge
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SuccessGreen.copy(alpha = 0.1f))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = SuccessGreen,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                val cartMessage = if (cartItemCount > 0) {
                    "Giỏ hàng hiện tại ($cartItemCount món) sẽ được bảo toàn nguyên vẹn 100% sau khi đăng nhập."
                } else {
                    "Mọi món ăn trong giỏ hàng sẽ được tự động đồng bộ sang tài khoản của bạn."
                }
                Text(
                    text = cartMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Primary CTA: Navigate to Login
            BiteFastButton(
                text = "Đăng nhập ngay",
                onClick = {
                    onDismiss()
                    onNavigateToLogin()
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Secondary CTA: Register
            BiteFastOutlinedButton(
                text = "Tạo tài khoản mới",
                onClick = {
                    onDismiss()
                    onNavigateToRegister()
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Dismiss Button
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Để sau (tiếp tục xem)",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "Login Gate BottomSheet - Light", showBackground = true)
@Composable
private fun LoginGateBottomSheetLightPreview() {
    BiteFastTheme(darkTheme = false) {
        LoginGateBottomSheet(
            onDismiss = {},
            onNavigateToLogin = {},
            onNavigateToRegister = {},
            cartItemCount = 3
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "Login Gate BottomSheet - Dark", showBackground = true)
@Composable
private fun LoginGateBottomSheetDarkPreview() {
    BiteFastTheme(darkTheme = true) {
        LoginGateBottomSheet(
            onDismiss = {},
            onNavigateToLogin = {},
            onNavigateToRegister = {},
            cartItemCount = 2
        )
    }
}
