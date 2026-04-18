package com.algoviz.plus.features.auth.data.remote

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.OtpType
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.Google
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.gotrue.providers.builtin.IDToken
import io.github.jan.supabase.gotrue.SessionStatus
import io.github.jan.supabase.gotrue.user.UserInfo
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject

data class RegistrationOutcome(
    val user: UserInfo?,
    val requiresEmailVerification: Boolean
)

class SupabaseAuthDataSource @Inject constructor(
    private val supabaseClient: SupabaseClient
) {
    companion object {
        private const val PASSWORD_RESET_REDIRECT_URL = "algovizplus://password-reset"
    }

    fun observeAuthState(): Flow<UserInfo?> {
        var lastKnownUser: UserInfo? = supabaseClient.auth.currentUserOrNull()

        return supabaseClient.auth.sessionStatus.map { status ->
            val currentUser = supabaseClient.auth.currentUserOrNull()
            val resolvedUser = when (status) {
                is SessionStatus.Authenticated -> {
                    // Session is authenticated, use the user from session or fallback to current user
                    status.session.user ?: currentUser ?: lastKnownUser
                }
                is SessionStatus.LoadingFromStorage -> {
                    // Session is loading from storage, keep the last authenticated user
                    currentUser ?: lastKnownUser
                }
                is SessionStatus.NotAuthenticated -> {
                    null
                }
                else -> {
                    // Some transient states can occur while a valid current user still exists.
                    currentUser ?: lastKnownUser
                }
            }

            if (resolvedUser != null) {
                lastKnownUser = resolvedUser
            } else if (status is SessionStatus.NotAuthenticated) {
                lastKnownUser = null
            }

            Timber.d(
                "Auth State - ${status::class.simpleName}, current=${currentUser?.id ?: "null"}, " +
                    "emitting=${resolvedUser?.id ?: "null"}, lastKnown=${lastKnownUser?.id ?: "null"}"
            )

            resolvedUser
        }
            .debounce(300) // Debounce for 300ms to prevent rapid navigation on transient states
            .distinctUntilChanged { old, new -> old?.id == new?.id }
    }

    suspend fun register(email: String, password: String): Result<RegistrationOutcome> {
        return try {
            supabaseClient.auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }
            val user = supabaseClient.auth.currentUserOrNull()
            Result.success(
                RegistrationOutcome(
                    user = user,
                    requiresEmailVerification = user == null
                )
            )
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
            val message = e.message.orEmpty()
            if (message.contains("Authorization=[Bearer ]", ignoreCase = true)) {
                Result.failure(
                    IllegalStateException(
                        "Build misconfigured: SUPABASE_KEY is missing in this APK. Install the latest release build."
                    )
                )
            } else {
                Result.failure(e)
            }
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
            val normalizedEmail = email.trim()
            Timber.d("AuthDataSource: Sending password reset email to: $normalizedEmail")

            val resetResult = runCatching {
                supabaseClient.auth.resetPasswordForEmail(
                    email = normalizedEmail,
                    redirectUrl = PASSWORD_RESET_REDIRECT_URL
                )
            }

            if (resetResult.isSuccess) {
                Timber.d("AuthDataSource: Password reset email sent successfully (with redirect URL)")
                return Result.success(Unit)
            }

            val firstError = resetResult.exceptionOrNull()!!
            val firstMessage = firstError.message.orEmpty()

            // If the custom app deep-link isn't whitelisted, retry with Supabase default redirect.
            val isRedirectIssue =
                (firstMessage.contains("redirect_to", ignoreCase = true) && firstMessage.contains("not allowed", ignoreCase = true)) ||
                    firstMessage.contains("redirect", ignoreCase = true)

            if (isRedirectIssue) {
                Timber.w("AuthDataSource: redirect URL not allowed, retrying password reset with default redirect")
                runCatching {
                    supabaseClient.auth.resetPasswordForEmail(email = normalizedEmail)
                }.onSuccess {
                    Timber.d("AuthDataSource: Password reset email sent successfully (default redirect)")
                    return Result.success(Unit)
                }.onFailure { retryError ->
                    throw retryError
                }
            }

            throw firstError
        } catch (e: Exception) {
            val message = e.message.orEmpty()
            Timber.e(e, "AuthDataSource: Password reset email error - $message")

            when {
                message.contains("Authorization=[Bearer ]", ignoreCase = true) -> {
                    Result.failure(
                        IllegalStateException(
                            "Build misconfigured: SUPABASE_KEY is missing in this APK. Install the latest release build."
                        )
                    )
                }

                message.contains("redirect_to", ignoreCase = true) && message.contains("not allowed", ignoreCase = true) -> {
                    Result.failure(
                        IllegalStateException(
                            "Password reset redirect URL is not allowed. Please update Supabase Auth redirect settings."
                        )
                    )
                }

                message.contains("rate limit", ignoreCase = true) ||
                    message.contains("too many", ignoreCase = true) ||
                    message.contains("for security purposes", ignoreCase = true) ||
                    message.contains("wait", ignoreCase = true) -> {
                    Result.failure(
                        IllegalStateException(
                            "Too many reset attempts. Please wait a few minutes and try again."
                        )
                    )
                }

                else -> Result.failure(e)
            }
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

    suspend fun updatePassword(newPassword: String): Result<Unit> {
        return try {
            if (supabaseClient.auth.currentSessionOrNull() == null) {
                return Result.failure(Exception("No authenticated user"))
            }

            supabaseClient.auth.modifyUser {
                this.password = newPassword
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Password update error")
            Result.failure(e)
        }
    }

    fun getCurrentUserEmail(): String? {
        return supabaseClient.auth.currentUserOrNull()?.email
    }

    fun getCurrentUser(): UserInfo? = supabaseClient.auth.currentUserOrNull()

    fun isGoogleSignInUser(): Boolean {
        val userInfo = supabaseClient.auth.currentUserOrNull() ?: return false
        val identities = userInfo.identities ?: emptyList()
        return identities.any { identity -> identity.provider == "google" }
    }
}
