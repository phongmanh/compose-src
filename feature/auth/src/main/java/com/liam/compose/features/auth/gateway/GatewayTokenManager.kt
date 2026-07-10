package com.liam.compose.features.auth.gateway

import com.liam.compose.features.auth.data.model.AuthPostRequest
import com.liam.compose.features.auth.data.repository.AuthRepository
import com.liam.compose.features.auth.data.repository.IAuthSessionStore
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GatewayTokenManager @Inject constructor(
    private val authRepository: AuthRepository,
    private val iAuthSessionStore: IAuthSessionStore
) {

    suspend fun ensureGatewayToken(): Result<Unit> {
        return authRepository.getToken(
            AuthPostRequest(
                GatewayClientCredentials.CLIENT_ID, GatewayClientCredentials.CLIENT_SECRET
            )
        ).fold(
            onSuccess = { result ->
                iAuthSessionStore.saveGatewayAuth(result.data!!)
                Result.success(Unit)
            },
            onFailure = { error -> Result.failure(error) }
        )
    }
}