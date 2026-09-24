package com.bitefast.core.domain.repository

import com.bitefast.core.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val isLoggedIn: Flow<Boolean>
    val isGuest: Flow<Boolean>
    suspend fun login(email: String, password: String): User
    suspend fun register(name: String, email: String, password: String): User
    suspend fun logout()
    suspend fun enableGuestMode()
    suspend fun getCurrentUser(): User?
}
