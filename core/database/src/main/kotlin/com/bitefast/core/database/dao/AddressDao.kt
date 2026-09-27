package com.bitefast.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.bitefast.core.database.entity.AddressEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for User Delivery Addresses.
 */
@Dao
interface AddressDao {

    @Query("SELECT * FROM addresses WHERE userId = :userId ORDER BY isDefault DESC, createdAt DESC")
    fun getAddressesByUserId(userId: String): Flow<List<AddressEntity>>

    @Query("SELECT * FROM addresses WHERE id = :addressId LIMIT 1")
    suspend fun getAddressById(addressId: String): AddressEntity?

    @Query("SELECT * FROM addresses WHERE userId = :userId AND isDefault = 1 LIMIT 1")
    suspend fun getDefaultAddress(userId: String): AddressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAddress(address: AddressEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAddresses(addresses: List<AddressEntity>)

    @Query("UPDATE addresses SET isDefault = 0 WHERE userId = :userId")
    suspend fun clearDefaultFlags(userId: String)

    @Query("UPDATE addresses SET isDefault = 1 WHERE id = :addressId AND userId = :userId")
    suspend fun setAddressDefaultFlag(addressId: String, userId: String)

    @Transaction
    suspend fun setDefaultAddress(addressId: String, userId: String) {
        clearDefaultFlags(userId)
        setAddressDefaultFlag(addressId, userId)
    }

    @Query("DELETE FROM addresses WHERE id = :addressId AND userId = :userId")
    suspend fun deleteAddressById(addressId: String, userId: String)

    @Query("DELETE FROM addresses WHERE userId = :userId")
    suspend fun deleteAllAddressesByUserId(userId: String)
}