package com.algoviz.plus.core.common.error

sealed class AlgoVizError(
    override val message: String,
    override val cause: Throwable? = null
) : Throwable(message, cause) {

    data class NetworkError(
        override val message: String = "Network error occurred",
        override val cause: Throwable? = null
    ) : AlgoVizError(message, cause)

    data class ServerError(
        val code: Int,
        override val message: String = "Server error: $code",
        override val cause: Throwable? = null
    ) : AlgoVizError(message, cause)

    data class AuthError(
        override val message: String = "Authentication failed",
        override val cause: Throwable? = null
    ) : AlgoVizError(message, cause)

    data class DatabaseError(
        override val message: String = "Database error occurred",
        override val cause: Throwable? = null
    ) : AlgoVizError(message, cause)

    data class ValidationError(
        override val message: String = "Validation failed",
        override val cause: Throwable? = null
    ) : AlgoVizError(message, cause)

    data class UnknownError(
        override val message: String = "Unknown error occurred",
        override val cause: Throwable? = null
    ) : AlgoVizError(message, cause)
}
