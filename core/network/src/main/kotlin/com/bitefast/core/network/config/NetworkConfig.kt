package com.bitefast.core.network.config

/**
 * Operating mode for BiteFast network layer.
 */
enum class NetworkMode {
    /**
     * Self-contained mock network engine with simulated realistic latency (300ms)
     * and RESTful status codes. Allows 100% full offline operation and verification.
     */
    MOCK,

    /**
     * Connects to a real backend server via HTTP/HTTPS.
     */
    LIVE
}

object NetworkConfig {
    /**
     * Default to MOCK mode for instant offline usability and reliable emulator testing.
     * Can be switched to LIVE mode anytime by setting NetworkConfig.mode = NetworkMode.LIVE.
     */
    var mode: NetworkMode = NetworkMode.MOCK

    /**
     * Base URL for LIVE mode.
     * Android Emulator localhost to PC host: "http://10.0.2.2:8080/"
     */
    var baseUrl: String = "https://api.bitefast.com/"

    const val LOCALHOST_EMULATOR_URL = "http://10.0.2.2:8080/"
    const val DEFAULT_PRODUCTION_URL = "https://api.bitefast.com/"
}
