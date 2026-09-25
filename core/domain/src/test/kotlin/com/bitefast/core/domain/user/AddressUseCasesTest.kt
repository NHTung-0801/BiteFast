package com.bitefast.core.domain.user

import com.bitefast.core.domain.repository.AddressRepository
import com.bitefast.core.model.Address
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class AddressUseCasesTest {

    private val addressRepository: AddressRepository = mockk(relaxed = true)
    private lateinit var getAddressesUseCase: GetAddressesUseCase
    private lateinit var saveAddressUseCase: SaveAddressUseCase
    private lateinit var setDefaultAddressUseCase: SetDefaultAddressUseCase
    private lateinit var deleteAddressUseCase: DeleteAddressUseCase

    @BeforeEach
    fun setUp() {
        getAddressesUseCase = GetAddressesUseCase(addressRepository)
        saveAddressUseCase = SaveAddressUseCase(addressRepository)
        setDefaultAddressUseCase = SetDefaultAddressUseCase(addressRepository)
        deleteAddressUseCase = DeleteAddressUseCase(addressRepository)
    }

    @Test
    @DisplayName("SaveAddress: returns Invalid when recipient name is blank")
    fun saveAddress_blankRecipientName_returnsInvalid() = runTest {
        val result = saveAddressUseCase(
            recipientName = "   ",
            phoneNumber = "0912345678",
            streetAddress = "123 Lê Lợi"
        )

        assertTrue(result is AddressValidationResult.Invalid)
        assertEquals("recipientName", (result as AddressValidationResult.Invalid).field)
    }

    @Test
    @DisplayName("SaveAddress: returns Invalid when phone number is incorrect")
    fun saveAddress_invalidPhoneNumber_returnsInvalid() = runTest {
        val result = saveAddressUseCase(
            recipientName = "Nguyễn Văn A",
            phoneNumber = "123456",
            streetAddress = "123 Lê Lợi"
        )

        assertTrue(result is AddressValidationResult.Invalid)
        assertEquals("phoneNumber", (result as AddressValidationResult.Invalid).field)
    }

    @Test
    @DisplayName("SaveAddress: returns Invalid when street address is blank")
    fun saveAddress_blankStreetAddress_returnsInvalid() = runTest {
        val result = saveAddressUseCase(
            recipientName = "Nguyễn Văn A",
            phoneNumber = "0912345678",
            streetAddress = "   "
        )

        assertTrue(result is AddressValidationResult.Invalid)
        assertEquals("streetAddress", (result as AddressValidationResult.Invalid).field)
    }

    @Test
    @DisplayName("SaveAddress: adds new address and sets default when isDefault is true")
    fun saveAddress_validNewAddress_addsSuccessfully() = runTest {
        coEvery { addressRepository.addAddress(any()) } answers { firstArg() }

        val result = saveAddressUseCase(
            recipientName = "Nguyễn Hữu Tùng",
            phoneNumber = "0912345678",
            streetAddress = "123 Nguyễn Huệ, Quận 1",
            isDefault = true
        )

        assertTrue(result is AddressValidationResult.Success)
        val address = (result as AddressValidationResult.Success).address
        assertEquals("Nguyễn Hữu Tùng", address.recipientName)
        assertEquals("0912345678", address.phoneNumber)
        assertTrue(address.isDefault)

        coVerify(exactly = 1) { addressRepository.addAddress(any()) }
        coVerify(exactly = 1) { addressRepository.setDefaultAddress(address.id) }
    }

    @Test
    @DisplayName("SaveAddress: updates existing address when id is provided")
    fun saveAddress_validExistingAddress_updatesSuccessfully() = runTest {
        coEvery { addressRepository.updateAddress(any()) } answers { firstArg() }

        val result = saveAddressUseCase(
            id = "addr_99",
            recipientName = "Nguyễn Hữu Tùng",
            phoneNumber = "+84912345678",
            streetAddress = "456 Lê Lợi, Quận 1",
            isDefault = false
        )

        assertTrue(result is AddressValidationResult.Success)
        val address = (result as AddressValidationResult.Success).address
        assertEquals("addr_99", address.id)

        coVerify(exactly = 1) { addressRepository.updateAddress(any()) }
        coVerify(exactly = 0) { addressRepository.setDefaultAddress(any()) }
    }

    @Test
    @DisplayName("GetAddresses: emits addresses from repository")
    fun getAddresses_emitsFlow() = runTest {
        val sample = listOf(Address(id = "1", recipientName = "User 1"))
        every { addressRepository.getAddresses() } returns flowOf(sample)

        val result = getAddressesUseCase().first()
        assertEquals(1, result.size)
        assertEquals("User 1", result.first().recipientName)
    }

    @Test
    @DisplayName("SetDefaultAddress: delegates to repository")
    fun setDefaultAddress_delegates() = runTest {
        setDefaultAddressUseCase("addr_123")
        coVerify(exactly = 1) { addressRepository.setDefaultAddress("addr_123") }
    }

    @Test
    @DisplayName("DeleteAddress: delegates to repository")
    fun deleteAddress_delegates() = runTest {
        deleteAddressUseCase("addr_123")
        coVerify(exactly = 1) { addressRepository.deleteAddress("addr_123") }
    }
}
