package com.bitefast.core.common.security

/**
 * Enterprise Native Security Bridge.
 * Obfuscates sensitive production keys (Maps API, Payment Gateway, Backend URLs)
 * using C++ JNI with multi-byte XOR masking.
 *
 * Implements a Graceful Fallback mechanism:
 * - If native `.so` is present, loads via JNI for high-security tamper resistance.
 * - If running on dev JVM / local test environment without NDK, safely falls back
 *   to in-memory XOR unmasking with 100% byte equivalence.
 */
object NativeSecurity {

    private val XOR_KEY = byteArrayOf(0x42, 0x69, 0x74, 0x65, 0x46, 0x61, 0x73, 0x74) // "BiteFast"

    private val MASKED_MAPS_KEY = byteArrayOf(
        0x03, 0x20, 0x0E, 0x04, 0x15, 0x18, 0x37, 0x59, 0x20, 0x00, 0x00, 0x00,
        0x20, 0x00, 0x00, 0x00, 0x6F, 0x04, 0x15, 0x15, 0x35, 0x4C, 0x00, 0x11,
        0x21, 0x1C, 0x06, 0x00, 0x6B, 0x11, 0x01, 0x1B, 0x26, 0x44, 0x1F, 0x00,
        0x3F, 0x4C, 0x41, 0x44, 0x70, 0x5F
    )

    private val MASKED_BACKEND_URL = byteArrayOf(
        0x2A, 0x1D, 0x00, 0x15, 0x35, 0x5B, 0x5C, 0x5B, 0x23, 0x19, 0x1D, 0x4B,
        0x24, 0x08, 0x07, 0x11, 0x24, 0x08, 0x07, 0x11, 0x68, 0x17, 0x1D, 0x5B,
        0x34, 0x58, 0x5B
    )

    private val MASKED_PAYMENT_KEY = byteArrayOf(
        0x32, 0x02, 0x2B, 0x09, 0x2F, 0x17, 0x16, 0x2B, 0x20, 0x00, 0x00, 0x00,
        0x20, 0x00, 0x00, 0x00, 0x1D, 0x19, 0x15, 0x1C, 0x19, 0x58, 0x4A, 0x4C,
        0x7A, 0x5B, 0x46, 0x56, 0x75, 0x55, 0x47, 0x45, 0x73
    )

    val isNativeLoaded: Boolean

    init {
        var loaded = false
        try {
            System.loadLibrary("bitefast_native")
            loaded = true
        } catch (_: Throwable) {
            // Graceful fallback for local development or non-NDK builds
            loaded = false
        }
        isNativeLoaded = loaded
    }

    // JNI Native methods
    private external fun getMapsApiKeyNative(): String
    private external fun getBackendBaseUrlNative(): String
    private external fun getPaymentGatewayKeyNative(): String

    /**
     * Retrieves the Google Maps API Key securely.
     */
    fun getMapsApiKey(): String {
        return if (isNativeLoaded) {
            try {
                getMapsApiKeyNative()
            } catch (_: UnsatisfiedLinkError) {
                unmask(MASKED_MAPS_KEY)
            }
        } else {
            unmask(MASKED_MAPS_KEY)
        }
    }

    /**
     * Retrieves the Backend Base URL securely.
     */
    fun getBackendBaseUrl(): String {
        return if (isNativeLoaded) {
            try {
                getBackendBaseUrlNative()
            } catch (_: UnsatisfiedLinkError) {
                unmask(MASKED_BACKEND_URL)
            }
        } else {
            unmask(MASKED_BACKEND_URL)
        }
    }

    /**
     * Retrieves the Payment Gateway Key securely.
     */
    fun getPaymentGatewayKey(): String {
        return if (isNativeLoaded) {
            try {
                getPaymentGatewayKeyNative()
            } catch (_: UnsatisfiedLinkError) {
                unmask(MASKED_PAYMENT_KEY)
            }
        } else {
            unmask(MASKED_PAYMENT_KEY)
        }
    }

    /**
     * In-memory XOR unmasking with UTF-8 decoding.
     */
    fun unmask(bytes: ByteArray, key: ByteArray = XOR_KEY): String {
        val result = ByteArray(bytes.size)
        for (i in bytes.indices) {
            result[i] = (bytes[i].toInt() xor key[i % key.size].toInt()).toByte()
        }
        return String(result, Charsets.UTF_8)
    }

    /**
     * In-memory XOR mask helper utility.
     */
    fun mask(input: String, key: ByteArray = XOR_KEY): ByteArray {
        val bytes = input.toByteArray(Charsets.UTF_8)
        val result = ByteArray(bytes.size)
        for (i in bytes.indices) {
            result[i] = (bytes[i].toInt() xor key[i % key.size].toInt()).toByte()
        }
        return result
    }
}
