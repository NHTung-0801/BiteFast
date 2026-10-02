package com.bitefast.feature.profile.address

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.bitefast.core.common.BaseViewModel
import com.bitefast.core.common.UiEffect
import com.bitefast.core.common.UiEvent
import com.bitefast.core.common.UiState
import com.bitefast.core.domain.user.AddressValidationResult
import com.bitefast.core.domain.user.DeleteAddressUseCase
import com.bitefast.core.domain.user.GetAddressesUseCase
import com.bitefast.core.domain.user.SaveAddressUseCase
import com.bitefast.core.domain.user.SetDefaultAddressUseCase
import com.bitefast.core.model.Address
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddressUiState(
    val isLoading: Boolean = true,
    val addresses: List<Address> = emptyList(),
    val selectedAddressId: String? = null,
    // Picker state
    val pickerLat: Double = 10.7769,
    val pickerLng: Double = 106.7009,
    val pickerStreetAddress: String = "134 Nguyễn Huệ, Phường Bến Nghé, Quận 1",
    val pickerCity: String = "TP. Hồ Chí Minh",
    val pickerRecipientName: String = "",
    val pickerPhoneNumber: String = "",
    val pickerLabel: String = "Nhà riêng",
    val pickerIsDefault: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
) : UiState

sealed interface AddressUiEvent : UiEvent {
    data object LoadAddresses : AddressUiEvent
    data class SelectAddress(val addressId: String) : AddressUiEvent
    data class SetDefault(val addressId: String) : AddressUiEvent
    data class DeleteAddress(val addressId: String) : AddressUiEvent
    // Picker events
    data class LocationMoved(val lat: Double, val lng: Double) : AddressUiEvent
    data class RecipientNameChanged(val name: String) : AddressUiEvent
    data class PhoneNumberChanged(val phone: String) : AddressUiEvent
    data class StreetAddressChanged(val address: String) : AddressUiEvent
    data class LabelChanged(val label: String) : AddressUiEvent
    data class ToggleDefault(val isDefault: Boolean) : AddressUiEvent
    data object SaveCurrentAddress : AddressUiEvent
    data object ClickBack : AddressUiEvent
}

sealed interface AddressUiEffect : UiEffect {
    data object NavigateBack : AddressUiEffect
    data class AddressSaved(val address: Address) : AddressUiEffect
    data class ShowSnackbar(val message: String) : AddressUiEffect
}

@HiltViewModel
class AddressViewModel @Inject constructor(
    private val getAddressesUseCase: GetAddressesUseCase,
    private val saveAddressUseCase: SaveAddressUseCase,
    private val setDefaultAddressUseCase: SetDefaultAddressUseCase,
    private val deleteAddressUseCase: DeleteAddressUseCase,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<AddressUiState, AddressUiEvent, AddressUiEffect>(
    initialState = AddressUiState(),
    savedStateHandle = savedStateHandle,
) {

    init {
        loadAddresses()
    }

    private fun loadAddresses() {
        getAddressesUseCase()
            .onEach { list ->
                updateState {
                    it.copy(
                        isLoading = false,
                        addresses = list,
                        selectedAddressId = list.firstOrNull { a -> a.isDefault }?.id ?: list.firstOrNull()?.id,
                        errorMessage = null
                    )
                }
            }
            .catch { e ->
                updateState {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Không thể tải danh sách địa chỉ"
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    override fun onEvent(event: AddressUiEvent) {
        when (event) {
            is AddressUiEvent.LoadAddresses -> loadAddresses()

            is AddressUiEvent.SelectAddress -> {
                updateState { it.copy(selectedAddressId = event.addressId) }
            }

            is AddressUiEvent.SetDefault -> {
                viewModelScope.launch {
                    setDefaultAddressUseCase(event.addressId)
                    sendEffect(AddressUiEffect.ShowSnackbar("Đã đặt làm địa chỉ mặc định"))
                }
            }

            is AddressUiEvent.DeleteAddress -> {
                viewModelScope.launch {
                    deleteAddressUseCase(event.addressId)
                    sendEffect(AddressUiEffect.ShowSnackbar("Đã xóa địa chỉ"))
                }
            }

            is AddressUiEvent.LocationMoved -> {
                updateState {
                    it.copy(
                        pickerLat = event.lat,
                        pickerLng = event.lng,
                        // Cập nhật địa chỉ giả lập theo vị trí ghim
                        pickerStreetAddress = "Tọa độ (${"%.4f".format(event.lat)}, ${"%.4f".format(event.lng)}) - Đường Nguyễn Huệ, Quận 1"
                    )
                }
            }

            is AddressUiEvent.RecipientNameChanged -> {
                updateState { it.copy(pickerRecipientName = event.name) }
            }

            is AddressUiEvent.PhoneNumberChanged -> {
                updateState { it.copy(pickerPhoneNumber = event.phone) }
            }

            is AddressUiEvent.StreetAddressChanged -> {
                updateState { it.copy(pickerStreetAddress = event.address) }
            }

            is AddressUiEvent.LabelChanged -> {
                updateState { it.copy(pickerLabel = event.label) }
            }

            is AddressUiEvent.ToggleDefault -> {
                updateState { it.copy(pickerIsDefault = event.isDefault) }
            }

            is AddressUiEvent.SaveCurrentAddress -> {
                val state = uiState.value
                viewModelScope.launch {
                    updateState { it.copy(isSaving = true) }
                    val result = saveAddressUseCase(
                        id = null,
                        recipientName = state.pickerRecipientName.ifBlank { "Khách hàng" },
                        phoneNumber = state.pickerPhoneNumber.ifBlank { "0987654321" },
                        streetAddress = state.pickerStreetAddress,
                        city = state.pickerCity,
                        label = state.pickerLabel,
                        latitude = state.pickerLat,
                        longitude = state.pickerLng,
                        isDefault = state.pickerIsDefault
                    )

                    updateState { it.copy(isSaving = false) }

                    when (result) {
                        is AddressValidationResult.Success -> {
                            sendEffect(AddressUiEffect.ShowSnackbar("Đã lưu địa chỉ thành công!"))
                            sendEffect(AddressUiEffect.AddressSaved(result.address))
                            sendEffect(AddressUiEffect.NavigateBack)
                        }
                        is AddressValidationResult.Invalid -> {
                            sendEffect(AddressUiEffect.ShowSnackbar(result.message))
                        }
                    }
                }
            }

            is AddressUiEvent.ClickBack -> {
                sendEffect(AddressUiEffect.NavigateBack)
            }
        }
    }
}
