package com.algoviz.plus.ui.studyrooms.chat.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.algoviz.plus.domain.usecase.DeleteRoomUseCase
import com.algoviz.plus.domain.usecase.GetRoomMembersUseCase
import com.algoviz.plus.domain.usecase.GetRoomMessagesUseCase
import com.algoviz.plus.domain.usecase.GetStudyRoomsUseCase
import com.algoviz.plus.domain.usecase.SendMessageUseCase
import com.algoviz.plus.features.auth.domain.usecase.GetCurrentUserUseCase
import com.algoviz.plus.ui.studyrooms.chat.state.ChatRoomUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import javax.inject.Inject

@HiltViewModel
class ChatRoomViewModel @Inject constructor(
    private val deleteRoomUseCase: DeleteRoomUseCase,
    private val getStudyRoomsUseCase: GetStudyRoomsUseCase,
    private val getRoomMessagesUseCase: GetRoomMessagesUseCase,
    private val getRoomMembersUseCase: GetRoomMembersUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val roomId: String = checkNotNull(savedStateHandle["roomId"])
    
    private val _uiState = MutableStateFlow<ChatRoomUiState>(ChatRoomUiState.Loading)
    val uiState: StateFlow<ChatRoomUiState> = _uiState.asStateFlow()
    
    private val _messageInput = MutableStateFlow("")
    val messageInput: StateFlow<String> = _messageInput.asStateFlow()
    
    private val _sendMessageError = MutableStateFlow<String?>(null)
    val sendMessageError: StateFlow<String?> = _sendMessageError.asStateFlow()

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()

    private val _isDeletingRoom = MutableStateFlow(false)
    val isDeletingRoom: StateFlow<Boolean> = _isDeletingRoom.asStateFlow()

    private val _deleteRoomError = MutableStateFlow<String?>(null)
    val deleteRoomError: StateFlow<String?> = _deleteRoomError.asStateFlow()

    private val _roomDeleted = MutableStateFlow(false)
    val roomDeleted: StateFlow<Boolean> = _roomDeleted.asStateFlow()

    private val _otherRoomsUnreadCounts = MutableStateFlow<Map<String, Int>>(emptyMap())
    val otherRoomsUnreadCounts: StateFlow<Map<String, Int>> = _otherRoomsUnreadCounts.asStateFlow()

    private var otherRoomsUnreadJob: Job? = null
    private var typingTimeoutJob: Job? = null
    private var currentUserId: String? = null
    private var isTypingActive = false
    private var lastTypingUpdateAt = 0L
    
    init {
        loadChatRoom()
    }
    
    private fun loadChatRoom() {
        viewModelScope.launch {
            val user = getCurrentUserUseCase().firstOrNull()
            if (user == null) {
                _uiState.value = ChatRoomUiState.Error("User not authenticated")
                return@launch
            }
            currentUserId = user.id

            getStudyRoomsUseCase.markRoomAsRead(roomId, user.id)
            observeOtherRoomsUnreadCounts(user.id)
            
            try {
                combine(
                    getStudyRoomsUseCase.roomById(roomId).catch { emit(null) },
                    getRoomMessagesUseCase(roomId, limit = 100).catch { emit(emptyList()) },
                    getRoomMembersUseCase(roomId).catch { emit(emptyList()) }
                ) { room, messages, members ->
                    if (room != null) {
                        ChatRoomUiState.Success(
                            room = room,
                            messages = messages,
                            members = members,
                            currentUserId = user.id
                        )
                    } else {
                        ChatRoomUiState.Error("Room not found or you don't have access")
                    }
                }
                .catch { e ->
                    _uiState.value = ChatRoomUiState.Error(e.message ?: "Failed to load chat room")
                }
                .collect { state ->
                    _uiState.value = state
                }
            } catch (e: Exception) {
                _uiState.value = ChatRoomUiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    private fun observeOtherRoomsUnreadCounts(userId: String) {
        otherRoomsUnreadJob?.cancel()
        otherRoomsUnreadJob = viewModelScope.launch {
            combine(
                getStudyRoomsUseCase.unreadCounts(userId).catch { emit(emptyMap()) },
                getStudyRoomsUseCase.myRooms(userId).catch { emit(emptyList()) }
            ) { unreadCounts, myRooms ->
                val myRoomIds = myRooms.map { it.id }.toSet()
                unreadCounts
                    .filterKeys { roomKey -> roomKey in myRoomIds && roomKey != roomId }
            }
                .catch {
                    _otherRoomsUnreadCounts.value = emptyMap()
                }
                .collect { filteredUnread ->
                    _otherRoomsUnreadCounts.value = filteredUnread
                }
        }
    }
    
    fun updateMessageInput(text: String) {
        _messageInput.value = text
        val userId = currentUserId ?: return

        if (text.isNotBlank()) {
            val now = System.currentTimeMillis()
            if (!isTypingActive) {
                isTypingActive = true
                lastTypingUpdateAt = now
                viewModelScope.launch {
                    getStudyRoomsUseCase.setTypingStatus(roomId, userId, true)
                }
            } else if (now - lastTypingUpdateAt >= 900L) {
                lastTypingUpdateAt = now
                viewModelScope.launch {
                    getStudyRoomsUseCase.setTypingStatus(roomId, userId, true)
                }
            }

            typingTimeoutJob?.cancel()
            typingTimeoutJob = viewModelScope.launch {
                delay(3200)
                isTypingActive = false
                lastTypingUpdateAt = 0L
                getStudyRoomsUseCase.setTypingStatus(roomId, userId, false)
            }
        } else {
            typingTimeoutJob?.cancel()
            if (isTypingActive) {
                isTypingActive = false
                lastTypingUpdateAt = 0L
                viewModelScope.launch {
                    getStudyRoomsUseCase.setTypingStatus(roomId, userId, false)
                }
            }
        }
    }
    
    fun sendMessage(type: String = "TEXT", contentOverride: String? = null) {
        if (_isSending.value) return

        val activeState = _uiState.value as? ChatRoomUiState.Success
        if (activeState != null && !activeState.room.isActive) {
            _sendMessageError.value = "This group has been deleted. You can no longer send messages."
            return
        }

        val text = (contentOverride ?: _messageInput.value).trim()
        if (text.isEmpty()) {
            _sendMessageError.value = "Message cannot be empty"
            return
        }
        
        if (text.length > 1000) {
            _sendMessageError.value = "Message is too long (max 1000 characters)"
            return
        }
        
        viewModelScope.launch {
            try {
                val user = getCurrentUserUseCase().firstOrNull()
                if (user == null) {
                    _sendMessageError.value = "User not authenticated. Please log in again."
                    return@launch
                }
                
                val userName = user.email.substringBefore("@")
                
                _isSending.value = true

                val result = sendMessageUseCase(
                    roomId = roomId,
                    userId = user.id,
                    userName = userName,
                    content = text,
                    type = type
                )
                
                result.onSuccess {
                    _messageInput.value = ""
                    _sendMessageError.value = null
                    typingTimeoutJob?.cancel()
                    isTypingActive = false
                    lastTypingUpdateAt = 0L
                    getStudyRoomsUseCase.setTypingStatus(roomId, user.id, false)
                }.onFailure { error ->
                    _sendMessageError.value = error.message ?: "Failed to send message. Check your connection and try again."
                }
            } catch (e: Exception) {
                _sendMessageError.value = e.message ?: "An unexpected error occurred while sending message"
            } finally {
                _isSending.value = false
            }
        }
    }
    
    fun clearSendMessageError() {
        _sendMessageError.value = null
    }
    
    fun retryLoadChatRoom() {
        _uiState.value = ChatRoomUiState.Loading
        loadChatRoom()
    }

    fun deleteRoom() {
        if (_isDeletingRoom.value) return

        val state = _uiState.value as? ChatRoomUiState.Success
        if (state == null) {
            _deleteRoomError.value = "Room state unavailable"
            return
        }

        if (state.currentUserId != state.room.createdBy) {
            _deleteRoomError.value = "Only the group creator can delete this group"
            return
        }

        viewModelScope.launch {
            try {
                _isDeletingRoom.value = true

                val user = getCurrentUserUseCase().firstOrNull()
                if (user == null) {
                    _deleteRoomError.value = "User not authenticated"
                    return@launch
                }

                val requesterName = user.email.substringBefore("@")
                val result = deleteRoomUseCase(
                    roomId = roomId,
                    requesterId = user.id,
                    requesterName = requesterName
                )

                result.onSuccess {
                    _roomDeleted.value = true
                }.onFailure { error ->
                    _deleteRoomError.value = error.message ?: "Failed to delete group"
                }
            } catch (e: Exception) {
                _deleteRoomError.value = e.message ?: "Failed to delete group"
            } finally {
                _isDeletingRoom.value = false
            }
        }
    }

    fun clearDeleteRoomError() {
        _deleteRoomError.value = null
    }

    fun clearRoomDeletedEvent() {
        _roomDeleted.value = false
    }

    override fun onCleared() {
        super.onCleared()
        typingTimeoutJob?.cancel()
        val userId = currentUserId ?: return
        viewModelScope.launch {
            getStudyRoomsUseCase.setTypingStatus(roomId, userId, false)
        }
    }
}
