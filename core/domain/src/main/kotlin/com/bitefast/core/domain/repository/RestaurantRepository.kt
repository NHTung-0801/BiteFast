package com.bitefast.core.domain.repository

import com.bitefast.core.model.MenuItem
import com.bitefast.core.model.Restaurant
import kotlinx.coroutines.flow.Flow

interface RestaurantRepository {
    fun getRestaurants(query: String? = null, cuisine: String? = null): Flow<List<Restaurant>>
    suspend fun getRestaurantDetail(id: String): Restaurant
    suspend fun getRestaurantMenu(restaurantId: String): List<MenuItem>
}
