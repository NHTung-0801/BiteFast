package com.bitefast.feature.order.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.bitefast.core.common.BaseViewModel
import com.bitefast.core.common.UiEffect
import com.bitefast.core.common.UiEvent
import com.bitefast.core.common.UiState
import com.bitefast.core.domain.order.GetOrderTrackingUseCase
import com.bitefast.core.domain.order.SmartReOrderResult
import com.bitefast.core.domain.order.SmartReOrderUseCase
import com.bitefast.core.model.Order
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OrderDetailUiState(
    val isLoading: Boolean = true,
    val order: Order? = null,
    val isReordering: Boolean = false,
    val showSupportDialog: Boolean = false,
    val errorMessage: String? = null,
) : UiState

sealed interface OrderDetailUiEvent : UiEvent {
    data class LoadOrder(val orderId: String) : OrderDetailUiEvent
    data object ReOrder : OrderDetailUiEvent
    data object ClickTrack : OrderDetailUiEvent
    data object ClickRate : OrderDetailUiEvent
    data object ClickRestaurant : OrderDetailUiEvent
    data object OpenSupport : OrderDetailUiEvent
    data object DismissSupport : OrderDetailUiEvent
    data object ClickBack : OrderDetailUiEvent
}

sealed interface OrderDetailUiEffect : UiEffect {
    data object NavigateBack : OrderDetailUiEffect
    data class NavigateToTracking(val orderId: String) : OrderDetailUiEffect
    data class NavigateToRating(val orderId: String) : OrderDetailUiEffect
    data class NavigateToDetail(val restaurantId: String) : OrderDetailUiEffect
    data object NavigateToCart : OrderDetailUiEffect
    data class ShowSnackbar(val message: String) : OrderDetailUiEffect
}

@HiltViewModel
class OrderDetailViewModel @Inject constructor(
    private val getOrderTrackingUseCase: GetOrderTrackingUseCase,
    private val smartReOrderUseCase: SmartReOrderUseCase,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<OrderDetailUiState, OrderDetailUiEvent, OrderDetailUiEffect>(
    initialState = OrderDetailUiState(),
    savedStateHandle = savedStateHandle,
) {

    init {
        savedStateHandle.get<String>("orderId")?.let { orderId ->
            if (orderId.isNotBlank()) {
                loadOrder(orderId)
            }
        }
    }

    fun loadOrder(orderId: String) {
        updateState { it.copy(isLoading = true, errorMessage = null) }
        getOrderTrackingUseCase.stream(orderId)
            .onEach { order ->
                updateState {
                    it.copy(
                        isLoading = false,
                        order = order,
                        errorMessage = if (order == null) "Không tìm thấy đơn hàng #$orderId" else null
                    )
                }
            }
            .catch { e ->
                updateState {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Lỗi tải chi tiết đơn hàng."
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    override fun onEvent(event: OrderDetailUiEvent) {
        when (event) {
            is OrderDetailUiEvent.LoadOrder -> {
                loadOrder(event.orderId)
            }

            is OrderDetailUiEvent.ReOrder -> {
                val currentOrder = uiState.value.order ?: return
                viewModelScope.launch {
                    updateState { it.copy(isReordering = true) }
                    when (val result = smartReOrderUseCase(currentOrder)) {
                        is SmartReOrderResult.Success -> {
                            updateState { it.copy(isReordering = false) }
                            sendEffect(OrderDetailUiEffect.NavigateToCart)
                        }
                        is SmartReOrderResult.RestaurantClosed -> {
                            updateState { it.copy(isReordering = false) }
                            sendEffect(OrderDetailUiEffect.ShowSnackbar("Nhà hàng hiện đang đóng cửa"))
                        }
                        is SmartReOrderResult.ItemsUnavailable -> {
                            updateState { it.copy(isReordering = false) }
                            sendEffect(OrderDetailUiEffect.ShowSnackbar("Một số món đã hết hàng: ${result.unavailableItemNames.joinToString(", ")}"))
                            sendEffect(OrderDetailUiEffect.NavigateToCart)
                        }
                        is SmartReOrderResult.CartConflict -> {
                            updateState { it.copy(isReordering = false) }
                            sendEffect(OrderDetailUiEffect.ShowSnackbar("Giỏ hàng đang có món của quán khác"))
                        }
                        is SmartReOrderResult.EmptyOrder -> {
                            updateState { it.copy(isReordering = false) }
                            sendEffect(OrderDetailUiEffect.ShowSnackbar("Đơn hàng rỗng"))
                        }
                    }
                }
            }

            is OrderDetailUiEvent.ClickTrack -> {
                uiState.value.order?.let {
                    sendEffect(OrderDetailUiEffect.NavigateToTracking(it.id))
                }
            }

            is OrderDetailUiEvent.ClickRate -> {
                uiState.value.order?.let {
                    sendEffect(OrderDetailUiEffect.NavigateToRating(it.id))
                }
            }

            is OrderDetailUiEvent.ClickRestaurant -> {
                uiState.value.order?.let {
                    sendEffect(OrderDetailUiEffect.NavigateToDetail(it.restaurantId))
                }
            }

            is OrderDetailUiEvent.OpenSupport -> {
                updateState { it.copy(showSupportDialog = true) }
            }

            is OrderDetailUiEvent.DismissSupport -> {
                updateState { it.copy(showSupportDialog = false) }
            }

            is OrderDetailUiEvent.ClickBack -> {
                sendEffect(OrderDetailUiEffect.NavigateBack)
            }
        }
    }
}
