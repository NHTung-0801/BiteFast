package com.bitefast.core.data.repository

import com.bitefast.core.database.dao.NotificationDao
import com.bitefast.core.database.entity.NotificationEntity
import com.bitefast.core.model.Notification
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class NotificationRepositoryImplTest {

    private lateinit var notificationDao: NotificationDao
    private lateinit var repository: NotificationRepositoryImpl

    private val sampleEntities = listOf(
        NotificationEntity(
            id = "notif_test_1",
            userId = "usr_1",
            title = "Đơn hàng đang giao",
            message = "Tài xế đang đến",
            type = "order",
            orderId = "ord_101",
            isRead = false,
            createdAt = 1000L
        ),
        NotificationEntity(
            id = "notif_test_2",
            userId = "usr_1",
            title = "Mã giảm giá",
            message = "Giảm 30k",
            type = "promotion",
            voucherCode = "BITE30",
            isRead = true,
            createdAt = 2000L
        )
    )

    @BeforeEach
    fun setUp() {
        notificationDao = mockk(relaxed = true)
        coEvery { notificationDao.getNotifications() } returns flowOf(sampleEntities)
        coEvery { notificationDao.insertNotifications(any()) } just Runs
        coEvery { notificationDao.insertNotification(any()) } just Runs
        coEvery { notificationDao.markAsRead(any()) } just Runs
        coEvery { notificationDao.markAllAsRead() } just Runs
        coEvery { notificationDao.deleteNotification(any()) } just Runs
        coEvery { notificationDao.clearAll() } just Runs

        repository = NotificationRepositoryImpl(notificationDao)
    }

    @Test
    @DisplayName("getNotifications emits mapped domain notifications from Room Dao")
    fun getNotifications_emitsMappedModels() = runTest {
        val result = repository.getNotifications().first()

        assertEquals(2, result.size)
        assertEquals("notif_test_1", result[0].id)
        assertEquals("Đơn hàng đang giao", result[0].title)
        assertEquals("ord_101", result[0].data["orderId"])
        assertFalse(result[0].isRead)

        assertEquals("notif_test_2", result[1].id)
        assertEquals("BITE30", result[1].data["voucherCode"])
        assertTrue(result[1].isRead)
    }

    @Test
    @DisplayName("addNotification calls dao insertNotification with entity")
    fun addNotification_callsDao() = runTest {
        val notif = Notification(
            id = "notif_new",
            title = "Test Title",
            message = "Test Message",
            type = "system"
        )
        repository.addNotification(notif)

        coVerify(exactly = 1) {
            notificationDao.insertNotification(match { it.id == "notif_new" && it.title == "Test Title" })
        }
    }

    @Test
    @DisplayName("markAsRead delegates to notificationDao")
    fun markAsRead_delegatesToDao() = runTest {
        repository.markAsRead("notif_test_1")
        coVerify(exactly = 1) { notificationDao.markAsRead("notif_test_1") }
    }

    @Test
    @DisplayName("markAllAsRead delegates to notificationDao")
    fun markAllAsRead_delegatesToDao() = runTest {
        repository.markAllAsRead()
        coVerify(exactly = 1) { notificationDao.markAllAsRead() }
    }

    @Test
    @DisplayName("deleteNotification delegates to notificationDao")
    fun deleteNotification_delegatesToDao() = runTest {
        repository.deleteNotification("notif_test_1")
        coVerify(exactly = 1) { notificationDao.deleteNotification("notif_test_1") }
    }

    @Test
    @DisplayName("clearAllNotifications delegates to notificationDao")
    fun clearAllNotifications_delegatesToDao() = runTest {
        repository.clearAllNotifications()
        coVerify(exactly = 1) { notificationDao.clearAll() }
    }
}
