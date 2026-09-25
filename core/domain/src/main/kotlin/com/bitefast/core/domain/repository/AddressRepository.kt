package com.bitefast.core.domain.repository

import com.bitefast.core.model.Address
import kotlinx.coroutines.flow.Flow

interface AddressRepository {
    fun getAddresses(): Flow<List<Address>>
    suspend fun getAddressById(id: String): Address?
    suspend fun addAddress(address: Address): Address
    suspend fun updateAddress(address: Address): Address
    suspend fun deleteAddress(id: String)
    suspend fun setDefaultAddress(id: String)
}
