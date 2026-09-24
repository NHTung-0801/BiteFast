package com.bitefast.core.database.di

import android.content.Context
import androidx.room.Room
import com.bitefast.core.database.BiteFastDatabase
import com.bitefast.core.database.dao.CartDao
import com.bitefast.core.database.dao.RestaurantDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideBiteFastDatabase(
        @ApplicationContext context: Context
    ): BiteFastDatabase {
        return Room.databaseBuilder(
            context,
            BiteFastDatabase::class.java,
            "bitefast.db"
        ).fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    fun provideCartDao(database: BiteFastDatabase): CartDao = database.cartDao()

    @Provides
    fun provideRestaurantDao(database: BiteFastDatabase): RestaurantDao = database.restaurantDao()
}
