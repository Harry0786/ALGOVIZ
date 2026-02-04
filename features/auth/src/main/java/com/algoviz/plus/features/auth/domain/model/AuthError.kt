package com.algoviz.plus.features.auth.domain.model

sealed class AuthError(val message: String) {
    data class InvalidCredentials(val detail: String = "Invalid email or password") : AuthError(detail)
    data class UserCollision(val detail: String = "An account with this email already exists") : AuthError(detail)
    data class WeakPassword(val detail: String = "Password is too weak. Use at least 6 characters") : AuthError(detail)
    data class NetworkError(val detail: String = "Network connection failed. Please check your internet") : AuthError(detail)
    data class EmailNotVerified(val detail: String = "Please verify your email before continuing") : AuthError(detail)
    data class InvalidEmail(val detail: String = "Please enter a valid email address") : AuthError(detail)
    data class Unknown(val detail: String = "An unexpected error occurred") : AuthError(detail)
}
