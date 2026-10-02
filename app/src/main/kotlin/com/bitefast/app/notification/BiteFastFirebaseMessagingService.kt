package com.bitefast.app.notification

import com.bitefast.core.domain.repository.NotificationRepository
import com.bitefast.core.model.Notification
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Service tiep nhan push notifications tu Firebase Cloud Messaging (FCM).
 */
@AndroidEntryPoint
class BiteFastFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var notificationRepository: NotificationRepository

    @Inject
    lateinit var notificationManager: BiteFastNotificationManager

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Token moi se duoc dong bo len backend khi can thiet
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val data = remoteMessage.data
        val orderId = data["orderId"]
        val voucherCode = data["voucherCode"]
        val type = data["type"] ?: if (orderId != null) "order" else if (voucherCode != null) "promotion" else "system"

        val title = remoteMessage.notification?.title
            ?: data["title"]
            ?: if (type == "order") "Cập nhật đơn hàng" else "Thông báo từ BiteFast"

        val body = remoteMessage.notification?.body
            ?: data["message"]
            ?: "Bạn có thông báo mới từ BiteFast."

        // 1. Luu thong bao vao Room DB qua NotificationRepository (Offline-First)
        serviceScope.launch {
            val notification = Notification(
                id = "notif_${System.currentTimeMillis()}",
                title = title,
                message = body,
                type = type,
                data = data,
                isRead = false,
                createdAt = System.currentTimeMillis()
            )
            notificationRepository.addNotification(notification)
        }

        // 2. Hien thi Heads-up Notification tren thanh thong bao he thong
        if (!orderId.isNullOrBlank()) {
            notificationManager.showOrderNotification(orderId, title, body)
        } else {
            notificationManager.showPromotionNotification(voucherCode, title, body)
        }
    }
}
