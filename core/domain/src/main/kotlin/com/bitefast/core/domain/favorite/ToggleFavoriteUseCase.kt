package com.bitefast.core.domain.favorite

import com.bitefast.core.domain.repository.FavoriteRepository
import com.bitefast.core.model.MenuItem
import com.bitefast.core.model.Restaurant
import javax.inject.Inject

class ToggleFavoriteUseCase @Inject constructor(
    private val favoriteRepository: FavoriteRepository
) {
    suspend fun toggleDish(item: MenuItem, restaurantName: String = ""): Boolean {
        return favoriteRepository.toggleFavoriteDish(item, restaurantName)
    }

    suspend fun toggleRestaurant(restaurant: Restaurant): Boolean {
        return favoriteRepository.toggleFavoriteRestaurant(restaurant)
    }

    suspend fun remove(targetId: String) {
        favoriteRepository.removeFavorite(targetId)
    }
}
