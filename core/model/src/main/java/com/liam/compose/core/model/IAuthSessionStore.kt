package com.liam.compose.core.model

/**
 * Persistence boundary for the signed-in user. Kept in `:core:model` (rather than `:feature:auth`)
 * so that `:core:datastore` can implement it without a core module depending on a feature module;
 * the auth feature depends on this contract, it doesn't own it.
 */
interface IAuthSessionStore {

    /** Current access token, or empty string if none is stored. */
    suspend fun saveGatewayAuth(gatewayAuthModel: GatewayAuthModel)

    /** Persists the authenticated user. */
    suspend fun saveUser(user: UserModel)

    /** Clears any stored session (e.g. on logout). */
    suspend fun clear()
}
