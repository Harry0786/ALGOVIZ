package com.algoviz.plus.features.auth.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.FirebaseNetworkException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject

class FirebaseAuthDataSource @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) {
    fun observeAuthState(): Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser)
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }
    
    suspend fun register(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user ?: return Result.failure(Exception("User is null after registration"))
            Result.success(user)
        } catch (e: FirebaseAuthWeakPasswordException) {
            Timber.e(e, "Weak password")
            Result.failure(e)
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Timber.e(e, "Invalid credentials")
            Result.failure(e)
        } catch (e: FirebaseAuthUserCollisionException) {
            Timber.e(e, "User collision")
            Result.failure(e)
        } catch (e: FirebaseNetworkException) {
            Timber.e(e, "Network error")
            Result.failure(e)
        } catch (e: Exception) {
            Timber.e(e, "Registration error")
            Result.failure(e)
        }
    }
    
    suspend fun login(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val user = result.user ?: return Result.failure(Exception("User is null after login"))
            Result.success(user)
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Timber.e(e, "Invalid credentials")
            Result.failure(e)
        } catch (e: FirebaseAuthInvalidUserException) {
            Timber.e(e, "Invalid user")
            Result.failure(e)
        } catch (e: FirebaseNetworkException) {
            Timber.e(e, "Network error")
            Result.failure(e)
        } catch (e: Exception) {
            Timber.e(e, "Login error")
            Result.failure(e)
        }
    }
    
    suspend fun signInWithGoogle(idToken: String): Result<FirebaseUser> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = firebaseAuth.signInWithCredential(credential).await()
            val user = result.user ?: return Result.failure(Exception("User is null after Google sign-in"))
            Result.success(user)
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Timber.e(e, "Invalid Google credentials")
            Result.failure(e)
        } catch (e: FirebaseNetworkException) {
            Timber.e(e, "Network error")
            Result.failure(e)
        } catch (e: Exception) {
            Timber.e(e, "Google sign-in error")
            Result.failure(e)
        }
    }
    
    suspend fun sendEmailVerification(): Result<Unit> {
        return try {
            val user = firebaseAuth.currentUser ?: return Result.failure(Exception("No authenticated user"))
            user.sendEmailVerification().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Email verification send error")
            Result.failure(e)
        }
    }
    
    suspend fun reloadUser(): Result<FirebaseUser?> {
        return try {
            val user = firebaseAuth.currentUser
            user?.reload()?.await()
            Result.success(firebaseAuth.currentUser)
        } catch (e: Exception) {
            Timber.e(e, "User reload error")
            Result.failure(e)
        }
    }
    
    suspend fun logout(): Result<Unit> {
        return try {
            firebaseAuth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Logout error")
            Result.failure(e)
        }
    }
    
    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            Timber.d("FirebaseAuthDataSource: Sending password reset email to: $email")
            firebaseAuth.sendPasswordResetEmail(email).await()
            Timber.d("FirebaseAuthDataSource: Password reset email sent successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "FirebaseAuthDataSource: Password reset email error - ${e.message}")
            Result.failure(e)
        }
    }
    
    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> {
        return try {
            val user = firebaseAuth.currentUser ?: return Result.failure(Exception("No authenticated user"))
            val email = user.email ?: return Result.failure(Exception("User email is null"))
            
            // Re-authenticate user before changing password
            val credential = com.google.firebase.auth.EmailAuthProvider.getCredential(email, currentPassword)
            user.reauthenticate(credential).await()
            
            // Change password
            user.updatePassword(newPassword).await()
            Result.success(Unit)
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Timber.e(e, "Invalid current password")
            Result.failure(Exception("Invalid current password"))
        } catch (e: FirebaseAuthWeakPasswordException) {
            Timber.e(e, "Weak new password")
            Result.failure(Exception("Password is too weak"))
        } catch (e: Exception) {
            Timber.e(e, "Password change error")
            Result.failure(e)
        }
    }
    
    fun getCurrentUserEmail(): String? {
        return firebaseAuth.currentUser?.email
    }
    
    fun getCurrentUser(): FirebaseUser? = firebaseAuth.currentUser
}
