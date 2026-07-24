package com.liam.compose.core.networking.remote

import com.liam.compose.core.networking.model.ApiResponse
import retrofit2.HttpException
import java.io.IOException
import java.io.InterruptedIOException
import java.net.SocketTimeoutException
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import javax.inject.Inject

class ErrorMapper @Inject constructor(private val gson: Gson) {
    
    fun convertToException(throwable: Throwable): ApiException {
        return when (throwable) {
            is HttpException -> convertHttpException(throwable)
            // SocketTimeoutException (connect/read/write) extends InterruptedIOException, and
            // OkHttp raises a plain InterruptedIOException when callTimeout fires — both are
            // timeouts, so they must be matched before the generic IOException branch.
            is SocketTimeoutException -> ApiException.TimeoutException()
            is InterruptedIOException -> ApiException.TimeoutException()
            is IOException -> ApiException.NetworkException(throwable)
            is JsonSyntaxException -> ApiException.ParseException("Invalid JSON response")
            is ApiException -> throwable
            else -> ApiException.UnknownException(throwable.message ?: "Unknown error")
        }
    }
    
    private fun convertHttpException(exception: HttpException): ApiException {
        val code = exception.code()
        val errorBody = exception.response()?.errorBody()?.string()
        
        return when (code) {
            400 -> parseErrorResponse(errorBody)
                ?.let { ApiException.BadRequestException(it) }
                ?: ApiException.BadRequestException("Invalid request")
            
            401 -> ApiException.UnauthorizedException(
                parseErrorResponse(errorBody) ?: "Unauthorized"
            )
            
            403 -> ApiException.ForbiddenException(
                parseErrorResponse(errorBody) ?: "Forbidden"
            )
            
            404 -> ApiException.NotFoundException(
                parseErrorResponse(errorBody) ?: "Not found"
            )
            
            409 -> ApiException.ConflictException(
                parseErrorResponse(errorBody) ?: "Conflict"
            )
            
            in 500..599 -> {
                val errorMessage = parseErrorResponse(errorBody)
                val errorCode = parseErrorCode(errorBody)
                ApiException.ServerException(code, errorMessage ?: "Server error", errorCode)
            }
            
            else -> {
                val errorMessage = parseErrorResponse(errorBody)
                ApiException.UnknownException(errorMessage ?: "HTTP Error: $code")
            }
        }
    }
    
    private fun parseErrorResponse(errorBody: String?): String? {
        return try {
            if (errorBody.isNullOrEmpty()) return null
            
            val errorResponse = gson.fromJson(errorBody, ApiResponse::class.java)
            errorResponse.error ?: errorResponse.message
        } catch (e: Exception) {
            null
        }
    }
    
    private fun parseErrorCode(errorBody: String?): Int? {
        return try {
            if (errorBody.isNullOrEmpty()) return null
            
            val errorResponse = gson.fromJson(errorBody, ApiResponse::class.java)
            errorResponse.code
        } catch (e: Exception) {
            null
        }
    }
}
