package com.bitefast.feature.tracking

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.bitefast.core.common.BaseViewModel
import com.bitefast.core.common.UiEffect
import com.bitefast.core.common.UiEvent
import com.bitefast.core.common.UiState
import com.bitefast.core.domain.order.CancelOrderUseCase
import com.bitefast.core.domain.order.GetOrderTrackingUseCase
import com.bitefast.core.model.Order
import com.bitefast.core.model.OrderStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── UiState ─────────────────────────────────────────────────────────────────

data class TrackingUiState(
    val isLoading: Boolean = true,
    val orderId: String = "",
    val order: Order? = null,
    val restaurantLat: Double = 10.8490,
    val restaurantLng: Double = 106.7725,
    val customerLat: Double = 10.8540,
    val customerLng: Double = 106.7810,
    val driverLat: Double = 10.8510,
    val driverLng: Double = 106.7760,
    val progressPercent: Float = 0.45f,
    val etaMinutes: Int = 18,
    val showCancelDialog: Boolean = false,
    val errorMessage: String? = null
) : UiState {
    val orderStatus: OrderStatus get() = order?.status ?: OrderStatus.PENDING

    val currentStepIndex: Int get() = when (orderStatus) {
        OrderStatus.PENDING, OrderStatus.CONFIRMED -> 0
        OrderStatus.PREPARING -> 1
        OrderStatus.READY, OrderStatus.ON_THE_WAY -> 2
        OrderStatus.DELIVERED -> 3
        OrderStatus.CANCELED -> -1
    }

    val isDelivered: Boolean get() = orderStatus == OrderStatus.DELIVERED
    val isCanceled: Boolean get() = orderStatus == OrderStatus.CANCELED
}

// ── UiEvent ─────────────────────────────────────────────────────────────────

sealed interface TrackingUiEvent : UiEvent {
    data object Refresh : TrackingUiEvent
    data object CallDriver : TrackingUiEvent
    data object MessageDriver : TrackingUiEvent
    data object CallRestaurant : TrackingUiEvent
    data object RequestCancel : TrackingUiEvent
    data object ConfirmCancel : TrackingUiEvent
    data object DismissCancelDialog : TrackingUiEvent
    data object ConfirmDelivered : TrackingUiEvent
    data object ClickRateOrder : TrackingUiEvent
    data object ClickBack : TrackingUiEvent
}

// ── UiEffect ────────────────────────────────────────────────────────────────

sealed interface TrackingUiEffect : UiEffect {
    data class DialPhone(val phoneNumber: String) : TrackingUiEffect
    data class SendSms(val phoneNumber: String) : TrackingUiEffect
    data class NavigateToRating(val orderId: String) : TrackingUiEffect
    data object NavigateBack : TrackingUiEffect
    data class ShowSnackbar(val message: String) : TrackingUiEffect
}

// ── ViewModel ───────────────────────────────────────────────────────────────

@HiltViewModel
class TrackingViewModel @Inject constructor(
    private val getOrderTrackingUseCase: GetOrderTrackingUseCase,
    private val cancelOrderUseCase: CancelOrderUseCase,
    savedStateHandle: SavedStateHandle
) : BaseViewModel<TrackingUiState, TrackingUiEvent, TrackingUiEffect>(
    initialState = TrackingUiState(orderId = savedStateHandle.get<String>("orderId") ?: "ord_101"),
    savedStateHandle = savedStateHandle
) {

    private var simulationJob: Job? = null

    init {
        loadOrder()
    }

    private fun loadOrder() {
        val id = uiState.value.orderId.ifBlank { "ord_101" }
        viewModelScope.launch {
            getOrderTrackingUseCase.stream(id)
                .catch { emit(getOrderTrackingUseCase(id)) }
                .collect { order ->
                    if (order != null) {
                        val resLat = 10.8490
                        val resLng = 106.7725
                        val custLat = if (order.address.latitude != 0.0) order.address.latitude else 10.8540
                        val custLng = if (order.address.longitude != 0.0) order.address.longitude else 106.7810

                        updateState {
                            it.copy(
                                isLoading = false,
                                order = order,
                                restaurantLat = resLat,
                                restaurantLng = resLng,
                                customerLat = custLat,
                                customerLng = custLng,
                                driverLat = resLat + (custLat - resLat) * it.progressPercent,
                                driverLng = resLng + (custLng - resLng) * it.progressPercent,
                                errorMessage = null
                            )
                        }

                        if (order.status == OrderStatus.ON_THE_WAY) {
                            startDriverSimulation()
                        } else {
                            simulationJob?.cancel()
                        }
                    } else {
                        updateState { it.copy(isLoading = false, errorMessage = "Khong tim thay don hang") }
                    }
                }
        }
    }

    private fun startDriverSimulation() {
        simulationJob?.cancel()
        simulationJob = viewModelScope.launch {
            while (isActive) {
                delay(3000)
                val current = uiState.value
                if (current.order?.status != OrderStatus.ON_THE_WAY) break

                val newProgress = (current.progressPercent + 0.04f).coerceAtMost(0.98f)
                val newEta = ((1.0f - newProgress) * 20).toInt().coerceAtLeast(1)
                val newDriverLat = current.restaurantLat + (current.customerLat - current.restaurantLat) * newProgress
                val newDriverLng = current.restaurantLng + (current.customerLng - current.restaurantLng) * newProgress

                updateState {
                    it.copy(
                        progressPercent = newProgress,
                        etaMinutes = newEta,
                        driverLat = newDriverLat,
                        driverLng = newDriverLng
                    )
                }
            }
        }
    }

    override fun onEvent(event: TrackingUiEvent) {
        when (event) {
            is TrackingUiEvent.Refresh -> loadOrder()

            is TrackingUiEvent.CallDriver -> {
                val phone = uiState.value.order?.driverPhone?.ifBlank { "0901234567" } ?: "0901234567"
                sendEffect(TrackingUiEffect.DialPhone(phone))
            }

            is TrackingUiEvent.MessageDriver -> {
                val phone = uiState.value.order?.driverPhone?.ifBlank { "0901234567" } ?: "0901234567"
                sendEffect(TrackingUiEffect.SendSms(phone))
            }

            is TrackingUiEvent.CallRestaurant -> {
                sendEffect(TrackingUiEffect.DialPhone("19001234"))
            }

            is TrackingUiEvent.RequestCancel -> {
                updateState { it.copy(showCancelDialog = true) }
            }

            is TrackingUiEvent.DismissCancelDialog -> {
                updateState { it.copy(showCancelDialog = false) }
            }

            is TrackingUiEvent.ConfirmCancel -> {
                val order = uiState.value.order ?: return
                updateState { it.copy(showCancelDialog = false) }
                viewModelScope.launch {
                    runCatching { cancelOrderUseCase(order.id, "Nguoi dung huy tren man hinh theo doi") }
                        .onSuccess {
                            sendEffect(TrackingUiEffect.ShowSnackbar("Da huy don hang thanh cong"))
                            updateState {
                                it.copy(order = it.order?.copy(status = OrderStatus.CANCELED, isCanceled = true))
                            }
                        }
                        .onFailure {
                            sendEffect(TrackingUiEffect.ShowSnackbar("Huy don that bai. Vui long lien he CSKH."))
                        }
                }
            }

            is TrackingUiEvent.ConfirmDelivered -> {
                simulationJob?.cancel()
                val order = uiState.value.order ?: return
                updateState {
                    it.copy(
                        order = order.copy(status = OrderStatus.DELIVERED, deliveredTime = System.currentTimeMillis()),
                        progressPercent = 1.0f,
                        etaMinutes = 0
                    )
                }
                sendEffect(TrackingUiEffect.ShowSnackbar("Chuc ban ngon mieng! Hay danh gia don hang nhe."))
            }

            is TrackingUiEvent.ClickRateOrder -> {
                val id = uiState.value.order?.id ?: uiState.value.orderId
                sendEffect(TrackingUiEffect.NavigateToRating(id))
            }

            is TrackingUiEvent.ClickBack -> {
                sendEffect(TrackingUiEffect.NavigateBack)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        simulationJob?.cancel()
    }
}
