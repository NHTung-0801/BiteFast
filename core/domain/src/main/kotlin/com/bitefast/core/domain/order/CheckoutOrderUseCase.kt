package com.bitefast.core.domain.order

import com.bitefast.core.domain.repository.CartRepository
import com.bitefast.core.domain.repository.OrderRepository
import com.bitefast.core.domain.voucher.ApplyVoucherUseCase
import com.bitefast.core.domain.voucher.VoucherValidationResult
import com.bitefast.core.model.Address
import com.bitefast.core.model.CartItem
import com.bitefast.core.model.Order
import com.bitefast.core.model.PaymentMethod
import com.bitefast.core.model.PaymentStatus
import kotlinx.coroutines.flow.first
import javax.inject.Inject

// ─── Domain Result ────────────────────────────────────────────────────────────

sealed interface CheckoutResult {
    /** Đặt hàng thành công, trả về đơn hàng đã được server xác nhận. */
    data class Success(val order: Order) : CheckoutResult

    /** Giỏ hàng trống — không thể thanh toán. */
    data object EmptyCart : CheckoutResult

    /** Địa chỉ giao hàng chưa được chọn / thiếu thông tin bắt buộc. */
    data class InvalidAddress(val reason: String) : CheckoutResult

    /** Mã voucher không hợp lệ. */
    data class VoucherError(val message: String) : CheckoutResult

    /** Lỗi hệ thống khi tạo đơn. */
    data class Failure(val exception: Throwable) : CheckoutResult
}

// ─── Input DTO (tất cả tham số checkout) ──────────────────────────────────────

data class CheckoutRequest(
    val userId: String,
    val restaurantId: String,
    val restaurantName: String,
    val deliveryAddress: Address,
    val paymentMethod: PaymentMethod = PaymentMethod.CASH,
    val voucherCode: String? = null,
    /** Phí giao hàng cơ bản từ server/logic nghiệp vụ giao hàng. */
    val deliveryFee: Double = 15_000.0,
    /** Thuế VAT (mặc định 8% — áp dụng trên subtotal). */
    val taxRate: Double = 0.08,
)

// ─── Use Case ─────────────────────────────────────────────────────────────────

/**
 * Điều phối toàn bộ luồng thanh toán:
 *  1. Kiểm tra giỏ hàng không trống.
 *  2. Xác thực địa chỉ giao hàng (có đủ thông tin bắt buộc).
 *  3. Áp mã voucher nếu người dùng nhập.
 *  4. Tính toán tổng tiền (subtotal + delivery + tax - discount).
 *  5. Gửi đơn hàng lên server qua [OrderRepository].
 *  6. Xóa sạch giỏ hàng sau khi tạo đơn thành công.
 */
class CheckoutOrderUseCase @Inject constructor(
    private val cartRepository: CartRepository,
    private val orderRepository: OrderRepository,
    private val applyVoucherUseCase: ApplyVoucherUseCase,
) {
    suspend operator fun invoke(request: CheckoutOrderUseCase.CheckoutRequest): CheckoutResult {
        // 1. Kiểm tra giỏ hàng
        val cartItems: List<CartItem> = cartRepository.getCartItems().first()
        if (cartItems.isEmpty()) return CheckoutResult.EmptyCart

        // 2. Xác thực địa chỉ giao hàng
        val addressError = validateAddress(request.deliveryAddress)
        if (addressError != null) return CheckoutResult.InvalidAddress(addressError)

        // 3. Tính subtotal
        val subtotal = cartItems.sumOf { it.totalPrice }

        // 4. Áp voucher (nếu có)
        var discount = 0.0
        if (!request.voucherCode.isNullOrBlank()) {
            val voucherResult = applyVoucherUseCase(
                code = request.voucherCode,
                subtotal = subtotal,
                restaurantId = request.restaurantId,
                deliveryFee = request.deliveryFee,
            )
            when (voucherResult) {
                is VoucherValidationResult.Valid -> discount = voucherResult.calculatedDiscount
                is VoucherValidationResult.Invalid -> return CheckoutResult.VoucherError(voucherResult.message)
            }
        }

        // 5. Tính toán tổng
        val taxes = subtotal * request.taxRate
        val total = (subtotal + request.deliveryFee + taxes - discount).coerceAtLeast(0.0)

        // 6. Tạo domain Order object
        val order = Order(
            userId = request.userId,
            restaurantId = request.restaurantId,
            restaurantName = request.restaurantName,
            items = cartItems,
            subtotal = subtotal,
            deliveryFee = request.deliveryFee,
            taxes = taxes,
            discount = discount,
            total = total,
            paymentMethod = request.paymentMethod,
            paymentStatus = PaymentStatus.PENDING,
            address = request.deliveryAddress,
        )

        // 7. Gửi đơn lên repository & xóa giỏ hàng nếu thành công
        return try {
            val created = orderRepository.createOrder(order)
            cartRepository.clearCart()
            CheckoutResult.Success(created)
        } catch (e: Exception) {
            CheckoutResult.Failure(e)
        }
    }

    private fun validateAddress(address: Address): String? = when {
        address.streetAddress.isBlank() -> "Vui lòng nhập địa chỉ đường phố."
        address.city.isBlank() -> "Vui lòng nhập thành phố."
        address.recipientName.isBlank() -> "Vui lòng nhập tên người nhận."
        address.phoneNumber.isBlank() -> "Vui lòng nhập số điện thoại người nhận."
        !address.phoneNumber.matches(Regex("^(\\+84|0)[3-9]\\d{8}$")) ->
            "Số điện thoại không hợp lệ (định dạng Việt Nam)."
        else -> null
    }

    // Alias inner type cho dễ dùng ngoài
    data class CheckoutRequest(
        val userId: String,
        val restaurantId: String,
        val restaurantName: String,
        val deliveryAddress: Address,
        val paymentMethod: PaymentMethod = PaymentMethod.CASH,
        val voucherCode: String? = null,
        val deliveryFee: Double = 15_000.0,
        val taxRate: Double = 0.08,
    )
}
