package com.bitefast.core.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ModelTest {

    @Test
    fun `CartItem calculates totalPrice accurately`() {
        val item = CartItem(
            price = 45000.0,
            quantity = 3
        )
        assertEquals(135000.0, item.totalPrice)
    }

    @Test
    fun `User displayName resolves name or email prefix or guest`() {
        val userWithName = User(name = "Nguyen Van A", email = "a@gmail.com")
        assertEquals("Nguyen Van A", userWithName.displayName)

        val userWithoutName = User(name = "", email = "tung.dev@bitefast.com")
        assertEquals("tung.dev", userWithoutName.displayName)

        val guestUser = User(name = "", email = "")
        assertEquals("Guest", guestUser.displayName)
    }

    @Test
    fun `Voucher isValid verifies time and usage limit`() {
        val now = System.currentTimeMillis()
        val validVoucher = Voucher(
            isActive = true,
            usedCount = 2,
            usageLimit = 5,
            startDate = now - 1000,
            endDate = now + 10000
        )
        assertTrue(validVoucher.isValid())

        val expiredVoucher = validVoucher.copy(endDate = now - 100)
        assertFalse(expiredVoucher.isValid())

        val limitReachedVoucher = validVoucher.copy(usedCount = 5, usageLimit = 5)
        assertFalse(limitReachedVoucher.isValid())
    }

    @Test
    fun `Voucher getDiscountAmount correctly applies percentage, fixed and caps maxDiscount`() {
        val percentageVoucher = Voucher(
            type = "percentage",
            value = 20.0,
            maxDiscount = 50000.0
        )
        // 20% of 200,000 = 40,000 <= 50,000 cap
        assertEquals(40000.0, percentageVoucher.getDiscountAmount(200000.0))
        // 20% of 300,000 = 60,000 > 50,000 cap -> 50,000
        assertEquals(50000.0, percentageVoucher.getDiscountAmount(300000.0))

        val fixedVoucher = Voucher(
            type = "fixed",
            value = 30000.0
        )
        assertEquals(30000.0, fixedVoucher.getDiscountAmount(100000.0))
        assertEquals(20000.0, fixedVoucher.getDiscountAmount(20000.0)) // limited to total

        val freeShippingVoucher = Voucher(
            type = "free_shipping",
            minOrderValue = 150000.0
        )
        assertEquals(15000.0, freeShippingVoucher.getDiscountAmount(160000.0))
        assertEquals(0.0, freeShippingVoucher.getDiscountAmount(100000.0))
    }

    @Test
    fun `OrderStatus, PaymentMethod, PaymentStatus enums have clean domain definitions`() {
        assertEquals(7, OrderStatus.entries.size)
        assertTrue(OrderStatus.entries.contains(OrderStatus.DELIVERED))

        assertEquals(4, PaymentMethod.entries.size)
        assertTrue(PaymentMethod.entries.contains(PaymentMethod.E_WALLET))

        assertEquals(4, PaymentStatus.entries.size)
        assertTrue(PaymentStatus.entries.contains(PaymentStatus.PAID))
    }
}