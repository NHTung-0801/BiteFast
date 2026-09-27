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
                name = "Com Tam Phuc Loc Tho - Le Van Viet",
                description = "Com tam suon bi cha nuong than hoa chuan vi Sai Gon",
                cuisine = "Com",
                rating = 4.8,
                distance = 1.2f,
                estimatedTime = 25,
                priceLevel = 2,
                imageUrl = "https://images.unsplash.com/photo-1544025162-d76694265947?w=500",
                coverImageUrl = "https://images.unsplash.com/photo-1544025162-d76694265947?w=800",
                address = "123 Le Van Viet, TP. Thu Duc",
                isOpen = true,
                isFreeDelivery = true,
                tags = listOf("Com tam", "Suon nuong", "Freeship")
            ),
            Restaurant(
                id = "res_2",
                name = "Pho Thin Lo Duc - Bo Tai Lan",
                description = "Nuoc dung beo ngay, thit bo xao lan thom lung hanh hoa",
                cuisine = "Pho",
                rating = 4.7,
                distance = 2.5f,
                estimatedTime = 30,
                priceLevel = 3,
                imageUrl = "https://images.unsplash.com/photo-1582878826629-29b7ad1cdc43?w=500",
                coverImageUrl = "https://images.unsplash.com/photo-1582878826629-29b7ad1cdc43?w=800",
                address = "456 Nguyen Hue, Quan 1",
                isOpen = true,
                isFreeDelivery = false,
                tags = listOf("Pho bo", "Tai lan", "Ha Noi")
            ),
            Restaurant(
                id = "res_3",
                name = "Pizza 4P's - Hai Ba Trung",
                description = "Pizza nuong cui pho mai Burrata tuoi san xuat thu cong",
                cuisine = "Pizza",
                rating = 4.9,
                distance = 3.1f,
                estimatedTime = 35,
                priceLevel = 4,
                imageUrl = "https://images.unsplash.com/photo-1513104890138-7c749659a591?w=500",
                coverImageUrl = "https://images.unsplash.com/photo-1513104890138-7c749659a591?w=800",
                address = "151 Hai Ba Trung, Quan 3",
                isOpen = true,
                isFreeDelivery = true,
                tags = listOf("Pizza", "Pho mai", "Y")
            ),
            Restaurant(
                id = "res_4",
                name = "Tra Sua Phuc Long - Landmark 81",
                description = "Tra o long dam vi truyen thong, tra dao cam sa thanh mat",
                cuisine = "Tra sua",
                rating = 4.6,
                distance = 1.8f,
                estimatedTime = 20,
                priceLevel = 2,
                imageUrl = "https://images.unsplash.com/photo-1558857563-b371033873b8?w=500",
                coverImageUrl = "https://images.unsplash.com/photo-1558857563-b371033873b8?w=800",
                address = "Tang tret Landmark 81, Binh Thanh",
                isOpen = true,
                isFreeDelivery = false,
                tags = listOf("Tra sua", "Dam vi", "Tra dao")
            )
        )

        val sampleMenuMap = mapOf(
            "res_1" to listOf(
                MenuItem(
                    id = "menu_1_1",
                    restaurantId = "res_1",
                    name = "Com Suon Nuong Than Hoa",
                    description = "Suon cay u vi mat ong thom lung kem do chua va mo hanh",
                    price = 48000.0,
                    imageUrl = "https://images.unsplash.com/photo-1544025162-d76694265947?w=500",
                    category = "Mon chinh",
                    isPopular = true,
                    preparationTime = 15
                ),
                MenuItem(
                    id = "menu_1_2",
                    restaurantId = "res_1",
                    name = "Com Suon Bi Cha Dac Biet",
                    description = "Day du suon, bi dai gion, cha trung hap nong hoi va trung op la",
                    price = 65000.0,
                    imageUrl = "https://images.unsplash.com/photo-1544025162-d76694265947?w=500",
                    category = "Mon chinh",
                    isPopular = true,
                    preparationTime = 15
                ),
                MenuItem(
                    id = "menu_1_3",
                    restaurantId = "res_1",
                    name = "Com Ba Roi Nuong Muoi Ot",
                    description = "Ba chi heo chay canh gion rum xat muoi ot tay ninh",
                    price = 55000.0,
                    imageUrl = "https://images.unsplash.com/photo-1544025162-d76694265947?w=500",
                    category = "Mon chinh",
                    isSpicy = true,
                    preparationTime = 15
                ),
                MenuItem(
                    id = "menu_1_4",
                    restaurantId = "res_1",
                    name = "Canh Kho Qua Nhoi Thit",
                    description = "Kho qua tuoi nhoi thit bam thanh mat giai nhiet",
                    price = 20000.0,
                    imageUrl = "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=500",
                    category = "Mon them",
                    preparationTime = 10
                ),
                MenuItem(
                    id = "menu_1_5",
                    restaurantId = "res_1",
                    name = "Sam Bi Dao Hat Chia",
                    description = "Thuc uong thanh nhiet nau tu bi dao tuoi va hat chia Uc",
                    price = 18000.0,
                    imageUrl = "https://images.unsplash.com/photo-1558857563-b371033873b8?w=500",
                    category = "Do uong",
                    isPopular = true,
                    preparationTime = 5
                )
            ),
            "res_2" to listOf(
                MenuItem(
                    id = "menu_2_1",
                    restaurantId = "res_2",
                    name = "Pho Bo Tai Lan Truyen Thong",
                    description = "Thit bo xao lan chao nong voi toi, gung va hanh hoa thom nuc",
                    price = 75000.0,
                    imageUrl = "https://images.unsplash.com/photo-1582878826629-29b7ad1cdc43?w=500",
                    category = "Mon chinh",
                    isPopular = true,
                    preparationTime = 15
                ),
                MenuItem(
                    id = "menu_2_2",
                    restaurantId = "res_2",
                    name = "Pho Tai Nam Gau Bo",
                    description = "To pho thap cam dac biet nuoc dung ninh xuong ong 24 gio",
                    price = 85000.0,
                    imageUrl = "https://images.unsplash.com/photo-1582878826629-29b7ad1cdc43?w=500",
                    category = "Mon chinh",
                    isPopular = false,
                    preparationTime = 15
                ),
                MenuItem(
                    id = "menu_2_3",
                    restaurantId = "res_2",
                    name = "Quay Gion Ha Noi (3 chiec)",
                    description = "Quay dac ruot vang gion cham nuoc dung pho",
                    price = 15000.0,
                    imageUrl = "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=500",
                    category = "Mon them",
                    preparationTime = 5
                ),
                MenuItem(
                    id = "menu_2_4",
                    restaurantId = "res_2",
                    name = "Tra Hoa Cuc Mat Ong",
                    description = "Tra hoa cuc nong thom diu ket hop mat ong rung",
                    price = 25000.0,
                    imageUrl = "https://images.unsplash.com/photo-1558857563-b371033873b8?w=500",
                    category = "Do uong",
                    preparationTime = 5
                )
            ),
            "res_3" to listOf(
                MenuItem(
                    id = "menu_3_1",
                    restaurantId = "res_3",
                    name = "Pizza Burrata Thit Nguoi Parma Ham",
                    price = 290000.0,
                    description = "Pho mai Burrata tuoi nguyen qua kem thit nguoi Parma Ham cao cap",
                    imageUrl = "https://images.unsplash.com/photo-1513104890138-7c749659a591?w=500",
                    category = "Pizza",
                    isPopular = true,
                    preparationTime = 20
                ),
                MenuItem(
                    id = "menu_3_2",
                    restaurantId = "res_3",
                    name = "Pizza 4 Loai Pho Mai (4 Cheese)",
                    price = 240000.0,
                    description = "Mozzarella, Gorgonzola, Camembert va Parmesan kem mat ong",
                    imageUrl = "https://images.unsplash.com/photo-1513104890138-7c749659a591?w=500",
                    category = "Pizza",
                    isPopular = true,
                    preparationTime = 20
                ),
                MenuItem(
                    id = "menu_3_3",
                    restaurantId = "res_3",
                    name = "Mi Y Cua Sot Kem Ca Chua",
                    price = 220000.0,
                    description = "Thit cua bien tuoi ngon trong sot kem thom beo chua nhe",
                    imageUrl = "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=500",
                    category = "Mon chinh",
                    preparationTime = 20
                )
            ),
            "res_4" to listOf(
                MenuItem(
                    id = "menu_4_1",
                    restaurantId = "res_4",
                    name = "Tra Dao Cam Sa Dac Biet",
                    description = "Tra o long cao cap ket hop dao mieng gion ngot va cam vang",
                    price = 55000.0,
                    imageUrl = "https://images.unsplash.com/photo-1558857563-b371033873b8?w=500",
                    category = "Tra trai cay",
                    isPopular = true,
                    preparationTime = 8
                ),
                MenuItem(
                    id = "menu_4_2",
                    restaurantId = "res_4",
                    name = "Tra Sua Phuc Long Truyen Thong",
                    description = "Tra den dam da hoa quyen sua dac beo ngay",
                    price = 50000.0,
                    imageUrl = "https://images.unsplash.com/photo-1558857563-b371033873b8?w=500",
                    category = "Tra sua",
                    isPopular = true,
                    preparationTime = 8
                ),
                MenuItem(
                    id = "menu_4_3",
                    restaurantId = "res_4",
                    name = "Ca Phe Sua Da Phin",
                    description = "Ca phe Robusta Tay Nguyen nguyen chat pha phin",
                    price = 42000.0,
                    imageUrl = "https://images.unsplash.com/photo-1558857563-b371033873b8?w=500",
                    category = "Ca phe",
                    preparationTime = 5
                )
            )
        )

        private fun defaultSampleMenu(restaurantId: String): List<MenuItem> = listOf(
            MenuItem(
                id = "item_def_1",
                restaurantId = restaurantId,
                name = "Mon An Dac Trung Nha Hang",
                description = "Mon ngon duoc che bien tu nguyen lieu tuoi sach moi ngay",
                price = 55000.0,
                imageUrl = "https://images.unsplash.com/photo-1544025162-d76694265947?w=500",
                category = "Mon chinh",
                isPopular = true,
                preparationTime = 15
            ),
            MenuItem(
                id = "item_def_2",
                restaurantId = restaurantId,
                name = "Mon Phu An Kem",
                description = "Gia vi dam da hop khau vi moi thanh vien trong gia dinh",
                price = 35000.0,
                imageUrl = "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=500",
                category = "Mon them",
                preparationTime = 10
            ),
            MenuItem(
                id = "item_def_3",
                restaurantId = restaurantId,
                name = "Nuoc Uong Giai Khat",
                description = "Tra trai cay tuoi mat giup tieu hoa tot",
                price = 25000.0,
                imageUrl = "https://images.unsplash.com/photo-1558857563-b371033873b8?w=500",
                category = "Do uong",
                preparationTime = 5
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
            sampleRestaurants.firstOrNull()?.copy(id = id, name = "Nha Hang Doi Tac BiteFast")
                ?: Restaurant(id = id, name = "Nha Hang Doi Tac BiteFast")
        }
    }

    override suspend fun getRestaurantMenu(restaurantId: String): List<MenuItem> {
        val remoteMenu = try {
            apiService.getRestaurantMenu(restaurantId)
        } catch (e: Exception) {
            emptyList()
        }
        return if (remoteMenu.isNotEmpty()) {
            remoteMenu
        } else {
            sampleMenuMap[restaurantId] ?: defaultSampleMenu(restaurantId)
        }
    }
}
