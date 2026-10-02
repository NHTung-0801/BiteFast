package com.bitefast.core.data.repository

import com.bitefast.core.database.dao.AddressDao
import com.bitefast.core.database.entity.AddressEntity
import com.bitefast.core.datastore.AuthPreferencesDataSource
import com.bitefast.core.model.Address
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class AddressRepositoryTest {

    private lateinit var addressDao: AddressDao
    private lateinit var authPreferences: AuthPreferencesDataSource
    private lateinit var repository: AddressRepositoryImpl

    private val sampleEntity = AddressEntity(
        id = "addr_1",
        userId = "usr_123",
        label = "Nhà riêng",
        recipientName = "Nguyễn Văn A",
        phoneNumber = "0909123456",
        streetAddress = "456 Lê Văn Việt",
        city = "TP. Thủ Đức",
        state = "TP. HCM",
        postalCode = "70000",
        isDefault = true
    )

    private val sampleAddress = Address(
        id = "addr_1",
        label = "Nhà riêng",
        recipientName = "Nguyễn Văn A",
        phoneNumber = "0909123456",
        streetAddress = "456 Lê Văn Việt",
        city = "TP. Thủ Đức",
        state = "TP. HCM",
        postalCode = "70000",
        isDefault = true
    )

    @BeforeEach
    fun setUp() {
        addressDao = mockk(relaxed = true)
        authPreferences = mockk(relaxed = true)
        coEvery { authPreferences.userId } returns flowOf("usr_123")
        repository = AddressRepositoryImpl(addressDao, authPreferences)
    }

    @Test
    @DisplayName("getAddresses returns mapped addresses for current user")
    fun getAddresses_returnsAddresses() = runTest {
        coEvery { addressDao.getAddressesByUserId("usr_123") } returns flowOf(listOf(sampleEntity))

        val addresses = repository.getAddresses().first()

        assertEquals(1, addresses.size)
        assertEquals("addr_1", addresses[0].id)
        assertEquals("Nhà riêng", addresses[0].label)
        assertTrue(addresses[0].isDefault)
    }

    @Test
    @DisplayName("addAddress clears defaults if new address is default, then inserts")
    fun addAddress_clearsDefaultsAndInserts() = runTest {
        val created = repository.addAddress(sampleAddress)

        assertEquals("addr_1", created.id)
        coVerify(exactly = 1) { addressDao.clearDefaultFlags("usr_123") }
        coVerify(exactly = 1) { addressDao.insertAddress(any()) }
    }

    @Test
    @DisplayName("deleteAddress calls dao deleteAddressById with user id")
    fun deleteAddress_callsDao() = runTest {
        repository.deleteAddress("addr_1")

        coVerify(exactly = 1) { addressDao.deleteAddressById("addr_1", "usr_123") }
    }

    @Test
    @DisplayName("setDefaultAddress calls dao setDefaultAddress")
    fun setDefaultAddress_callsDao() = runTest {
        repository.setDefaultAddress("addr_1")

        coVerify(exactly = 1) { addressDao.setDefaultAddress("addr_1", "usr_123") }
    }
}
