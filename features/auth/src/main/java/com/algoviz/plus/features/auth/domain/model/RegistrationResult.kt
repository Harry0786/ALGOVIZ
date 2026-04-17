package com.algoviz.plus.features.auth.domain.model

data class RegistrationResult(
    val user: User?,
    val requiresEmailVerification: Boolean
)