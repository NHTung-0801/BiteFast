package com.bitefast.core.domain.notification

import com.bitefast.core.domain.repository.NotificationRepository
import com.bitefast.core.model.Notification
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetNotificationsUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository
) {
    operator fun invoke(): Flow<List<Notification>> = notificationRepository.getNotifications()
}

class MarkNotificationAsReadUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository
) {
    suspend operator fun invoke(id: String) {
        notificationRepository.markAsRead(id)
    }

    suspend fun markAll() {
        notificationRepository.markAllAsRead()
    }
}

class DeleteNotificationUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository
) {
    suspend operator fun invoke(id: String) {
        notificationRepository.deleteNotification(id)
    }
}

class ClearAllNotificationsUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository
) {
    suspend operator fun invoke() {
        notificationRepository.clearAllNotifications()
    }
}
