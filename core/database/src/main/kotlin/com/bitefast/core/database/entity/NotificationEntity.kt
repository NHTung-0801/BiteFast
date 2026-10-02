package com.bitefast.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity luu tru thong bao trong co so du lieu Room (Offline-First).
 */
@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey
    val id: String,
    val userId: String = "",
    val title: String,
    val message: String,
    val type: String,
    val orderId: String? = null,
    val voucherCode: String? = null,
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
