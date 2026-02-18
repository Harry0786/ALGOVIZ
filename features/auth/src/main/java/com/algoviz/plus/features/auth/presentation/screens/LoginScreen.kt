package com.algoviz.plus.features.auth.presentation.screens

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import android.widget.Toast
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.algoviz.plus.features.auth.presentation.components.AppLogo
import com.algoviz.plus.features.auth.presentation.components.EmailTextField
import com.algoviz.plus.features.auth.presentation.components.GoogleSignInButton
import com.algoviz.plus.features.auth.presentation.components.PasswordTextField
import com.algoviz.plus.features.auth.presentation.components.PrimaryButton
import com.algoviz.plus.features.auth.presentation.state.AuthUiState
import com.algoviz.plus.features.auth.presentation.viewmodel.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import timber.log.Timber
import androidx.annotation.DrawableRes

@Composable
fun LoginScreen(
    @DrawableRes logoRes: Int,
    onNavigateToRegister: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                account.idToken?.let { idToken ->
                    viewModel.signInWithGoogle(idToken)
                }
            } catch (e: ApiException) {
                Timber.e(e, "Google sign-in failed")
            }
        }
    }

    LaunchedEffect(uiState) {
        when (uiState) {
            is AuthUiState.Error -> {
                val message = (uiState as AuthUiState.Error).message
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                snackbarHostState.showSnackbar(message)
                viewModel.clearError()
            }
            is AuthUiState.PasswordResetEmailSent -> {
                val message = "Password reset email sent! Check your inbox."
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                snackbarHostState.showSnackbar(message)
                showForgotPasswordDialog = false
                viewModel.clearError()
            }
            else -> {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A1344),
                        Color(0xFF2D1B69),
                        Color(0xFF3D2080)
                    )
                )
            )
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = Color.Transparent
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(60.dp))
                
                // Logo
                AppLogo(
                    logoRes = logoRes,
                    size = 80.dp
                )
                
                Spacer(modifier = Modifier.height(40.dp))
                
                Text(
                    text = "Welcome Back",
                    style = MaterialTheme.typography.displaySmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Sign in to continue your learning journey",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )

            Spacer(modifier = Modifier.height(56.dp))

            EmailTextField(
                value = email,
                onValueChange = { email = it },
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                imeAction = ImeAction.Next
            )

            Spacer(modifier = Modifier.height(20.dp))

            PasswordTextField(
                value = password,
                onValueChange = { password = it },
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        if (email.isNotBlank() && password.isNotBlank()) {
                            viewModel.login(email, password)
                        }
                    }
                )
            )

            Spacer(modifier = Modifier.height(8.dp))
            
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {
                TextButton(onClick = { showForgotPasswordDialog = true }) {
                    Text(
                        text = "Forgot Password?",
                        color = Color(0xFF5EEAD4),
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            PrimaryButton(
                text = "Sign In",
                onClick = { viewModel.login(email, password) },
                enabled = email.isNotBlank() && password.isNotBlank(),
                isLoading = uiState is AuthUiState.Loading
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "OR",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.5f),
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(24.dp))

            GoogleSignInButton(
                onClick = {
                    val webClientId = "139161182086-rk5tes8e2hdhvkc698sd1rkb7i7j0lrf.apps.googleusercontent.com"
                    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(webClientId)
                        .requestEmail()
                        .build()
                    val googleSignInClient = GoogleSignIn.getClient(context, gso)
                    googleSignInLauncher.launch(googleSignInClient.signInIntent)
                },
                enabled = uiState !is AuthUiState.Loading
            )

            Spacer(modifier = Modifier.height(40.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Don't have an account?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f)
                )
                TextButton(onClick = onNavigateToRegister) {
                    Text(
                        text = "Sign Up",
                        color = Color(0xFF5EEAD4),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }
    
    if (showForgotPasswordDialog) {
        ForgotPasswordDialog(
            onDismiss = { showForgotPasswordDialog = false },
            onSendResetEmail = { resetEmail ->
                Timber.d("LoginScreen: Forgot password clicked with email: $resetEmail")
                viewModel.sendPasswordResetEmail(resetEmail)
            },
            isLoading = uiState is AuthUiState.Loading
        )
    }
}

@Composable
private fun ForgotPasswordDialog(
    onDismiss: () -> Unit,
    onSendResetEmail: (String) -> Unit,
    isLoading: Boolean
) {
    var resetEmail by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Reset Password",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )
        },
        text = {
            Column {
                Text(
                    text = "Enter your email address and we'll send you a link to reset your password.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                EmailTextField(
                    value = resetEmail,
                    onValueChange = { 
                        resetEmail = it
                        errorMessage = null
                    },
                    imeAction = ImeAction.Done
                )
                
                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Red
                    )
                }
            }
        },
        confirmButton = {
            PrimaryButton(
                text = "Send Reset Email",
                onClick = {
                    when {
                        resetEmail.isBlank() -> {
                            errorMessage = "Please enter your email address"
                        }
                        !android.util.Patterns.EMAIL_ADDRESS.matcher(resetEmail).matches() -> {
                            errorMessage = "Please enter a valid email address"
                        }
                        else -> {
                            errorMessage = null
                            onSendResetEmail(resetEmail)
                        }
                    }
                },
                enabled = !isLoading,
                isLoading = isLoading,
                modifier = Modifier.fillMaxWidth()
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isLoading) {
                Text(
                    text = "Cancel",
                    color = Color(0xFF5EEAD4)
                )
            }
        },
        containerColor = Color(0xFF1A1344),
        shape = RoundedCornerShape(16.dp)
    )
}

