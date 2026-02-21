package com.algoviz.plus.ui.placeholder

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.algoviz.plus.features.auth.presentation.viewmodel.AuthViewModel
import com.algoviz.plus.ui.algorithms.AlgorithmListScreen
import com.algoviz.plus.ui.algorithms.AlgorithmVisualizationScreen
import com.algoviz.plus.ui.home.HomeScreen
import com.algoviz.plus.ui.profile.ProfileEditScreen

@Composable
fun PlaceholderScreen(
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(
                onLogoutClick = { authViewModel.logout() },
                onProfileClick = { navController.navigate("profile_edit") },
                onVisualize = { navController.navigate("algorithms") }
            )
        }
        
        composable("profile_edit") {
            ProfileEditScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
        
        composable("algorithms") {
            AlgorithmListScreen(
                onBackClick = { navController.popBackStack() },
                onAlgorithmClick = { algorithmId ->
                    navController.navigate("visualization/$algorithmId")
                }
            )
        }
        
        composable(
            route = "visualization/{algorithmId}",
            arguments = listOf(navArgument("algorithmId") { type = NavType.StringType })
        ) {
            AlgorithmVisualizationScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
