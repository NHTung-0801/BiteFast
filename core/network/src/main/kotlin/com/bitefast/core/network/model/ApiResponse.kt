package com.bitefast.core.network.model

import kotlinx.serialization.Serializable

/**
 * Standard API Response envelope for all BiteFast backend endpoints.
 */
@Serializable
data class ApiResponse<T>(
    val success: Boolean = true,
    val code: Int = 200,
    val message: String = "OK",
    val data: T? = null
)
