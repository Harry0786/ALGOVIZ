package com.algoviz.plus.features.auth.presentation.state

import com.algoviz.plus.features.auth.domain.model.User

sealed class AuthUiState {
    data object Idle : AuthUiState()
    data object Loading : AuthUiState()
    data class Authenticated(val user: User) : AuthUiState()
    data object Unauthenticated : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}
