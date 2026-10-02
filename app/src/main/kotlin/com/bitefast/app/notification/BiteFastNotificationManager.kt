package com.bitefast.app.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.bitefast.app.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Quan ly thong bao he thong Android (Channels, Heads-up Notification, Deep-linking).
 */
@Singleton
class BiteFastNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        const val CHANNEL_ORDER_UPDATES = "bitefast_order_updates"
        const val CHANNEL_PROMOTIONS = "bitefast_promotions"

        private const val CHANNEL_ORDER_NAME = "Đơn hàng & Theo dõi trực tiếp"
        private const val CHANNEL_ORDER_DESC = "Cập nhật tiến trình tài xế và trạng thái giao hàng"

        private const val CHANNEL_PROMO_NAME = "Ưu đãi & Khuyến mãi"
        private const val CHANNEL_PROMO_DESC = "Thông báo voucher và chương trình giảm giá đặc biệt"
    }

    /**
     * Tao cac Notification Channels tren Android 8.0 (API 26) tro len.
     */
    fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val orderChannel = NotificationChannel(
                CHANNEL_ORDER_UPDATES,
                CHANNEL_ORDER_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_ORDER_DESC
                enableVibration(true)
                enableLights(true)
            }

            val promoChannel = NotificationChannel(
                CHANNEL_PROMOTIONS,
                CHANNEL_PROMO_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_PROMO_DESC
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannels(listOf(orderChannel, promoChannel))
        }
    }

    /**
     * Hien thi thong bao cap nhat trang thai don hang kem Deep Link mo man hinh Tracking.
     */
    fun showOrderNotification(orderId: String, title: String, message: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigateTo", "tracking")
            putExtra("orderId", orderId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            orderId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ORDER_UPDATES)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(orderId.hashCode(), notification)
        } catch (_: SecurityException) {
            // Bo qua neu nguoi dung chua cap quyen POST_NOTIFICATIONS
        }
    }

    /**
     * Hien thi thong bao chuong trinh khuyen mai voucher.
     */
    fun showPromotionNotification(voucherCode: String?, title: String, message: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigateTo", "voucher_wallet")
            voucherCode?.let { putExtra("voucherCode", it) }
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            title.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_PROMOTIONS)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(title.hashCode(), notification)
        } catch (_: SecurityException) {
            // Bo qua neu nguoi dung chua cap quyen POST_NOTIFICATIONS
        }
    }
}
