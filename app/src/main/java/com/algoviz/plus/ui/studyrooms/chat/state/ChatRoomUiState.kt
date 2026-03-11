package com.algoviz.plus.ui.studyrooms.chat.state

import com.algoviz.plus.domain.model.Message
import com.algoviz.plus.domain.model.RoomMember
import com.algoviz.plus.domain.model.StudyRoom

sealed class ChatRoomUiState {
    data object Loading : ChatRoomUiState()
    data class Success(
        val room: StudyRoom,
        val messages: List<Message>,
        val members: List<RoomMember>,
        val currentUserId: String
    ) : ChatRoomUiState()
    data class Error(val message: String) : ChatRoomUiState()
}
