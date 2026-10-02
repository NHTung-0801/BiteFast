package com.bitefast.core.data.repository

import com.bitefast.core.data.mapper.toEntity
import com.bitefast.core.data.mapper.toFavoriteItem
import com.bitefast.core.database.dao.FavoriteDao
import com.bitefast.core.database.entity.FavoriteEntity
import com.bitefast.core.domain.repository.FavoriteRepository
import com.bitefast.core.model.FavoriteItem
import com.bitefast.core.model.FavoriteType
import com.bitefast.core.model.MenuItem
import com.bitefast.core.model.Restaurant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoriteRepositoryImpl @Inject constructor(
    private val favoriteDao: FavoriteDao
) : FavoriteRepository {

    override fun getFavoriteDishes(): Flow<List<FavoriteItem>> {
        return favoriteDao.getFavoritesByType(FavoriteType.DISH.name)
            .map { list -> list.map { it.toFavoriteItem() } }
    }

    override fun getFavoriteRestaurants(): Flow<List<FavoriteItem>> {
        return favoriteDao.getFavoritesByType(FavoriteType.RESTAURANT.name)
            .map { list -> list.map { it.toFavoriteItem() } }
    }

    override fun isFavorite(targetId: String): Flow<Boolean> {
        return favoriteDao.isFavorite(targetId)
    }

    override suspend fun toggleFavoriteDish(item: MenuItem, restaurantName: String): Boolean {
        val existing = favoriteDao.getFavoriteByTargetId(item.id)
        return if (existing != null) {
            favoriteDao.deleteFavoriteByTargetId(item.id)
            false
        } else {
            val entity = FavoriteEntity(
                id = "dish_${item.id}",
                type = FavoriteType.DISH.name,
                targetId = item.id,
                name = item.name,
                description = item.description,
                price = item.price,
                imageUrl = item.imageUrl,
                rating = item.rating,
                restaurantId = item.restaurantId,
                restaurantName = restaurantName,
                category = item.category,
                createdAt = System.currentTimeMillis()
            )
            favoriteDao.insertFavorite(entity)
            true
        }
    }

    override suspend fun toggleFavoriteRestaurant(restaurant: Restaurant): Boolean {
        val existing = favoriteDao.getFavoriteByTargetId(restaurant.id)
        return if (existing != null) {
            favoriteDao.deleteFavoriteByTargetId(restaurant.id)
            false
        } else {
            val entity = FavoriteEntity(
                id = "res_${restaurant.id}",
                type = FavoriteType.RESTAURANT.name,
                targetId = restaurant.id,
                name = restaurant.name,
                description = restaurant.description,
                price = 0.0,
                imageUrl = restaurant.imageUrl,
                rating = restaurant.rating,
                restaurantId = restaurant.id,
                restaurantName = restaurant.name,
                category = restaurant.cuisine,
                createdAt = System.currentTimeMillis()
            )
            favoriteDao.insertFavorite(entity)
            true
        }
    }

    override suspend fun removeFavorite(targetId: String) {
        favoriteDao.deleteFavoriteByTargetId(targetId)
    }

    override fun getFavoritesCount(): Flow<Int> {
        return favoriteDao.getFavoriteCount()
    }
}
