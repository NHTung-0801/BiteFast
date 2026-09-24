package com.bitefast.core.data.di

import com.bitefast.core.data.repository.AuthRepositoryImpl
import com.bitefast.core.data.repository.CartRepositoryImpl
import com.bitefast.core.data.repository.OrderRepositoryImpl
import com.bitefast.core.data.repository.RestaurantRepositoryImpl
import com.bitefast.core.domain.repository.AuthRepository
import com.bitefast.core.domain.repository.CartRepository
import com.bitefast.core.domain.repository.OrderRepository
import com.bitefast.core.domain.repository.RestaurantRepository
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
}
