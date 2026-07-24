package com.liam.compose.core.networking.repository

import android.util.Log
import com.liam.compose.core.networking.model.AppResult
import com.liam.compose.core.networking.remote.ApiException
import com.liam.compose.core.networking.remote.ErrorMapper

abstract class BaseRepository(
    protected val errorMapper: ErrorMapper
) {

    // The helpers below are `protected inline`, so they cannot reach private/internal members —
    // hence @PublishedApi. Tag is the concrete subclass name (AuthRepository, CustomerRepository)
    // so every line is traceable to the repository that emitted it.
    @PublishedApi
    internal val tag: String get() = this::class.java.simpleName

    @PublishedApi
    internal fun logEnter(operation: String) {
        Log.d(tag, "$operation: enter")
    }

    @PublishedApi
    internal fun logExit(operation: String, startedAt: Long) {
        Log.d(tag, "$operation: exit, ${elapsedMs(startedAt)}ms")
    }

    // Logs the failure and its stack trace but never the request or response payload, which can
    // carry credentials or personal data. ApiException messages are server/user-facing text only.
    @PublishedApi
    internal fun logFailure(operation: String, error: ApiException, startedAt: Long) {
        Log.w(tag, "$operation: exit with error after ${elapsedMs(startedAt)}ms", error)
    }

    // nanoTime is monotonic (unlike currentTimeMillis) and pure JVM, so it stays correct across
    // clock changes and works unchanged in unit tests.
    @PublishedApi
    internal fun startTiming(): Long = System.nanoTime()

    @PublishedApi
    internal fun elapsedMs(startedAt: Long): Long = (System.nanoTime() - startedAt) / 1_000_000

    protected suspend inline fun <T> safeApiCall(
        operation: String,
        apiCall: suspend () -> T
    ): Result<T> {
        val startedAt = startTiming()
        logEnter(operation)
        return try {
            val result = apiCall()
            logExit(operation, startedAt)
            Result.success(result)
        } catch (e: Exception) {
            val apiException = errorMapper.convertToException(e)
            logFailure(operation, apiException, startedAt)
            Result.failure(apiException)
        }
    }

    protected suspend inline fun <T> safeApiCallWithErrorBody(
        operation: String,
        apiCall: suspend () -> T,
        onError: (ApiException) -> Unit = {}
    ): Result<T> {
        val startedAt = startTiming()
        logEnter(operation)
        return try {
            val result = apiCall()
            logExit(operation, startedAt)
            Result.success(result)
        } catch (e: Exception) {
            val apiException = errorMapper.convertToException(e)
            logFailure(operation, apiException, startedAt)
            onError(apiException)
            Result.failure(apiException)
        }
    }

    protected suspend inline fun <T> safeApiCall1(
        operation: String,
        apiCall: suspend () -> T
    ): AppResult<T> {
        val startedAt = startTiming()
        logEnter(operation)
        return try {
            val result = apiCall()
            logExit(operation, startedAt)
            AppResult.Success(result)
        } catch (e: Exception) {
            val apiException = errorMapper.convertToException(e)
            logFailure(operation, apiException, startedAt)
            AppResult.Error(apiException)
        }
    }

    protected suspend inline fun <T> safeApiCallWithErrorBody1(
        operation: String,
        apiCall: suspend () -> T,
        onError: (ApiException) -> Unit = {}
    ): AppResult<T> {
        val startedAt = startTiming()
        logEnter(operation)
        return try {
            val result = apiCall()
            logExit(operation, startedAt)
            AppResult.Success(result)
        } catch (e: Exception) {
            val apiException = errorMapper.convertToException(e)
            logFailure(operation, apiException, startedAt)
            onError(apiException)
            AppResult.Error(apiException)
        }
    }
}
