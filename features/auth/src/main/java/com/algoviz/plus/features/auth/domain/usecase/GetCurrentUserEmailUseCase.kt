package com.algoviz.plus.features.auth.domain.usecase

import com.algoviz.plus.features.auth.domain.repository.AuthRepository
import javax.inject.Inject

class GetCurrentUserEmailUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    operator fun invoke(): String? {
        return repository.getCurrentUserEmail()
    }
}
