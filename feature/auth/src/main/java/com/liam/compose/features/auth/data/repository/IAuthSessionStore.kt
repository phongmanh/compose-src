package com.liam.compose.features.auth.data.repository

import com.liam.compose.features.auth.data.model.GatewayAuthModel
import com.liam.compose.features.auth.data.model.UserModel

/**
 * Persistence boundary for the signed-in user, owned by the auth feature so it stays
 * independent of the app's concrete storage (Proto DataStore). The app module supplies
 * the implementation via Hilt, keeping the module dependency one-way (:app -> :feature:auth).
 */
interface IAuthSessionStore {

    /** Current access token, or empty string if none is stored. */
    suspend fun saveGatewayAuth(gatewayAuthModel: GatewayAuthModel)

    /** Persists the authenticated user. */
    suspend fun saveUser(user: UserModel)

    /** Clears any stored session (e.g. on logout). */
    suspend fun clear()
}
