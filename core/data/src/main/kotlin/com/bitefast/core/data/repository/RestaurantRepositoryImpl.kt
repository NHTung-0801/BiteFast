package com.bitefast.core.data.repository

import com.bitefast.core.data.mapper.asExternalModel
import com.bitefast.core.database.dao.RestaurantDao
import com.bitefast.core.domain.repository.RestaurantRepository
import com.bitefast.core.model.MenuItem
import com.bitefast.core.model.Restaurant
import com.bitefast.core.network.api.BiteFastApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RestaurantRepositoryImpl @Inject constructor(
    private val restaurantDao: RestaurantDao,
    private val apiService: BiteFastApiService
) : RestaurantRepository {

    companion object {
        val sampleRestaurants = listOf(
            Restaurant(
                id = "res_1",
                name = "Cơm Tấm Phúc Lộc Thọ - Lê Văn Việt",
                description = "Cơm tấm sườn bì chả nướng than hoa chuẩn vị Sài Gòn",
                cuisine = "Cơm",
                rating = 4.8,
                distance = 1.2f,
                estimatedTime = 25,
                priceLevel = 2,
                imageUrl = "https://images.unsplash.com/photo-1544025162-d76694265947?w=500",
                coverImageUrl = "https://images.unsplash.com/photo-1544025162-d76694265947?w=800",
                address = "123 Lê Văn Việt, TP. Thủ Đức",
                isOpen = true,
                isFreeDelivery = true,
                tags = listOf("Cơm tấm", "Sườn nướng", "Freeship")
            ),
            Restaurant(
                id = "res_2",
                name = "Phở Thìn Lò Đúc - Bò Tái Lăn",
                description = "Nước dùng béo ngậy, thịt bò xào lăn thơm lừng hành hoa",
                cuisine = "Phở",
                rating = 4.7,
                distance = 2.5f,
                estimatedTime = 30,
                priceLevel = 3,
                imageUrl = "https://images.unsplash.com/photo-1582878826629-29b7ad1cdc43?w=500",
                coverImageUrl = "https://images.unsplash.com/photo-1582878826629-29b7ad1cdc43?w=800",
                address = "456 Nguyễn Huệ, Quận 1",
                isOpen = true,
                isFreeDelivery = false,
                tags = listOf("Phở bò", "Tái lăn", "Hà Nội")
            ),
            Restaurant(
                id = "res_3",
                name = "Pizza 4P's - Hai Bà Trưng",
                description = "Pizza nướng củi phô mai Burrata tươi sản xuất thủ công",
                cuisine = "Pizza",
                rating = 4.9,
                distance = 3.1f,
                estimatedTime = 35,
                priceLevel = 4,
                imageUrl = "https://images.unsplash.com/photo-1513104890138-7c749659a591?w=500",
                coverImageUrl = "https://images.unsplash.com/photo-1513104890138-7c749659a591?w=800",
                address = "151 Hai Bà Trưng, Quận 3",
                isOpen = true,
                isFreeDelivery = true,
                tags = listOf("Pizza", "Phô mai", "Ý")
            ),
            Restaurant(
                id = "res_4",
                name = "Trà Sữa Phúc Long - Landmark 81",
                description = "Trà ô long đậm vị truyền thống, trà đào cam sả thanh mát",
                cuisine = "Trà sữa",
                rating = 4.6,
                distance = 1.8f,
                estimatedTime = 20,
                priceLevel = 2,
                imageUrl = "https://images.unsplash.com/photo-1558857563-b371033873b8?w=500",
                coverImageUrl = "https://images.unsplash.com/photo-1558857563-b371033873b8?w=800",
                address = "Tầng trệt Landmark 81, Bình Thạnh",
                isOpen = true,
                isFreeDelivery = false,
                tags = listOf("Trà sữa", "Đậm vị", "Trà đào")
            )
        )
    }

    override fun getRestaurants(query: String?, cuisine: String?): Flow<List<Restaurant>> {
        return restaurantDao.getAllRestaurants().map { localList ->
            val list = if (localList.isNotEmpty()) {
                localList.map { it.asExternalModel() }
            } else {
                sampleRestaurants
            }

            list.filter { item ->
                val matchesQuery = query.isNullOrBlank() || item.name.contains(query, ignoreCase = true)
                val matchesCuisine = cuisine.isNullOrBlank() || item.cuisine.equals(cuisine, ignoreCase = true)
                matchesQuery && matchesCuisine
            }
        }
    }

    override suspend fun getRestaurantDetail(id: String): Restaurant {
        val local = restaurantDao.getRestaurantById(id)
        if (local != null) return local.asExternalModel()
        return sampleRestaurants.find { it.id == id } ?: try {
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
