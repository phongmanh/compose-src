package com.liam.compose.core.networking.repository

import com.liam.compose.core.networking.model.AppResult
import com.liam.compose.core.networking.remote.ApiException
import com.liam.compose.core.networking.remote.ErrorMapper

abstract class BaseRepository(
    protected val errorMapper: ErrorMapper
) {

    protected suspend inline fun <T> safeApiCall(
        apiCall: suspend () -> T
    ): Result<T> {
        return try {
            Result.success(apiCall())
        } catch (e: Exception) {
            val apiException = errorMapper.convertToException(e)
            Result.failure(apiException)
        }
    }

    protected suspend inline fun <T> safeApiCallWithErrorBody(
        apiCall: suspend () -> T,
        onError: (ApiException) -> Unit = {}
    ): Result<T> {
        return try {
            Result.success(apiCall())
        } catch (e: Exception) {
            val apiException = errorMapper.convertToException(e)
            onError(apiException)
            Result.failure(apiException)
        }
    }

    protected suspend inline fun <T> safeApiCall1(
        apiCall: suspend () -> T
    ): AppResult<T> {
        return try {
            AppResult.Success(apiCall())
        } catch (e: Exception) {
            val apiException = errorMapper.convertToException(e)
            AppResult.Error(apiException)
        }
    }

    protected suspend inline fun <T> safeApiCallWithErrorBody1(
        apiCall: suspend () -> T,
        onError: (ApiException) -> Unit = {}
    ): AppResult<T> {
        return try {
            AppResult.Success(apiCall())
        } catch (e: Exception) {
            val apiException = errorMapper.convertToException(e)
            onError(apiException)
            AppResult.Error(apiException)
        }
    }
}
