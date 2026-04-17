package com.algoviz.plus.features.auth.data.mapper

import com.algoviz.plus.features.auth.domain.model.AuthError
import io.github.jan.supabase.exceptions.HttpRequestException
import io.github.jan.supabase.exceptions.RestException

object AuthErrorMapper {
    fun mapExceptionToAuthError(exception: Throwable): AuthError {
        val message = exception.message.orEmpty().lowercase()

        return when (exception) {
            is HttpRequestException -> AuthError.NetworkError()
            is RestException -> {
                when {
                    message.contains("invalid login") || message.contains("invalid credentials") || message.contains("bad jwt") -> AuthError.InvalidCredentials()
                    message.contains("email not confirmed") -> AuthError.EmailNotVerified()
                    message.contains("already registered") || message.contains("already exists") || message.contains("user already") -> AuthError.UserCollision()
                    message.contains("invalid email") || message.contains("validation") -> AuthError.InvalidEmail()
                    message.contains("password") && message.contains("weak") -> AuthError.WeakPassword()
                    else -> AuthError.Unknown(exception.message ?: "Unknown server error")
                }
            }

            else -> AuthError.Unknown(exception.message ?: "Unknown error occurred")
        }
    }
}
