package com.bitefast.core.network.websocket

import com.bitefast.core.network.config.NetworkConfig
import com.bitefast.core.network.config.NetworkMode
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class BiteFastWebSocketClientTest {

    private lateinit var okHttpClient: OkHttpClient
    private lateinit var json: Json
    private lateinit var mockSocket: MockOrderTrackingSocket
    private lateinit var client: BiteFastWebSocketClient

    @BeforeEach
    fun setUp() {
        NetworkConfig.mode = NetworkMode.MOCK
        okHttpClient = mockk(relaxed = true)
        json = Json { ignoreUnknownKeys = true }
        mockSocket = MockOrderTrackingSocket()
        client = BiteFastWebSocketClient(okHttpClient, json, mockSocket)
    }

    @AfterEach
    fun tearDown() {
        client.close()
    }

    @Test
    @DisplayName("Mock socket stream emits live tracking event with coordinates and ETA")
    fun mockSocket_streamTracking_emitsValidEvent() = runTest {
        val event = mockSocket.streamTracking("ord_test").first()

        assertEquals("ord_test", event.orderId)
        assertEquals("ON_THE_WAY", event.status)
        assertEquals("Nguyễn Văn Hùng", event.driverName)
        assertTrue(event.driverLat > 0.0)
        assertTrue(event.driverLng > 0.0)
        assertTrue(event.etaMinutes > 0)
        assertTrue(event.progressPercent >= 0.35f)
    }

    @Test
    @DisplayName("BiteFastWebSocketClient in MOCK mode delegates to mock socket and connects")
    fun client_inMockMode_connectsAndEmits() = runTest {
        val event = client.observeLiveTracking("ord_101").first()

        assertNotNull(event)
        assertEquals("ord_101", event.orderId)
        assertEquals(WebSocketConnectionState.Connected, client.connectionState.value)
    }

    @Test
    @DisplayName("Client close transitions connection state to Disconnected")
    fun client_close_updatesState() {
        client.close()
        assertTrue(client.connectionState.value is WebSocketConnectionState.Disconnected)
    }
}
