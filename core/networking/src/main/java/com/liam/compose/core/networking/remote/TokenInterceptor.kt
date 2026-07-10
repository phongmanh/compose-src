package com.liam.compose.core.networking.remote

import com.liam.compose.core.networking.BuildConfig
import com.liam.compose.core.networking.repository.ITokenProvider
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class TokenInterceptor @Inject constructor(
    private val authenticator: ITokenProvider
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val token = getToken() // Persisted access token, empty until the user is authenticated.

        val requestWithToken = if (token.isNotEmpty()) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            originalRequest.newBuilder()
                .header("gm-gateway-key", BuildConfig.GATEWAY_KEY)
                .build()
        }

        return chain.proceed(requestWithToken)
    }

    // OkHttp interceptors are synchronous, so bridge the suspend DataStore read.
    // DataStore serves subsequent reads from its in-memory cache, so this is cheap.
    private fun getToken(): String = runBlocking {
        authenticator.accessToken()
    }
}
