package com.liam.compose.core.networking.model

import com.liam.compose.core.networking.remote.ApiException

sealed class AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>()
    data class Error(val exception: ApiException) : AppResult<Nothing>()
    data class Loading<T>(val data: T? = null) : AppResult<T>()

    fun getOrNull(): T? = when (this) {
        is Success -> data
        else -> null
    }

    fun exceptionOrNull(): ApiException? = when (this) {
        is Error -> exception
        else -> null
    }

    fun isSuccess(): Boolean = this is Success
    fun isError(): Boolean = this is Error
    fun isLoading(): Boolean = this is Loading
}