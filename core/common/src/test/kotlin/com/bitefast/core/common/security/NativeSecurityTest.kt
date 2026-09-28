package com.bitefast.core.common.security

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class NativeSecurityTest {

    @Test
    fun getMapsApiKey_returnsValidFormat() {
        val key = NativeSecurity.getMapsApiKey()
        assertNotNull(key)
        assertTrue(key.startsWith("AIzaSyD-"), "Maps API key should start with AIzaSyD-")
        assertTrue(key.contains("bitefast"), "Maps key should contain bitefast identity")
    }

    @Test
    fun getBackendBaseUrl_returnsValidHttpsUrl() {
        val url = NativeSecurity.getBackendBaseUrl()
        assertNotNull(url)
        assertTrue(url.startsWith("https://"), "Backend URL must be secure HTTPS")
        assertTrue(url.endsWith("/v1/"), "Backend URL should point to v1 API endpoint")
    }

    @Test
    fun getPaymentGatewayKey_returnsValidKey() {
        val key = NativeSecurity.getPaymentGatewayKey()
        assertNotNull(key)
        assertTrue(key.startsWith("pk_live_"), "Payment key must be live publishable key")
    }

    @Test
    fun maskAndUnmask_areBijectiveAndSymmetric() {
        val original = "TestSecretPayload123!@#"
        val masked = NativeSecurity.mask(original)
        val unmasked = NativeSecurity.unmask(masked)
        assertEquals(original, unmasked)
    }

    @Test
    fun securityUtils_reportReturnsValidScore() {
        val report = SecurityUtils.getSecurityReport()
        assertNotNull(report)
        assertTrue(report.securityScore in 0..100)
    }
}
