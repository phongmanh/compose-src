package com.liam.compose.features.auth.data.remote

import com.liam.compose.features.auth.data.model.AuthPostRequest
import com.liam.compose.features.auth.data.model.ChangePassModel
import com.liam.compose.core.networking.model.AppResponse
import com.liam.compose.features.auth.data.model.UserModel
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface AuthService {

    @POST("account/login")
    @Headers("Content-Type: application/json")
    suspend fun auth(@Body request: AuthPostRequest): AppResponse<UserModel>

    @POST("account/update-password")
    @Headers("Content-Type: application/json")
    suspend fun changePass(@Body request: ChangePassModel): AppResponse<Any>
}
