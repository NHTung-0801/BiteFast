package com.bitefast.feature.order

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.bitefast.core.common.BaseViewModel
import com.bitefast.core.common.UiEffect
import com.bitefast.core.common.UiEvent
import com.bitefast.core.common.UiState
import com.bitefast.core.domain.order.CancelOrderUseCase
import com.bitefast.core.domain.order.CategorizedOrders
import com.bitefast.core.domain.order.GetOrderHistoryUseCase
import com.bitefast.core.model.Order
import com.bitefast.core.model.OrderStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

// ─── Filter Tab ──────────────────────────────────────────────────────────────

enum class OrderFilterTab(val label: String) {
    ALL("Tat ca"),
    ACTIVE("Dang giao"),
    COMPLETED("Hoan thanh"),
    CANCELED("Da huy"),
}

// ─── UiState ─────────────────────────────────────────────────────────────────

data class OrderUiState(
    val isLoading: Boolean = true,
    val activeOrders: List<Order> = emptyList(),
    val completedOrders: List<Order> = emptyList(),
    val selectedTab: OrderFilterTab = OrderFilterTab.ALL,
    val showCancelDialog: Boolean = false,
    val orderToCancel: Order? = null,
    val cancelReason: String = "",
    val errorMessage: String? = null,
) : UiState {
    val allOrders: List<Order> get() = (activeOrders + completedOrders).sortedByDescending { it.orderTime }

    val canceledOrders: List<Order>
        get() = completedOrders.filter { it.status == OrderStatus.CANCELED }

    val filteredOrders: List<Order>
        get() = when (selectedTab) {
            OrderFilterTab.ALL -> allOrders
            OrderFilterTab.ACTIVE -> activeOrders
            OrderFilterTab.COMPLETED -> completedOrders.filter { it.status == OrderStatus.DELIVERED }
            OrderFilterTab.CANCELED -> canceledOrders
        }
}

// ─── UiEvent ─────────────────────────────────────────────────────────────────

sealed interface OrderUiEvent : UiEvent {
    data class SelectTab(val tab: OrderFilterTab) : OrderUiEvent
    data class ClickOrder(val orderId: String) : OrderUiEvent
    data class ClickTrackOrder(val orderId: String) : OrderUiEvent
    data class ClickReorder(val order: Order) : OrderUiEvent
    data class RequestCancelOrder(val order: Order) : OrderUiEvent
    data class CancelReasonChanged(val reason: String) : OrderUiEvent
    data object ConfirmCancelOrder : OrderUiEvent
    data object DismissCancelDialog : OrderUiEvent
    data object DismissError : OrderUiEvent
}

// ─── UiEffect ────────────────────────────────────────────────────────────────

sealed interface OrderUiEffect : UiEffect {
    data class NavigateToTracking(val orderId: String) : OrderUiEffect
    data class NavigateToDetail(val restaurantId: String) : OrderUiEffect
    data class ShowSnackbar(val message: String) : OrderUiEffect
}

// ─── ViewModel ───────────────────────────────────────────────────────────────

@HiltViewModel
class OrderViewModel @Inject constructor(
    private val getOrderHistoryUseCase: GetOrderHistoryUseCase,
    private val cancelOrderUseCase: CancelOrderUseCase,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<OrderUiState, OrderUiEvent, OrderUiEffect>(
    initialState = OrderUiState(),
    savedStateHandle = savedStateHandle,
) {
    init { observeOrders() }

    private fun observeOrders() {
        viewModelScope.launch {
            getOrderHistoryUseCase.categorized().collect { categorized: CategorizedOrders ->
                updateState {
                    it.copy(
                        isLoading = false,
                        activeOrders = categorized.active,
                        completedOrders = categorized.completed,
                    )
                }
            }
        }
    }

    override fun onEvent(event: OrderUiEvent) {
        when (event) {
            is OrderUiEvent.SelectTab -> updateState { it.copy(selectedTab = event.tab) }

            is OrderUiEvent.ClickTrackOrder -> sendEffect(OrderUiEffect.NavigateToTracking(event.orderId))

            is OrderUiEvent.ClickOrder -> {
                val order = uiState.value.allOrders.find { it.id == event.orderId } ?: return
                if (order.status in setOf(OrderStatus.ON_THE_WAY, OrderStatus.PREPARING, OrderStatus.READY)) {
                    sendEffect(OrderUiEffect.NavigateToTracking(event.orderId))
                }
            }

            is OrderUiEvent.ClickReorder -> {
                sendEffect(OrderUiEffect.NavigateToDetail(event.order.restaurantId))
                sendEffect(OrderUiEffect.ShowSnackbar("Dang chuyen den nha hang ${event.order.restaurantName}"))
            }

            is OrderUiEvent.RequestCancelOrder -> {
                updateState { it.copy(showCancelDialog = true, orderToCancel = event.order, cancelReason = "") }
            }

            is OrderUiEvent.CancelReasonChanged -> {
                updateState { it.copy(cancelReason = event.reason) }
            }

            is OrderUiEvent.ConfirmCancelOrder -> {
                val state = uiState.value
                val order = state.orderToCancel ?: return
                updateState { it.copy(showCancelDialog = false) }
                viewModelScope.launch {
                    runCatching { cancelOrderUseCase(order.id, state.cancelReason.ifBlank { "Nguoi dung huy" }) }
                        .onSuccess { sendEffect(OrderUiEffect.ShowSnackbar("Da huy don hang thanh cong")) }
                        .onFailure { sendEffect(OrderUiEffect.ShowSnackbar("Huy don hang that bai. Vui long thu lai.")) }
                }
            }

            is OrderUiEvent.DismissCancelDialog -> {
                updateState { it.copy(showCancelDialog = false, orderToCancel = null) }
            }

            is OrderUiEvent.DismissError -> updateState { it.copy(errorMessage = null) }
        }
    }
}
