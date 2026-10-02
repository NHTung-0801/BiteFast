package com.bitefast.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.bitefast.core.database.dao.AddressDao
import com.bitefast.core.database.dao.CartDao
import com.bitefast.core.database.dao.FavoriteDao
import com.bitefast.core.database.dao.NotificationDao
import com.bitefast.core.database.dao.RestaurantDao
import com.bitefast.core.database.entity.AddressEntity
import com.bitefast.core.database.entity.CartItemEntity
import com.bitefast.core.database.entity.FavoriteEntity
import com.bitefast.core.database.entity.NotificationEntity
import com.bitefast.core.database.entity.RestaurantEntity

@Database(
    entities = [
        CartItemEntity::class,
        RestaurantEntity::class,
        AddressEntity::class,
        NotificationEntity::class,
        FavoriteEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class BiteFastDatabase : RoomDatabase() {
    abstract fun cartDao(): CartDao
    abstract fun restaurantDao(): RestaurantDao
    abstract fun addressDao(): AddressDao
    abstract fun notificationDao(): NotificationDao
    abstract fun favoriteDao(): FavoriteDao
}