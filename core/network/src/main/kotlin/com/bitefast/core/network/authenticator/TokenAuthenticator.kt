package com.bitefast.core.network.authenticator

import com.bitefast.core.datastore.AuthPreferencesDataSource
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenAuthenticator @Inject constructor(
    private val authPreferencesDataSource: AuthPreferencesDataSource
) : Authenticator {

    private val mutex = Mutex()

    override fun authenticate(route: Route?, response: Response): Request? {
        if (responseCount(response) >= 3) {
            return null // Do not retry more than 3 times
        }

        return runBlocking {
            mutex.withLock {
                val currentToken = authPreferencesDataSource.accessToken.firstOrNull()
                val requestHeaderToken = response.request.header("Authorization")?.removePrefix("Bearer ")

                // If token was already refreshed by another concurrent request, retry with the new one
                if (currentToken != null && currentToken != requestHeaderToken) {
                    return@withLock response.request.newBuilder()
                        .header("Authorization", "Bearer $currentToken")
                        .build()
                }

                val refreshToken = authPreferencesDataSource.refreshToken.firstOrNull()
                if (refreshToken.isNullOrBlank()) {
                    authPreferencesDataSource.clearAuthTokens()
                    return@withLock null
                }

                // In a production app, execute silent token refresh network call here
                // For now, if refresh is successful, save tokens and retry:
                val newAccessToken = "refreshed_access_token_${System.currentTimeMillis()}"
                val newRefreshToken = "refreshed_refresh_token_${System.currentTimeMillis()}"
                val userId = authPreferencesDataSource.userId.firstOrNull() ?: ""

                authPreferencesDataSource.saveAuthTokens(newAccessToken, newRefreshToken, userId)

                response.request.newBuilder()
                    .header("Authorization", "Bearer $newAccessToken")
                    .build()
            }
        }
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }
}
