package com.bitefast.core.database.di

import android.content.Context
import androidx.room.Room
import com.bitefast.core.database.BiteFastDatabase
import com.bitefast.core.database.dao.AddressDao
import com.bitefast.core.database.dao.CartDao
import com.bitefast.core.database.dao.RestaurantDao
import com.bitefast.core.database.security.KeystoreManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.sqlcipher.database.SupportFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideBiteFastDatabase(
        @ApplicationContext context: Context,
        keystoreManager: KeystoreManager
    ): BiteFastDatabase {
        // Load native SQLCipher binary library
        System.loadLibrary("sqlcipher")

        // Retrieve or generate secure 256-bit passphrase from Android Hardware Keystore
        val passphrase = keystoreManager.getOrCreateDatabasePassphrase()
        val openHelperFactory = SupportFactory(passphrase)

        return Room.databaseBuilder(
            context,
            BiteFastDatabase::class.java,
            "bitefast.db"
        )
        .openHelperFactory(openHelperFactory)
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    fun provideCartDao(database: BiteFastDatabase): CartDao = database.cartDao()

    @Provides
    fun provideRestaurantDao(database: BiteFastDatabase): RestaurantDao = database.restaurantDao()

    @Provides
    fun provideAddressDao(database: BiteFastDatabase): AddressDao = database.addressDao()

    @Provides
    fun provideNotificationDao(database: BiteFastDatabase): com.bitefast.core.database.dao.NotificationDao = database.notificationDao()

    @Provides
    fun provideFavoriteDao(database: BiteFastDatabase): com.bitefast.core.database.dao.FavoriteDao = database.favoriteDao()
}