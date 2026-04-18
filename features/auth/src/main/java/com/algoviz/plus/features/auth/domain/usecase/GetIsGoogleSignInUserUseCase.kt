package com.algoviz.plus.features.auth.domain.usecase

import com.algoviz.plus.features.auth.domain.repository.AuthRepository
import javax.inject.Inject

class GetIsGoogleSignInUserUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    operator fun invoke(): Boolean {
        return repository.isGoogleSignInUser()
    }
}
