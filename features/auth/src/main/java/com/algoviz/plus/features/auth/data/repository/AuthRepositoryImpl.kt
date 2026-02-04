package com.algoviz.plus.features.auth.data.repository

import com.algoviz.plus.features.auth.data.mapper.AuthErrorMapper
import com.algoviz.plus.features.auth.data.mapper.AuthMapper
import com.algoviz.plus.features.auth.data.remote.FirebaseAuthDataSource
import com.algoviz.plus.features.auth.domain.model.AuthError
import com.algoviz.plus.features.auth.domain.model.User
import com.algoviz.plus.features.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val dataSource: FirebaseAuthDataSource
) : AuthRepository {
    
    override fun observeAuthState(): Flow<User?> {
        return dataSource.observeAuthState().map { firebaseUser ->
            AuthMapper.mapFirebaseUserToDomainOrNull(firebaseUser)
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
            .mapCatching { firebaseUser ->
                AuthMapper.mapFirebaseUserToDomain(firebaseUser)
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
            .mapCatching { firebaseUser ->
                AuthMapper.mapFirebaseUserToDomain(firebaseUser)
            }
            .recoverCatching { exception ->
                throw Exception(AuthErrorMapper.mapExceptionToAuthError(exception).message)
            }
    }
    
    override suspend fun signInWithGoogle(idToken: String): Result<User> {
        return dataSource.signInWithGoogle(idToken)
            .mapCatching { firebaseUser ->
                AuthMapper.mapFirebaseUserToDomain(firebaseUser)
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
            .mapCatching { firebaseUser ->
                AuthMapper.mapFirebaseUserToDomainOrNull(firebaseUser)
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
        return AuthMapper.mapFirebaseUserToDomainOrNull(dataSource.getCurrentUser())
    }
    
    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    
    private fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }
}
