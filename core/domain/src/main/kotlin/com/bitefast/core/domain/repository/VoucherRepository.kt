package com.bitefast.core.domain.repository

import com.bitefast.core.model.Voucher

interface VoucherRepository {
    suspend fun getVouchers(restaurantId: String? = null): List<Voucher>
    suspend fun getVoucherByCode(code: String): Voucher?
}
