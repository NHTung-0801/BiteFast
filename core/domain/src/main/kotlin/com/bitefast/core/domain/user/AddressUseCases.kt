package com.bitefast.core.domain.user

import com.bitefast.core.domain.repository.AddressRepository
import com.bitefast.core.model.Address
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject

sealed interface AddressValidationResult {
    data class Success(val address: Address) : AddressValidationResult
    data class Invalid(val field: String, val message: String) : AddressValidationResult
}

class GetAddressesUseCase @Inject constructor(
    private val addressRepository: AddressRepository
) {
    operator fun invoke(): Flow<List<Address>> {
        return addressRepository.getAddresses()
    }
}

class SaveAddressUseCase @Inject constructor(
    private val addressRepository: AddressRepository
) {
    companion object {
        private val PHONE_REGEX = Regex("""^(0|\+84)(3|5|7|8|9)[0-9]{8}$""")
    }

    suspend operator fun invoke(
        id: String? = null,
        userId: String = "",
        label: String = "Nhà riêng",
        recipientName: String,
        phoneNumber: String,
        streetAddress: String,
        city: String = "TP. Hồ Chí Minh",
        latitude: Double = 0.0,
        longitude: Double = 0.0,
        isDefault: Boolean = false
    ): AddressValidationResult {
        if (recipientName.isBlank()) {
            return AddressValidationResult.Invalid("recipientName", "Tên người nhận không được để trống.")
        }

        val cleanPhone = phoneNumber.replace(" ", "").trim()
        if (!PHONE_REGEX.matches(cleanPhone)) {
            return AddressValidationResult.Invalid("phoneNumber", "Số điện thoại nhận hàng không hợp lệ.")
        }

        if (streetAddress.isBlank()) {
            return AddressValidationResult.Invalid("streetAddress", "Địa chỉ chi tiết không được để trống.")
        }

        val addressId = id?.takeIf { it.isNotBlank() } ?: UUID.randomUUID().toString()
        val address = Address(
            id = addressId,
            userId = userId,
            label = label.ifBlank { "Địa chỉ" },
            recipientName = recipientName.trim(),
            phoneNumber = cleanPhone,
            streetAddress = streetAddress.trim(),
            city = city,
            latitude = latitude,
            longitude = longitude,
            isDefault = isDefault
        )

        val saved = if (id.isNullOrBlank()) {
            addressRepository.addAddress(address)
        } else {
            addressRepository.updateAddress(address)
        }

        if (isDefault) {
            addressRepository.setDefaultAddress(saved.id)
        }

        return AddressValidationResult.Success(saved)
    }
}

class SetDefaultAddressUseCase @Inject constructor(
    private val addressRepository: AddressRepository
) {
    suspend operator fun invoke(addressId: String) {
        if (addressId.isNotBlank()) {
            addressRepository.setDefaultAddress(addressId)
        }
    }
}

class DeleteAddressUseCase @Inject constructor(
    private val addressRepository: AddressRepository
) {
    suspend operator fun invoke(addressId: String) {
        if (addressId.isNotBlank()) {
            addressRepository.deleteAddress(addressId)
        }
    }
}
