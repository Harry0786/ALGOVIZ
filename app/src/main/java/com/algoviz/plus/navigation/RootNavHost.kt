package com.algoviz.plus.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.algoviz.plus.ui.placeholder.PlaceholderScreen

@Composable
fun RootNavHost() {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = "placeholder"
    ) {
        composable("placeholder") {
            PlaceholderScreen()
        }
        
        // Feature graphs will be added here
        // Example: authNavGraph(navController)
        // Example: homeNavGraph(navController)
    }
}
