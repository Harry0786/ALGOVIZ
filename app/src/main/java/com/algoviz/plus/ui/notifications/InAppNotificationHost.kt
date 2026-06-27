package com.algoviz.plus.ui.notifications

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay

@Composable
fun InAppNotificationHost(
    onOpenChatRoom: (String) -> Unit,
    modifier: Modifier = Modifier,
    globalStudyRoomNotificationViewModel: GlobalStudyRoomNotificationViewModel = hiltViewModel()
) {
    val notificationQueue = remember { mutableListOf<InAppNotification>() }
    var activeNotification by remember { mutableStateOf<InAppNotification?>(null) }

    LaunchedEffect(globalStudyRoomNotificationViewModel) {
        // Keep the global unread observer active while the main nav host is visible.
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
                val queuedIndex = notificationQueue.indexOfFirst { queued ->
                    canClubNotifications(queued, incoming)
                }
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
                    delay(visibleDuration)
                    activeNotification = null
                    delay(140)
                }
            }
        }
    }

    Box(modifier = modifier) {
        TopInAppNotificationBar(
            notification = activeNotification,
            onClick = {
                val roomId = activeNotification?.roomId ?: return@TopInAppNotificationBar
                onOpenChatRoom(roomId)
            },
            onDismiss = {
                activeNotification = null
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
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

    return if (existing.type == InAppNotificationType.Chat && sameRoom) {
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
    } else if (existing.type == InAppNotificationType.Chat) {
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
            InAppNotificationType.Error -> "Multiple issues detected"
            InAppNotificationType.Update -> "Update activity"
            InAppNotificationType.Success -> "Completed actions"
            else -> existing.title
        }
        val groupedMessage = when (existing.type) {
            InAppNotificationType.Error ->
                "$combinedCount similar issues were grouped together. Open the latest screen action for details."
            InAppNotificationType.Update ->
                "$combinedCount related update notifications were combined into one banner."
            InAppNotificationType.Success ->
                "$combinedCount successful actions were grouped together."
            else -> incoming.message
        }

        existing.copy(
            title = groupedTitle,
            message = groupedMessage,
            roomId = if (existing.type == InAppNotificationType.Chat) existing.roomId else null,
            count = combinedCount,
            dedupeKey = "${existing.groupKey}:${existing.type}:$combinedCount"
        )
    }
}
