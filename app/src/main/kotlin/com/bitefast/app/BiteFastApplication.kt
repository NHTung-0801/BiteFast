package com.bitefast.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class BiteFastApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
