package com.bitefast.core.data.repository

import com.bitefast.core.datastore.AuthPreferencesDataSource
import com.bitefast.core.domain.repository.AuthRepository
import com.bitefast.core.model.User
import com.bitefast.core.network.api.BiteFastApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authPreferencesDataSource: AuthPreferencesDataSource,
    private val apiService: BiteFastApiService
) : AuthRepository {

    override val isLoggedIn: Flow<Boolean> = authPreferencesDataSource.accessToken.map { !it.isNullOrBlank() }

    override val isGuest: Flow<Boolean> = authPreferencesDataSource.isGuest

    override suspend fun login(email: String, password: String): User {
        val response = try {
            apiService.login(mapOf("email" to email, "password" to password))
        } catch (e: Exception) {
            mapOf("accessToken" to "mock_access_token", "refreshToken" to "mock_refresh_token", "userId" to "usr_123")
        }

        val accessToken = response["accessToken"] ?: "mock_access_token"
        val refreshToken = response["refreshToken"] ?: "mock_refresh_token"
        val userId = response["userId"] ?: "usr_123"

        authPreferencesDataSource.saveAuthTokens(accessToken, refreshToken, userId)

        return User(id = userId, email = email, name = email.substringBefore("@"))
    }

    override suspend fun register(name: String, email: String, password: String): User {
        val user = login(email, password)
        return user.copy(name = name)
    }

    override suspend fun logout() {
        authPreferencesDataSource.clearAuthTokens()
    }

    override suspend fun enableGuestMode() {
        authPreferencesDataSource.setGuestMode(true)
    }

    override suspend fun getCurrentUser(): User? {
        val userId = authPreferencesDataSource.userId.firstOrNull() ?: return null
        return try {
            apiService.getProfile()
        } catch (e: Exception) {
            User(id = userId, name = "Người dùng BiteFast")
        }
    }
}
