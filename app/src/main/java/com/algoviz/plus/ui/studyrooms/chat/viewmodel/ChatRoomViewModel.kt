package com.algoviz.plus.ui.studyrooms.chat.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.algoviz.plus.domain.usecase.GetRoomMembersUseCase
import com.algoviz.plus.domain.usecase.GetRoomMessagesUseCase
import com.algoviz.plus.domain.usecase.GetStudyRoomsUseCase
import com.algoviz.plus.domain.usecase.SendMessageUseCase
import com.algoviz.plus.features.auth.domain.usecase.GetCurrentUserUseCase
import com.algoviz.plus.ui.studyrooms.chat.state.ChatRoomUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatRoomViewModel @Inject constructor(
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
    
    fun updateMessageInput(text: String) {
        _messageInput.value = text
    }
    
    fun sendMessage(type: String = "TEXT", contentOverride: String? = null) {
        if (_isSending.value) return

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
}
