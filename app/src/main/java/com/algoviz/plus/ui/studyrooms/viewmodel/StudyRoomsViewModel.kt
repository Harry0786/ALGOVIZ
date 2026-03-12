package com.algoviz.plus.ui.studyrooms.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.algoviz.plus.domain.usecase.CreateRoomUseCase
import com.algoviz.plus.domain.usecase.GetStudyRoomsUseCase
import com.algoviz.plus.domain.usecase.JoinStudyRoomUseCase
import com.algoviz.plus.domain.usecase.LeaveRoomUseCase
import com.algoviz.plus.features.auth.domain.usecase.GetCurrentUserUseCase
import com.algoviz.plus.ui.studyrooms.state.CreateRoomEvent
import com.algoviz.plus.ui.studyrooms.state.StudyRoomAction
import com.algoviz.plus.ui.studyrooms.state.StudyRoomsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import javax.inject.Inject

@HiltViewModel
class StudyRoomsViewModel @Inject constructor(
    private val getStudyRoomsUseCase: GetStudyRoomsUseCase,
    private val joinStudyRoomUseCase: JoinStudyRoomUseCase,
    private val leaveRoomUseCase: LeaveRoomUseCase,
    private val createRoomUseCase: CreateRoomUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<StudyRoomsUiState>(StudyRoomsUiState.Loading)
    val uiState: StateFlow<StudyRoomsUiState> = _uiState.asStateFlow()
    
    private val _selectedCategory = MutableStateFlow<String?>(null)
    private val _searchQuery = MutableStateFlow("")
    
    private val _createRoomEvent = MutableStateFlow<CreateRoomEvent?>(null)
    val createRoomEvent: StateFlow<CreateRoomEvent?> = _createRoomEvent.asStateFlow()
    
    private var loadRoomsJob: kotlinx.coroutines.Job? = null
    
    init {
        loadRooms()
        setupSearchDebounce()
    }
    
    @OptIn(FlowPreview::class)
    private fun setupSearchDebounce() {
        viewModelScope.launch {
            _searchQuery
                .debounce(300)
                .collect { query ->
                    if (query.isBlank()) {
                        // Clear search, reload normal rooms
                        loadRooms()
                    } else {
                        performSearch(query)
                    }
                }
        }
    }
    
    private fun performSearch(query: String) {
        loadRoomsJob?.cancel()
        loadRoomsJob = viewModelScope.launch {
            try {
                val user = getCurrentUserUseCase().firstOrNull()
                if (user == null) {
                    _uiState.value = StudyRoomsUiState.Error("User not authenticated")
                    return@launch
                }
                
                getStudyRoomsUseCase.search(query)
                    .combine(getStudyRoomsUseCase.myRooms(user.id)) { rooms, myRooms ->
                        rooms to myRooms
                    }
                    .combine(getStudyRoomsUseCase.unreadCounts(user.id)) { (rooms, myRooms), unreadCounts ->
                        StudyRoomsUiState.Success(
                            rooms = rooms,
                            myRooms = myRooms,
                            selectedCategory = null,
                            loadingRoomId = null,
                            unreadCounts = unreadCounts
                        )
                    }
                    .catch { e ->
                        if (e !is CancellationException) {
                            _uiState.value = StudyRoomsUiState.Error(e.message ?: "Search failed")
                        }
                    }
                    .collect { state ->
                        _uiState.value = state
                    }
            } catch (e: CancellationException) {
                // Ignore cancellation exceptions - this is normal when switching between search/load
                throw e
            } catch (e: Exception) {
                _uiState.value = StudyRoomsUiState.Error(e.message ?: "Search error")
            }
        }
    }
    
    private fun loadRooms() {
        loadRoomsJob?.cancel()
        loadRoomsJob = viewModelScope.launch {
            try {
                val user = getCurrentUserUseCase().firstOrNull()
                if (user == null) {
                    _uiState.value = StudyRoomsUiState.Error("User not authenticated")
                    return@launch
                }
                
                combine(
                    getStudyRoomsUseCase(), 
                    getStudyRoomsUseCase.myRooms(user.id),
                    _selectedCategory,
                    getStudyRoomsUseCase.unreadCounts(user.id)
                ) { allRooms, myRooms, category, unreadCounts ->
                    val filteredRooms = if (category != null) {
                        allRooms.filter { it.category.name == category }
                    } else {
                        allRooms
                    }
                    StudyRoomsUiState.Success(
                        rooms = filteredRooms,
                        myRooms = myRooms,
                        selectedCategory = category,
                        loadingRoomId = null,
                        unreadCounts = unreadCounts
                    )
                }
                .catch { e ->
                    if (e !is CancellationException) {
                        _uiState.value = StudyRoomsUiState.Error(e.message ?: "Failed to load rooms. Pull to refresh.")
                    }
                }
                .collect { state ->
                    _uiState.value = state
                }
            } catch (e: CancellationException) {
                // Ignore cancellation exceptions - this is normal when reloading
                throw e
            } catch (e: Exception) {
                _uiState.value = StudyRoomsUiState.Error(e.message ?: "An unexpected error occurred")
            }
        }
    }
    
    fun retryLoadRooms() {
        _uiState.value = StudyRoomsUiState.Loading
        loadRooms()
    }
    
    fun onAction(action: StudyRoomAction) {
        when (action) {
            is StudyRoomAction.JoinRoom -> joinRoom(action.roomId)
            is StudyRoomAction.FilterByCategory -> filterByCategory(action.category)
            is StudyRoomAction.SearchRooms -> searchRooms(action.query)
            is StudyRoomAction.LeaveRoom -> leaveRoom(action.roomId)
            is StudyRoomAction.CreateRoom -> createRoom(action.name, action.description, action.category, action.maxMembers, action.isPrivate)
        }
    }
    
    private fun joinRoom(roomId: String) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is StudyRoomsUiState.Success) {
                // Show loading state for this specific room
                _uiState.value = currentState.copy(loadingRoomId = roomId)
            }
            
            try {
                val user = getCurrentUserUseCase().firstOrNull()
                if (user == null) {
                    if (currentState is StudyRoomsUiState.Success) {
                        _uiState.value = currentState.copy(loadingRoomId = null)
                    }
                    _uiState.value = StudyRoomsUiState.Error("Authentication required to join rooms")
                    return@launch
                }
                
                val userName = user.email.substringBefore("@")
                
                val result = withTimeout(10000L) { // 10 second timeout
                    joinStudyRoomUseCase(
                        roomId = roomId,
                        userId = user.id,
                        userName = userName
                    )
                }
                
                // Clear loading state on failure (success handled by real-time listener)
                result.onFailure { error ->
                    if (currentState is StudyRoomsUiState.Success) {
                        _uiState.value = currentState.copy(loadingRoomId = null)
                    }
                    _uiState.value = StudyRoomsUiState.Error(
                        error.message ?: "Failed to join room. Please try again."
                    )
                }
            } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
                if (currentState is StudyRoomsUiState.Success) {
                    _uiState.value = currentState.copy(loadingRoomId = null)
                }
                _uiState.value = StudyRoomsUiState.Error("Request timed out. Please check your connection.")
            } catch (e: Exception) {
                if (currentState is StudyRoomsUiState.Success) {
                    _uiState.value = currentState.copy(loadingRoomId = null)
                }
                _uiState.value = StudyRoomsUiState.Error(e.message ?: "An error occurred")
            }
        }
    }
    
    private fun filterByCategory(category: String?) {
        _selectedCategory.value = category
    }
    
    @OptIn(FlowPreview::class)
    private fun searchRooms(query: String) {
        _searchQuery.value = query
    }
    
    private fun createRoom(name: String, description: String, category: String, maxMembers: Int = 50, isPrivate: Boolean = false) {
        viewModelScope.launch {
            try {
                _createRoomEvent.value = CreateRoomEvent.Loading
                
                val user = getCurrentUserUseCase().firstOrNull()
                if (user == null) {
                    _createRoomEvent.value = CreateRoomEvent.Error("User not authenticated")
                    return@launch
                }
                
                val creatorName = user.email.substringBefore("@")
                val result = withTimeout(30000L) { // 30 second timeout
                    createRoomUseCase(
                        name = name,
                        description = description,
                        category = category,
                        createdBy = user.id,
                        creatorName = creatorName,
                        maxMembers = maxMembers,
                        isPrivate = isPrivate
                    )
                }
                
                if (result.isSuccess) {
                    _createRoomEvent.value = CreateRoomEvent.Success
                } else {
                    val error = result.exceptionOrNull()
                    _createRoomEvent.value = CreateRoomEvent.Error(
                        error?.message ?: "Failed to create room"
                    )
                }
            } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
                _createRoomEvent.value = CreateRoomEvent.Error("Request timed out. Please try again.")
            } catch (e: Exception) {
                _createRoomEvent.value = CreateRoomEvent.Error(e.message ?: "An unexpected error occurred")
            }
        }
    }
    
    fun clearCreateRoomEvent() {
        _createRoomEvent.value = null
    }
    
    private fun leaveRoom(roomId: String) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is StudyRoomsUiState.Success) {
                // Show loading state for this specific room
                _uiState.value = currentState.copy(loadingRoomId = roomId)
            }
            
            try {
                val user = getCurrentUserUseCase().firstOrNull()
                if (user == null) {
                    _uiState.value = StudyRoomsUiState.Error("Authentication required")
                    return@launch
                }
                
                val result = withTimeout(10000L) { // 10 second timeout
                    leaveRoomUseCase(roomId, user.id)
                }
                
                result.onFailure { error ->
                    // Clear loading and show error
                    if (currentState is StudyRoomsUiState.Success) {
                        _uiState.value = currentState.copy(loadingRoomId = null)
                    }
                    _uiState.value = StudyRoomsUiState.Error(
                        error.message ?: "Failed to leave room. Please try again."
                    )
                }
                // Success cleared by real-time listener in loadRooms()
            } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
                if (currentState is StudyRoomsUiState.Success) {
                    _uiState.value = currentState.copy(loadingRoomId = null)
                }
                _uiState.value = StudyRoomsUiState.Error("Request timed out. Please check your connection.")
            } catch (e: Exception) {
                if (currentState is StudyRoomsUiState.Success) {
                    _uiState.value = currentState.copy(loadingRoomId = null)
                }
                _uiState.value = StudyRoomsUiState.Error(e.message ?: "An error occurred")
            }
        }
    }
}
