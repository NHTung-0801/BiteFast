package com.bitefast.feature.profile

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
fun ProfileRoute() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .semantics { contentDescription = "Màn hình Tài khoản — Đang xây dựng" },
        contentAlignment = Alignment.Center
    ) {
        Text("Tài khoản — Đang phát triển 🚧", style = MaterialTheme.typography.bodyLarge)
    }
}
