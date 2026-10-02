package com.bitefast.core.data.di

import com.bitefast.core.data.repository.AddressRepositoryImpl
import com.bitefast.core.data.repository.AuthRepositoryImpl
import com.bitefast.core.data.repository.CartRepositoryImpl
import com.bitefast.core.data.repository.FavoriteRepositoryImpl
import com.bitefast.core.data.repository.NotificationRepositoryImpl
import com.bitefast.core.data.repository.OrderRepositoryImpl
import com.bitefast.core.data.repository.RatingRepositoryImpl
import com.bitefast.core.data.repository.RestaurantRepositoryImpl
import com.bitefast.core.data.repository.VoucherRepositoryImpl
import com.bitefast.core.domain.repository.AddressRepository
import com.bitefast.core.domain.repository.AuthRepository
import com.bitefast.core.domain.repository.CartRepository
import com.bitefast.core.domain.repository.FavoriteRepository
import com.bitefast.core.domain.repository.NotificationRepository
import com.bitefast.core.domain.repository.OrderRepository
import com.bitefast.core.domain.repository.RatingRepository
import com.bitefast.core.domain.repository.RestaurantRepository
import com.bitefast.core.domain.repository.VoucherRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindCartRepository(impl: CartRepositoryImpl): CartRepository

    @Binds
    @Singleton
    abstract fun bindRestaurantRepository(impl: RestaurantRepositoryImpl): RestaurantRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindOrderRepository(impl: OrderRepositoryImpl): OrderRepository

    @Binds
    @Singleton
    abstract fun bindAddressRepository(impl: AddressRepositoryImpl): AddressRepository

    @Binds
    @Singleton
    abstract fun bindRatingRepository(impl: RatingRepositoryImpl): RatingRepository

    @Binds
    @Singleton
    abstract fun bindNotificationRepository(impl: NotificationRepositoryImpl): NotificationRepository

    @Binds
    @Singleton
    abstract fun bindVoucherRepository(impl: VoucherRepositoryImpl): VoucherRepository

    @Binds
    @Singleton
    abstract fun bindFavoriteRepository(impl: FavoriteRepositoryImpl): FavoriteRepository
}
