package com.liam.compose.core.datastore.di

import com.liam.compose.core.datastore.UserPreferencesRepository
import com.liam.compose.core.model.IAuthSessionStore
import com.liam.compose.core.networking.repository.ITokenProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Binds app-owned implementations to the abstractions declared by other modules:
 * - the auth feature's [IAuthSessionStore]
 * - core:networking's [ITokenProvider] (consumed by TokenInterceptor)
 *
 * Both resolve to the Proto DataStore-backed [com.liam.compose.core.datastore.UserPreferencesRepository], keeping the
 * module dependency one-way (:app depends on :feature:auth and :core:networking).
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AuthBindingsModule {

    @Binds
    @Singleton
    abstract fun bindAuthSessionStore(impl: UserPreferencesRepository): IAuthSessionStore

    @Binds
    @Singleton
    abstract fun bindTokenProvider(impl: UserPreferencesRepository): ITokenProvider

}
