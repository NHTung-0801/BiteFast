package com.bitefast.core.data.repository

import com.bitefast.core.domain.repository.VoucherRepository
import com.bitefast.core.model.Voucher
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VoucherRepositoryImpl @Inject constructor() : VoucherRepository {

    private val vouchers = ConcurrentHashMap<String, Voucher>()

    init {
        val now = System.currentTimeMillis()
        val future = now + 30L * 24 * 60 * 60 * 1000 // 30 days
        listOf(
            Voucher(
                id = "v_welcome",
                code = "WELCOME50",
                name = "Giảm 50k cho đơn đầu tiên",
                description = "Áp dụng cho đơn hàng từ 100.000đ",
                type = "fixed",
                value = 50000.0,
                minOrderValue = 100000.0,
                maxDiscount = 50000.0,
                startDate = now - 10000,
                endDate = future,
                isActive = true
            ),
            Voucher(
                id = "v_freeship",
                code = "FREESHIP",
                name = "Miễn phí vận chuyển",
                description = "Giảm tối đa 25.000đ phí giao hàng",
                type = "fixed",
                value = 25000.0,
                minOrderValue = 50000.0,
                maxDiscount = 25000.0,
                startDate = now - 10000,
                endDate = future,
                isActive = true
            ),
            Voucher(
                id = "v_bitefast20",
                code = "BITEFAST20",
                name = "Giảm 20% tổng đơn",
                description = "Giảm tối đa 40.000đ cho đơn từ 120.000đ",
                type = "percentage",
                value = 20.0,
                minOrderValue = 120000.0,
                maxDiscount = 40000.0,
                startDate = now - 10000,
                endDate = future,
                isActive = true
            )
        ).forEach { vouchers[it.code.uppercase()] = it }
    }

    override suspend fun getVouchers(restaurantId: String?): List<Voucher> {
        return vouchers.values.filter { voucher ->
            voucher.isActive && (restaurantId == null || voucher.applicableRestaurants.isEmpty() || voucher.applicableRestaurants.contains(restaurantId))
        }
    }

    override suspend fun getVoucherByCode(code: String): Voucher? {
        return vouchers[code.trim().uppercase()]
    }
}
