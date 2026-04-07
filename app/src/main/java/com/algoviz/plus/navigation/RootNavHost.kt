package com.algoviz.plus.navigation

import android.graphics.drawable.BitmapDrawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
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

@Composable
fun RootNavHost(
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()
    var splashFinished by remember { mutableStateOf(false) }

    // Minimum 3-second splash screen display
    LaunchedEffect(Unit) {
        delay(3000)
        splashFinished = true
    }

    // Show splash screen while checking auth state or during minimum display time
    if (!splashFinished || authState is AuthUiState.Idle || authState is AuthUiState.Loading) {
        SplashScreen()
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

@Composable
private fun SplashScreen() {
    val context = LocalContext.current
    val painter = remember {
        val drawable = ContextCompat.getDrawable(context, com.algoviz.plus.R.mipmap.ic_launcher)
        val bitmap = (drawable as? BitmapDrawable)?.bitmap
            ?: android.graphics.Bitmap.createBitmap(
                drawable!!.intrinsicWidth,
                drawable.intrinsicHeight,
                android.graphics.Bitmap.Config.ARGB_8888
            ).also { bmp ->
                val canvas = android.graphics.Canvas(bmp)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
            }
        BitmapPainter(bitmap.asImageBitmap())
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0B0B0D),
                        Color(0xFF141418),
                        Color(0xFF1A1A1F)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Image(
                painter = painter,
                contentDescription = "AlgoViz Logo",
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(30.dp))
            )
            Text(
                text = "AlgoViz+",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}



