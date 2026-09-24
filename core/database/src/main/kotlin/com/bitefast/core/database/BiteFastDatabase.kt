package com.bitefast.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.bitefast.core.database.dao.CartDao
import com.bitefast.core.database.dao.RestaurantDao
import com.bitefast.core.database.entity.CartItemEntity
import com.bitefast.core.database.entity.RestaurantEntity

@Database(
    entities = [
        CartItemEntity::class,
        RestaurantEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class BiteFastDatabase : RoomDatabase() {
    abstract fun cartDao(): CartDao
    abstract fun restaurantDao(): RestaurantDao
}
