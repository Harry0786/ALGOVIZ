package com.algoviz.plus.features.auth.domain.usecase

import com.algoviz.plus.features.auth.domain.model.User
import com.algoviz.plus.features.auth.domain.repository.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<User> {
        return repository.login(email, password)
    }
}
