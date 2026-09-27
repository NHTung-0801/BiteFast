package com.bitefast.feature.tracking

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics

@Composable
fun TrackingRoute(orderId: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .semantics { contentDescription = "Màn hình Theo dõi đơn hàng — Đang xây dựng" },
        contentAlignment = Alignment.Center
    ) {
        Text("Theo dõi đơn: $orderId 🚧", style = MaterialTheme.typography.bodyLarge)
    }
}
