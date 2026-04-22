package com.algoviz.plus.ui.studyrooms.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.algoviz.plus.core.common.utils.UserIdentityUtils
import com.algoviz.plus.core.datastore.PreferencesManager
import com.algoviz.plus.domain.usecase.CreateRoomUseCase
import com.algoviz.plus.domain.usecase.GetRoomMembersUseCase
import com.algoviz.plus.domain.usecase.GetStudyRoomsUseCase
import com.algoviz.plus.domain.usecase.JoinStudyRoomUseCase
import com.algoviz.plus.domain.usecase.LeaveRoomUseCase
import com.algoviz.plus.domain.usecase.SyncMemberCountUseCase
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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

@HiltViewModel
class StudyRoomsViewModel @Inject constructor(
    private val getStudyRoomsUseCase: GetStudyRoomsUseCase,
    private val getRoomMembersUseCase: GetRoomMembersUseCase,
    private val joinStudyRoomUseCase: JoinStudyRoomUseCase,
    private val leaveRoomUseCase: LeaveRoomUseCase,
    private val createRoomUseCase: CreateRoomUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val syncMemberCountUseCase: SyncMemberCountUseCase,
    private val preferencesManager: PreferencesManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<StudyRoomsUiState>(StudyRoomsUiState.Loading)
    val uiState: StateFlow<StudyRoomsUiState> = _uiState.asStateFlow()
    
    private val _selectedCategory = MutableStateFlow<String?>(null)
    private val _searchQuery = MutableStateFlow("")
    
    private val _createRoomEvent = MutableStateFlow<CreateRoomEvent?>(null)
    val createRoomEvent: StateFlow<CreateRoomEvent?> = _createRoomEvent.asStateFlow()

    private val _roomMembersByRoom = MutableStateFlow<Map<String, List<com.algoviz.plus.domain.model.RoomMember>>>(emptyMap())
    private val _onlineFriends = MutableStateFlow<List<com.algoviz.plus.domain.model.RoomMember>>(emptyList())
    
    private var loadRoomsJob: kotlinx.coroutines.Job? = null
    private var membersObservationJob: kotlinx.coroutines.Job? = null
    
    init {
        loadRooms()
        setupSearchDebounce()
    }

    private suspend fun awaitAuthenticatedUser() =
        withTimeoutOrNull(2500L) {
            getCurrentUserUseCase().filterNotNull().firstOrNull()
        } ?: getCurrentUserUseCase().firstOrNull()

    private fun Throwable.toUserMessage(defaultMessage: String): String {
        val raw = message?.trim().orEmpty()
        if (raw.isBlank()) return defaultMessage

        val normalized = raw.lowercase()
        return when {
            normalized.contains("http") ||
                normalized.contains("url") ||
                normalized.contains("socket") ||
                normalized.contains("timeout") ||
                normalized.contains("postgrest") ||
                normalized.contains("supabase") ||
                normalized.contains("failed to fetch") -> "Connection issue. Please try again."
            else -> raw
        }
    }

    private suspend fun resolveDisplayName(userEmail: String): String {
        val username = preferencesManager.profileUsername.firstOrNull().orEmpty().trim()
        if (username.isNotBlank()) return username

        val profileName = preferencesManager.profileName.firstOrNull().orEmpty().trim()
        if (profileName.isNotBlank() && profileName != "AlgoViz User") return profileName

        return UserIdentityUtils.resolveDisplayName(
            email = userEmail,
            fallback = "AlgoViz User"
        )
    }
    
    @OptIn(FlowPreview::class)
    private fun setupSearchDebounce() {
        viewModelScope.launch {
            _searchQuery
                .drop(1)
                .distinctUntilChanged()
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
                val user = awaitAuthenticatedUser()
                if (user == null) {
                    _uiState.value = StudyRoomsUiState.Error("User not authenticated")
                    return@launch
                }

                observeRoomMembers(user.id)
                
                val normalizedQuery = query.trim()
                getStudyRoomsUseCase()
                    .combine(getStudyRoomsUseCase.myRooms(user.id)) { allRooms, myRooms ->
                        val roomMatches: (com.algoviz.plus.domain.model.StudyRoom) -> Boolean = { room ->
                            room.name.contains(normalizedQuery, ignoreCase = true) ||
                                room.description.contains(normalizedQuery, ignoreCase = true) ||
                                room.category.displayName.contains(normalizedQuery, ignoreCase = true)
                        }

                        val filteredAllRooms = allRooms.filter(roomMatches)
                        val filteredMyRooms = myRooms.filter(roomMatches)

                        filteredAllRooms to filteredMyRooms
                    }
                    .combine(getStudyRoomsUseCase.unreadCounts(user.id)) { (filteredAllRooms, filteredMyRooms), unreadCounts ->
                        Triple(filteredAllRooms, filteredMyRooms, unreadCounts)
                    }
                    .combine(_roomMembersByRoom) { (filteredAllRooms, filteredMyRooms, unreadCounts), membersByRoom ->
                        Quadruple(filteredAllRooms, filteredMyRooms, unreadCounts, membersByRoom)
                    }
                    .combine(_onlineFriends) { (filteredAllRooms, filteredMyRooms, unreadCounts, membersByRoom), onlineFriends ->
                        StudyRoomsUiState.Success(
                            rooms = filteredAllRooms,
                            myRooms = filteredMyRooms,
                            selectedCategory = null,
                            loadingRoomId = null,
                            unreadCounts = unreadCounts,
                            roomMembersByRoom = membersByRoom,
                            onlineFriends = onlineFriends,
                            currentUserId = user.id
                        )
                    }
                    .catch { e ->
                        if (e !is CancellationException) {
                                _uiState.value = StudyRoomsUiState.Error(e.toUserMessage("Search failed"))
                        }
                    }
                    .collect { state ->
                        _uiState.value = state
                    }
            } catch (e: CancellationException) {
                // Ignore cancellation exceptions - this is normal when switching between search/load
                throw e
            } catch (e: Exception) {
                _uiState.value = StudyRoomsUiState.Error(e.toUserMessage("Search error"))
            }
        }
    }
    
    private fun loadRooms() {
        loadRoomsJob?.cancel()
        loadRoomsJob = viewModelScope.launch {
            try {
                val user = awaitAuthenticatedUser()
                if (user == null) {
                    _uiState.value = StudyRoomsUiState.Error("User not authenticated")
                    return@launch
                }

                observeRoomMembers(user.id)
                
                getStudyRoomsUseCase()
                    .combine(getStudyRoomsUseCase.myRooms(user.id)) { allRooms, myRooms ->
                        allRooms to myRooms
                    }
                    .combine(_selectedCategory) { (allRooms, myRooms), category ->
                        Triple(allRooms, myRooms, category)
                    }
                    .combine(getStudyRoomsUseCase.unreadCounts(user.id)) { (allRooms, myRooms, category), unreadCounts ->
                        Quadruple(allRooms, myRooms, category, unreadCounts)
                    }
                    .combine(_roomMembersByRoom) { (allRooms, myRooms, category, unreadCounts), membersByRoom ->
                        Quintuple(allRooms, myRooms, category, unreadCounts, membersByRoom)
                    }
                    .combine(_onlineFriends) { (allRooms, myRooms, category, unreadCounts, membersByRoom), onlineFriends ->
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
                            unreadCounts = unreadCounts,
                            roomMembersByRoom = membersByRoom,
                            onlineFriends = onlineFriends,
                            currentUserId = user.id
                        )
                    }
                .catch { e ->
                    if (e !is CancellationException) {
                        _uiState.value = StudyRoomsUiState.Error(e.toUserMessage("Failed to load rooms. Pull to refresh."))
                    }
                }
                .collect { state ->
                    _uiState.value = state
                }
            } catch (e: CancellationException) {
                // Ignore cancellation exceptions - this is normal when reloading
                throw e
            } catch (e: Exception) {
                _uiState.value = StudyRoomsUiState.Error(e.toUserMessage("An unexpected error occurred"))
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
                
                val userName = resolveDisplayName(user.email)
                
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
                        error.toUserMessage("Failed to join room. Please try again.")
                    )
                }
                
                // On success, sync member count to ensure consistency
                result.onSuccess {
                    syncMemberCountUseCase(roomId)
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
                _uiState.value = StudyRoomsUiState.Error(e.toUserMessage("An error occurred"))
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
                
                val creatorName = resolveDisplayName(user.email)
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
                        error?.toUserMessage("Failed to create room") ?: "Failed to create room"
                    )
                }
            } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
                _createRoomEvent.value = CreateRoomEvent.Error("Request timed out. Please try again.")
            } catch (e: Exception) {
                _createRoomEvent.value = CreateRoomEvent.Error(e.toUserMessage("An unexpected error occurred"))
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
                        error.toUserMessage("Failed to leave room. Please try again.")
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
                _uiState.value = StudyRoomsUiState.Error(e.toUserMessage("An error occurred"))
            }
        }
    }

    private fun observeRoomMembers(currentUserId: String) {
        if (membersObservationJob?.isActive == true) return

        membersObservationJob = viewModelScope.launch {
            combine(
                getStudyRoomsUseCase(),
                getStudyRoomsUseCase.myRooms(currentUserId)
            ) { allRooms, myRooms ->
                allRooms to myRooms
            }
                .distinctUntilChangedBy { (allRooms, myRooms) ->
                    allRooms.map { it.id }.sorted() to myRooms.map { it.id }.sorted()
                }
                .collectLatest { (allRooms, myRooms) ->
                    if (allRooms.isEmpty()) {
                        _roomMembersByRoom.value = emptyMap()
                        _onlineFriends.value = emptyList()
                        return@collectLatest
                    }

                    val myRoomIds = myRooms.map { it.id }.toSet()

                    val memberFlows = allRooms.map { room ->
                        getRoomMembersUseCase(room.id)
                            .map { members -> room.id to members }
                            .catch { emit(room.id to emptyList()) }
                    }

                    combine(memberFlows) { roomMembers ->
                        roomMembers.toMap()
                    }
                        .catch {
                            _roomMembersByRoom.value = emptyMap()
                            _onlineFriends.value = emptyList()
                        }
                        .collect { membersByRoom ->
                            _roomMembersByRoom.value = membersByRoom

                            val onlineFriends = membersByRoom
                                .filterKeys { roomId -> roomId in myRoomIds }
                                .values
                                .flatten()
                                .filter { member -> member.userId != currentUserId && member.isOnline }
                                .groupBy { member -> member.userId }
                                .map { (_, entries) -> entries.first() }
                                .sortedBy { member -> member.userName.lowercase() }

                            _onlineFriends.value = onlineFriends
                        }
                }
        }
    }
}

private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)

private data class Quintuple<A, B, C, D, E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E
)
