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
import com.algoviz.plus.ui.studyrooms.CreateRoomScreen
import com.algoviz.plus.ui.studyrooms.StudyRoomsScreen
import com.algoviz.plus.ui.studyrooms.chat.ChatRoomScreen
import com.algoviz.plus.ui.update.AdminAppUpdateScreen

@Composable
fun PlaceholderScreen(
    onSignOutClick: () -> Unit,
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
                onVisualize = { navController.navigate("algorithms") },
                onStudyRooms = { navController.navigate("study_rooms") },
                onAdminUpdate = { navController.navigate("admin_app_update") }
            )
        }

        composable("admin_app_update") {
            AdminAppUpdateScreen(
                onBackClick = { navController.popBackStack() }
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
        
        composable("study_rooms") {
            StudyRoomsScreen(
                onRoomClick = { roomId ->
                    navController.navigate("chat/$roomId")
                },
                onCreateRoomClick = {
                    navController.navigate("create_room")
                },
                onBackClick = { navController.popBackStack() }
            )
        }
        
        composable("create_room") {
            CreateRoomScreen(
                onBackClick = { navController.popBackStack() },
                onRoomCreated = { navController.popBackStack() }
            )
        }
        
        composable(
            route = "chat/{roomId}",
            arguments = listOf(navArgument("roomId") { type = NavType.StringType })
        ) {
            ChatRoomScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}
