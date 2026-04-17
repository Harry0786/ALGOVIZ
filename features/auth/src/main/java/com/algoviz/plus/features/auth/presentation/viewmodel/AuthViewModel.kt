package com.algoviz.plus.features.auth.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.algoviz.plus.core.datastore.PreferencesManager
import com.algoviz.plus.features.auth.domain.usecase.ChangePasswordUseCase
import com.algoviz.plus.features.auth.domain.usecase.GetCurrentUserEmailUseCase
import com.algoviz.plus.features.auth.domain.usecase.GetCurrentUserUseCase
import com.algoviz.plus.features.auth.domain.usecase.GoogleSignInUseCase
import com.algoviz.plus.features.auth.domain.usecase.LoginUseCase
import com.algoviz.plus.features.auth.domain.usecase.LogoutUseCase
import com.algoviz.plus.features.auth.domain.usecase.RegisterUseCase
import com.algoviz.plus.features.auth.domain.usecase.SendPasswordResetEmailUseCase
import com.algoviz.plus.features.auth.domain.usecase.UpdatePasswordUseCase
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
    private val logoutUseCase: LogoutUseCase,
    private val sendPasswordResetEmailUseCase: SendPasswordResetEmailUseCase,
    private val updatePasswordUseCase: UpdatePasswordUseCase,
    private val changePasswordUseCase: ChangePasswordUseCase,
    private val getCurrentUserEmailUseCase: GetCurrentUserEmailUseCase,
    private val preferencesManager: PreferencesManager
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
                    // Save email to DataStore
                    user.email?.let { userEmail ->
                        preferencesManager.saveProfileEmail(userEmail)
                    }
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
                .onSuccess { result ->
                    preferencesManager.saveProfileEmail(email)

                    result.user?.let { user ->
                        preferencesManager.saveProfileEmail(user.email)
                        _uiState.value = AuthUiState.Authenticated(user)
                        return@onSuccess
                    }

                    if (result.requiresEmailVerification) {
                        _uiState.value = AuthUiState.EmailVerificationRequired(
                            "Account created. Check your email to verify your account before signing in."
                        )
                        return@onSuccess
                    }

                    _uiState.value = AuthUiState.Error("Registration completed, but no user session was created.")
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
                    // Save email to DataStore
                    user.email?.let { userEmail ->
                        preferencesManager.saveProfileEmail(userEmail)
                    }
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
        if (_uiState.value is AuthUiState.Error ||
            _uiState.value is AuthUiState.EmailVerificationRequired ||
            _uiState.value is AuthUiState.PasswordResetEmailSent ||
            _uiState.value is AuthUiState.PasswordChanged) {
            _uiState.value = AuthUiState.Idle
        }
    }
    
    fun sendPasswordResetEmail(email: String) {
        Timber.d("Sending password reset email to: $email")
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            Timber.d("State set to Loading")
            sendPasswordResetEmailUseCase(email)
                .onSuccess {
                    Timber.d("Password reset email sent successfully")
                    _uiState.value = AuthUiState.PasswordResetEmailSent
                }
                .onFailure { error ->
                    Timber.e(error, "Password reset email failed")
                    _uiState.value = AuthUiState.Error(error.message ?: "Failed to send password reset email")
                }
        }
    }

    fun updatePassword(newPassword: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            updatePasswordUseCase(newPassword)
                .onSuccess {
                    _uiState.value = AuthUiState.PasswordChanged
                }
                .onFailure { error ->
                    Timber.e(error, "Recovery password update failed")
                    _uiState.value = AuthUiState.Error(error.message ?: "Failed to update password")
                }
        }
    }
    
    fun changePassword(currentPassword: String, newPassword: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            changePasswordUseCase(currentPassword, newPassword)
                .onSuccess {
                    _uiState.value = AuthUiState.PasswordChanged
                }
                .onFailure { error ->
                    Timber.e(error, "Password change failed")
                    _uiState.value = AuthUiState.Error(error.message ?: "Failed to change password")
                }
        }
    }
}
