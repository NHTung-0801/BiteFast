package com.bitefast.core.domain.repository

import com.bitefast.core.model.FavoriteItem
import com.bitefast.core.model.MenuItem
import com.bitefast.core.model.Restaurant
import kotlinx.coroutines.flow.Flow

interface FavoriteRepository {
    fun getFavoriteDishes(): Flow<List<FavoriteItem>>
    fun getFavoriteRestaurants(): Flow<List<FavoriteItem>>
    fun isFavorite(targetId: String): Flow<Boolean>
    suspend fun toggleFavoriteDish(item: MenuItem, restaurantName: String = ""): Boolean
    suspend fun toggleFavoriteRestaurant(restaurant: Restaurant): Boolean
    suspend fun removeFavorite(targetId: String)
    fun getFavoritesCount(): Flow<Int>
}
