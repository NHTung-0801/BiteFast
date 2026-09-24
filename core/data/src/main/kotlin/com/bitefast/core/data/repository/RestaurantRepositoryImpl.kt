package com.bitefast.core.data.repository

import com.bitefast.core.data.mapper.asEntity
import com.bitefast.core.data.mapper.asExternalModel
import com.bitefast.core.database.dao.RestaurantDao
import com.bitefast.core.domain.repository.RestaurantRepository
import com.bitefast.core.model.MenuItem
import com.bitefast.core.model.Restaurant
import com.bitefast.core.network.api.BiteFastApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RestaurantRepositoryImpl @Inject constructor(
    private val restaurantDao: RestaurantDao,
    private val apiService: BiteFastApiService
) : RestaurantRepository {

    override fun getRestaurants(query: String?, cuisine: String?): Flow<List<Restaurant>> {
        return restaurantDao.getAllRestaurants().map { localList ->
            localList.map { it.asExternalModel() }
        }
    }

    override suspend fun getRestaurantDetail(id: String): Restaurant {
        val local = restaurantDao.getRestaurantById(id)
        if (local != null) return local.asExternalModel()
        return try {
            apiService.getRestaurantDetail(id)
        } catch (e: Exception) {
            Restaurant(id = id, name = "Nhà hàng đối tác")
        }
    }

    override suspend fun getRestaurantMenu(restaurantId: String): List<MenuItem> {
        return try {
            apiService.getRestaurantMenu(restaurantId)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
