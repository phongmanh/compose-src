package com.liam.compose.core.networking.remote

sealed class ApiException(message: String) : Exception(message) {
    
    data class NetworkException(val throwable: Throwable) : 
        ApiException("Network error: ${throwable.message}")
    
    data class ServerException(
        val code: Int,
        val errorMessage: String,
        val errorCode: Int? = null
    ) : ApiException("Server error ($code): $errorMessage")
    
    data class BadRequestException(val errorMessage: String) : 
        ApiException("Bad request: $errorMessage")
    
    data class UnauthorizedException(val errorMessage: String = "Unauthorized") : 
        ApiException(errorMessage)
    
    data class ForbiddenException(val errorMessage: String = "Forbidden") : 
        ApiException(errorMessage)
    
    data class NotFoundException(val errorMessage: String = "Not found") : 
        ApiException(errorMessage)
    
    data class ConflictException(val errorMessage: String = "Conflict") : 
        ApiException(errorMessage)
    
    data class TimeoutException(val errorMessage: String = "Request timeout") : 
        ApiException(errorMessage)
    
    data class ParseException(val errorMessage: String = "Failed to parse response") : 
        ApiException(errorMessage)
    
    data class UnknownException(val errorMessage: String = "Unknown error") : 
        ApiException(errorMessage)
    
    val userMessage: String
        get() = when (this) {
            is NetworkException -> "Unable to connect. Please check your internet connection."
            is ServerException -> errorMessage.takeIf { it.isNotEmpty() } ?: "Server error. Please try again."
            is BadRequestException -> "Invalid request. Please try again."
            is UnauthorizedException -> "Session expired. Please login again."
            is ForbiddenException -> "You don't have permission to perform this action."
            is NotFoundException -> "The requested resource was not found."
            is ConflictException -> "Conflict with existing data."
            is TimeoutException -> "Request took too long. Please try again."
            is ParseException -> "Failed to process the response. Please try again."
            is UnknownException -> "Something went wrong. Please try again."
        }
}
