package com.bitefast.core.domain.favorite

import com.bitefast.core.domain.repository.FavoriteRepository
import com.bitefast.core.model.FavoriteItem
import com.bitefast.core.model.FavoriteType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFavoritesUseCase @Inject constructor(
    private val favoriteRepository: FavoriteRepository
) {
    operator fun invoke(type: FavoriteType): Flow<List<FavoriteItem>> {
        return when (type) {
            FavoriteType.DISH -> favoriteRepository.getFavoriteDishes()
            FavoriteType.RESTAURANT -> favoriteRepository.getFavoriteRestaurants()
        }
    }

    fun isFavorite(targetId: String): Flow<Boolean> {
        return favoriteRepository.isFavorite(targetId)
    }

    fun getCount(): Flow<Int> {
        return favoriteRepository.getFavoritesCount()
    }
}
