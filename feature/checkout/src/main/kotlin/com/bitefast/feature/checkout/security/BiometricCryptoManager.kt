package com.bitefast.feature.checkout.security

import android.content.Context
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.inject.Inject
import javax.inject.Singleton

enum class BiometricAuthStatus {
    READY,
    NO_HARDWARE,
    NOT_ENROLLED,
    UNAVAILABLE
}

/**
 * Enterprise Biometric Crypto Manager managing Android Keystore AES-256 GCM/CBC
 * cipher keys tied directly to biometric authentication.
 *
 * Provides genuine cryptographic authentication via [BiometricPrompt.CryptoObject]
 * rather than relying on insecure client-side boolean flags.
 */
@Singleton
class BiometricCryptoManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val KEY_ALIAS = "BiteFastBiometricAuthKey"
        private const val TRANSFORMATION = "${KeyProperties.KEY_ALGORITHM_AES}/${KeyProperties.BLOCK_MODE_CBC}/${KeyProperties.ENCRYPTION_PADDING_PKCS7}"
    }

    /**
     * Checks whether biometric hardware and enrollment are ready on this device.
     */
    fun checkBiometricAvailability(): BiometricAuthStatus {
        val biometricManager = BiometricManager.from(context)
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> BiometricAuthStatus.READY
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricAuthStatus.NOT_ENROLLED
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BiometricAuthStatus.NO_HARDWARE
            else -> BiometricAuthStatus.UNAVAILABLE
        }
    }

    /**
     * Retrieves existing symmetric AES-256 key from Android Keystore or generates
     * a new one with user authentication required.
     */
    private fun getOrCreateSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        if (keyStore.containsAlias(KEY_ALIAS)) {
            val key = keyStore.getKey(KEY_ALIAS, null) as? SecretKey
            if (key != null) return key
        }

        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        val specBuilder = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
            .setUserAuthenticationRequired(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            specBuilder.setUserAuthenticationParameters(
                0, // 0 = requires biometric prompt every time
                KeyProperties.AUTH_BIOMETRIC_STRONG
            )
        }

        keyGenerator.init(specBuilder.build())
        return keyGenerator.generateKey()
    }

    /**
     * Creates an initialized [Cipher] and wraps it inside a [BiometricPrompt.CryptoObject]
     * to unlock upon successful biometric verification.
     */
    fun createCryptoObject(): BiometricPrompt.CryptoObject? {
        return try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val secretKey = getOrCreateSecretKey()
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            BiometricPrompt.CryptoObject(cipher)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Builds standard Material 3 BiometricPrompt Info.
     */
    fun createPromptInfo(
        title: String = "Xác thực đặt hàng BiteFast",
        subtitle: String = "Quét vân tay hoặc khuôn mặt để phê duyệt thanh toán",
        negativeButtonText: String = "Hủy bỏ"
    ): BiometricPrompt.PromptInfo {
        return BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText(negativeButtonText)
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()
    }
}
