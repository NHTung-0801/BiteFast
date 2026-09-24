package com.bitefast.core.domain.auth

import com.bitefast.core.domain.repository.AuthRepository
import com.bitefast.core.model.User
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): User {
        return authRepository.login(email, password)
    }
}

class RegisterUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(name: String, email: String, password: String): User {
        return authRepository.register(name, email, password)
    }
}

class LogoutUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke() {
        authRepository.logout()
    }
}

class CheckAuthStatusUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    val isLoggedIn: Flow<Boolean> = authRepository.isLoggedIn
    val isGuest: Flow<Boolean> = authRepository.isGuest
}
