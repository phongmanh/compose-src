package com.liam.compose.features.customer.di

import com.liam.compose.core.networking.remote.ErrorMapper
import com.liam.compose.features.customer.data.remote.CustomerService
import com.liam.compose.features.customer.data.repository.CustomerRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CustomerModule {

    @Provides
    @Singleton
    fun provideCustomerService(retrofit: Retrofit): CustomerService =
        retrofit.create(CustomerService::class.java)

    @Provides
    @Singleton
    fun provideCustomerRepository(
        service: CustomerService,
        errorMapper: ErrorMapper,
    ): CustomerRepository = CustomerRepository(service, errorMapper)
}
