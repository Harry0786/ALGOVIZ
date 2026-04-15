package com.algoviz.plus.ui.placeholder
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.compose.ui.unit.dp
import com.algoviz.plus.features.auth.presentation.viewmodel.AuthViewModel
import com.algoviz.plus.ui.algorithms.AlgorithmListScreen
import com.algoviz.plus.ui.algorithms.AlgorithmVisualizationScreen
import com.algoviz.plus.ui.home.HomeScreen
import com.algoviz.plus.ui.notifications.GlobalStudyRoomNotificationViewModel
import com.algoviz.plus.ui.notifications.InAppNotification
import com.algoviz.plus.ui.notifications.InAppNotificationCenter
import com.algoviz.plus.ui.notifications.TopInAppNotificationBar
import com.algoviz.plus.ui.learn.LearnScreen
import com.algoviz.plus.ui.profile.ProfileEditScreen
import com.algoviz.plus.ui.studyrooms.CreateRoomScreen
import com.algoviz.plus.ui.studyrooms.StudyRoomsScreen
import com.algoviz.plus.ui.studyrooms.chat.ChatRoomScreen

@Composable
fun PlaceholderScreen(
    onSignOutClick: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val globalStudyRoomNotificationViewModel: GlobalStudyRoomNotificationViewModel = hiltViewModel()
    val notificationQueue = remember { mutableListOf<InAppNotification>() }
    var activeNotification by remember { mutableStateOf<InAppNotification?>(null) }

    LaunchedEffect(globalStudyRoomNotificationViewModel) {
        // Keep global unread observer active while authenticated nav host is visible.
    }
    
    LaunchedEffect(Unit) {
        InAppNotificationCenter.events.collect { incoming ->
            val activeKey = activeNotification?.dedupeKey
            val queuedKeys = notificationQueue.map { it.dedupeKey }
            if (incoming.dedupeKey == activeKey || incoming.dedupeKey in queuedKeys) {
                return@collect
            }

            var handled = false

            val active = activeNotification
            if (!handled && active != null && canClubNotifications(active, incoming)) {
                activeNotification = clubNotifications(active, incoming)
                handled = true
            }

            if (!handled) {
                val queuedIndex = notificationQueue.indexOfFirst { queued -> canClubNotifications(queued, incoming) }
                if (queuedIndex >= 0) {
                    notificationQueue[queuedIndex] = clubNotifications(notificationQueue[queuedIndex], incoming)
                    handled = true
                }
            }

            if (!handled) {
                notificationQueue.add(incoming)
            }

            if (activeNotification == null) {
                while (notificationQueue.isNotEmpty()) {
                    activeNotification = notificationQueue.removeAt(0)
                    val visibleDuration = if ((activeNotification?.count ?: 1) > 1) 3600L else 2600L
                    kotlinx.coroutines.delay(visibleDuration)
                    activeNotification = null
                    kotlinx.coroutines.delay(140)
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = "home",
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
            popEnterTransition = { EnterTransition.None },
            popExitTransition = { ExitTransition.None }
        ) {
            composable("home") {
                HomeScreen(
                    onLogoutClick = { authViewModel.logout() },
                    onProfileClick = { navController.navigateSafely("profile_edit") },
                    onVisualize = { navController.navigateSafely("algorithms") },
                    onLearn = { navController.navigateSafely("learn") },
                    onStudyRooms = { navController.navigateSafely("study_rooms") }
                )
            }

            composable("learn") {
                LearnScreen(
                    onBackClick = { navController.popBackStack() },
                    onVisualizeAlgorithm = { algorithmId ->
                        navController.navigate("visualization/$algorithmId")
                    }
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

        TopInAppNotificationBar(
            notification = activeNotification,
            onClick = {
                val roomId = activeNotification?.roomId ?: return@TopInAppNotificationBar
                navController.navigate("chat/$roomId") {
                    launchSingleTop = true
                }
            },
            onDismiss = {
                activeNotification = null
            },
            modifier = Modifier
                .align(androidx.compose.ui.Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 26.dp)
        )
    }
}

private fun canClubNotifications(existing: InAppNotification, incoming: InAppNotification): Boolean {
    return existing.groupKey != null && existing.groupKey == incoming.groupKey
}

private fun clubNotifications(existing: InAppNotification, incoming: InAppNotification): InAppNotification {
    val combinedCount = existing.count + incoming.count
    val sameRoom = existing.roomId != null && existing.roomId == incoming.roomId

    return if (existing.type == com.algoviz.plus.ui.notifications.InAppNotificationType.Chat && sameRoom) {
        existing.copy(
            title = existing.title,
            message = if (combinedCount == 2) {
                "You have 2 unread updates waiting in this room."
            } else {
                "You have $combinedCount unread updates waiting in this room."
            },
            count = combinedCount,
            dedupeKey = "${existing.groupKey}:${existing.roomId}:$combinedCount"
        )
    } else if (existing.type == com.algoviz.plus.ui.notifications.InAppNotificationType.Chat) {
        existing.copy(
            title = "Study room updates",
            message = if (combinedCount == 2) {
                "2 new chat updates arrived across your study rooms."
            } else {
                "$combinedCount new chat updates arrived across your study rooms."
            },
            roomId = null,
            count = combinedCount,
            dedupeKey = "${existing.groupKey}:multi:$combinedCount"
        )
    } else {
        val groupedTitle = when (existing.type) {
            com.algoviz.plus.ui.notifications.InAppNotificationType.Error -> "Multiple issues detected"
            com.algoviz.plus.ui.notifications.InAppNotificationType.Update -> "Update activity"
            com.algoviz.plus.ui.notifications.InAppNotificationType.Success -> "Completed actions"
            else -> existing.title
        }
        val groupedMessage = when (existing.type) {
            com.algoviz.plus.ui.notifications.InAppNotificationType.Error ->
                "$combinedCount similar issues were grouped together. Open the latest screen action for details."
            com.algoviz.plus.ui.notifications.InAppNotificationType.Update ->
                "$combinedCount related update notifications were combined into one banner."
            com.algoviz.plus.ui.notifications.InAppNotificationType.Success ->
                "$combinedCount successful actions were grouped together."
            else -> incoming.message
        }

        existing.copy(
            title = groupedTitle,
            message = groupedMessage,
            roomId = if (existing.type == com.algoviz.plus.ui.notifications.InAppNotificationType.Chat) existing.roomId else null,
            count = combinedCount,
            dedupeKey = "${existing.groupKey}:${existing.type}:$combinedCount"
        )
    }
}

private fun NavHostController.navigateSafely(route: String) {
    if (currentDestination?.route == route) {
        return
    }

    runCatching {
        navigate(route) {
            launchSingleTop = true
        }
    }
}
