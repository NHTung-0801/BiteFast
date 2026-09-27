package com.bitefast.feature.notification

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.bitefast.core.common.BaseViewModel
import com.bitefast.core.common.UiEffect
import com.bitefast.core.common.UiEvent
import com.bitefast.core.common.UiState
import com.bitefast.core.domain.notification.ClearAllNotificationsUseCase
import com.bitefast.core.domain.notification.DeleteNotificationUseCase
import com.bitefast.core.domain.notification.GetNotificationsUseCase
import com.bitefast.core.domain.notification.MarkNotificationAsReadUseCase
import com.bitefast.core.model.Notification
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── Filter Enum ─────────────────────────────────────────────────────────────

enum class NotificationFilter(val label: String) {
    ALL("Tat ca"),
    ORDER("Don hang"),
    PROMOTION("Khuyen mai"),
    SYSTEM("He thong")
}

// ── UiState ─────────────────────────────────────────────────────────────────

data class NotificationUiState(
    val isLoading: Boolean = true,
    val notifications: List<Notification> = emptyList(),
    val selectedFilter: NotificationFilter = NotificationFilter.ALL,
    val errorMessage: String? = null
) : UiState {
    val unreadCount: Int get() = notifications.count { !it.isRead }

    val filteredNotifications: List<Notification>
        get() = when (selectedFilter) {
            NotificationFilter.ALL -> notifications
            NotificationFilter.ORDER -> notifications.filter { it.type.equals("order", ignoreCase = true) }
            NotificationFilter.PROMOTION -> notifications.filter { it.type.equals("promotion", ignoreCase = true) }
            NotificationFilter.SYSTEM -> notifications.filter { it.type.equals("system", ignoreCase = true) }
        }
}

// ── UiEvent ─────────────────────────────────────────────────────────────────

sealed interface NotificationUiEvent : UiEvent {
    data class SelectFilter(val filter: NotificationFilter) : NotificationUiEvent
    data class ClickNotification(val notification: Notification) : NotificationUiEvent
    data class DeleteNotification(val id: String) : NotificationUiEvent
    data object MarkAllAsRead : NotificationUiEvent
    data object ClearAll : NotificationUiEvent
    data object ClickBack : NotificationUiEvent
}

// ── UiEffect ────────────────────────────────────────────────────────────────

sealed interface NotificationUiEffect : UiEffect {
    data class NavigateToOrder(val orderId: String) : NotificationUiEffect
    data object NavigateBack : NotificationUiEffect
    data class ShowSnackbar(val message: String) : NotificationUiEffect
}

// ── ViewModel ───────────────────────────────────────────────────────────────

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val getNotificationsUseCase: GetNotificationsUseCase,
    private val markNotificationAsReadUseCase: MarkNotificationAsReadUseCase,
    private val deleteNotificationUseCase: DeleteNotificationUseCase,
    private val clearAllNotificationsUseCase: ClearAllNotificationsUseCase,
    savedStateHandle: SavedStateHandle
) : BaseViewModel<NotificationUiState, NotificationUiEvent, NotificationUiEffect>(
    initialState = NotificationUiState(),
    savedStateHandle = savedStateHandle
) {

    init {
        observeNotifications()
    }

    private fun observeNotifications() {
        viewModelScope.launch {
            getNotificationsUseCase()
                .catch { emit(emptyList()) }
                .collect { list ->
                    updateState {
                        it.copy(
                            isLoading = false,
                            notifications = list.sortedByDescending { n -> n.createdAt }
                        )
                    }
                }
        }
    }

    override fun onEvent(event: NotificationUiEvent) {
        when (event) {
            is NotificationUiEvent.SelectFilter -> {
                updateState { it.copy(selectedFilter = event.filter) }
            }

            is NotificationUiEvent.ClickNotification -> {
                viewModelScope.launch {
                    markNotificationAsReadUseCase(event.notification.id)
                }
                val orderId = event.notification.data["orderId"]
                if (!orderId.isNullOrBlank()) {
                    sendEffect(NotificationUiEffect.NavigateToOrder(orderId))
                } else {
                    sendEffect(NotificationUiEffect.ShowSnackbar(event.notification.title))
                }
            }

            is NotificationUiEvent.DeleteNotification -> {
                viewModelScope.launch {
                    deleteNotificationUseCase(event.id)
                    sendEffect(NotificationUiEffect.ShowSnackbar("Da xoa thong bao"))
                }
            }

            is NotificationUiEvent.MarkAllAsRead -> {
                viewModelScope.launch {
                    markNotificationAsReadUseCase.markAll()
                    sendEffect(NotificationUiEffect.ShowSnackbar("Da danh dau tat ca la da doc"))
                }
            }

            is NotificationUiEvent.ClearAll -> {
                viewModelScope.launch {
                    clearAllNotificationsUseCase()
                    sendEffect(NotificationUiEffect.ShowSnackbar("Da xoa toan bo thong bao"))
                }
            }

            is NotificationUiEvent.ClickBack -> {
                sendEffect(NotificationUiEffect.NavigateBack)
            }
        }
    }
}
