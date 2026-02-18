package com.algoviz.plus.ui.placeholder

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.algoviz.plus.features.auth.presentation.viewmodel.AuthViewModel
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
                onProfileClick = { navController.navigate("profile_edit") }
            )
        }
        
        composable("profile_edit") {
            ProfileEditScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
