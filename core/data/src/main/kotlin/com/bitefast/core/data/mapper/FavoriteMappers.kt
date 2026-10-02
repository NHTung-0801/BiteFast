package com.bitefast.core.data.mapper

import com.bitefast.core.database.entity.FavoriteEntity
import com.bitefast.core.model.FavoriteItem
import com.bitefast.core.model.FavoriteType

fun FavoriteEntity.toFavoriteItem(): FavoriteItem {
    val favType = try {
        FavoriteType.valueOf(type)
    } catch (_: Exception) {
        FavoriteType.DISH
    }
    return FavoriteItem(
        id = id,
        type = favType,
        targetId = targetId,
        name = name,
        description = description,
        price = price,
        imageUrl = imageUrl,
        rating = rating,
        restaurantId = restaurantId,
        restaurantName = restaurantName,
        category = category,
        createdAt = createdAt
    )
}

fun FavoriteItem.toEntity(): FavoriteEntity {
    return FavoriteEntity(
        id = id.ifEmpty { "${type.name.lowercase()}_$targetId" },
        type = type.name,
        targetId = targetId,
        name = name,
        description = description,
        price = price,
        imageUrl = imageUrl,
        rating = rating,
        restaurantId = restaurantId,
        restaurantName = restaurantName,
        category = category,
        createdAt = createdAt
    )
}
