package com.bitefast.app

import android.app.Application
import com.bitefast.app.notification.BiteFastNotificationManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class BiteFastApplication : Application() {

    @Inject
    lateinit var notificationManager: BiteFastNotificationManager

    override fun onCreate() {
        super.onCreate()
        notificationManager.createNotificationChannels()
    }
}
