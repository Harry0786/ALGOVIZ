package com.algoviz.plus.data.repository

import com.algoviz.plus.core.common.result.Result
import com.algoviz.plus.core.network.util.safeApiCall

abstract class BaseRepository {
    
    protected suspend fun <T> executeApiCall(
        apiCall: suspend () -> T
    ): Result<T> = safeApiCall { apiCall() }
    
    protected suspend fun <T, R> executeWithMapping(
        apiCall: suspend () -> T,
        mapper: (T) -> R
    ): Result<R> {
        return when (val result = safeApiCall { apiCall() }) {
            is Result.Success -> Result.Success(mapper(result.data))
            is Result.Error -> Result.Error(result.exception)
            is Result.Loading -> Result.Loading
        }
    }
}
