package com.liam.compose.features.auth.data.remote

import com.liam.compose.core.model.GatewayAuthModel
import com.liam.compose.features.auth.data.model.AuthPostRequest
import com.liam.compose.core.networking.model.AppResponse
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

/**
 * Talks to the OAuth/token service (built from the `@AuthRetrofit` Retrofit), so it is kept
 * separate from [AuthService], which targets the main API.
 */
interface TokenService {

    @POST("auth/get-token")
    @Headers("Content-Type: application/json")
    suspend fun getToken(@Body request: AuthPostRequest): AppResponse<GatewayAuthModel>
}
