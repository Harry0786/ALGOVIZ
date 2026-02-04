package com.algoviz.plus.features.auth.domain.usecase

import com.algoviz.plus.features.auth.domain.model.User
import com.algoviz.plus.features.auth.domain.repository.AuthRepository
import javax.inject.Inject

class ReloadUserUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(): Result<User?> {
        return repository.reloadUser()
    }
}
