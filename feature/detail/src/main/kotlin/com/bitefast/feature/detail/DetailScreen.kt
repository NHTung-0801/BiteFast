package com.bitefast.feature.detail

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
fun DetailRoute(restaurantId: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .semantics { contentDescription = "Màn hình Chi tiết nhà hàng — Đang xây dựng" },
        contentAlignment = Alignment.Center
    ) {
        Text("Chi tiết nhà hàng: $restaurantId 🚧", style = MaterialTheme.typography.bodyLarge)
    }
}
