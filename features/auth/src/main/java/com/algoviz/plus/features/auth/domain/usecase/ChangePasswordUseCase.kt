package com.algoviz.plus.features.auth.domain.usecase

import com.algoviz.plus.features.auth.domain.repository.AuthRepository
import javax.inject.Inject

class ChangePasswordUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(currentPassword: String, newPassword: String): Result<Unit> {
        return repository.changePassword(currentPassword, newPassword)
    }
}
