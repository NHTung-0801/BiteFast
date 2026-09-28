package com.bitefast.core.domain.auth

import app.cash.turbine.test
import com.bitefast.core.domain.repository.AuthRepository
import com.bitefast.core.model.User
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AuthUseCasesTest {

    private lateinit var authRepository: AuthRepository
    private val testUser = User(
        id = "u1",
        email = "user@bitefast.vn",
        name = "Test User",
        phone = "0901234567"
    )

    @BeforeEach
    fun setUp() {
        authRepository = mockk(relaxed = true)
    }

    @Test
    fun loginUseCase_returnsUserOnSuccess() = runTest {
        coEvery { authRepository.login("user@bitefast.vn", "password123") } returns testUser

        val useCase = LoginUseCase(authRepository)
        val result = useCase("user@bitefast.vn", "password123")

        assertEquals(testUser, result)
        coVerify(exactly = 1) { authRepository.login("user@bitefast.vn", "password123") }
    }

    @Test
    fun registerUseCase_returnsRegisteredUser() = runTest {
        coEvery { authRepository.register("Test User", "user@bitefast.vn", "password123") } returns testUser

        val useCase = RegisterUseCase(authRepository)
        val result = useCase("Test User", "user@bitefast.vn", "password123")

        assertEquals(testUser, result)
        coVerify(exactly = 1) { authRepository.register("Test User", "user@bitefast.vn", "password123") }
    }

    @Test
    fun logoutUseCase_callsRepositoryLogout() = runTest {
        val useCase = LogoutUseCase(authRepository)
        useCase()

        coVerify(exactly = 1) { authRepository.logout() }
    }

    @Test
    fun enableGuestModeUseCase_callsRepositoryEnableGuest() = runTest {
        val useCase = EnableGuestModeUseCase(authRepository)
        useCase()

        coVerify(exactly = 1) { authRepository.enableGuestMode() }
    }

    @Test
    fun checkAuthStatusUseCase_emitsStatusCorrectly() = runTest {
        every { authRepository.isLoggedIn } returns flowOf(true)
        every { authRepository.isGuest } returns flowOf(false)

        val useCase = CheckAuthStatusUseCase(authRepository)

        useCase.isLoggedIn.test {
            assertTrue(awaitItem())
            awaitComplete()
        }
        useCase.isGuest.test {
            assertEquals(false, awaitItem())
            awaitComplete()
        }
    }
}
