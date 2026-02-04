package com.algoviz.plus.features.auth.domain.model

data class User(
    val id: String,
    val email: String,
    val isEmailVerified: Boolean
)
