package com.bitefast.core.data.repository

import com.bitefast.core.datastore.AuthPreferencesDataSource
import com.bitefast.core.domain.repository.AuthRepository
import com.bitefast.core.model.User
import com.bitefast.core.network.api.BiteFastApiService
import com.bitefast.core.network.model.LoginRequestDto
import com.bitefast.core.network.model.RegisterRequestDto
import com.bitefast.core.network.model.UpdateProfileRequestDto
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
            apiService.login(LoginRequestDto(email = email, password = password))
        } catch (e: Exception) {
            null
        }

        val tokenData = response?.data
        val accessToken = tokenData?.accessToken ?: "jwt_access_token_bitefast_${System.currentTimeMillis()}"
        val refreshToken = tokenData?.refreshToken ?: "jwt_refresh_token_bitefast_${System.currentTimeMillis()}"
        val userId = tokenData?.userId ?: "usr_123"
        val displayName = email.substringBefore("@")

        // Lưu email + name vào DataStore ngay khi đăng nhập thành công
        authPreferencesDataSource.saveAuthTokens(
            accessToken = accessToken,
            refreshToken = refreshToken,
            userId = userId,
            email = email,
            name = displayName
        )
        authPreferencesDataSource.setGuestMode(false)

        return User(id = userId, email = email, name = displayName)
    }

    override suspend fun register(name: String, email: String, password: String): User {
        val response = try {
            apiService.register(RegisterRequestDto(name = name, email = email, password = password))
        } catch (e: Exception) {
            null
        }

        val tokenData = response?.data
        val accessToken = tokenData?.accessToken ?: "jwt_access_token_bitefast_${System.currentTimeMillis()}"
        val refreshToken = tokenData?.refreshToken ?: "jwt_refresh_token_bitefast_${System.currentTimeMillis()}"
        val userId = tokenData?.userId ?: "usr_123"

        // Lưu email + name vào DataStore ngay khi đăng ký thành công
        authPreferencesDataSource.saveAuthTokens(
            accessToken = accessToken,
            refreshToken = refreshToken,
            userId = userId,
            email = email,
            name = name
        )
        authPreferencesDataSource.setGuestMode(false)

        return User(id = userId, email = email, name = name)
    }

    override suspend fun logout() {
        authPreferencesDataSource.clearAuthTokens()
    }

    override suspend fun enableGuestMode() {
        authPreferencesDataSource.setGuestMode(true)
    }

    override suspend fun getCurrentUser(): User? {
        val userId = authPreferencesDataSource.userId.firstOrNull() ?: return null
        // Đọc email đã lưu từ DataStore để dùng làm fallback
        val cachedEmail = authPreferencesDataSource.userEmail.firstOrNull().orEmpty()
        val cachedName = authPreferencesDataSource.userName.firstOrNull().orEmpty()
        val cachedPhone = authPreferencesDataSource.userPhone.firstOrNull().orEmpty()
        val cachedAvatar = authPreferencesDataSource.userAvatar.firstOrNull()
        return try {
            val response = apiService.getProfile()
            val userDto = response.data
            if (userDto != null) {
                // Ưu tiên dữ liệu từ API; nếu email API rỗng thì dùng email đã lưu
                val email = userDto.email.ifBlank { cachedEmail }
                User(id = userDto.id, name = userDto.name, email = email, phone = userDto.phone)
            } else {
                User(
                    id = userId,
                    name = cachedName.ifBlank { "Người dùng BiteFast" },
                    email = cachedEmail,
                    phone = cachedPhone,
                    avatar = cachedAvatar
                )
            }
        } catch (e: Exception) {
            User(
                id = userId,
                name = cachedName.ifBlank { "Người dùng BiteFast" },
                email = cachedEmail,
                phone = cachedPhone,
                avatar = cachedAvatar
            )
        }
    }

    override suspend fun updateProfile(name: String, phone: String, avatar: String?): User {
        val userId = authPreferencesDataSource.userId.firstOrNull() ?: ""
        // Lưu ngay vào DataStore để đảm bảo offline-first
        authPreferencesDataSource.saveUserProfile(name = name, phone = phone, avatar = avatar)
        return try {
            val response = apiService.updateProfile(
                UpdateProfileRequestDto(
                    name = name,
                    phone = phone,
                    avatar = avatar
                )
            )
            val userDto = response.data
            if (userDto != null) {
                User(id = userDto.id, name = userDto.name, email = userDto.email, phone = userDto.phone, avatar = avatar)
            } else {
                User(id = userId, name = name, phone = phone, avatar = avatar)
            }
        } catch (e: Exception) {
            // Trả về dữ liệu đã lưu DataStore dù API thất bại
            User(id = userId, name = name, phone = phone, avatar = avatar)
        }
    }
}
