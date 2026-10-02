package com.bitefast.core.network.di

import com.bitefast.core.network.api.BiteFastApiService
import com.bitefast.core.network.authenticator.TokenAuthenticator
import com.bitefast.core.network.config.NetworkConfig
import com.bitefast.core.network.config.NetworkMode
import com.bitefast.core.network.interceptor.AuthInterceptor
import com.bitefast.core.network.mock.MockNetworkInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.CertificatePinner
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
        encodeDefaults = true
    }

    @Provides
    @Singleton
    fun provideCertificatePinner(): CertificatePinner {
        return CertificatePinner.Builder()
            .add("api.bitefast.com", "sha256/k2oTQLGenANUdYzs1-Ky5Pznw9XuhxzFuWYHX9CF6Ng=")
            .add("api.bitefast.com", "sha256/WoiWRyIOVNa9ihaBciRSC7XHjliYS9VwUGOIud4PB18=")
            .build()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        mockNetworkInterceptor: MockNetworkInterceptor,
        tokenAuthenticator: TokenAuthenticator,
        certificatePinner: CertificatePinner
    ): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val builder = OkHttpClient.Builder()
            // Auth interceptor attaches Bearer token if user is logged in
            .addInterceptor(authInterceptor)
            // Mock network interceptor handles requests if NetworkConfig.mode is MOCK
            .addInterceptor(mockNetworkInterceptor)
            .addInterceptor(loggingInterceptor)
            .authenticator(tokenAuthenticator)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)

        // Only enforce Certificate Pinning when strictly in LIVE mode against official domain
        if (NetworkConfig.mode == NetworkMode.LIVE && NetworkConfig.baseUrl.contains("api.bitefast.com")) {
            builder.certificatePinner(certificatePinner)
        }

        return builder.build()
    }

    @Provides
    @Singleton
    fun provideBiteFastApiService(
        okHttpClient: OkHttpClient,
        json: Json
    ): BiteFastApiService {
        val contentType = "application/json".toMediaType()

        val url = if (NetworkConfig.mode == NetworkMode.LIVE) {
            NetworkConfig.baseUrl
        } else {
            "https://api.bitefast.com/"
        }

        return Retrofit.Builder()
            .baseUrl(url)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(BiteFastApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideOrderTrackingSocketClient(
        client: com.bitefast.core.network.websocket.BiteFastWebSocketClient
    ): com.bitefast.core.network.websocket.OrderTrackingSocketClient = client
}

