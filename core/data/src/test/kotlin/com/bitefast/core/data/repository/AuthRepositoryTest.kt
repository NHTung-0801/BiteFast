package com.bitefast.core.data.repository

import com.bitefast.core.datastore.AuthPreferencesDataSource
import com.bitefast.core.network.api.BiteFastApiService
import com.bitefast.core.network.model.ApiResponse
import com.bitefast.core.network.model.AuthTokenResponseDto
import com.bitefast.core.network.model.LoginRequestDto
import com.bitefast.core.network.model.RegisterRequestDto
import com.bitefast.core.network.model.UserDto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AuthRepositoryTest {

    private lateinit var authPreferencesDataSource: AuthPreferencesDataSource
    private lateinit var apiService: BiteFastApiService
    private lateinit var repository: AuthRepositoryImpl

    @BeforeEach
    fun setUp() {
        authPreferencesDataSource = mockk(relaxed = true)
        apiService = mockk()
        repository = AuthRepositoryImpl(authPreferencesDataSource, apiService)
    }

    @Test
    fun `login calls apiService and saves tokens to DataStore`() = runTest {
        val email = "test@example.com"
        val password = "password123"
        val expectedToken = AuthTokenResponseDto(
            accessToken = "access_token_abc",
            refreshToken = "refresh_token_xyz",
            userId = "usr_999"
        )

        coEvery { apiService.login(LoginRequestDto(email, password)) } returns ApiResponse(
            success = true,
            code = 200,
            data = expectedToken
        )

        val user = repository.login(email, password)

        assertEquals("usr_999", user.id)
        assertEquals(email, user.email)
        coVerify(exactly = 1) {
            authPreferencesDataSource.saveAuthTokens("access_token_abc", "refresh_token_xyz", "usr_999")
        }
        coVerify(exactly = 1) {
            authPreferencesDataSource.setGuestMode(false)
        }
    }

    @Test
    fun `register calls apiService register and saves tokens`() = runTest {
        val name = "Nguyen Van B"
        val email = "b@example.com"
        val password = "secure_pass"
        val token = AuthTokenResponseDto(
            accessToken = "reg_access_token",
            refreshToken = "reg_refresh_token",
            userId = "usr_new"
        )

        coEvery { apiService.register(RegisterRequestDto(name, email, password)) } returns ApiResponse(
            success = true,
            data = token
        )

        val user = repository.register(name, email, password)

        assertEquals("usr_new", user.id)
        assertEquals(name, user.name)
        assertEquals(email, user.email)
        coVerify(exactly = 1) {
            authPreferencesDataSource.saveAuthTokens("reg_access_token", "reg_refresh_token", "usr_new")
        }
    }

    @Test
    fun `logout clears auth tokens`() = runTest {
        repository.logout()
        coVerify(exactly = 1) { authPreferencesDataSource.clearAuthTokens() }
    }

    @Test
    fun `getCurrentUser returns profile from apiService`() = runTest {
        coEvery { authPreferencesDataSource.userId } returns flowOf("usr_123")
        coEvery { apiService.getProfile() } returns ApiResponse(
            data = UserDto(
                id = "usr_123",
                name = "User Name",
                email = "user@example.com",
                phone = "0123456789"
            )
        )

        val user = repository.getCurrentUser()

        assertNotNull(user)
        assertEquals("usr_123", user?.id)
        assertEquals("User Name", user?.name)
    }
}
