package com.bitefast.core.network.model

import kotlinx.serialization.Serializable

@Serializable
data class AddressDto(
    val id: String,
    val userId: String = "",
    val label: String = "",
    val recipientName: String = "",
    val phoneNumber: String = "",
    val streetAddress: String = "",
    val city: String = "",
    val state: String = "",
    val postalCode: String = "",
    val country: String = "Vietnam",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val isDefault: Boolean = false
)

@Serializable
data class CreateAddressRequestDto(
    val label: String,
    val recipientName: String,
    val phoneNumber: String,
    val streetAddress: String,
    val city: String = "TP. Ho Chi Minh",
    val state: String = "",
    val postalCode: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val isDefault: Boolean = false
)
