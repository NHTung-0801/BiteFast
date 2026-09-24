package com.bitefast.core.data.mapper

import com.bitefast.core.database.entity.CartItemEntity
import com.bitefast.core.database.entity.RestaurantEntity
import com.bitefast.core.model.CartItem
import com.bitefast.core.model.Restaurant

fun CartItemEntity.asExternalModel(): CartItem = CartItem(
    id = id,
    cartId = cartId,
    menuItemId = menuItemId,
    restaurantId = restaurantId,
    name = name,
    price = price,
    quantity = quantity,
    notes = notes,
    imageUrl = imageUrl,
    timestamp = timestamp
)

fun CartItem.asEntity(): CartItemEntity = CartItemEntity(
    id = id,
    cartId = cartId,
    menuItemId = menuItemId,
    restaurantId = restaurantId,
    name = name,
    price = price,
    quantity = quantity,
    notes = notes,
    imageUrl = imageUrl,
    timestamp = timestamp
)

fun RestaurantEntity.asExternalModel(): Restaurant = Restaurant(
    id = id,
    name = name,
    description = description,
    cuisine = cuisine,
    rating = rating,
    distance = distance,
    estimatedTime = estimatedTime,
    priceLevel = priceLevel,
    imageUrl = imageUrl,
    coverImageUrl = coverImageUrl,
    latitude = latitude,
    longitude = longitude,
    address = address,
    phoneNumber = phoneNumber,
    openingHours = openingHours,
    isOpen = isOpen,
    isFreeDelivery = isFreeDelivery,
    isFavorite = isFavorite
)

fun Restaurant.asEntity(): RestaurantEntity = RestaurantEntity(
    id = id,
    name = name,
    description = description,
    cuisine = cuisine,
    rating = rating,
    distance = distance,
    estimatedTime = estimatedTime,
    priceLevel = priceLevel,
    imageUrl = imageUrl,
    coverImageUrl = coverImageUrl,
    latitude = latitude,
    longitude = longitude,
    address = address,
    phoneNumber = phoneNumber,
    openingHours = openingHours,
    isOpen = isOpen,
    isFreeDelivery = isFreeDelivery,
    isFavorite = isFavorite
)
