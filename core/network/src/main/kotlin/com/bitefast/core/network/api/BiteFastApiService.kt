package com.bitefast.core.network.api

import com.bitefast.core.model.MenuItem
import com.bitefast.core.model.Order
import com.bitefast.core.model.Restaurant
import com.bitefast.core.model.User
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface BiteFastApiService {

    @GET("api/v1/restaurants")
    suspend fun getRestaurants(
        @Query("query") query: String? = null,
        @Query("cuisine") cuisine: String? = null
    ): List<Restaurant>

    @GET("api/v1/restaurants/{id}")
    suspend fun getRestaurantDetail(
        @Path("id") id: String
    ): Restaurant

    @GET("api/v1/restaurants/{id}/menu")
    suspend fun getRestaurantMenu(
        @Path("id") restaurantId: String
    ): List<MenuItem>

    @POST("api/v1/orders")
    suspend fun createOrder(
        @Body order: Order
    ): Order

    @GET("api/v1/orders/history")
    suspend fun getOrderHistory(): List<Order>

    @POST("api/v1/auth/login")
    suspend fun login(
        @Body credentials: Map<String, String>
    ): Map<String, String>

    @GET("api/v1/user/profile")
    suspend fun getProfile(): User
}
