package com.bitefast.core.network.model

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequestDto(
    val email: String,
    val password: String
)

@Serializable
data class RegisterRequestDto(
    val name: String,
    val email: String,
    val password: String,
    val phone: String = ""
)

@Serializable
data class AuthTokenResponseDto(
    val accessToken: String,
    val refreshToken: String,
    val userId: String,
    val tokenType: String = "Bearer",
    val expiresIn: Long = 3600L
)

@Serializable
data class RefreshTokenRequestDto(
    val refreshToken: String
)

@Serializable
data class ForgotPasswordRequestDto(
    val email: String
)

@Serializable
data class UserDto(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val avatar: String? = null,
    val isGuest: Boolean = false
)

@Serializable
data class UpdateProfileRequestDto(
    val name: String,
    val phone: String = "",
    val avatar: String? = null
)
