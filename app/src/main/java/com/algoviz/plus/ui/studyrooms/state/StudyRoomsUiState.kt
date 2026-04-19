package com.algoviz.plus.ui.studyrooms.state

import com.algoviz.plus.domain.model.StudyRoom
import com.algoviz.plus.domain.model.RoomMember

sealed class StudyRoomsUiState {
    data object Loading : StudyRoomsUiState()
    data class Success(
        val rooms: List<StudyRoom>,
        val myRooms: List<StudyRoom>,
        val selectedCategory: String? = null,
        val loadingRoomId: String? = null, // Room being joined/left
        val unreadCounts: Map<String, Int> = emptyMap(),
        val roomMembersByRoom: Map<String, List<RoomMember>> = emptyMap(),
        val onlineFriends: List<RoomMember> = emptyList(),
        val currentUserId: String? = null
    ) : StudyRoomsUiState()
    data class Error(val message: String) : StudyRoomsUiState()
}

sealed class CreateRoomEvent {
    data object Loading : CreateRoomEvent()
    data object Success : CreateRoomEvent()
    data class Error(val message: String) : CreateRoomEvent()
}

sealed class StudyRoomAction {
    data class JoinRoom(val roomId: String) : StudyRoomAction()
    data class LeaveRoom(val roomId: String) : StudyRoomAction()
    data class CreateRoom(
        val name: String,
        val description: String,
        val category: String,
        val maxMembers: Int = 50,
        val isPrivate: Boolean = false
    ) : StudyRoomAction()
    data class FilterByCategory(val category: String?) : StudyRoomAction()
    data class SearchRooms(val query: String) : StudyRoomAction()
}
