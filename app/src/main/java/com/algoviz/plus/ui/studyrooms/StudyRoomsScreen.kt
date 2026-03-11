package com.algoviz.plus.ui.studyrooms

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.algoviz.plus.domain.model.StudyRoom
import com.algoviz.plus.ui.studyrooms.state.StudyRoomAction
import com.algoviz.plus.ui.studyrooms.state.StudyRoomsUiState
import com.algoviz.plus.ui.studyrooms.viewmodel.StudyRoomsViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyRoomsScreen(
    onRoomClick: (String) -> Unit,
    onCreateRoomClick: () -> Unit = {},
    onBackClick: (() -> Unit)? = null,
    viewModel: StudyRoomsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    var isRefreshing by remember { mutableStateOf(false) }
    
    // Handle refresh state
    LaunchedEffect(uiState) {
        if (uiState is StudyRoomsUiState.Success || uiState is StudyRoomsUiState.Error) {
            isRefreshing = false
        }
    }
    
    Scaffold(
        modifier = Modifier.background(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF1A1344),
                    Color(0xFF2D1B69),
                    Color(0xFF3D2080)
                )
            )
        ),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1A1344),
                            Color(0xFF2D1B69),
                            Color(0xFF3D2080)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Custom Top Bar with Title and Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, start = 16.dp, end = 16.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (onBackClick != null) {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    }
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Study Rooms",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    
                    Button(
                        onClick = { onCreateRoomClick() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF5EEAD4),
                            contentColor = Color(0xFF1A1344)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Create Room",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Create", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
                
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    // Search bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = {
                            searchQuery = it
                            viewModel.onAction(StudyRoomAction.SearchRooms(it))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { 
                            Text("Search rooms...", color = Color.White.copy(alpha = 0.5f)) 
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Search, "Search", tint = Color.White.copy(alpha = 0.7f))
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF5EEAD4),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                            cursorColor = Color(0xFF5EEAD4)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                
                    // Content
                    when (val state = uiState) {
                        is StudyRoomsUiState.Loading -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = Color(0xFF5EEAD4))
                            }
                        }
                        is StudyRoomsUiState.Success -> {
                            StudyRoomsList(
                                rooms = state.rooms,
                                myRooms = state.myRooms,
                                unreadCounts = state.unreadCounts,
                                onRoomClick = onRoomClick,
                                onJoinRoom = { roomId ->
                                    viewModel.onAction(StudyRoomAction.JoinRoom(roomId))
                                },
                                onLeaveRoom = { roomId ->
                                    viewModel.onAction(StudyRoomAction.LeaveRoom(roomId))
                                },
                                loadingRoomId = state.loadingRoomId
                            )
                        }
                        is StudyRoomsUiState.Error -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(16.dp),
                                    modifier = Modifier.padding(20.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Error,
                                        contentDescription = null,
                                        tint = Color(0xFFEF4444),
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Text(
                                        text = state.message,
                                        color = Color(0xFFEF4444),
                                        fontSize = 14.sp,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                    Button(
                                        onClick = { viewModel.retryLoadRooms() },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF5EEAD4),
                                            contentColor = Color(0xFF1A1344)
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Retry")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}

@Composable
fun StudyRoomsList(
    rooms: List<StudyRoom>,
    myRooms: List<StudyRoom>,
    unreadCounts: Map<String, Int>,
    onRoomClick: (String) -> Unit,
    onJoinRoom: (String) -> Unit,
    onLeaveRoom: (String) -> Unit,
    loadingRoomId: String? = null
) {
    val availableRooms = rooms.filter { room -> myRooms.none { it.id == room.id } }
    
    if (rooms.isEmpty() && myRooms.isEmpty()) {
        // Empty state
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Group,
                    contentDescription = null,
                    tint = Color(0xFF5EEAD4),
                    modifier = Modifier.size(64.dp)
                )
                Text(
                    text = "No study rooms yet",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Text(
                    text = "Create one to start collaborating!",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
        }
        return
    }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (myRooms.isNotEmpty()) {
            item {
                Text(
                    text = "My Rooms",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            items(myRooms) { room ->
                RoomCard(
                    room = room,
                    isJoined = true,
                    unreadCount = unreadCounts[room.id] ?: 0,
                    onClick = { onRoomClick(room.id) },
                    onActionClick = { onLeaveRoom(room.id) },
                    isLoading = loadingRoomId == room.id
                )
            }
            
            if (availableRooms.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "All Rooms",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }
        }
        
        items(availableRooms) { room ->
            RoomCard(
                room = room,
                isJoined = false,
                unreadCount = unreadCounts[room.id] ?: 0,
                onClick = { onRoomClick(room.id) },
                onActionClick = { onJoinRoom(room.id) },
                isLoading = loadingRoomId == room.id
            )
        }
    }
}

@Composable
fun RoomCard(
    room: StudyRoom,
    isJoined: Boolean,
    unreadCount: Int,
    onClick: () -> Unit,
    onActionClick: () -> Unit,
    isLoading: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isJoined) Modifier.clickable(onClick = onClick)
                else Modifier
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.08f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = room.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                        if (room.isPrivate) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = "Private",
                                tint = Color(0xFFFBBF24),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    Text(
                        text = room.category.displayName,
                        fontSize = 13.sp,
                        color = Color(0xFF5EEAD4)
                    )
                    if (isJoined && unreadCount > 0) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            color = Color(0xFF22C55E),
                            shape = RoundedCornerShape(999.dp)
                        ) {
                            Text(
                                text = if (unreadCount > 99) "99+" else unreadCount.toString(),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
                
                Button(
                    onClick = onActionClick,
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isJoined) Color(0xFFEF4444) else Color(0xFF5EEAD4),
                        contentColor = if (isJoined) Color.White else Color(0xFF1A1344)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(if (isJoined) "Leave" else "Join")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = room.description,
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.65f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${room.memberCount}/${room.maxMembers}",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
                
                room.lastMessageAt?.let { timestamp ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = formatTime(timestamp),
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}

private fun formatTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60_000 -> "Just now"
        diff < 3_600_000 -> "${diff / 60_000}m ago"
        diff < 86_400_000 -> "${diff / 3_600_000}h ago"
        else -> SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(timestamp))
    }
}
