package com.algoviz.plus.features.auth.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.algoviz.plus.features.auth.presentation.screens.LoginScreen
import com.algoviz.plus.features.auth.presentation.screens.RegisterScreen
import com.algoviz.plus.features.auth.presentation.state.AuthUiState
import com.algoviz.plus.features.auth.presentation.viewmodel.AuthViewModel
import androidx.annotation.DrawableRes

sealed class AuthRoute(val route: String) {
    data object AuthGraph : AuthRoute("auth_graph")
    data object Login : AuthRoute("login")
    data object Register : AuthRoute("register")
}

fun NavGraphBuilder.authNavGraph(
    @DrawableRes logoRes: Int,
    navController: NavHostController,
    onAuthSuccess: () -> Unit
) {
    navigation(
        startDestination = AuthRoute.Login.route,
        route = AuthRoute.AuthGraph.route
    ) {
        composable(AuthRoute.Login.route) {
            LoginScreen(
                logoRes = logoRes,
                onNavigateToRegister = {
                    navController.navigate(AuthRoute.Register.route) {
                        popUpTo(AuthRoute.Login.route) {
                            inclusive = false
                        }
                    }
                }
            )
        }
        
        composable(AuthRoute.Register.route) {
            RegisterScreen(
                logoRes = logoRes,
                onNavigateToLogin = {
                    navController.navigate(AuthRoute.Login.route) {
                        popUpTo(AuthRoute.Register.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun AuthNavigation(
    @DrawableRes logoRes: Int,
    navController: NavHostController,
    onAuthSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    LaunchedEffect(uiState) {
        when (uiState) {
            is AuthUiState.Authenticated -> {
                onAuthSuccess()
            }
            else -> {
                // Stay on current auth screen
            }
        }
    }
    
    NavHost(
        navController = navController,
        startDestination = AuthRoute.AuthGraph.route
    ) {
        authNavGraph(            logoRes = logoRes,            navController = navController,
            onAuthSuccess = onAuthSuccess
        )
    }
}
