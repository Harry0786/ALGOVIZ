package com.algoviz.plus.domain.usecase

import com.algoviz.plus.core.common.dispatcher.DispatcherProvider
import com.algoviz.plus.core.common.result.Result
import kotlinx.coroutines.withContext

abstract class UseCase<in P, out R>(
    private val dispatchers: DispatcherProvider
) {
    suspend operator fun invoke(params: P): Result<R> {
        return try {
            withContext(dispatchers.io) {
                execute(params)
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    protected abstract suspend fun execute(params: P): Result<R>
}

abstract class NoParamUseCase<out R>(
    private val dispatchers: DispatcherProvider
) {
    suspend operator fun invoke(): Result<R> {
        return try {
            withContext(dispatchers.io) {
                execute()
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    protected abstract suspend fun execute(): Result<R>
}
