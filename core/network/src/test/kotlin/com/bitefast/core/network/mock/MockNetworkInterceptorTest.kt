package com.bitefast.core.network.mock

import com.bitefast.core.network.config.NetworkConfig
import com.bitefast.core.network.config.NetworkMode
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class MockNetworkInterceptorTest {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    private lateinit var interceptor: MockNetworkInterceptor
    private lateinit var client: OkHttpClient

    @BeforeEach
    fun setUp() {
        NetworkConfig.mode = NetworkMode.MOCK
        interceptor = MockNetworkInterceptor(json)
        client = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .build()
    }

    @AfterEach
    fun tearDown() {
        NetworkConfig.mode = NetworkMode.MOCK
    }

    @Test
    @DisplayName("GET /api/v1/restaurants should return HTTP 200 with restaurants list JSON")
    fun getRestaurants_returns200WithPayload() {
        val request = Request.Builder()
            .url("https://api.bitefast.vn/api/v1/restaurants")
            .get()
            .build()

        val response = client.newCall(request).execute()

        assertEquals(200, response.code)
        val body = response.body?.string().orEmpty()
        assertTrue(body.contains("\"success\":true"))
        assertTrue(body.contains("Cơm Tấm Phúc Lộc Thọ") || body.contains("Phở Thìn"))
    }

    @Test
    @DisplayName("GET /api/v1/vouchers/wallet should return HTTP 200 with vouchers list")
    fun getVouchers_returns200WithVouchers() {
        val request = Request.Builder()
            .url("https://api.bitefast.vn/api/v1/vouchers/wallet")
            .get()
            .build()

        val response = client.newCall(request).execute()

        assertEquals(200, response.code)
        val body = response.body?.string().orEmpty()
        assertTrue(body.contains("BITEFAST20K") || body.contains("FREESHIP15K"))
    }

    @Test
    @DisplayName("POST and GET /api/v1/orders should persist and return order history")
    fun createAndGetOrdersHistory_returns200() {
        val postPayload = """
            {
                "restaurantId": "res_1",
                "restaurantName": "Cơm Tấm Phúc Lộc Thọ",
                "items": [],
                "subtotal": 50000.0,
                "deliveryFee": 15000.0,
                "discount": 0.0,
                "total": 65000.0,
                "paymentMethod": "CASH"
            }
        """.trimIndent()

        val postRequest = Request.Builder()
            .url("https://api.bitefast.vn/api/v1/orders")
            .post(postPayload.toRequestBody("application/json".toMediaType()))
            .build()

        val postResponse = client.newCall(postRequest).execute()
        assertEquals(200, postResponse.code)

        val getRequest = Request.Builder()
            .url("https://api.bitefast.vn/api/v1/orders/history")
            .get()
            .build()

        val getResponse = client.newCall(getRequest).execute()
        assertEquals(200, getResponse.code)
        val body = getResponse.body?.string().orEmpty()
        assertTrue(body.contains("res_1"))
        assertTrue(body.contains("Cơm Tấm Phúc Lộc Thọ"))
    }

    @Test
    @DisplayName("Fallback endpoint should return HTTP 200 with success message")
    fun unknownEndpoint_returns200Fallback() {
        val request = Request.Builder()
            .url("https://api.bitefast.vn/api/v1/unknown/resource")
            .get()
            .build()

        val response = client.newCall(request).execute()

        assertEquals(200, response.code)
        val body = response.body?.string().orEmpty()
        assertTrue(body.contains("Success"))
    }
}
