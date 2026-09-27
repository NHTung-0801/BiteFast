package com.bitefast.feature.order

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
fun OrderRoute() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .semantics { contentDescription = "Màn hình Đơn hàng — Đang xây dựng" },
        contentAlignment = Alignment.Center
    ) {
        Text("Đơn hàng — Đang phát triển 🚧", style = MaterialTheme.typography.bodyLarge)
    }
}
