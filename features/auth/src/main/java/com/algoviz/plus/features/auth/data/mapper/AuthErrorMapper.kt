package com.algoviz.plus.features.auth.data.mapper

import com.algoviz.plus.features.auth.domain.model.AuthError
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.exceptions.HttpRequestException
import io.github.jan.supabase.exceptions.RestException

object AuthErrorMapper {
    fun mapExceptionToAuthError(exception: Throwable): AuthError {
        val message = exception.message.orEmpty().lowercase()

        return when (exception) {
            is HttpRequestException -> AuthError.NetworkError()
            is AuthRestException -> mapAuthRestException(exception)
            is RestException -> {
                when {
                    message.contains("invalid login") || message.contains("invalid credentials") || message.contains("bad jwt") -> AuthError.InvalidCredentials()
                    message.contains("email not confirmed") -> AuthError.EmailNotVerified()
                    message.contains("no active session") || message.contains("check your email") || message.contains("verification") -> AuthError.EmailNotVerified("Account created. Check your email to verify your account.")
                    message.contains("already registered") || message.contains("already exists") || message.contains("user already") -> AuthError.UserCollision()
                    message.contains("invalid email") || message.contains("validation") -> AuthError.InvalidEmail()
                    message.contains("password") && message.contains("weak") -> AuthError.WeakPassword()
                    message.contains("oauth provider not supported") ||
                        message.contains("provider disabled") ||
                        message.contains("unexpected audience") ||
                        message.contains("bad jwt") -> {
                        AuthError.Unknown(
                            "Google Sign-In is not configured correctly for this build. Check the Supabase Google provider and the web client ID used by the app."
                        )
                    }
                    else -> AuthError.Unknown(exception.description ?: exception.message ?: "Unknown server error")
                }
            }

            else -> {
                when {
                    message.contains("no active session") || message.contains("check your email") || message.contains("verification") -> {
                        AuthError.EmailNotVerified("Account created. Check your email to verify your account.")
                    }

                    else -> AuthError.Unknown(exception.message ?: "Unknown error occurred")
                }
            }
        }
    }

    private fun mapAuthRestException(exception: AuthRestException): AuthError {
        val code = exception.errorCode?.value ?: exception.error.lowercase()
        val description = exception.errorDescription.trim().ifBlank { exception.message.orEmpty() }

        return when (code) {
            "invalid_credentials" -> AuthError.InvalidCredentials(description.ifBlank { "Invalid email or password" })
            "bad_jwt", "unexpected_audience", "oauth_provider_not_supported", "provider_disabled", "email_provider_disabled", "anonymous_provider_disabled" -> {
                AuthError.Unknown(
                    "Google Sign-In is not configured correctly for this build. Check the Supabase Google provider and the web client ID used by the app."
                )
            }
            "email_not_confirmed", "provider_email_needs_verification" -> {
                AuthError.EmailNotVerified(description.ifBlank { "Please verify your email before continuing" })
            }
            "email_exists", "user_already_exists", "identity_already_exists", "conflict" -> AuthError.UserCollision()
            "weak_password" -> AuthError.WeakPassword(description.ifBlank { "Password is too weak. Use at least 6 characters" })
            "validation_failed", "email_address_invalid", "bad_json" -> AuthError.InvalidEmail(description.ifBlank { "Please enter a valid email address" })
            "signup_disabled" -> AuthError.Unknown("New account creation is disabled for this environment.")
            "over_request_rate_limit", "over_email_send_rate_limit", "request_timeout" -> {
                AuthError.NetworkError("Too many requests. Please wait a moment and try again.")
            }
            "session_not_found" -> AuthError.EmailNotVerified("Your session expired. Sign in again to continue.")
            else -> AuthError.Unknown(description.ifBlank { exception.error })
        }
    }
}
