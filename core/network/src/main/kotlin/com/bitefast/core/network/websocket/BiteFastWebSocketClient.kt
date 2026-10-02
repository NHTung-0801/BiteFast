package com.bitefast.core.network.websocket

import com.bitefast.core.network.config.NetworkConfig
import com.bitefast.core.network.config.NetworkMode
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Cai dat WebSocket Client tieu chuan su dung OkHttp WebSocket & Mock Fallback Simulator.
 */
@Singleton
class BiteFastWebSocketClient @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val json: Json,
    private val mockOrderTrackingSocket: MockOrderTrackingSocket
) : OrderTrackingSocketClient {

    private val _connectionState = MutableStateFlow<WebSocketConnectionState>(WebSocketConnectionState.Idle)
    override val connectionState: StateFlow<WebSocketConnectionState> = _connectionState.asStateFlow()

    private var activeWebSocket: WebSocket? = null

    override fun observeLiveTracking(orderId: String): Flow<OrderLiveTrackingEvent> {
        if (NetworkConfig.mode == NetworkMode.MOCK) {
            _connectionState.value = WebSocketConnectionState.Connected
            return mockOrderTrackingSocket.streamTracking(orderId)
        }

        return callbackFlow {
            _connectionState.value = WebSocketConnectionState.Connecting

            val wsUrl = NetworkConfig.baseUrl
                .replace("https://", "wss://")
                .replace("http://", "ws://")
                .let { if (it.endsWith("/")) it else "$it/" } + "ws/orders/$orderId/tracking"

            val request = Request.Builder().url(wsUrl).build()

            val listener = object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    activeWebSocket = webSocket
                    _connectionState.value = WebSocketConnectionState.Connected
                }

                override fun onMessage(webSocket: WebSocket, text: String) {
                    try {
                        val event = json.decodeFromString<OrderLiveTrackingEvent>(text)
                        trySend(event)
                    } catch (e: Exception) {
                        // Bo qua frame loi cu phap JSON
                    }
                }

                override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                    _connectionState.value = WebSocketConnectionState.Disconnected(code, reason)
                    webSocket.close(code, reason)
                }

                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    _connectionState.value = WebSocketConnectionState.Disconnected(code, reason)
                    close()
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    _connectionState.value = WebSocketConnectionState.Error(t)
                    close(t)
                }
            }

            val webSocket = okHttpClient.newWebSocket(request, listener)
            activeWebSocket = webSocket

            awaitClose {
                webSocket.close(1000, "Closed by client Flow collector")
                activeWebSocket = null
                _connectionState.value = WebSocketConnectionState.Disconnected(1000, "Flow closed")
            }
        }
    }

    override fun close() {
        activeWebSocket?.close(1000, "Normal closure")
        activeWebSocket = null
        _connectionState.value = WebSocketConnectionState.Disconnected(1000, "Manually closed")
    }
}
