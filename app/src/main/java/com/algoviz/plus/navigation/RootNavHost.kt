package com.algoviz.plus.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.algoviz.plus.features.auth.presentation.navigation.AuthRoute
import com.algoviz.plus.features.auth.presentation.navigation.authNavGraph
import com.algoviz.plus.features.auth.presentation.state.AuthUiState
import com.algoviz.plus.features.auth.presentation.viewmodel.AuthViewModel
import com.algoviz.plus.ui.algorithms.AlgorithmVisualizationScreen
import com.algoviz.plus.ui.algorithms.AlgorithmListScreen
import com.algoviz.plus.ui.home.HomeScreen
import com.algoviz.plus.ui.learn.LearnScreen
import com.algoviz.plus.ui.profile.ProfileEditScreen
import com.algoviz.plus.ui.profile.ProfileScreen
import com.algoviz.plus.ui.studyrooms.CreateRoomScreen
import com.algoviz.plus.ui.studyrooms.StudyRoomsScreen
import com.algoviz.plus.ui.studyrooms.chat.ChatRoomScreen
import com.algoviz.plus.update.AppUpdateDialog

private const val MIN_SPLASH_DURATION_MS = 1200L
private const val SPLASH_FRAME_STEP_MS = 320L
private const val ROUTE_MAIN = "main"
private const val ROUTE_PROFILE = "profile"
private const val ROUTE_PROFILE_EDIT = "profile/edit"
private const val ROUTE_LEARN = "learn"
private const val ROUTE_ALGORITHMS = "algorithms"
private const val ROUTE_STUDY_ROOMS = "study_rooms"
private const val ROUTE_CREATE_ROOM = "create_room"
private const val ROUTE_CHAT = "chat"
private const val ROUTE_VISUALIZATION = "visualization"
private const val ROUTE_CHAT_PATTERN = "$ROUTE_CHAT/{roomId}"
private const val ROUTE_VISUALIZATION_PATTERN = "$ROUTE_VISUALIZATION/{algorithmId}"
private const val DEFAULT_ALGORITHM_ID = "bubble_sort"

@Composable
fun RootNavHost(
    authViewModel: AuthViewModel = hiltViewModel(),
    isPasswordResetLink: Boolean = false
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()
    var splashFinished by remember { mutableStateOf(false) }
    var pendingResetFlow by remember { mutableStateOf(isPasswordResetLink) }

    // Keep a short splash to avoid feeling delayed at startup.
    LaunchedEffect(Unit) {
        delay(MIN_SPLASH_DURATION_MS)
        splashFinished = true
    }

    val showSplash = !splashFinished || authState is AuthUiState.Idle || authState is AuthUiState.Loading

    // Determine start destination based on auth state
    val startDestination = when (authState) {
        is AuthUiState.Authenticated -> ROUTE_MAIN
        else -> AuthRoute.AuthGraph.route
    }

    // Handle navigation based on auth state changes
    LaunchedEffect(authState, showSplash, currentRoute) {
        if (showSplash || currentRoute == null) return@LaunchedEffect

        when (authState) {
            is AuthUiState.Authenticated -> {
                if (pendingResetFlow) {
                    pendingResetFlow = false
                    if (currentRoute != AuthRoute.ResetPassword.route) {
                        navController.navigate(AuthRoute.ResetPassword.route) {
                            launchSingleTop = true
                        }
                    }
                } else if (currentRoute !in setOf(
                        ROUTE_MAIN,
                        ROUTE_PROFILE,
                        ROUTE_PROFILE_EDIT,
                        ROUTE_LEARN,
                        ROUTE_ALGORITHMS,
                        ROUTE_STUDY_ROOMS,
                        ROUTE_CREATE_ROOM,
                        ROUTE_CHAT_PATTERN,
                        ROUTE_VISUALIZATION_PATTERN
                    )) {
                    navController.navigate(ROUTE_MAIN) {
                        popUpTo(AuthRoute.AuthGraph.route) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            }
            is AuthUiState.Unauthenticated -> {
                if (currentRoute in setOf(
                        ROUTE_MAIN,
                        ROUTE_PROFILE,
                        ROUTE_PROFILE_EDIT,
                        ROUTE_LEARN,
                        ROUTE_ALGORITHMS,
                        ROUTE_STUDY_ROOMS,
                        ROUTE_CREATE_ROOM,
                        ROUTE_CHAT_PATTERN,
                        ROUTE_VISUALIZATION_PATTERN
                    )) {
                    navController.navigate(AuthRoute.AuthGraph.route) {
                        popUpTo(ROUTE_MAIN) {
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

                composable(ROUTE_MAIN) {
                    HomeScreen(
                        onVisualize = { navController.navigate(ROUTE_ALGORITHMS) },
                        onLearn = { navController.navigate(ROUTE_LEARN) },
                        onStudyRooms = { navController.navigate(ROUTE_STUDY_ROOMS) },
                        onProfileClick = { navController.navigate(ROUTE_PROFILE) }
                    )
                }

                composable(ROUTE_ALGORITHMS) {
                    AlgorithmListScreen(
                        onBackClick = { navController.popBackStack() },
                        onAlgorithmClick = { algorithmId ->
                            navController.navigate("$ROUTE_VISUALIZATION/$algorithmId")
                        }
                    )
                }

                composable(ROUTE_LEARN) {
                    LearnScreen(
                        onBackClick = { navController.popBackStack() },
                        onVisualizeAlgorithm = { algorithmId ->
                            navController.navigate("$ROUTE_VISUALIZATION/$algorithmId")
                        }
                    )
                }

                composable(ROUTE_STUDY_ROOMS) {
                    StudyRoomsScreen(
                        onRoomClick = { roomId ->
                            navController.navigate("$ROUTE_CHAT/$roomId")
                        },
                        onCreateRoomClick = { navController.navigate(ROUTE_CREATE_ROOM) },
                        onBackClick = { navController.popBackStack() }
                    )
                }

                composable(ROUTE_CREATE_ROOM) {
                    CreateRoomScreen(
                        onBackClick = { navController.popBackStack() },
                        onRoomCreated = { navController.popBackStack() }
                    )
                }

                composable(
                    route = ROUTE_CHAT_PATTERN,
                    arguments = listOf(
                        navArgument("roomId") {
                            type = NavType.StringType
                        }
                    )
                ) {
                    ChatRoomScreen(
                        onBackClick = { navController.popBackStack() }
                    )
                }

                composable(
                    route = ROUTE_VISUALIZATION_PATTERN,
                    arguments = listOf(
                        navArgument("algorithmId") {
                            type = NavType.StringType
                            defaultValue = DEFAULT_ALGORITHM_ID
                        }
                    )
                ) {
                    AlgorithmVisualizationScreen(
                        onBackClick = { navController.popBackStack() }
                    )
                }

                composable(ROUTE_PROFILE) {
                    ProfileScreen(
                        onEditProfileClick = { navController.navigate(ROUTE_PROFILE_EDIT) },
                        onLogoutClick = { authViewModel.logout() },
                        authViewModel = authViewModel
                    )
                }

                composable(ROUTE_PROFILE_EDIT) {
                    ProfileEditScreen(
                        onBackClick = { navController.popBackStack() }
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

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
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

        // Hide the white bar artifact embedded in the final splash frame.
        if (showFinalFrame) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = maxHeight * 0.615f)
                    .width(maxWidth * 0.12f)
                    .height(maxHeight * 0.0065f)
                    .background(Color.Black, RoundedCornerShape(percent = 50))
            )
        }
    }
}



