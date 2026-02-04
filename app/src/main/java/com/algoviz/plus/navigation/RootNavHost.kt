package com.algoviz.plus.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.algoviz.plus.features.auth.presentation.navigation.AuthRoute
import com.algoviz.plus.features.auth.presentation.navigation.authNavGraph
import com.algoviz.plus.features.auth.presentation.state.AuthUiState
import com.algoviz.plus.features.auth.presentation.viewmodel.AuthViewModel
import com.algoviz.plus.ui.placeholder.PlaceholderScreen

@Composable
fun RootNavHost(
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()

    // Show loading screen while checking auth state
    if (authState is AuthUiState.Idle || authState is AuthUiState.Loading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF1A1344)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color(0xFF5EEAD4))
        }
        return
    }

    // Determine start destination based on auth state
    val startDestination = when (authState) {
        is AuthUiState.Authenticated -> "main"
        else -> AuthRoute.AuthGraph.route
    }

    // Handle navigation based on auth state changes
    LaunchedEffect(authState) {
        when (authState) {
            is AuthUiState.Authenticated -> {
                if (navController.currentDestination?.route != "main") {
                    navController.navigate("main") {
                        popUpTo(navController.graph.startDestinationId) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            }
            is AuthUiState.Unauthenticated -> {
                val currentRoute = navController.currentDestination?.route
                if (currentRoute == "main") {
                    navController.navigate(AuthRoute.AuthGraph.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            }
            else -> {
                // Idle or Loading state handled above
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        authNavGraph(
            logoRes = com.algoviz.plus.R.mipmap.ic_launcher,
            navController = navController,
            onAuthSuccess = {
                // Navigation handled by LaunchedEffect above
            }
        )

        composable("main") {
            PlaceholderScreen(authViewModel = authViewModel)
        }
    }
}



