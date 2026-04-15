package com.algoviz.plus.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.algoviz.plus.features.auth.presentation.navigation.AuthRoute
import com.algoviz.plus.features.auth.presentation.navigation.authNavGraph
import com.algoviz.plus.features.auth.presentation.state.AuthUiState
import com.algoviz.plus.features.auth.presentation.viewmodel.AuthViewModel
import com.algoviz.plus.ui.placeholder.PlaceholderScreen
import com.algoviz.plus.update.AppUpdateDialog

private const val MIN_SPLASH_DURATION_MS = 1200L
private const val SPLASH_FRAME_STEP_MS = 320L

@Composable
fun RootNavHost(
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()
    var splashFinished by remember { mutableStateOf(false) }

    // Keep a short splash to avoid feeling delayed at startup.
    LaunchedEffect(Unit) {
        delay(MIN_SPLASH_DURATION_MS)
        splashFinished = true
    }

    val showSplash = !splashFinished || authState is AuthUiState.Idle || authState is AuthUiState.Loading

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

    Crossfade(
        targetState = showSplash,
        animationSpec = tween(
            durationMillis = 240,
            easing = FastOutSlowInEasing
        ),
        label = "SplashToAppCrossfade"
    ) { splashVisible ->
        if (splashVisible) {
            SplashScreen()
        } else {
            NavHost(
                navController = navController,
                startDestination = startDestination,
                enterTransition = { EnterTransition.None },
                exitTransition = { ExitTransition.None },
                popEnterTransition = { EnterTransition.None },
                popExitTransition = { ExitTransition.None }
            ) {
                authNavGraph(
                    backgroundRes = com.algoviz.plus.R.drawable.auth_bg1,
                    logoRes = com.algoviz.plus.R.drawable.auth_logo_circle,
                    navController = navController,
                    onAuthSuccess = {
                        // Navigation handled by LaunchedEffect above
                    }
                )

                composable("main") {
                    PlaceholderScreen(
                        onSignOutClick = {
                            authViewModel.logout()
                        },
                        authViewModel = authViewModel
                    )
                }
            }

            // Overlay: show update dialog if a newer version is available
            AppUpdateDialog()
        }
    }
}

@Composable
private fun SplashScreen() {
    var showFrame2 by remember { mutableStateOf(false) }
    var showFinalFrame by remember { mutableStateOf(false) }

    val frame2Alpha by animateFloatAsState(
        targetValue = if (showFrame2) 1f else 0f,
        animationSpec = tween(
            durationMillis = 220,
            easing = FastOutSlowInEasing
        ),
        label = "SplashFrame2Alpha"
    )
    val finalFrameAlpha by animateFloatAsState(
        targetValue = if (showFinalFrame) 1f else 0f,
        animationSpec = tween(
            durationMillis = 220,
            easing = FastOutSlowInEasing
        ),
        label = "SplashFinalFrameAlpha"
    )

    // Layered fades prevent hard image swaps and keep the sequence smooth.
    LaunchedEffect(Unit) {
        delay(SPLASH_FRAME_STEP_MS)
        showFrame2 = true
        delay(SPLASH_FRAME_STEP_MS)
        showFinalFrame = true
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = com.algoviz.plus.R.drawable.sp1),
            contentDescription = "Splash Screen Frame 1",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )
        Image(
            painter = painterResource(id = com.algoviz.plus.R.drawable.sp2),
            contentDescription = "Splash Screen Frame 2",
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(alpha = frame2Alpha),
            contentScale = ContentScale.FillBounds
        )
        Image(
            painter = painterResource(id = com.algoviz.plus.R.drawable.splash_screen),
            contentDescription = "Splash Screen",
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(alpha = finalFrameAlpha),
            contentScale = ContentScale.FillBounds
        )
    }
}



