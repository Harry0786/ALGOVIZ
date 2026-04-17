package com.algoviz.plus.features.auth.domain.repository

import com.algoviz.plus.features.auth.domain.model.AuthError
import com.algoviz.plus.features.auth.domain.model.RegistrationResult
import com.algoviz.plus.features.auth.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun observeAuthState(): Flow<User?>
    
    suspend fun register(email: String, password: String): Result<RegistrationResult>
    
    suspend fun login(email: String, password: String): Result<User>
    
    suspend fun signInWithGoogle(idToken: String): Result<User>
    
    suspend fun sendEmailVerification(): Result<Unit>
    
    suspend fun reloadUser(): Result<User?>
    
    suspend fun logout(): Result<Unit>
    
    fun getCurrentUser(): User?
    
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>
    
    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit>

    suspend fun updatePassword(newPassword: String): Result<Unit>
    
    fun getCurrentUserEmail(): String?
}
