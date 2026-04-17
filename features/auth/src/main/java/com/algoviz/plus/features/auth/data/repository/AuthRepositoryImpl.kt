package com.algoviz.plus.features.auth.data.repository

import com.algoviz.plus.features.auth.data.mapper.AuthErrorMapper
import com.algoviz.plus.features.auth.data.mapper.AuthMapper
import com.algoviz.plus.features.auth.data.remote.SupabaseAuthDataSource
import com.algoviz.plus.features.auth.domain.model.AuthError
import com.algoviz.plus.features.auth.domain.model.User
import com.algoviz.plus.features.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val dataSource: SupabaseAuthDataSource
) : AuthRepository {
    
    override fun observeAuthState(): Flow<User?> {
        return dataSource.observeAuthState().map { supabaseUser ->
            AuthMapper.mapSupabaseUserToDomainOrNull(supabaseUser)
        }
    }
    
    override suspend fun register(email: String, password: String): Result<User> {
        if (!isValidEmail(email)) {
            return Result.failure(Exception(AuthError.InvalidEmail().message))
        }
        
        if (!isValidPassword(password)) {
            return Result.failure(Exception(AuthError.WeakPassword().message))
        }
        
        return dataSource.register(email, password)
            .mapCatching { supabaseUser ->
                AuthMapper.mapSupabaseUserToDomain(supabaseUser)
            }
            .recoverCatching { exception ->
                throw Exception(AuthErrorMapper.mapExceptionToAuthError(exception).message)
            }
    }
    
    override suspend fun login(email: String, password: String): Result<User> {
        if (!isValidEmail(email)) {
            return Result.failure(Exception(AuthError.InvalidEmail().message))
        }
        
        return dataSource.login(email, password)
            .mapCatching { supabaseUser ->
                AuthMapper.mapSupabaseUserToDomain(supabaseUser)
            }
            .recoverCatching { exception ->
                throw Exception(AuthErrorMapper.mapExceptionToAuthError(exception).message)
            }
    }
    
    override suspend fun signInWithGoogle(idToken: String): Result<User> {
        return dataSource.signInWithGoogle(idToken)
            .mapCatching { supabaseUser ->
                AuthMapper.mapSupabaseUserToDomain(supabaseUser)
            }
            .recoverCatching { exception ->
                throw Exception(AuthErrorMapper.mapExceptionToAuthError(exception).message)
            }
    }
    
    override suspend fun sendEmailVerification(): Result<Unit> {
        return dataSource.sendEmailVerification()
            .recoverCatching { exception ->
                throw Exception(AuthErrorMapper.mapExceptionToAuthError(exception).message)
            }
    }
    
    override suspend fun reloadUser(): Result<User?> {
        return dataSource.reloadUser()
            .mapCatching { supabaseUser ->
                AuthMapper.mapSupabaseUserToDomainOrNull(supabaseUser)
            }
            .recoverCatching { exception ->
                throw Exception(AuthErrorMapper.mapExceptionToAuthError(exception).message)
            }
    }
    
    override suspend fun logout(): Result<Unit> {
        return dataSource.logout()
            .recoverCatching { exception ->
                throw Exception(AuthErrorMapper.mapExceptionToAuthError(exception).message)
            }
    }
    
    override fun getCurrentUser(): User? {
        return AuthMapper.mapSupabaseUserToDomainOrNull(dataSource.getCurrentUser())
    }
    
    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        Timber.d("AuthRepository: Validating email: $email")
        if (!isValidEmail(email)) {
            Timber.e("AuthRepository: Invalid email format")
            return Result.failure(Exception(AuthError.InvalidEmail().message))
        }
        
        Timber.d("AuthRepository: Email validated, calling data source")
        return dataSource.sendPasswordResetEmail(email)
            .recoverCatching { exception ->
                Timber.e(exception, "AuthRepository: Error from data source")
                throw Exception(AuthErrorMapper.mapExceptionToAuthError(exception).message)
            }
    }
    
    override suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> {
        if (!isValidPassword(newPassword)) {
            return Result.failure(Exception(AuthError.WeakPassword().message))
        }
        
        return dataSource.changePassword(currentPassword, newPassword)
            .recoverCatching { exception ->
                throw Exception(AuthErrorMapper.mapExceptionToAuthError(exception).message)
            }
    }
    
    override fun getCurrentUserEmail(): String? {
        return dataSource.getCurrentUserEmail()
    }
    
    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    
    private fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }
}
