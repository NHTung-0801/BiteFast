package com.bitefast.core.network.mock

import com.bitefast.core.network.config.NetworkConfig
import com.bitefast.core.network.config.NetworkMode
import com.bitefast.core.network.model.AddressDto
import com.bitefast.core.network.model.ApiResponse
import com.bitefast.core.network.model.ApplyVoucherRequestDto
import com.bitefast.core.network.model.AuthTokenResponseDto
import com.bitefast.core.network.model.CreateAddressRequestDto
import com.bitefast.core.network.model.CreateDishReviewRequestDto
import com.bitefast.core.network.model.CreateOrderRequestDto
import com.bitefast.core.network.model.DishReviewDto
import com.bitefast.core.network.model.LoginRequestDto
import com.bitefast.core.network.model.MenuItemDto
import com.bitefast.core.network.model.OrderItemDto
import com.bitefast.core.network.model.OrderResponseDto
import com.bitefast.core.network.model.RegisterRequestDto
import com.bitefast.core.network.model.RestaurantDetailDto
import com.bitefast.core.network.model.RestaurantDto
import com.bitefast.core.network.model.UserDto
import com.bitefast.core.network.model.VoucherDto
import com.bitefast.core.network.model.VoucherValidationResponseDto
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * High-fidelity Mock Network Interceptor.
 * Simulates RESTful backend endpoints with realistic network latency (250-400ms)
 * and rich Vietnamese food delivery data.
 */
@Singleton
class MockNetworkInterceptor @Inject constructor(
    private val json: Json
) : Interceptor {

    // In-memory state for mock orders created during session
    private val createdOrders = mutableListOf<OrderResponseDto>()
    private val dishReviewsMap = mutableMapOf<String, MutableList<DishReviewDto>>()
    private val userAddresses = mutableListOf(
        AddressDto(
            id = "addr_default_1",
            userId = "usr_123",
            label = "Nhà riêng",
            recipientName = "Nguyễn Văn A",
            phoneNumber = "0909123456",
            streetAddress = "456 Lê Văn Việt, Phường Tăng Nhơn Phú A",
            city = "TP. Thủ Đức",
            isDefault = true
        ),
        AddressDto(
            id = "addr_default_2",
            userId = "usr_123",
            label = "Công ty",
            recipientName = "Nguyễn Văn A",
            phoneNumber = "0909123456",
            streetAddress = "Tòa nhà Bitexco, 2 Hải Triều, Bến Nghé",
            city = "Quận 1",
            isDefault = false
        )
    )

    private val sampleRestaurants = listOf(
        RestaurantDto(
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
            phoneNumber = "02873002060",
            isOpen = true,
            isFreeDelivery = true,
            tags = listOf("Cơm tấm", "Sườn nướng", "Freeship")
        ),
        RestaurantDto(
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
            phoneNumber = "0912345678",
            isOpen = true,
            isFreeDelivery = false,
            tags = listOf("Phở bò", "Tái lăn", "Hà Nội")
        ),
        RestaurantDto(
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
            phoneNumber = "02836220500",
            isOpen = true,
            isFreeDelivery = true,
            tags = listOf("Pizza", "Phô mai", "Ý")
        ),
        RestaurantDto(
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
            phoneNumber = "02871001968",
            isOpen = true,
            isFreeDelivery = false,
            tags = listOf("Trà sữa", "Đậm vị", "Trà đào")
        )
    )

    private val sampleMenuMap = mapOf(
        "res_1" to listOf(
            MenuItemDto("menu_1_1", "res_1", "Cơm Sườn Nướng Than Hoa", "Sườn nướng ướp mật ong đậm đà", 48000.0, imageUrl = "https://images.unsplash.com/photo-1544025162-d76694265947?w=500", category = "Món chính", isPopular = true, rating = 4.9, reviewCount = 68),
            MenuItemDto("menu_1_2", "res_1", "Cơm Sườn Bì Chả Đặc Biệt", "Sườn cây, bì thính, chả trứng hấp", 65000.0, imageUrl = "https://images.unsplash.com/photo-1544025162-d76694265947?w=500", category = "Món chính", isPopular = true, rating = 4.8, reviewCount = 52),
            MenuItemDto("menu_1_3", "res_1", "Canh Khổ Qua Nhồi Thịt", "Khổ qua thanh mát giải nhiệt", 22000.0, imageUrl = "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=500", category = "Canh", rating = 4.7, reviewCount = 18)
        ),
        "res_2" to listOf(
            MenuItemDto("menu_2_1", "res_2", "Phở Bò Tái Lăn Hà Nội", "Thịt bò xào lăn thơm phức hành tươi", 75000.0, imageUrl = "https://images.unsplash.com/photo-1582878826629-29b7ad1cdc43?w=500", category = "Món chính", isPopular = true, rating = 4.9, reviewCount = 85),
            MenuItemDto("menu_2_2", "res_2", "Phở Bò Tái Nạm Gầu", "Nạm giòn gầu béo nước dùng thanh ngọt", 80000.0, imageUrl = "https://images.unsplash.com/photo-1582878826629-29b7ad1cdc43?w=500", category = "Món chính", isPopular = true, rating = 4.8, reviewCount = 42),
            MenuItemDto("menu_2_3", "res_2", "Quẩy Giòn Ăn Kèm", "Quẩy vàng ruộm giòn tan", 10000.0, imageUrl = "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=500", category = "Món thêm", rating = 4.6, reviewCount = 30)
        ),
        "res_3" to listOf(
            MenuItemDto("menu_3_1", "res_3", "Pizza Phô Mai Burrata Thịt Nguội", "Phô mai tươi Burrata béo ngậy kèm Parma ham", 290000.0, imageUrl = "https://images.unsplash.com/photo-1513104890138-7c749659a591?w=500", category = "Pizza", isPopular = true, rating = 5.0, reviewCount = 95),
            MenuItemDto("menu_3_2", "res_3", "Pizza 4 Loại Phô Mai (4 Cheese)", "Mozzarella, Gorgonzola, Camembert và Parmesan", 240000.0, imageUrl = "https://images.unsplash.com/photo-1513104890138-7c749659a591?w=500", category = "Pizza", isPopular = true, rating = 4.8, reviewCount = 60)
        ),
        "res_4" to listOf(
            MenuItemDto("menu_4_1", "res_4", "Trà Đào Cam Sả Đặc Biệt", "Trà ô long kết hợp đào miếng giòn và cam vàng", 55000.0, imageUrl = "https://images.unsplash.com/photo-1558857563-b371033873b8?w=500", category = "Trà trái cây", isPopular = true, rating = 4.7, reviewCount = 110),
            MenuItemDto("menu_4_2", "res_4", "Trà Sữa Phúc Long Truyền Thống", "Trà đen đậm đà hòa quyện sữa béo", 50000.0, imageUrl = "https://images.unsplash.com/photo-1558857563-b371033873b8?w=500", category = "Trà sữa", isPopular = true, rating = 4.8, reviewCount = 145)
        )
    )

    private val sampleVouchers = listOf(
        VoucherDto(
            id = "v_free_ship",
            code = "FREESHIP15K",
            name = "Miễn phí vận chuyển 15K",
            description = "Giảm tối đa 15.000đ phí giao hàng cho đơn từ 80.000đ",
            type = "free_shipping",
            value = 15000.0,
            minOrderValue = 80000.0,
            startDate = 0L,
            endDate = System.currentTimeMillis() + 30 * 86400000L,
            isActive = true
        ),
        VoucherDto(
            id = "v_giam_20k",
            code = "BITEFAST20K",
            name = "Giảm 20K đơn đầu",
            description = "Giảm 20.000đ áp dụng cho mọi đơn từ 100.000đ",
            type = "fixed",
            value = 20000.0,
            minOrderValue = 100000.0,
            startDate = 0L,
            endDate = System.currentTimeMillis() + 30 * 86400000L,
            isActive = true
        )
    )

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        // If in LIVE mode, let the request proceed to real network
        if (NetworkConfig.mode == NetworkMode.LIVE) {
            return chain.proceed(request)
        }

        // Otherwise, intercept and return mock response with realistic simulated network latency
        Thread.sleep((250L..350L).random())

        val path = request.url.encodedPath
        val method = request.method

        val (statusCode, jsonBody) = handleMockRequest(method, path, request)

        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(statusCode)
            .message(if (statusCode == 200) "OK" else "Error")
            .body(jsonBody.toResponseBody("application/json".toMediaTypeOrNull()))
            .build()
    }

    private fun handleMockRequest(method: String, path: String, request: okhttp3.Request): Pair<Int, String> {
        return when {
            // AUTH
            method == "POST" && path.endsWith("/auth/login") -> {
                val token = AuthTokenResponseDto(
                    accessToken = "jwt_access_token_bitefast_${System.currentTimeMillis()}",
                    refreshToken = "jwt_refresh_token_bitefast_${System.currentTimeMillis()}",
                    userId = "usr_123"
                )
                200 to json.encodeToString(ApiResponse(success = true, code = 200, message = "Đăng nhập thành công", data = token))
            }
            method == "POST" && path.endsWith("/auth/register") -> {
                val token = AuthTokenResponseDto(
                    accessToken = "jwt_access_token_bitefast_${System.currentTimeMillis()}",
                    refreshToken = "jwt_refresh_token_bitefast_${System.currentTimeMillis()}",
                    userId = "usr_123"
                )
                200 to json.encodeToString(ApiResponse(success = true, code = 200, message = "Đăng ký thành công", data = token))
            }
            method == "POST" && path.endsWith("/auth/refresh") -> {
                val token = AuthTokenResponseDto(
                    accessToken = "refreshed_jwt_token_${System.currentTimeMillis()}",
                    refreshToken = "refreshed_refresh_token_${System.currentTimeMillis()}",
                    userId = "usr_123"
                )
                200 to json.encodeToString(ApiResponse(success = true, code = 200, message = "Làm mới phiên thành công", data = token))
            }
            method == "GET" && path.endsWith("/user/profile") -> {
                val user = UserDto(
                    id = "usr_123",
                    name = "Nguyễn Văn A",
                    email = "nguyenvana@gmail.com",
                    phone = "0909123456"
                )
                200 to json.encodeToString(ApiResponse(data = user))
            }
            method == "PUT" && path.endsWith("/user/profile") -> {
                // Trả về UserDto với dữ liệu mock phản chiếu request
                val updatedUser = UserDto(
                    id = "usr_123",
                    name = "Nguyễn Văn A",
                    email = "nguyenvana@gmail.com",
                    phone = "0909123456"
                )
                200 to json.encodeToString(ApiResponse(success = true, code = 200, message = "Cập nhật hồ sơ thành công", data = updatedUser))
            }

            // RESTAURANTS
            method == "GET" && path.endsWith("/restaurants") -> {
                200 to json.encodeToString(ApiResponse(data = sampleRestaurants))
            }
            method == "GET" && path.contains("/restaurants/") && path.endsWith("/menu") -> {
                val restaurantId = path.substringAfter("/restaurants/").substringBefore("/menu")
                val menu = sampleMenuMap[restaurantId] ?: sampleMenuMap["res_1"] ?: emptyList()
                200 to json.encodeToString(ApiResponse(data = menu))
            }
            method == "GET" && path.contains("/restaurants/") -> {
                val restaurantId = path.substringAfterLast("/")
                val restaurant = sampleRestaurants.find { it.id == restaurantId } ?: sampleRestaurants.first()
                val menu = sampleMenuMap[restaurantId] ?: sampleMenuMap["res_1"] ?: emptyList()
                val detail = RestaurantDetailDto(
                    restaurant = restaurant,
                    menu = menu,
                    categories = menu.map { it.category }.distinct()
                )
                200 to json.encodeToString(ApiResponse(data = detail))
            }

            // ORDERS
            method == "POST" && path.endsWith("/orders") -> {
                val newOrder = OrderResponseDto(
                    id = "ord_${System.currentTimeMillis()}",
                    userId = "usr_123",
                    restaurantId = "res_1",
                    restaurantName = "Cơm Tấm Phúc Lộc Thọ - Lê Văn Việt",
                    driverId = "drv_99",
                    driverName = "Trần Văn Tài",
                    driverPhone = "0988776655",
                    items = listOf(
                        OrderItemDto("menu_1_1", "Cơm Sườn Nướng Than Hoa", 48000.0, 1)
                    ),
                    subtotal = 48000.0,
                    deliveryFee = 15000.0,
                    total = 63000.0,
                    status = "PENDING",
                    paymentMethod = "CASH",
                    paymentStatus = "PENDING",
                    deliveryAddressText = "456 Lê Văn Việt, TP. Thủ Đức",
                    orderTime = System.currentTimeMillis()
                )
                createdOrders.add(0, newOrder)
                200 to json.encodeToString(ApiResponse(data = newOrder))
            }
            method == "GET" && path.endsWith("/orders/history") -> {
                200 to json.encodeToString(ApiResponse(data = createdOrders))
            }
            method == "GET" && path.contains("/orders/") -> {
                val orderId = path.substringAfterLast("/")
                val order = createdOrders.find { it.id == orderId } ?: createdOrders.firstOrNull() ?: OrderResponseDto(
                    id = orderId,
                    userId = "usr_123",
                    restaurantId = "res_1",
                    restaurantName = "Cơm Tấm Phúc Lộc Thọ",
                    subtotal = 50000.0,
                    deliveryFee = 15000.0,
                    total = 65000.0,
                    status = "ON_THE_WAY",
                    paymentMethod = "CASH",
                    paymentStatus = "PENDING",
                    orderTime = System.currentTimeMillis()
                )
                200 to json.encodeToString(ApiResponse(data = order))
            }

            // VOUCHERS
            method == "GET" && path.endsWith("/vouchers/wallet") -> {
                200 to json.encodeToString(ApiResponse(data = sampleVouchers))
            }
            method == "POST" && path.endsWith("/vouchers/validate") -> {
                val validation = VoucherValidationResponseDto(
                    isValid = true,
                    discountAmount = 15000.0,
                    voucher = sampleVouchers.first(),
                    message = "Áp dụng mã giảm 15.000đ thành công"
                )
                200 to json.encodeToString(ApiResponse(data = validation))
            }

            // REVIEWS
            method == "GET" && path.contains("/items/") && path.endsWith("/reviews") -> {
                val dishId = path.substringAfter("/items/").substringBefore("/reviews")
                val reviews = dishReviewsMap[dishId] ?: mutableListOf(
                    DishReviewDto("rev_1", dishId, "Trần Tuấn", 5, "Món ăn nóng hổi, rất vừa miệng!", listOf("Ngon xuất sắc", "Đậm đà")),
                    DishReviewDto("rev_2", dishId, "Lê Mai", 4, "Giao hàng nhanh, đóng gói cẩn thận", listOf("Đóng gói sạch"))
                )
                200 to json.encodeToString(ApiResponse(data = reviews))
            }

            // ADDRESSES
            method == "GET" && path.endsWith("/user/addresses") -> {
                200 to json.encodeToString(ApiResponse(data = userAddresses))
            }
            method == "POST" && path.endsWith("/user/addresses") -> {
                val newAddr = AddressDto(
                    id = "addr_${System.currentTimeMillis()}",
                    userId = "usr_123",
                    label = "Địa chỉ mới",
                    recipientName = "Nguyễn Văn A",
                    phoneNumber = "0909123456",
                    streetAddress = "123 Đường Số 1, TP. Thủ Đức",
                    city = "TP. Thủ Đức"
                )
                userAddresses.add(newAddr)
                200 to json.encodeToString(ApiResponse(data = newAddr))
            }

            // FALLBACK
            else -> {
                200 to json.encodeToString(ApiResponse(success = true, code = 200, message = "Success", data = "OK"))
            }
        }
    }
}
