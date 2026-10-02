package com.bitefast.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthPreferencesDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private object PreferencesKeys {
        val ACCESS_TOKEN = stringPreferencesKey("access_token")
        val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        val USER_ID = stringPreferencesKey("user_id")
        val USER_EMAIL = stringPreferencesKey("user_email")
        val IS_GUEST = booleanPreferencesKey("is_guest")
        val USER_NAME = stringPreferencesKey("user_name")
        val USER_PHONE = stringPreferencesKey("user_phone")
        val USER_AVATAR = stringPreferencesKey("user_avatar")
    }

    val accessToken: Flow<String?> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.ACCESS_TOKEN]
    }

    val refreshToken: Flow<String?> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.REFRESH_TOKEN]
    }

    val userId: Flow<String?> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.USER_ID]
    }

    val userEmail: Flow<String?> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.USER_EMAIL]
    }

    val userName: Flow<String?> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.USER_NAME]
    }

    val userPhone: Flow<String?> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.USER_PHONE]
    }

    val userAvatar: Flow<String?> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.USER_AVATAR]
    }

    val isGuest: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.IS_GUEST] ?: true
    }

    suspend fun saveAuthTokens(
        accessToken: String,
        refreshToken: String,
        userId: String,
        email: String = "",
        name: String = ""
    ) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.ACCESS_TOKEN] = accessToken
            preferences[PreferencesKeys.REFRESH_TOKEN] = refreshToken
            preferences[PreferencesKeys.USER_ID] = userId
            preferences[PreferencesKeys.IS_GUEST] = false
            if (email.isNotBlank()) preferences[PreferencesKeys.USER_EMAIL] = email
            if (name.isNotBlank()) preferences[PreferencesKeys.USER_NAME] = name
        }
    }

    suspend fun saveUserProfile(name: String, phone: String, avatar: String?, email: String? = null) {
        dataStore.edit { preferences ->
            if (name.isNotBlank()) preferences[PreferencesKeys.USER_NAME] = name
            if (phone.isNotBlank()) preferences[PreferencesKeys.USER_PHONE] = phone
            if (!email.isNullOrBlank()) preferences[PreferencesKeys.USER_EMAIL] = email
            if (avatar != null) {
                preferences[PreferencesKeys.USER_AVATAR] = avatar
            } else {
                preferences.remove(PreferencesKeys.USER_AVATAR)
            }
        }
    }

    suspend fun setGuestMode(isGuest: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_GUEST] = isGuest
        }
    }

    suspend fun clearAuthTokens() {
        dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.ACCESS_TOKEN)
            preferences.remove(PreferencesKeys.REFRESH_TOKEN)
            preferences.remove(PreferencesKeys.USER_ID)
            preferences.remove(PreferencesKeys.USER_EMAIL)
            preferences.remove(PreferencesKeys.USER_NAME)
            preferences.remove(PreferencesKeys.USER_PHONE)
            preferences.remove(PreferencesKeys.USER_AVATAR)
            preferences[PreferencesKeys.IS_GUEST] = true
        }
    }
}
