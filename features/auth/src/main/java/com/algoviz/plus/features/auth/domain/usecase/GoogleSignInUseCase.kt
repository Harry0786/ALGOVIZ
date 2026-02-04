package com.algoviz.plus.features.auth.domain.usecase

import com.algoviz.plus.features.auth.domain.model.User
import com.algoviz.plus.features.auth.domain.repository.AuthRepository
import javax.inject.Inject

class GoogleSignInUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(idToken: String): Result<User> {
        return repository.signInWithGoogle(idToken)
    }
}
