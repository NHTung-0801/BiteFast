package com.bitefast.app.navigation

import kotlinx.serialization.Serializable

/**
 * Định nghĩa Type-Safe Navigation Destinations cho toàn bộ ứng dụng BiteFast
 * sử dụng Kotlinx Serialization (Navigation Compose 2.8+).
 */

// ─── Cụm Xác thực (Auth Graph) ───────────────────────────────────────────────
@Serializable
object AuthGraph

@Serializable
object LoginDestination

@Serializable
object RegisterDestination

@Serializable
data class ForgotPasswordDestination(
    val initialEmailOrPhone: String? = null
)

// ─── Cụm Tab Chính (Main Bottom Navigation) ──────────────────────────────────
@Serializable
object MainGraph

@Serializable
object DiscoveryDestination

@Serializable
object CartDestination

@Serializable
object OrdersDestination

@Serializable
object ProfileDestination

// ─── Cụm Tính Năng Mở Rộng & Chi Tiết (Features & Slices) ────────────────────

/** Màn hình tìm kiếm chuyên sâu kèm lịch sử & xu hướng */
@Serializable
object SearchDestination

/** Chi tiết nhà hàng và danh mục thực đơn */
@Serializable
data class RestaurantDetailDestination(
    val restaurantId: String
)

/** Kho mã giảm giá & voucher khuyến mãi */
@Serializable
object VoucherWalletDestination

/** Màn hình xác nhận và thanh toán đơn hàng */
@Serializable
object CheckoutDestination

/** Màn hình kết quả thanh toán & hiển thị mã VietQR động */
@Serializable
data class PaymentResultDestination(
    val orderId: String,
    val amount: Long = 0L,
    val qrPayload: String = ""
)

/** Màn hình theo dõi hành trình shipper GPS thời gian thực */
@Serializable
data class TrackingDestination(
    val orderId: String
)

/** Màn hình hóa đơn chi tiết & khiếu nại đơn hàng */
@Serializable
data class OrderDetailDestination(
    val orderId: String
)

/** Màn hình đánh giá dịch vụ quán ăn & tài xế */
@Serializable
data class RatingDestination(
    val orderId: String
)

/** Trung tâm thông báo hệ thống & đơn hàng */
@Serializable
object NotificationDestination

/** Sổ quản lý địa chỉ nhận hàng */
@Serializable
object AddressListDestination

/** Màn hình ghim vị trí nhận hàng trên bản đồ */
@Serializable
data class AddressPickerDestination(
    val initialLat: Double = 10.7769,
    val initialLng: Double = 106.7009
)

/** Danh sách quán ăn & món yêu thích */
@Serializable
object WishlistDestination

/** Chỉnh sửa thông tin hồ sơ cá nhân */
@Serializable
object EditProfileDestination

/** Trung tâm trợ giúp & CSKH */
@Serializable
object HelpCenterDestination
