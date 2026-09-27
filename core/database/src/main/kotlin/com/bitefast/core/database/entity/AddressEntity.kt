package com.bitefast.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.bitefast.core.model.Address

/**
 * Room Entity representing a saved delivery address in the encrypted SQLite database.
 */
@Entity(tableName = "addresses")
data class AddressEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val label: String,
    val recipientName: String,
    val phoneNumber: String,
    val streetAddress: String,
    val city: String,
    val state: String,
    val postalCode: String,
    val country: String = "Vietnam",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val isDefault: Boolean = false,
    val isSelected: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

fun AddressEntity.toExternalModel(): Address = Address(
    id = id,
    userId = userId,
    label = label,
    recipientName = recipientName,
    phoneNumber = phoneNumber,
    streetAddress = streetAddress,
    city = city,
    state = state,
    postalCode = postalCode,
    country = country,
    latitude = latitude,
    longitude = longitude,
    isDefault = isDefault,
    isSelected = isSelected
)

fun Address.toEntity(createdAt: Long = System.currentTimeMillis()): AddressEntity = AddressEntity(
    id = id,
    userId = userId,
    label = label,
    recipientName = recipientName,
    phoneNumber = phoneNumber,
    streetAddress = streetAddress,
    city = city,
    state = state,
    postalCode = postalCode,
    country = country,
    latitude = latitude,
    longitude = longitude,
    isDefault = isDefault,
    isSelected = isSelected,
    createdAt = createdAt
)