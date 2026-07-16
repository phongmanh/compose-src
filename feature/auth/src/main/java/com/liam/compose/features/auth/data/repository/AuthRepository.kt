package com.liam.compose.features.auth.data.repository

import com.liam.compose.core.networking.model.AppResponse
import com.liam.compose.core.networking.remote.ErrorMapper
import com.liam.compose.core.networking.repository.BaseRepository
import com.liam.compose.core.model.GatewayAuthModel
import com.liam.compose.features.auth.data.model.AuthPostRequest
import com.liam.compose.features.auth.data.model.ChangePassModel
import com.liam.compose.core.model.UserModel
import com.liam.compose.features.auth.data.remote.AuthService
import com.liam.compose.features.auth.data.remote.TokenService
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val authService: AuthService,
    private val tokenService: TokenService,
    errormapper: ErrorMapper
) : BaseRepository(errormapper) {

    suspend fun getToken(request: AuthPostRequest): Result<AppResponse<GatewayAuthModel>> {
        return safeApiCall { tokenService.getToken(request) }
    }

    suspend fun auth(authPostRequest: AuthPostRequest): Result<AppResponse<UserModel>> {
        return safeApiCall { authService.auth(authPostRequest) }
    }

    suspend fun changePassword(request: ChangePassModel): Result<Any> {
        return safeApiCall { authService.changePass(request) }
    }
}
