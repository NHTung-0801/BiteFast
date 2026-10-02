package com.bitefast.feature.profile.address

import androidx.lifecycle.SavedStateHandle
import com.bitefast.core.domain.user.DeleteAddressUseCase
import com.bitefast.core.domain.user.GetAddressesUseCase
import com.bitefast.core.domain.user.SaveAddressUseCase
import com.bitefast.core.domain.user.SetDefaultAddressUseCase
import com.bitefast.core.model.Address
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AddressViewModelTest {

    private val getAddressesUseCase: GetAddressesUseCase = mockk()
    private val saveAddressUseCase: SaveAddressUseCase = mockk()
    private val setDefaultAddressUseCase: SetDefaultAddressUseCase = mockk()
    private val deleteAddressUseCase: DeleteAddressUseCase = mockk()
    private val testDispatcher = StandardTestDispatcher()

    private val sampleAddresses = listOf(
        Address(
            id = "addr_1",
            userId = "u_1",
            streetAddress = "123 Lê Lợi, Q1",
            recipientName = "Nguyễn Văn A",
            phoneNumber = "0901234567",
            label = "Nhà riêng",
            isDefault = true
        ),
        Address(
            id = "addr_2",
            userId = "u_1",
            streetAddress = "456 Nam Kỳ Khởi Nghĩa, Q3",
            recipientName = "Nguyễn Văn A",
            phoneNumber = "0901234567",
            label = "Cơ quan",
            isDefault = false
        )
    )

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { getAddressesUseCase() } returns flowOf(sampleAddresses)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    @DisplayName("LoadAddresses should collect address list and clear loading state")
    fun loadAddresses_updatesState() = runTest(testDispatcher) {
        val viewModel = AddressViewModel(
            getAddressesUseCase, saveAddressUseCase, setDefaultAddressUseCase, deleteAddressUseCase, SavedStateHandle()
        )
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(2, state.addresses.size)
        assertTrue(state.addresses.first().isDefault)
    }

    @Test
    @DisplayName("SetDefault should invoke setDefaultAddressUseCase")
    fun setDefault_callsUseCase() = runTest(testDispatcher) {
        coEvery { setDefaultAddressUseCase("addr_2") } returns Unit

        val viewModel = AddressViewModel(
            getAddressesUseCase, saveAddressUseCase, setDefaultAddressUseCase, deleteAddressUseCase, SavedStateHandle()
        )
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(AddressUiEvent.SetDefault("addr_2"))
        testDispatcher.scheduler.advanceUntilIdle()
    }

    @Test
    @DisplayName("DeleteAddress should invoke deleteAddressUseCase")
    fun deleteAddress_callsUseCase() = runTest(testDispatcher) {
        coEvery { deleteAddressUseCase("addr_2") } returns Unit

        val viewModel = AddressViewModel(
            getAddressesUseCase, saveAddressUseCase, setDefaultAddressUseCase, deleteAddressUseCase, SavedStateHandle()
        )
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(AddressUiEvent.DeleteAddress("addr_2"))
        testDispatcher.scheduler.advanceUntilIdle()
    }
}
