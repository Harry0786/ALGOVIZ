package com.algoviz.plus.features.auth.data.remote

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.OtpType
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.Google
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.gotrue.providers.builtin.IDToken
import io.github.jan.supabase.gotrue.SessionStatus
import io.github.jan.supabase.gotrue.user.UserInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject

class FirebaseAuthDataSource @Inject constructor(
    private val supabaseClient: SupabaseClient
) {
    fun observeAuthState(): Flow<UserInfo?> {
        return supabaseClient.auth.sessionStatus.map { status ->
            when (status) {
                is SessionStatus.Authenticated -> status.session.user ?: supabaseClient.auth.currentUserOrNull()
                else -> null
            }
        }
    }

    suspend fun register(email: String, password: String): Result<UserInfo> {
        return try {
            supabaseClient.auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }
            val user = supabaseClient.auth.currentUserOrNull()
                ?: return Result.failure(Exception("Registration created account but no active session"))
            Result.success(user)
        } catch (e: Exception) {
            Timber.e(e, "Registration error")
            Result.failure(e)
        }
    }

    suspend fun login(email: String, password: String): Result<UserInfo> {
        return try {
            supabaseClient.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            val user = supabaseClient.auth.currentUserOrNull()
                ?: return Result.failure(Exception("User is null after login"))
            Result.success(user)
        } catch (e: Exception) {
            Timber.e(e, "Login error")
            Result.failure(e)
        }
    }

    suspend fun signInWithGoogle(idToken: String): Result<UserInfo> {
        return try {
            supabaseClient.auth.signInWith(IDToken) {
                this.idToken = idToken
                provider = Google
            }
            val user = supabaseClient.auth.currentUserOrNull()
                ?: return Result.failure(Exception("User is null after Google sign-in"))
            Result.success(user)
        } catch (e: Exception) {
            Timber.e(e, "Google sign-in error")
            Result.failure(e)
        }
    }

    suspend fun sendEmailVerification(): Result<Unit> {
        return try {
            val email = supabaseClient.auth.currentUserOrNull()?.email
                ?: return Result.failure(Exception("No authenticated user"))
            supabaseClient.auth.resendEmail(OtpType.Email.SIGNUP, email)
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Email verification send error")
            Result.failure(e)
        }
    }

    suspend fun reloadUser(): Result<UserInfo?> {
        return try {
            val session = supabaseClient.auth.currentSessionOrNull()
            if (session == null) {
                Result.success(null)
            } else {
                Result.success(supabaseClient.auth.retrieveUserForCurrentSession(updateSession = true))
            }
        } catch (e: Exception) {
            Timber.e(e, "User reload error")
            Result.failure(e)
        }
    }

    suspend fun logout(): Result<Unit> {
        return try {
            supabaseClient.auth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Logout error")
            Result.failure(e)
        }
    }

    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            Timber.d("AuthDataSource: Sending password reset email to: $email")
            supabaseClient.auth.resetPasswordForEmail(email)
            Timber.d("AuthDataSource: Password reset email sent successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "AuthDataSource: Password reset email error - ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> {
        return try {
            if (supabaseClient.auth.currentSessionOrNull() == null) {
                return Result.failure(Exception("No authenticated user"))
            }

            val email = supabaseClient.auth.currentUserOrNull()?.email
                ?: return Result.failure(Exception("Current password verification is only available for email/password accounts"))

            // Re-authenticate with the current password so incorrect current passwords are rejected.
            supabaseClient.auth.signInWith(Email) {
                this.email = email
                this.password = currentPassword
            }

            supabaseClient.auth.modifyUser {
                this.password = newPassword
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Password change error")
            Result.failure(e)
        }
    }

    fun getCurrentUserEmail(): String? {
        return supabaseClient.auth.currentUserOrNull()?.email
    }

    fun getCurrentUser(): UserInfo? = supabaseClient.auth.currentUserOrNull()
}
