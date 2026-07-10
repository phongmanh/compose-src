package com.liam.compose.features.auth.di

import com.liam.compose.features.auth.data.remote.AuthService
import com.liam.compose.features.auth.data.remote.TokenService
import com.liam.compose.core.networking.di.AuthRetrofit
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

/**
 * Provides the auth feature's Retrofit services. [AuthService] uses the default (unqualified)
 * [Retrofit] from core:networking, which targets the main API where the `account/` endpoints
 * live. [TokenService] uses the [AuthRetrofit]-qualified Retrofit for the OAuth/token service.
 */
@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    @Provides
    @Singleton
    fun provideAuthService(retrofit: Retrofit): AuthService =
        retrofit.create(AuthService::class.java)

    @Provides
    @Singleton
    fun provideTokenService(@AuthRetrofit retrofit: Retrofit): TokenService =
        retrofit.create(TokenService::class.java)
}
