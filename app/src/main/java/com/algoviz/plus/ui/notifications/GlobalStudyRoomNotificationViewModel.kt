package com.algoviz.plus.ui.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.algoviz.plus.domain.usecase.GetStudyRoomsUseCase
import com.algoviz.plus.features.auth.domain.model.User
import com.algoviz.plus.features.auth.domain.usecase.GetCurrentUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GlobalStudyRoomNotificationViewModel @Inject constructor(
    private val getStudyRoomsUseCase: GetStudyRoomsUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private var hasBaseline = false
    private var previousUnread: Map<String, Int> = emptyMap()
    private var eventMarkers: Set<String> = emptySet()

    init {
        startObservingUnread()
    }

    private fun startObservingUnread() {
        viewModelScope.launch {
            val user = awaitAuthenticatedUser() ?: return@launch

            combine(
                getStudyRoomsUseCase.unreadCounts(user.id).catch { emit(emptyMap()) },
                getStudyRoomsUseCase.myRooms(user.id).catch { emit(emptyList()) },
                ActiveChatRoomTracker.activeRoomId
            ) { unreadCounts, myRooms, activeRoomId ->
                Triple(unreadCounts, myRooms.associate { it.id to it.name }, activeRoomId)
            }.collect { (unreadCounts, roomNamesById, activeRoomId) ->
                if (!hasBaseline) {
                    previousUnread = unreadCounts
                    hasBaseline = true
                    return@collect
                }

                unreadCounts.forEach { (roomId, unread) ->
                    if (unread <= 0) {
                        eventMarkers = eventMarkers.filterNot { it.startsWith("$roomId:") }.toSet()
                    }
                }

                val deltas = unreadCounts
                    .mapNotNull { (roomId, unread) ->
                        val prev = previousUnread[roomId] ?: 0
                        val delta = unread - prev
                        val marker = "$roomId:$unread"
                        if (delta > 0 && marker !in eventMarkers) {
                            eventMarkers = eventMarkers + marker
                            roomId to delta
                        } else {
                            null
                        }
                    }
                    .toMap()

                previousUnread = unreadCounts
                val filteredDeltas = if (activeRoomId == null) {
                    deltas
                } else {
                    deltas.filterKeys { it != activeRoomId }
                }

                if (filteredDeltas.isEmpty()) return@collect

                val totalMessages = filteredDeltas.values.sum()
                val roomIds = filteredDeltas.keys
                val roomCount = roomIds.size
                val message = if (roomCount == 1) {
                    val roomId = roomIds.first()
                    val roomName = roomNamesById[roomId] ?: "a room"
                    if (totalMessages == 1) {
                        "You have a new message waiting in $roomName."
                    } else {
                        "$totalMessages new messages just arrived in $roomName."
                    }
                } else {
                    "$totalMessages unread messages need attention across $roomCount study rooms."
                }

                val title = if (roomCount == 1) {
                    val roomId = roomIds.first()
                    val roomName = roomNamesById[roomId] ?: "Study Room"
                    "New activity in $roomName"
                } else {
                    "Study room updates"
                }

                InAppNotificationCenter.post(
                    InAppNotification(
                        title = title,
                        message = message,
                        type = InAppNotificationType.Chat,
                        roomId = if (roomCount == 1) roomIds.first() else null,
                        groupKey = "study_room_chat_updates",
                        dedupeKey = "global_unread:${roomIds.sorted().joinToString("|")}:$totalMessages"
                    )
                )
            }
        }
    }

    private suspend fun awaitAuthenticatedUser(timeoutMs: Long = 10_000L): User? {
        val start = System.currentTimeMillis()
        while (System.currentTimeMillis() - start < timeoutMs) {
            val user = getCurrentUserUseCase().firstOrNull()
            if (user != null) {
                return user
            }
            delay(250L)
        }
        return null
    }
}
