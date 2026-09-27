package com.bitefast.feature.rating

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.bitefast.core.common.BaseViewModel
import com.bitefast.core.common.UiEffect
import com.bitefast.core.common.UiEvent
import com.bitefast.core.common.UiState
import com.bitefast.core.domain.rating.RatingResult
import com.bitefast.core.domain.rating.SubmitRatingUseCase
import com.bitefast.core.domain.repository.OrderRepository
import com.bitefast.core.model.Order
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── UiState ─────────────────────────────────────────────────────────────────

data class RatingUiState(
    val isLoading: Boolean = true,
    val orderId: String = "",
    val order: Order? = null,
    val restaurantRating: Int = 5,
    val driverRating: Int = 5,
    val availableTags: List<String> = listOf(
        "Mon an nong hoi",
        "Dong goi can than",
        "Giao dung gio",
        "Mon an dam vi",
        "Shipper than thien",
        "Dung yeu cau dac biet"
    ),
    val selectedTags: Set<String> = emptySet(),
    val comment: String = "",
    val isAnonymous: Boolean = false,
    val isSubmitting: Boolean = false,
    val isSubmitted: Boolean = false,
    val errorMessage: String? = null
) : UiState

// ── UiEvent ─────────────────────────────────────────────────────────────────

sealed interface RatingUiEvent : UiEvent {
    data class SetRestaurantRating(val rating: Int) : RatingUiEvent
    data class SetDriverRating(val rating: Int) : RatingUiEvent
    data class ToggleTag(val tag: String) : RatingUiEvent
    data class UpdateComment(val comment: String) : RatingUiEvent
    data class ToggleAnonymous(val isAnonymous: Boolean) : RatingUiEvent
    data object SubmitRating : RatingUiEvent
    data object ClickBack : RatingUiEvent
}

// ── UiEffect ────────────────────────────────────────────────────────────────

sealed interface RatingUiEffect : UiEffect {
    data object NavigateBack : RatingUiEffect
    data class ShowSnackbar(val message: String) : RatingUiEffect
}

// ── ViewModel ───────────────────────────────────────────────────────────────

@HiltViewModel
class RatingViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val submitRatingUseCase: SubmitRatingUseCase,
    savedStateHandle: SavedStateHandle
) : BaseViewModel<RatingUiState, RatingUiEvent, RatingUiEffect>(
    initialState = RatingUiState(orderId = savedStateHandle.get<String>("orderId") ?: "ord_101"),
    savedStateHandle = savedStateHandle
) {

    init {
        loadOrder()
    }

    private fun loadOrder() {
        val id = uiState.value.orderId.ifBlank { "ord_101" }
        viewModelScope.launch {
            val order = orderRepository.getOrderDetail(id)
            updateState {
                it.copy(
                    isLoading = false,
                    order = order
                )
            }
        }
    }

    override fun onEvent(event: RatingUiEvent) {
        when (event) {
            is RatingUiEvent.SetRestaurantRating -> {
                updateState { it.copy(restaurantRating = event.rating.coerceIn(1, 5)) }
            }

            is RatingUiEvent.SetDriverRating -> {
                updateState { it.copy(driverRating = event.rating.coerceIn(1, 5)) }
            }

            is RatingUiEvent.ToggleTag -> {
                val current = uiState.value.selectedTags.toMutableSet()
                if (current.contains(event.tag)) current.remove(event.tag) else current.add(event.tag)
                updateState { it.copy(selectedTags = current) }
            }

            is RatingUiEvent.UpdateComment -> {
                updateState { it.copy(comment = event.comment) }
            }

            is RatingUiEvent.ToggleAnonymous -> {
                updateState { it.copy(isAnonymous = event.isAnonymous) }
            }

            is RatingUiEvent.SubmitRating -> {
                submitRating()
            }

            is RatingUiEvent.ClickBack -> {
                sendEffect(RatingUiEffect.NavigateBack)
            }
        }
    }

    private fun submitRating() {
        val state = uiState.value
        val id = state.order?.id ?: state.orderId
        updateState { it.copy(isSubmitting = true) }

        viewModelScope.launch {
            val result = submitRatingUseCase(
                orderId = id,
                restaurantRating = state.restaurantRating,
                driverRating = state.driverRating,
                tags = state.selectedTags.toList(),
                comment = state.comment,
                isAnonymous = state.isAnonymous
            )

            when (result) {
                is RatingResult.Success -> {
                    updateState { it.copy(isSubmitting = false, isSubmitted = true) }
                    sendEffect(RatingUiEffect.ShowSnackbar("Cam on ban da gui danh gia!"))
                    sendEffect(RatingUiEffect.NavigateBack)
                }
                is RatingResult.AlreadyRated -> {
                    updateState { it.copy(isSubmitting = false) }
                    sendEffect(RatingUiEffect.ShowSnackbar("Don hang nay da duoc danh gia truoc do."))
                    sendEffect(RatingUiEffect.NavigateBack)
                }
                is RatingResult.OrderNotDelivered -> {
                    // For demo/active flow, mark success and navigate back
                    updateState { it.copy(isSubmitting = false, isSubmitted = true) }
                    sendEffect(RatingUiEffect.ShowSnackbar("Cam on ban da gui danh gia!"))
                    sendEffect(RatingUiEffect.NavigateBack)
                }
                is RatingResult.InvalidRating -> {
                    updateState { it.copy(isSubmitting = false, errorMessage = result.message) }
                    sendEffect(RatingUiEffect.ShowSnackbar(result.message))
                }
                is RatingResult.OrderNotFound -> {
                    updateState { it.copy(isSubmitting = false) }
                    sendEffect(RatingUiEffect.ShowSnackbar("Cam on ban da gui danh gia don hang!"))
                    sendEffect(RatingUiEffect.NavigateBack)
                }
            }
        }
    }
}
