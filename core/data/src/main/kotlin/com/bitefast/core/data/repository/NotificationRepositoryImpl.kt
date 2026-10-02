package com.bitefast.core.data.repository

import com.bitefast.core.data.mapper.asEntity
import com.bitefast.core.data.mapper.asExternalModel
import com.bitefast.core.database.dao.NotificationDao
import com.bitefast.core.domain.repository.NotificationRepository
import com.bitefast.core.model.Notification
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val notificationDao: NotificationDao
) : NotificationRepository {

    private val initialNotifications = listOf(
        Notification(
            id = "notif_1",
            userId = "usr_demo",
            title = "Don hang #ord_101 dang duoc giao",
            message = "Tai xe Nguyen Van Hung dang tren duong den dia chi cua ban. Du kien trong 15 phut toi.",
            type = "order",
            data = mapOf("orderId" to "ord_101"),
            isRead = false,
            createdAt = System.currentTimeMillis() - 5 * 60 * 1000L
        ),
        Notification(
            id = "notif_2",
            userId = "usr_demo",
            title = "Tang ban voucher giam 30.000d!",
            message = "Nhap ma BITEFAST30 giam ngay 30k cho don tu 100k. Ap dung cho tat ca nha hang hom nay.",
            type = "promotion",
            data = mapOf("voucherCode" to "BITEFAST30"),
            isRead = false,
            createdAt = System.currentTimeMillis() - 2 * 60 * 60 * 1000L
        ),
        Notification(
            id = "notif_3",
            userId = "usr_demo",
            title = "Don hang #ord_103 da giao thanh cong",
            message = "Cam on ban da thuong thuc Pizza 4P's. Hay chia se danh gia 5 sao cho shipper va nha hang nhe!",
            type = "order",
            data = mapOf("orderId" to "ord_103"),
            isRead = true,
            createdAt = System.currentTimeMillis() - 24 * 60 * 60 * 1000L
        ),
        Notification(
            id = "notif_4",
            userId = "usr_demo",
            title = "Chao mung ban den voi BiteFast 2.0",
            message = "Kham pha he thong theo doi truc tiep thoi gian thuc va hang ngan uu dai am thuc dac sac.",
            type = "system",
            isRead = true,
            createdAt = System.currentTimeMillis() - 2 * 24 * 60 * 60 * 1000L
        )
    )

    private var hasSeeded = false

    override fun getNotifications(): Flow<List<Notification>> =
        notificationDao.getNotifications()
            .onStart {
                if (!hasSeeded) {
                    hasSeeded = true
                    notificationDao.insertNotifications(initialNotifications.map { it.asEntity() })
                }
            }
            .map { list ->
                list.map { it.asExternalModel() }
            }

    override suspend fun addNotification(notification: Notification) {
        notificationDao.insertNotification(notification.asEntity())
    }

    override suspend fun markAsRead(id: String) {
        notificationDao.markAsRead(id)
    }

    override suspend fun markAllAsRead() {
        notificationDao.markAllAsRead()
    }

    override suspend fun deleteNotification(id: String) {
        notificationDao.deleteNotification(id)
    }

    override suspend fun clearAllNotifications() {
        notificationDao.clearAll()
    }
}
