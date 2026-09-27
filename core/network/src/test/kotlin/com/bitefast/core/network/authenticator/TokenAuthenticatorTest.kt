package com.bitefast.core.network.authenticator

import com.bitefast.core.datastore.AuthPreferencesDataSource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.concurrent.atomic.AtomicInteger

/**
 * Enterprise Integration Test suite for TokenAuthenticator using MockWebServer.
 * Verifies Mutex-protected thread-safe token renewal under heavy concurrent load (race condition prevention).
 */
class TokenAuthenticatorTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var authPreferencesDataSource: AuthPreferencesDataSource
    private lateinit var tokenAuthenticator: TokenAuthenticator
    private lateinit var okHttpClient: OkHttpClient

    private val currentAccessTokenFlow = MutableStateFlow<String?>("expired_token")
    private val currentRefreshTokenFlow = MutableStateFlow<String?>("valid_refresh_token")
    private val currentUserIdFlow = MutableStateFlow<String?>("user_456")

    @BeforeEach
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        authPreferencesDataSource = mockk(relaxed = true)
        every { authPreferencesDataSource.accessToken } returns currentAccessTokenFlow.asStateFlow()
        every { authPreferencesDataSource.refreshToken } returns currentRefreshTokenFlow.asStateFlow()
        every { authPreferencesDataSource.userId } returns currentUserIdFlow.asStateFlow()

        coEvery { authPreferencesDataSource.saveAuthTokens(any(), any(), any()) } coAnswers {
            val newAccess = firstArg<String>()
            val newRefresh = secondArg<String>()
            val uid = thirdArg<String>()
            currentAccessTokenFlow.value = newAccess
            currentRefreshTokenFlow.value = newRefresh
            currentUserIdFlow.value = uid
        }

        coEvery { authPreferencesDataSource.clearAuthTokens() } coAnswers {
            currentAccessTokenFlow.value = null
            currentRefreshTokenFlow.value = null
        }

        tokenAuthenticator = TokenAuthenticator(authPreferencesDataSource)

        okHttpClient = OkHttpClient.Builder()
            .authenticator(tokenAuthenticator)
            .build()
    }

    @AfterEach
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `concurrent requests receiving 401 triggers single token renewal and all retry successfully`() = runBlocking {
        val initial401Count = AtomicInteger(0)
        val successRetriedCount = AtomicInteger(0)

        // MockWebServer dispatcher handling 401 on expired token and 200 on refreshed token
        mockWebServer.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                val authHeader = request.getHeader("Authorization")
                return when {
                    authHeader == "Bearer expired_token" -> {
                        initial401Count.incrementAndGet()
                        MockResponse().setResponseCode(401)
                    }
                    authHeader != null && authHeader.startsWith("Bearer refreshed_access_token_") -> {
                        successRetriedCount.incrementAndGet()
                        MockResponse().setResponseCode(200).setBody("""{"status":"ok","path":"${request.path}"}""")
                    }
                    else -> MockResponse().setResponseCode(400)
                }
            }
        }

        val baseUrl = mockWebServer.url("/api/").toString()

        // 3 concurrent requests simultaneously fired with the expired token
        val deferredList = (1..3).map { index ->
            async(Dispatchers.IO) {
                val request = Request.Builder()
                    .url("${baseUrl}endpoint$index")
                    .header("Authorization", "Bearer expired_token")
                    .build()
                okHttpClient.newCall(request).execute()
            }
        }

        val responses = deferredList.awaitAll()

        // Assert all 3 requests eventually succeeded with 200 OK
        assertEquals(3, responses.size)
        responses.forEach { response ->
            assertEquals(200, response.code)
            response.close()
        }

        // Assert that saveAuthTokens was called exactly once despite 3 concurrent 401s
        coVerify(exactly = 1) { authPreferencesDataSource.saveAuthTokens(any(), any(), any()) }
        assertEquals(3, initial401Count.get())
        assertEquals(3, successRetriedCount.get())
    }

    @Test
    fun `missing refresh token aborts retry and clears authentication state`() = runBlocking {
        // Refresh token is empty/missing
        currentRefreshTokenFlow.value = ""

        mockWebServer.enqueue(MockResponse().setResponseCode(401))

        val request = Request.Builder()
            .url(mockWebServer.url("/api/orders").toString())
            .header("Authorization", "Bearer expired_token")
            .build()

        val response = okHttpClient.newCall(request).execute()

        // Should return 401 without retry because no valid refresh token exists
        assertEquals(401, response.code)
        response.close()

        coVerify(exactly = 1) { authPreferencesDataSource.clearAuthTokens() }
        coVerify(exactly = 0) { authPreferencesDataSource.saveAuthTokens(any(), any(), any()) }
    }

    @Test
    fun `infinite retry protection stops after 3 failed attempts`() = runBlocking {
        // Server persistently returns 401 even for new tokens
        mockWebServer.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                return MockResponse().setResponseCode(401)
            }
        }

        val request = Request.Builder()
            .url(mockWebServer.url("/api/secure").toString())
            .header("Authorization", "Bearer initial_token")
            .build()

        val response = okHttpClient.newCall(request).execute()

        // OkHttp gives up when authenticator returns null after 3 attempts
        assertEquals(401, response.code)
        response.close()

        // Total attempts on server: initial + 3 retries = 4
        assertTrue(mockWebServer.requestCount <= 4)
    }

    @Test
    fun `already refreshed token by prior request immediately retries without second refresh`() = runBlocking {
        // Simulate that token was already refreshed by thread A
        val alreadyRefreshedToken = "already_refreshed_access_token_999"
        currentAccessTokenFlow.value = alreadyRefreshedToken

        mockWebServer.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                val authHeader = request.getHeader("Authorization")
                return if (authHeader == "Bearer $alreadyRefreshedToken") {
                    MockResponse().setResponseCode(200).setBody("""{"status":"success"}""")
                } else {
                    MockResponse().setResponseCode(401)
                }
            }
        }

        // Thread B sends request with old stale token
        val request = Request.Builder()
            .url(mockWebServer.url("/api/profile").toString())
            .header("Authorization", "Bearer stale_old_token")
            .build()

        val response = okHttpClient.newCall(request).execute()

        assertEquals(200, response.code)
        response.close()

        // saveAuthTokens was NOT called because token in DataSource was already fresh
        coVerify(exactly = 0) { authPreferencesDataSource.saveAuthTokens(any(), any(), any()) }
    }
}