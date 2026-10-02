package com.bitefast.core.data.mapper

import com.bitefast.core.database.entity.AddressEntity
import com.bitefast.core.database.entity.CartItemEntity
import com.bitefast.core.database.entity.RestaurantEntity
import com.bitefast.core.model.Address
import com.bitefast.core.model.CartItem
import com.bitefast.core.model.MenuItem
import com.bitefast.core.model.Order
import com.bitefast.core.model.OrderStatus
import com.bitefast.core.model.PaymentMethod
import com.bitefast.core.model.PaymentStatus
import com.bitefast.core.model.Restaurant
import com.bitefast.core.model.User
import com.bitefast.core.model.Voucher
import com.bitefast.core.network.model.AddressDto
import com.bitefast.core.network.model.CreateAddressRequestDto
import com.bitefast.core.network.model.MenuItemDto
import com.bitefast.core.network.model.OrderItemDto
import com.bitefast.core.network.model.OrderResponseDto
import com.bitefast.core.network.model.RestaurantDto
import com.bitefast.core.network.model.UserDto
import com.bitefast.core.network.model.VoucherDto

// ==================== CART ITEM MAPPERS ====================

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

fun CartItem.asOrderItemDto(): OrderItemDto = OrderItemDto(
    menuItemId = menuItemId,
    name = name,
    price = price,
    quantity = quantity,
    notes = notes,
    imageUrl = imageUrl
)

fun OrderItemDto.asExternalModel(restaurantId: String = ""): CartItem = CartItem(
    id = menuItemId,
    cartId = "",
    menuItemId = menuItemId,
    restaurantId = restaurantId,
    name = name,
    price = price,
    quantity = quantity,
    notes = notes,
    imageUrl = imageUrl
)

// ==================== RESTAURANT MAPPERS ====================

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

fun RestaurantDto.asExternalModel(): Restaurant = Restaurant(
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
    tags = tags,
    totalOrders = totalOrders
)

fun RestaurantDto.asEntity(): RestaurantEntity = RestaurantEntity(
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
    isFavorite = false
)

// ==================== MENU ITEM MAPPERS ====================

fun MenuItemDto.asExternalModel(): MenuItem = MenuItem(
    id = id,
    restaurantId = restaurantId,
    name = name,
    description = description,
    price = price,
    currency = currency,
    imageUrl = imageUrl,
    category = category,
    isAvailable = isAvailable,
    isVegetarian = isVegetarian,
    isSpicy = isSpicy,
    isPopular = isPopular,
    preparationTime = preparationTime,
    tags = tags,
    rating = rating,
    reviewCount = reviewCount
)

// ==================== ORDER MAPPERS ====================

fun OrderResponseDto.asExternalModel(): Order {
    val orderStatus = try {
        OrderStatus.valueOf(status)
    } catch (e: Exception) {
        OrderStatus.PENDING
    }

    val payMethod = try {
        PaymentMethod.valueOf(paymentMethod)
    } catch (e: Exception) {
        PaymentMethod.CASH
    }

    val payStatus = try {
        PaymentStatus.valueOf(paymentStatus)
    } catch (e: Exception) {
        PaymentStatus.PENDING
    }

    return Order(
        id = id,
        userId = userId,
        restaurantId = restaurantId,
        restaurantName = restaurantName,
        driverId = driverId,
        driverName = driverName,
        driverPhone = driverPhone,
        items = items.map { it.asExternalModel(restaurantId) },
        subtotal = subtotal,
        deliveryFee = deliveryFee,
        taxes = taxes,
        discount = discount,
        total = total,
        status = orderStatus,
        paymentMethod = payMethod,
        paymentStatus = payStatus,
        address = Address(
            streetAddress = deliveryAddressText,
            recipientName = recipientName,
            phoneNumber = recipientPhone
        ),
        orderTime = orderTime,
        estimatedDeliveryTime = estimatedDeliveryTime
    )
}

// ==================== VOUCHER MAPPERS ====================

fun VoucherDto.asExternalModel(): Voucher = Voucher(
    id = id,
    code = code,
    name = name,
    description = description,
    type = type,
    value = value,
    minOrderValue = minOrderValue,
    maxDiscount = maxDiscount,
    usageLimit = usageLimit,
    usedCount = usedCount,
    startDate = startDate,
    endDate = endDate,
    isActive = isActive,
    applicableRestaurants = applicableRestaurants,
    imageUrl = imageUrl
)

// ==================== USER MAPPERS ====================

fun UserDto.asExternalModel(): User = User(
    id = id,
    name = name,
    email = email,
    phone = phone,
    avatar = avatar,
    isGuest = isGuest
)

// ==================== ADDRESS MAPPERS ====================

fun AddressDto.asExternalModel(): Address = Address(
    id = id,
    userId = userId,
    label = label,
    recipientName = recipientName,
    phoneNumber = phoneNumber,
    streetAddress = streetAddress,
    city = city,
    state = state,
    postalCode = postalCode,
    country = country,
    latitude = latitude,
    longitude = longitude,
    isDefault = isDefault
)

fun Address.asCreateDto(): CreateAddressRequestDto = CreateAddressRequestDto(
    label = label,
    recipientName = recipientName,
    phoneNumber = phoneNumber,
    streetAddress = streetAddress,
    city = city,
    state = state,
    postalCode = postalCode,
    latitude = latitude,
    longitude = longitude,
    isDefault = isDefault
)
