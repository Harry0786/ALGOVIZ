package com.algoviz.plus.features.auth.data.mapper

import com.algoviz.plus.features.auth.domain.model.AuthError
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.FirebaseNetworkException

object AuthErrorMapper {
    fun mapExceptionToAuthError(exception: Throwable): AuthError {
        return when (exception) {
            is FirebaseAuthInvalidCredentialsException -> AuthError.InvalidCredentials()
            is FirebaseAuthInvalidUserException -> AuthError.InvalidCredentials()
            is FirebaseAuthUserCollisionException -> AuthError.UserCollision()
            is FirebaseAuthWeakPasswordException -> AuthError.WeakPassword()
            is FirebaseNetworkException -> AuthError.NetworkError()
            else -> AuthError.Unknown(exception.message ?: "Unknown error occurred")
        }
    }
}
