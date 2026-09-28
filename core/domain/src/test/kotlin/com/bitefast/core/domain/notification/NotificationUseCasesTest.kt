package com.bitefast.core.domain.notification

import app.cash.turbine.test
import com.bitefast.core.domain.repository.NotificationRepository
import com.bitefast.core.model.Notification
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class NotificationUseCasesTest {

    private lateinit var notificationRepository: NotificationRepository
    private val testNotification = Notification(
        id = "notif_1",
        userId = "u1",
        title = "Đơn hàng đang giao",
        message = "Shipper đang tới",
        type = "ORDER_STATUS",
        createdAt = 1000L,
        isRead = false
    )

    @BeforeEach
    fun setUp() {
        notificationRepository = mockk(relaxed = true)
    }

    @Test
    fun getNotificationsUseCase_emitsList() = runTest {
        every { notificationRepository.getNotifications() } returns flowOf(listOf(testNotification))

        val useCase = GetNotificationsUseCase(notificationRepository)
        useCase().test {
            val list = awaitItem()
            assertEquals(1, list.size)
            assertEquals("notif_1", list[0].id)
            awaitComplete()
        }
    }

    @Test
    fun markNotificationAsReadUseCase_marksSingleAndAll() = runTest {
        val useCase = MarkNotificationAsReadUseCase(notificationRepository)
        useCase("notif_1")
        coVerify(exactly = 1) { notificationRepository.markAsRead("notif_1") }

        useCase.markAll()
        coVerify(exactly = 1) { notificationRepository.markAllAsRead() }
    }

    @Test
    fun deleteNotificationUseCase_deletesSingle() = runTest {
        val useCase = DeleteNotificationUseCase(notificationRepository)
        useCase("notif_1")
        coVerify(exactly = 1) { notificationRepository.deleteNotification("notif_1") }
    }

    @Test
    fun clearAllNotificationsUseCase_clearsAll() = runTest {
        val useCase = ClearAllNotificationsUseCase(notificationRepository)
        useCase()
        coVerify(exactly = 1) { notificationRepository.clearAllNotifications() }
    }
}
