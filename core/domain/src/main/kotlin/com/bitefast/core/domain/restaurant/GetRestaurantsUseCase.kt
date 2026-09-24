package com.bitefast.core.domain.restaurant

import com.bitefast.core.domain.repository.RestaurantRepository
import com.bitefast.core.model.Restaurant
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRestaurantsUseCase @Inject constructor(
    private val restaurantRepository: RestaurantRepository
) {
    operator fun invoke(query: String? = null, cuisine: String? = null): Flow<List<Restaurant>> {
        return restaurantRepository.getRestaurants(query, cuisine)
    }
}
