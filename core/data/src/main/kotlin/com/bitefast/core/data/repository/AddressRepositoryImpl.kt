package com.bitefast.core.data.repository

import com.bitefast.core.database.dao.AddressDao
import com.bitefast.core.database.entity.toEntity
import com.bitefast.core.database.entity.toExternalModel
import com.bitefast.core.datastore.AuthPreferencesDataSource
import com.bitefast.core.domain.repository.AddressRepository
import com.bitefast.core.model.Address
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AddressRepositoryImpl @Inject constructor(
    private val addressDao: AddressDao,
    private val authPreferences: AuthPreferencesDataSource
) : AddressRepository {

    override fun getAddresses(): Flow<List<Address>> {
        return authPreferences.userId.flatMapLatest { userId ->
            val effectiveUserId = if (userId.isNullOrBlank()) "guest_user" else userId
            addressDao.getAddressesByUserId(effectiveUserId).map { entities ->
                entities.map { it.toExternalModel() }
            }
        }
    }

    override suspend fun getAddressById(id: String): Address? {
        return addressDao.getAddressById(id)?.toExternalModel()
    }

    override suspend fun addAddress(address: Address): Address {
        val currentUserId = authPreferences.userId.firstOrNull()?.takeIf { it.isNotBlank() } ?: "guest_user"
        val addressId = if (address.id.isBlank()) UUID.randomUUID().toString() else address.id
        val entity = address.copy(id = addressId, userId = currentUserId).toEntity()

        if (address.isDefault) {
            addressDao.clearDefaultFlags(currentUserId)
        }
        addressDao.insertAddress(entity)
        return entity.toExternalModel()
    }

    override suspend fun updateAddress(address: Address): Address {
        val currentUserId = authPreferences.userId.firstOrNull()?.takeIf { it.isNotBlank() } ?: "guest_user"
        val entity = address.copy(userId = currentUserId).toEntity()

        if (address.isDefault) {
            addressDao.clearDefaultFlags(currentUserId)
        }
        addressDao.insertAddress(entity)
        return entity.toExternalModel()
    }

    override suspend fun deleteAddress(id: String) {
        val currentUserId = authPreferences.userId.firstOrNull()?.takeIf { it.isNotBlank() } ?: "guest_user"
        addressDao.deleteAddressById(id, currentUserId)
    }

    override suspend fun setDefaultAddress(id: String) {
        val currentUserId = authPreferences.userId.firstOrNull()?.takeIf { it.isNotBlank() } ?: "guest_user"
        addressDao.setDefaultAddress(id, currentUserId)
    }
}