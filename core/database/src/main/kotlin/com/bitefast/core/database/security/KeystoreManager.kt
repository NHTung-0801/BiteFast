package com.bitefast.core.database.security

import android.content.Context
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Enterprise hardware-backed Keystore Manager.
 * Manages the generation and secure retrieval of the 256-bit AES database passphrase
 * stored inside hardware-backed (TEE / StrongBox) EncryptedSharedPreferences.
 */
@Singleton
class KeystoreManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val masterKey: MasterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    private val securePreferences by lazy {
        EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    /**
     * Retrieves the existing 256-bit database passphrase from hardware-backed encrypted storage,
     * or generates a cryptographically secure random 256-bit passphrase, persists it, and returns it.
     */
    fun getOrCreateDatabasePassphrase(): ByteArray {
        val encodedPassphrase = securePreferences.getString(KEY_DB_PASSPHRASE, null)
        if (!encodedPassphrase.isNullOrEmpty()) {
            return Base64.decode(encodedPassphrase, Base64.NO_WRAP)
        }

        val newPassphrase = ByteArray(32)
        SecureRandom().nextBytes(newPassphrase)
        val base64Encoded = Base64.encodeToString(newPassphrase, Base64.NO_WRAP)
        securePreferences.edit()
            .putString(KEY_DB_PASSPHRASE, base64Encoded)
            .apply()

        return newPassphrase
    }

    /**
     * Clears the stored database passphrase. Used only for emergency security wipe or test cleanups.
     */
    fun clearDatabasePassphrase() {
        securePreferences.edit().remove(KEY_DB_PASSPHRASE).apply()
    }

    companion object {
        private const val PREFS_NAME = "bitefast_encrypted_db_keystore_prefs"
        private const val KEY_DB_PASSPHRASE = "bitefast_sqlcipher_master_passphrase"
    }
}