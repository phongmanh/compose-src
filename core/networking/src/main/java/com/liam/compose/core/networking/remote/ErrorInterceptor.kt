package com.liam.compose.core.networking.remote

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class ErrorInterceptor @Inject constructor(private val errorMapper: ErrorMapper) : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        
        return try {
            val response = chain.proceed(request)
            
            if (!response.isSuccessful) {
                val exception = ApiException.ServerException(
                    response.code,
                    response.message,
                    null
                )
                throw exception
            }
            
            response
        } catch (e: Exception) {
            if (e is ApiException) throw e
            throw errorMapper.convertToException(e)
        }
    }
}
