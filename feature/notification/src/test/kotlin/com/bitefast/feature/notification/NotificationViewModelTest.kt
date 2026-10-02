package com.bitefast.feature.notification

import androidx.lifecycle.SavedStateHandle
import com.bitefast.core.domain.notification.ClearAllNotificationsUseCase
import com.bitefast.core.domain.notification.DeleteNotificationUseCase
import com.bitefast.core.domain.notification.GetNotificationsUseCase
import com.bitefast.core.domain.notification.MarkNotificationAsReadUseCase
import com.bitefast.core.model.Notification
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NotificationViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var getNotificationsUseCase: GetNotificationsUseCase
    private lateinit var markNotificationAsReadUseCase: MarkNotificationAsReadUseCase
    private lateinit var deleteNotificationUseCase: DeleteNotificationUseCase
    private lateinit var clearAllNotificationsUseCase: ClearAllNotificationsUseCase

    private val sampleNotification = Notification(
        id = "notif_1",
        userId = "usr_123",
        title = "Đơn hàng đang giao",
        message = "Tài xế đang trên đường đến",
        type = "order",
        isRead = false,
        createdAt = System.currentTimeMillis()
    )

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        getNotificationsUseCase = mockk(relaxed = true)
        markNotificationAsReadUseCase = mockk(relaxed = true)
        deleteNotificationUseCase = mockk(relaxed = true)
        clearAllNotificationsUseCase = mockk(relaxed = true)

        coEvery { getNotificationsUseCase() } returns flowOf(listOf(sampleNotification))
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    @DisplayName("Init loads and sorts notifications list")
    fun init_loadsNotifications() = runTest(testDispatcher) {
        val viewModel = NotificationViewModel(
            getNotificationsUseCase = getNotificationsUseCase,
            markNotificationAsReadUseCase = markNotificationAsReadUseCase,
            deleteNotificationUseCase = deleteNotificationUseCase,
            clearAllNotificationsUseCase = clearAllNotificationsUseCase,
            savedStateHandle = SavedStateHandle()
        )

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(1, state.notifications.size)
        assertEquals("notif_1", state.notifications[0].id)
    }

    @Test
    @DisplayName("ClickNotification marks notification as read")
    fun clickNotification_marksAsRead() = runTest(testDispatcher) {
        val viewModel = NotificationViewModel(
            getNotificationsUseCase = getNotificationsUseCase,
            markNotificationAsReadUseCase = markNotificationAsReadUseCase,
            deleteNotificationUseCase = deleteNotificationUseCase,
            clearAllNotificationsUseCase = clearAllNotificationsUseCase,
            savedStateHandle = SavedStateHandle()
        )
        advanceUntilIdle()

        viewModel.onEvent(NotificationUiEvent.ClickNotification(sampleNotification))
        testDispatcher.scheduler.runCurrent()

        coVerify(exactly = 1) { markNotificationAsReadUseCase("notif_1") }
    }

    @Test
    @DisplayName("DeleteNotification invokes deleteNotificationUseCase")
    fun deleteNotification_invokesUseCase() = runTest(testDispatcher) {
        val viewModel = NotificationViewModel(
            getNotificationsUseCase = getNotificationsUseCase,
            markNotificationAsReadUseCase = markNotificationAsReadUseCase,
            deleteNotificationUseCase = deleteNotificationUseCase,
            clearAllNotificationsUseCase = clearAllNotificationsUseCase,
            savedStateHandle = SavedStateHandle()
        )
        advanceUntilIdle()

        viewModel.onEvent(NotificationUiEvent.DeleteNotification("notif_1"))
        testDispatcher.scheduler.runCurrent()

        coVerify(exactly = 1) { deleteNotificationUseCase("notif_1") }
    }

    @Test
    @DisplayName("ClearAll invokes clearAllNotificationsUseCase")
    fun clearAll_invokesUseCase() = runTest(testDispatcher) {
        val viewModel = NotificationViewModel(
            getNotificationsUseCase = getNotificationsUseCase,
            markNotificationAsReadUseCase = markNotificationAsReadUseCase,
            deleteNotificationUseCase = deleteNotificationUseCase,
            clearAllNotificationsUseCase = clearAllNotificationsUseCase,
            savedStateHandle = SavedStateHandle()
        )
        advanceUntilIdle()

        viewModel.onEvent(NotificationUiEvent.ClearAll)
        testDispatcher.scheduler.runCurrent()

        coVerify(exactly = 1) { clearAllNotificationsUseCase() }
    }
}
