package com.algoviz.plus.features.auth.domain.usecase

import com.algoviz.plus.features.auth.domain.repository.AuthRepository
import javax.inject.Inject

class UpdatePasswordUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(newPassword: String): Result<Unit> {
        return repository.updatePassword(newPassword)
    }
}