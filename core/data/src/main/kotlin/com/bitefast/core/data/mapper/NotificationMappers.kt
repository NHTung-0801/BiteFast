package com.bitefast.core.data.mapper

import com.bitefast.core.database.entity.NotificationEntity
import com.bitefast.core.model.Notification

/**
 * Mappers 2 chieu giua NotificationEntity (Room DB) va Notification (Domain Entity).
 */
fun NotificationEntity.asExternalModel(): Notification = Notification(
    id = id,
    userId = userId,
    title = title,
    message = message,
    type = type,
    data = buildMap {
        orderId?.let { put("orderId", it) }
        voucherCode?.let { put("voucherCode", it) }
    },
    isRead = isRead,
    createdAt = createdAt
)

fun Notification.asEntity(): NotificationEntity = NotificationEntity(
    id = id.ifBlank { "notif_${System.currentTimeMillis()}" },
    userId = userId,
    title = title,
    message = message,
    type = type,
    orderId = data["orderId"],
    voucherCode = data["voucherCode"],
    isRead = isRead,
    createdAt = createdAt
)
