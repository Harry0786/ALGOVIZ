package com.algoviz.plus.features.auth.data.mapper

import com.algoviz.plus.features.auth.domain.model.AuthError
import io.github.jan.supabase.exceptions.HttpRequestException
import io.github.jan.supabase.exceptions.RestException

object AuthErrorMapper {
    fun mapExceptionToAuthError(exception: Throwable): AuthError {
        val message = exception.message.orEmpty().lowercase()

        return when (exception) {
            is HttpRequestException -> AuthError.NetworkError()
            is RestException -> mapRestException(exception)

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

    private fun mapRestException(exception: RestException): AuthError {
        val description = exception.description.orEmpty().trim().ifBlank { exception.message.orEmpty() }
        val combined = "${exception.error} ${exception.description.orEmpty()} ${exception.message.orEmpty()}".lowercase()
        val code = extractAuthCode(exception, combined)

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
            else -> {
                when {
                    combined.contains("invalid login") || combined.contains("invalid credentials") -> AuthError.InvalidCredentials()
                    combined.contains("email not confirmed") || combined.contains("provider_email_needs_verification") -> AuthError.EmailNotVerified()
                    combined.contains("already registered") || combined.contains("already exists") || combined.contains("user already") -> AuthError.UserCollision()
                    combined.contains("password") && combined.contains("weak") -> AuthError.WeakPassword()
                    combined.contains("invalid email") || combined.contains("validation") -> AuthError.InvalidEmail()
                    else -> AuthError.Unknown(description.ifBlank { exception.error })
                }
            }
        }
    }

    private fun extractAuthCode(exception: RestException, combined: String): String {
        val raw = exception.error.lowercase()
        if (raw.contains('_')) return raw

        val knownCodes = listOf(
            "invalid_credentials",
            "bad_jwt",
            "unexpected_audience",
            "oauth_provider_not_supported",
            "provider_disabled",
            "email_provider_disabled",
            "anonymous_provider_disabled",
            "email_not_confirmed",
            "provider_email_needs_verification",
            "email_exists",
            "user_already_exists",
            "identity_already_exists",
            "conflict",
            "weak_password",
            "validation_failed",
            "email_address_invalid",
            "bad_json",
            "signup_disabled",
            "over_request_rate_limit",
            "over_email_send_rate_limit",
            "request_timeout",
            "session_not_found"
        )

        return knownCodes.firstOrNull { combined.contains(it) } ?: raw
    }
}
