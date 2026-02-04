package com.algoviz.plus.core.network.util

import com.algoviz.plus.core.common.error.AlgoVizError
import com.algoviz.plus.core.common.result.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import retrofit2.HttpException
import java.io.IOException

suspend fun <T> safeApiCall(
    apiCall: suspend () -> T
): Result<T> {
    return try {
        Result.Success(apiCall())
    } catch (e: HttpException) {
        Result.Error(
            AlgoVizError.ServerError(
                code = e.code(),
                message = e.message(),
                cause = e
            )
        )
    } catch (e: IOException) {
        Result.Error(
            AlgoVizError.NetworkError(
                message = "Network error: ${e.message}",
                cause = e
            )
        )
    } catch (e: Exception) {
        Result.Error(
            AlgoVizError.UnknownError(
                message = e.message ?: "Unknown error occurred",
                cause = e
            )
        )
    }
}

fun <T> Flow<T>.asResult(): Flow<Result<T>> {
    return this
        .map<T, Result<T>> { Result.Success(it) }
        .onStart { emit(Result.Loading) }
        .catch { throwable ->
            val error = when (throwable) {
                is HttpException -> AlgoVizError.ServerError(
                    code = throwable.code(),
                    message = throwable.message(),
                    cause = throwable
                )
                is IOException -> AlgoVizError.NetworkError(
                    message = "Network error: ${throwable.message}",
                    cause = throwable
                )
                else -> AlgoVizError.UnknownError(
                    message = throwable.message ?: "Unknown error occurred",
                    cause = throwable
                )
            }
            emit(Result.Error(error))
        }
}
