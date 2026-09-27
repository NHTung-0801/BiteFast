package com.bitefast.core.domain.order

import com.bitefast.core.domain.repository.CartRepository
import com.bitefast.core.domain.repository.OrderRepository
import com.bitefast.core.domain.voucher.ApplyVoucherUseCase
import com.bitefast.core.domain.voucher.ValidateVoucherUseCase
import com.bitefast.core.domain.voucher.VoucherValidationResult
import com.bitefast.core.domain.voucher.VoucherInvalidReason
import com.bitefast.core.model.*
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("CheckoutOrderUseCase")
class CheckoutOrderUseCaseTest {

    @MockK lateinit var cartRepository: CartRepository
    @MockK lateinit var orderRepository: OrderRepository
    @MockK lateinit var applyVoucherUseCase: ApplyVoucherUseCase

    private lateinit var useCase: CheckoutOrderUseCase

    private val validAddress = Address(
        recipientName = "Nguyen Van A",
        phoneNumber = "0901234567",
        streetAddress = "123 Nguyen Hue",
        city = "Ho Chi Minh",
    )

    private val sampleCartItem = CartItem(
        id = "item-1",
        menuItemId = "menu-1",
        restaurantId = "rest-1",
        name = "Pho Bo",
        price = 50_000.0,
        quantity = 2,
    )

    private val baseRequest = CheckoutOrderUseCase.CheckoutRequest(
        userId = "user-1",
        restaurantId = "rest-1",
        restaurantName = "Pho Ha Noi",
        deliveryAddress = validAddress,
        paymentMethod = PaymentMethod.CASH,
        deliveryFee = 15_000.0,
        taxRate = 0.08,
    )

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        useCase = CheckoutOrderUseCase(cartRepository, orderRepository, applyVoucherUseCase)
    }

    @Nested
    @DisplayName("Khi giỏ hàng trống")
    inner class EmptyCart {
        @Test
        fun `trả về EmptyCart`() = runTest {
            every { cartRepository.getCartItems() } returns flowOf(emptyList())

            val result = useCase(baseRequest)

            assertTrue(result is CheckoutResult.EmptyCart)
        }
    }

    @Nested
    @DisplayName("Khi địa chỉ không hợp lệ")
    inner class InvalidAddress {
        @Test
        fun `thiếu streetAddress trả về InvalidAddress`() = runTest {
            every { cartRepository.getCartItems() } returns flowOf(listOf(sampleCartItem))

            val result = useCase(baseRequest.copy(deliveryAddress = validAddress.copy(streetAddress = "")))

            assertTrue(result is CheckoutResult.InvalidAddress)
        }

        @Test
        fun `số điện thoại sai format trả về InvalidAddress`() = runTest {
            every { cartRepository.getCartItems() } returns flowOf(listOf(sampleCartItem))

            val result = useCase(baseRequest.copy(deliveryAddress = validAddress.copy(phoneNumber = "12345")))

            assertTrue(result is CheckoutResult.InvalidAddress)
        }

        @Test
        fun `số điện thoại hợp lệ 10 chữ số bắt đầu 0 không bị lỗi`() = runTest {
            every { cartRepository.getCartItems() } returns flowOf(listOf(sampleCartItem))
            val createdOrder = Order(id = "order-1", total = 123_000.0)
            coEvery { orderRepository.createOrder(any()) } returns createdOrder
            coEvery { cartRepository.clearCart() } just Runs

            val result = useCase(baseRequest.copy(deliveryAddress = validAddress.copy(phoneNumber = "0901234567")))

            assertTrue(result is CheckoutResult.Success)
        }
    }

    @Nested
    @DisplayName("Khi áp voucher")
    inner class VoucherHandling {
        @Test
        fun `voucher hợp lệ được trừ vào tổng tiền`() = runTest {
            every { cartRepository.getCartItems() } returns flowOf(listOf(sampleCartItem))
            val discount = 10_000.0
            coEvery {
                applyVoucherUseCase(any(), any(), any(), any(), any())
            } returns VoucherValidationResult.Valid(
                voucher = Voucher(code = "SAVE10", type = "fixed", value = 10_000.0),
                calculatedDiscount = discount,
            )
            val capturedOrder = slot<Order>()
            coEvery { orderRepository.createOrder(capture(capturedOrder)) } answers { capturedOrder.captured }
            coEvery { cartRepository.clearCart() } just Runs

            val result = useCase(baseRequest.copy(voucherCode = "SAVE10"))

            assertTrue(result is CheckoutResult.Success)
            assertEquals(discount, (result as CheckoutResult.Success).order.discount)
        }

        @Test
        fun `voucher không hợp lệ trả về VoucherError`() = runTest {
            every { cartRepository.getCartItems() } returns flowOf(listOf(sampleCartItem))
            coEvery {
                applyVoucherUseCase(any(), any(), any(), any(), any())
            } returns VoucherValidationResult.Invalid(
                reason = VoucherInvalidReason.EXPIRED,
                message = "Mã giảm giá đã hết hạn sử dụng.",
            )

            val result = useCase(baseRequest.copy(voucherCode = "EXPIRED123"))

            assertTrue(result is CheckoutResult.VoucherError)
            assertEquals("Mã giảm giá đã hết hạn sử dụng.", (result as CheckoutResult.VoucherError).message)
        }
    }

    @Nested
    @DisplayName("Tính toán tổng tiền")
    inner class TotalCalculation {
        @Test
        fun `total = subtotal + delivery + tax - discount`() = runTest {
            // 2 items × 50_000 = 100_000 subtotal
            every { cartRepository.getCartItems() } returns flowOf(listOf(sampleCartItem))
            val capturedOrder = slot<Order>()
            coEvery { orderRepository.createOrder(capture(capturedOrder)) } answers { capturedOrder.captured }
            coEvery { cartRepository.clearCart() } just Runs

            useCase(baseRequest)

            val order = capturedOrder.captured
            val expectedSubtotal = 100_000.0      // 50_000 × 2
            val expectedTax = 100_000.0 * 0.08    // 8_000
            val expectedTotal = 100_000.0 + 15_000.0 + 8_000.0  // 123_000

            assertEquals(expectedSubtotal, order.subtotal, 0.01)
            assertEquals(expectedTax, order.taxes, 0.01)
            assertEquals(expectedTotal, order.total, 0.01)
        }
    }

    @Nested
    @DisplayName("Khi tạo đơn thất bại")
    inner class OrderCreationFailure {
        @Test
        fun `exception từ repository trả về Failure`() = runTest {
            every { cartRepository.getCartItems() } returns flowOf(listOf(sampleCartItem))
            val ex = RuntimeException("Server error")
            coEvery { orderRepository.createOrder(any()) } throws ex

            val result = useCase(baseRequest)

            assertTrue(result is CheckoutResult.Failure)
            assertEquals(ex, (result as CheckoutResult.Failure).exception)
        }
    }

    @Test
    @DisplayName("Sau khi đặt hàng thành công, giỏ hàng được xóa")
    fun `cart cleared after successful checkout`() = runTest {
        every { cartRepository.getCartItems() } returns flowOf(listOf(sampleCartItem))
        val createdOrder = Order(id = "order-new")
        coEvery { orderRepository.createOrder(any()) } returns createdOrder
        coEvery { cartRepository.clearCart() } just Runs

        useCase(baseRequest)

        coVerify(exactly = 1) { cartRepository.clearCart() }
    }
}
