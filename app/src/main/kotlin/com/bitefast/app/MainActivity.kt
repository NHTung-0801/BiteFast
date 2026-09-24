package com.bitefast.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.bitefast.app.navigation.BiteFastApp
import com.bitefast.core.designsystem.theme.BiteFastTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            BiteFastTheme {
                BiteFastApp()
            }
        }
    }
}
