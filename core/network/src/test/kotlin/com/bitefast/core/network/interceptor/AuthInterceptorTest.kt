package com.bitefast.core.network.interceptor

import com.bitefast.core.datastore.AuthPreferencesDataSource
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class AuthInterceptorTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var authPreferencesDataSource: AuthPreferencesDataSource
    private lateinit var authInterceptor: AuthInterceptor
    private lateinit var okHttpClient: OkHttpClient

    @BeforeEach
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        authPreferencesDataSource = mockk()
        authInterceptor = AuthInterceptor(authPreferencesDataSource)

        okHttpClient = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .build()
    }

    @AfterEach
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    @DisplayName("When access token exists, Authorization Bearer header must be attached")
    fun whenTokenExists_attachesBearerHeader() {
        every { authPreferencesDataSource.accessToken } returns flowOf("jwt_sample_token_12345")

        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("{}"))

        val request = Request.Builder()
            .url(mockWebServer.url("/api/v1/restaurants"))
            .build()

        val response = okHttpClient.newCall(request).execute()
        assertEquals(200, response.code)

        val recordedRequest = mockWebServer.takeRequest()
        assertEquals("Bearer jwt_sample_token_12345", recordedRequest.getHeader("Authorization"))
    }

    @Test
    @DisplayName("When access token is null or blank, Authorization header must not be attached")
    fun whenTokenNullOrBlank_doesNotAttachHeader() {
        every { authPreferencesDataSource.accessToken } returns flowOf(null)

        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("{}"))

        val request = Request.Builder()
            .url(mockWebServer.url("/api/v1/restaurants"))
            .build()

        val response = okHttpClient.newCall(request).execute()
        assertEquals(200, response.code)

        val recordedRequest = mockWebServer.takeRequest()
        assertNull(recordedRequest.getHeader("Authorization"))
    }
}
