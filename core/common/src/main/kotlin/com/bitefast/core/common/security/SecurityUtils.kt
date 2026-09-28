package com.bitefast.core.common.security

import android.os.Build
import java.io.File

/**
 * Enterprise Security Utility suite for Anti-Tamper, Root Detection,
 * and Emulator Environment verification.
 */
object SecurityUtils {

    private val ROOT_PATHS = arrayOf(
        "/system/app/Superuser.apk",
        "/sbin/su",
        "/system/bin/su",
        "/system/xbin/su",
        "/data/local/xbin/su",
        "/data/local/bin/su",
        "/system/sd/xbin/su",
        "/system/bin/failsafe/su",
        "/data/local/su",
        "/su/bin/su"
    )

    /**
     * Detects if the current device has been rooted or runs test-keys firmware.
     */
    fun isDeviceRooted(): Boolean {
        return checkBuildTags() || checkRootBinaries() || checkSuExecution()
    }

    /**
     * Detects if the app is executing inside an Android emulator or simulator.
     */
    fun isEmulator(): Boolean {
        val fingerprint = Build.FINGERPRINT.orEmpty()
        val model = Build.MODEL.orEmpty()
        val manufacturer = Build.MANUFACTURER.orEmpty()
        val brand = Build.BRAND.orEmpty()
        val device = Build.DEVICE.orEmpty()
        val product = Build.PRODUCT.orEmpty()
        val hardware = Build.HARDWARE.orEmpty()

        return fingerprint.startsWith("generic") ||
                fingerprint.startsWith("unknown") ||
                model.contains("google_sdk") ||
                model.contains("Emulator") ||
                model.contains("Android SDK built for x86") ||
                manufacturer.contains("Genymotion") ||
                (brand.startsWith("generic") && device.startsWith("generic")) ||
                product.contains("sdk_google") ||
                product.contains("google_sdk") ||
                product.contains("sdk") ||
                product.contains("sdk_x86") ||
                product.contains("vbox86p") ||
                product.contains("emulator") ||
                product.contains("simulator") ||
                hardware.contains("goldfish") ||
                hardware.contains("ranchu")
    }

    /**
     * Generates a comprehensive device security report.
     */
    fun getSecurityReport(): DeviceSecurityReport {
        val rooted = isDeviceRooted()
        val emulator = isEmulator()
        var score = 100
        if (rooted) score -= 50
        if (emulator) score -= 20
        return DeviceSecurityReport(
            isRooted = rooted,
            isEmulator = emulator,
            isTampered = rooted,
            securityScore = score.coerceAtLeast(0)
        )
    }

    private fun checkBuildTags(): Boolean {
        val tags = Build.TAGS
        return tags != null && tags.contains("test-keys")
    }

    private fun checkRootBinaries(): Boolean {
        for (path in ROOT_PATHS) {
            try {
                if (File(path).exists()) return true
            } catch (_: Throwable) {
                // Ignore security exceptions
            }
        }
        return false
    }

    private fun checkSuExecution(): Boolean {
        var process: Process? = null
        return try {
            process = Runtime.getRuntime().exec(arrayOf("/system/xbin/which", "su"))
            val exitCode = process.waitFor()
            exitCode == 0
        } catch (_: Throwable) {
            false
        } finally {
            process?.destroy()
        }
    }
}

/**
 * Encapsulates device integrity and security assessment results.
 */
data class DeviceSecurityReport(
    val isRooted: Boolean,
    val isEmulator: Boolean,
    val isTampered: Boolean,
    val securityScore: Int
)
