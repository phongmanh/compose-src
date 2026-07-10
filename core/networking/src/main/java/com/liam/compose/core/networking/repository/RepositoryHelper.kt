package com.liam.compose.core.networking.repository

import com.liam.compose.core.networking.model.AppResult
import com.liam.compose.core.networking.remote.ApiException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

fun <T> apiCallFlow(
    apiCall: suspend () -> T
): Flow<AppResult<T>> = flow {
    emit(AppResult.Loading())
    try {
        val result = apiCall()
        emit(AppResult.Success(result))
    } catch (e: Exception) {
        val exception = when (e) {
            is ApiException -> e
            else -> ApiException.UnknownException(e.message ?: "Unknown error")
        }
        emit(AppResult.Error(exception))
    }
}

fun <T> apiCall(
    apiCall: suspend () -> T
): Flow<AppResult<T>> = flow {
    emit(AppResult.Loading())
    try {
        val result = apiCall()
        emit(AppResult.Success(result))
    } catch (e: Exception) {
        val exception = when (e) {
            is ApiException -> e
            else -> ApiException.UnknownException(e.message ?: "Unknown error")
        }
        emit(AppResult.Error(exception))
    }
}
