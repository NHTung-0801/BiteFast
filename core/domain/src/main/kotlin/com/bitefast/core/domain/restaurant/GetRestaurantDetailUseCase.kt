package com.bitefast.core.domain.restaurant

import com.bitefast.core.domain.repository.RestaurantRepository
import com.bitefast.core.model.MenuItem
import com.bitefast.core.model.Restaurant
import javax.inject.Inject

data class RestaurantDetailResult(
    val restaurant: Restaurant,
    val menuItems: List<MenuItem>
)

class GetRestaurantDetailUseCase @Inject constructor(
    private val restaurantRepository: RestaurantRepository
) {
    suspend operator fun invoke(restaurantId: String): RestaurantDetailResult {
        val restaurant = restaurantRepository.getRestaurantDetail(restaurantId)
        val menu = restaurantRepository.getRestaurantMenu(restaurantId)
        return RestaurantDetailResult(restaurant, menu)
    }
}
