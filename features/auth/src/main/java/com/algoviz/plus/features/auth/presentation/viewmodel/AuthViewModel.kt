package com.algoviz.plus.features.auth.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.algoviz.plus.features.auth.domain.usecase.GetCurrentUserUseCase
import com.algoviz.plus.features.auth.domain.usecase.GoogleSignInUseCase
import com.algoviz.plus.features.auth.domain.usecase.LoginUseCase
import com.algoviz.plus.features.auth.domain.usecase.LogoutUseCase
import com.algoviz.plus.features.auth.domain.usecase.RegisterUseCase
import com.algoviz.plus.features.auth.presentation.state.AuthUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val googleSignInUseCase: GoogleSignInUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()
    
    init {
        observeAuthState()
    }
    
    private fun observeAuthState() {
        viewModelScope.launch {
            getCurrentUserUseCase().collect { user ->
                _uiState.value = when {
                    user == null -> AuthUiState.Unauthenticated
                    else -> AuthUiState.Authenticated(user)
                }
            }
        }
    }
    
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            loginUseCase(email, password)
                .onSuccess { user ->
                    _uiState.value = AuthUiState.Authenticated(user)
                }
                .onFailure { error ->
                    Timber.e(error, "Login failed")
                    _uiState.value = AuthUiState.Error(error.message ?: "Login failed")
                }
        }
    }
    
    fun register(email: String, password: String, confirmPassword: String) {
        if (password != confirmPassword) {
            _uiState.value = AuthUiState.Error("Passwords do not match")
            return
        }
        
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            registerUseCase(email, password)
                .onSuccess { user ->
                    _uiState.value = AuthUiState.Authenticated(user)
                }
                .onFailure { error ->
                    Timber.e(error, "Registration failed")
                    _uiState.value = AuthUiState.Error(error.message ?: "Registration failed")
                }
        }
    }
    
    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            googleSignInUseCase(idToken)
                .onSuccess { user ->
                    _uiState.value = AuthUiState.Authenticated(user)
                }
                .onFailure { error ->
                    Timber.e(error, "Google sign-in failed")
                    _uiState.value = AuthUiState.Error(error.message ?: "Google sign-in failed")
                }
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            logoutUseCase()
                .onSuccess {
                    _uiState.value = AuthUiState.Unauthenticated
                }
                .onFailure { error ->
                    Timber.e(error, "Logout failed")
                }
        }
    }
    
    fun clearError() {
        if (_uiState.value is AuthUiState.Error) {
            _uiState.value = AuthUiState.Idle
        }
    }
}
