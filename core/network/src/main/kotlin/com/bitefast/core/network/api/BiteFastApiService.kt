package com.bitefast.core.network.api

import com.bitefast.core.network.model.AddressDto
import com.bitefast.core.network.model.ApiResponse
import com.bitefast.core.network.model.ApplyVoucherRequestDto
import com.bitefast.core.network.model.AuthTokenResponseDto
import com.bitefast.core.network.model.CancelOrderRequestDto
import com.bitefast.core.network.model.CreateAddressRequestDto
import com.bitefast.core.network.model.CreateDishReviewRequestDto
import com.bitefast.core.network.model.CreateOrderRequestDto
import com.bitefast.core.network.model.DishReviewDto
import com.bitefast.core.network.model.ForgotPasswordRequestDto
import com.bitefast.core.network.model.LoginRequestDto
import com.bitefast.core.network.model.MenuItemDto
import com.bitefast.core.network.model.OrderResponseDto
import com.bitefast.core.network.model.RefreshTokenRequestDto
import com.bitefast.core.network.model.RegisterRequestDto
import com.bitefast.core.network.model.RestaurantDetailDto
import com.bitefast.core.network.model.RestaurantDto
import com.bitefast.core.network.model.RestaurantRatingRequestDto
import com.bitefast.core.network.model.UpdateProfileRequestDto
import com.bitefast.core.network.model.UserDto
import com.bitefast.core.network.model.VoucherDto
import com.bitefast.core.network.model.VoucherValidationResponseDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Enterprise Retrofit API Service for BiteFast food delivery platform.
 * All responses are wrapped in standard [ApiResponse] envelopes.
 */
interface BiteFastApiService {

    // ==================== AUTH & USER ====================

    @POST("api/v1/auth/login")
    suspend fun login(
        @Body body: LoginRequestDto
    ): ApiResponse<AuthTokenResponseDto>

    @POST("api/v1/auth/register")
    suspend fun register(
        @Body body: RegisterRequestDto
    ): ApiResponse<AuthTokenResponseDto>

    @POST("api/v1/auth/refresh")
    suspend fun refreshToken(
        @Body body: RefreshTokenRequestDto
    ): ApiResponse<AuthTokenResponseDto>

    @POST("api/v1/auth/forgot-password")
    suspend fun forgotPassword(
        @Body body: ForgotPasswordRequestDto
    ): ApiResponse<Unit>

    @GET("api/v1/user/profile")
    suspend fun getProfile(): ApiResponse<UserDto>

    @PUT("api/v1/user/profile")
    suspend fun updateProfile(
        @Body body: UpdateProfileRequestDto
    ): ApiResponse<UserDto>

    // ==================== RESTAURANTS & MENU ====================

    @GET("api/v1/restaurants")
    suspend fun getRestaurants(
        @Query("query") query: String? = null,
        @Query("cuisine") cuisine: String? = null
    ): ApiResponse<List<RestaurantDto>>

    @GET("api/v1/restaurants/{id}")
    suspend fun getRestaurantDetail(
        @Path("id") id: String
    ): ApiResponse<RestaurantDetailDto>

    @GET("api/v1/restaurants/{id}/menu")
    suspend fun getRestaurantMenu(
        @Path("id") restaurantId: String
    ): ApiResponse<List<MenuItemDto>>

    // ==================== ORDERS ====================

    @POST("api/v1/orders")
    suspend fun createOrder(
        @Body body: CreateOrderRequestDto
    ): ApiResponse<OrderResponseDto>

    @GET("api/v1/orders/history")
    suspend fun getOrderHistory(): ApiResponse<List<OrderResponseDto>>

    @GET("api/v1/orders/{id}")
    suspend fun getOrderDetail(
        @Path("id") id: String
    ): ApiResponse<OrderResponseDto>

    @POST("api/v1/orders/{id}/cancel")
    suspend fun cancelOrder(
        @Path("id") id: String,
        @Body body: CancelOrderRequestDto
    ): ApiResponse<OrderResponseDto>

    // ==================== VOUCHERS ====================

    @GET("api/v1/vouchers/wallet")
    suspend fun getVouchers(): ApiResponse<List<VoucherDto>>

    @POST("api/v1/vouchers/validate")
    suspend fun validateVoucher(
        @Body body: ApplyVoucherRequestDto
    ): ApiResponse<VoucherValidationResponseDto>

    // ==================== RATINGS & REVIEWS ====================

    @GET("api/v1/items/{id}/reviews")
    suspend fun getDishReviews(
        @Path("id") dishId: String
    ): ApiResponse<List<DishReviewDto>>

    @POST("api/v1/items/{id}/reviews")
    suspend fun submitDishReview(
        @Path("id") dishId: String,
        @Body body: CreateDishReviewRequestDto
    ): ApiResponse<DishReviewDto>

    @POST("api/v1/orders/{id}/rating")
    suspend fun submitRestaurantRating(
        @Path("id") orderId: String,
        @Body body: RestaurantRatingRequestDto
    ): ApiResponse<Unit>

    // ==================== ADDRESSES ====================

    @GET("api/v1/user/addresses")
    suspend fun getAddresses(): ApiResponse<List<AddressDto>>

    @POST("api/v1/user/addresses")
    suspend fun addAddress(
        @Body body: CreateAddressRequestDto
    ): ApiResponse<AddressDto>

    @DELETE("api/v1/user/addresses/{id}")
    suspend fun deleteAddress(
        @Path("id") id: String
    ): ApiResponse<Unit>
}
