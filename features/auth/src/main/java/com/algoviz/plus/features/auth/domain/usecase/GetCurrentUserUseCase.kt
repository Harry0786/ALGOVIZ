package com.algoviz.plus.features.auth.domain.usecase

import com.algoviz.plus.features.auth.domain.model.User
import com.algoviz.plus.features.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCurrentUserUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    operator fun invoke(): Flow<User?> {
        return repository.observeAuthState()
    }
    
    fun sync(): User? {
        return repository.getCurrentUser()
    }
}
