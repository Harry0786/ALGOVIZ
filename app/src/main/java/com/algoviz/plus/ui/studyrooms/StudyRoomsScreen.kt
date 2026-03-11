package com.algoviz.plus.ui.studyrooms

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.algoviz.plus.domain.model.RoomCategory
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
    onBackClick: (() -> Unit)? = null,
    viewModel: StudyRoomsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val createRoomEvent by viewModel.createRoomEvent.collectAsStateWithLifecycle()
    var showCreateDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    var isRefreshing by remember { mutableStateOf(false) }
    
    // Handle create room events
    LaunchedEffect(createRoomEvent) {
        when (val event = createRoomEvent) {
            is com.algoviz.plus.ui.studyrooms.state.CreateRoomEvent.Success -> {
                snackbarHostState.showSnackbar("Room created successfully!")
                showCreateDialog = false
                viewModel.clearCreateRoomEvent()
            }
            is com.algoviz.plus.ui.studyrooms.state.CreateRoomEvent.Error -> {
                snackbarHostState.showSnackbar(event.message)
                viewModel.clearCreateRoomEvent()
            }
            else -> {}
        }
    }
    
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
                                imageVector = Icons.Default.ArrowBack,
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
                        onClick = { showCreateDialog = true },
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
    
    if (showCreateDialog) {
        CreateRoomDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { name, description, category ->
                viewModel.onAction(StudyRoomAction.CreateRoom(name, description, category))
            },
            isLoading = createRoomEvent is com.algoviz.plus.ui.studyrooms.state.CreateRoomEvent.Loading
        )
    }
}

@Composable
fun StudyRoomsList(
    rooms: List<StudyRoom>,
    myRooms: List<StudyRoom>,
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
                    Text(
                        text = room.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Text(
                        text = room.category.displayName,
                        fontSize = 13.sp,
                        color = Color(0xFF5EEAD4)
                    )
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
                        text = "${room.memberCount} members",
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRoomDialog(
    onDismiss: () -> Unit,
    onCreate: (name: String, description: String, category: String) -> Unit,
    isLoading: Boolean = false
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(RoomCategory.GENERAL) }
    var categoryMenuExpanded by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        containerColor = Color(0xFF1A1344),
        shape = RoundedCornerShape(20.dp),
        title = { 
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF5EEAD4), Color(0xFF14B8A6))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        tint = Color(0xFF1A1344),
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Create Study Room",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.White
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { if (it.length <= 50) name = it },
                    label = { Text("Room Name (${name.length}/50)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = name.length < 3 && name.isNotEmpty(),
                    supportingText = if (name.isNotEmpty() && name.length < 3) {
                        { Text("Name must be at least 3 characters", color = Color(0xFFEF4444)) }
                    } else null,
                    enabled = !isLoading,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF5EEAD4),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                        focusedLabelColor = Color(0xFF5EEAD4),
                        unfocusedLabelColor = Color.White.copy(alpha = 0.6f),
                        cursorColor = Color(0xFF5EEAD4),
                        errorBorderColor = Color(0xFFEF4444),
                        errorLabelColor = Color(0xFFEF4444),
                        focusedContainerColor = Color(0xFF2D1B69).copy(alpha = 0.3f),
                        unfocusedContainerColor = Color(0xFF2D1B69).copy(alpha = 0.2f)
                    )
                )
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { if (it.length <= 200) description = it },
                    label = { Text("Description (${description.length}/200)") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    isError = description.length < 10 && description.isNotEmpty(),
                    supportingText = if (description.isNotEmpty() && description.length < 10) {
                        { Text("Description must be at least 10 characters", color = Color(0xFFEF4444)) }
                    } else null,
                    enabled = !isLoading,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF5EEAD4),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                        focusedLabelColor = Color(0xFF5EEAD4),
                        unfocusedLabelColor = Color.White.copy(alpha = 0.6f),
                        cursorColor = Color(0xFF5EEAD4),
                        errorBorderColor = Color(0xFFEF4444),
                        errorLabelColor = Color(0xFFEF4444),
                        focusedContainerColor = Color(0xFF2D1B69).copy(alpha = 0.3f),
                        unfocusedContainerColor = Color(0xFF2D1B69).copy(alpha = 0.2f)
                    )
                )
                
                // Category dropdown
                ExposedDropdownMenuBox(
                    expanded = categoryMenuExpanded,
                    onExpandedChange = { if (!isLoading) categoryMenuExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedCategory.displayName,
                        onValueChange = {},
                        label = { Text("Category") },
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(
                                expanded = categoryMenuExpanded
                            )
                        },
                        enabled = !isLoading,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF5EEAD4),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                            focusedLabelColor = Color(0xFF5EEAD4),
                            unfocusedLabelColor = Color.White.copy(alpha = 0.6f),
                            focusedContainerColor = Color(0xFF2D1B69).copy(alpha = 0.3f),
                            unfocusedContainerColor = Color(0xFF2D1B69).copy(alpha = 0.2f),
                            focusedTrailingIconColor = Color(0xFF5EEAD4),
                            unfocusedTrailingIconColor = Color.White.copy(alpha = 0.6f)
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = categoryMenuExpanded,
                        onDismissRequest = { categoryMenuExpanded = false },
                        modifier = Modifier.background(Color(0xFF2D1B69))
                    ) {
                        RoomCategory.entries.forEach { category ->
                            DropdownMenuItem(
                                text = { 
                                    Text(
                                        category.displayName,
                                        color = Color.White
                                    ) 
                                },
                                onClick = {
                                    selectedCategory = category
                                    categoryMenuExpanded = false
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                                modifier = Modifier.background(
                                    if (category == selectedCategory) 
                                        Color(0xFF5EEAD4).copy(alpha = 0.2f) 
                                    else 
                                        Color.Transparent
                                )
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onCreate(name, description, selectedCategory.name)
                },
                enabled = !isLoading && name.length >= 3 && description.length >= 10,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF5EEAD4),
                    contentColor = Color(0xFF1A1344),
                    disabledContainerColor = Color(0xFF374151),
                    disabledContentColor = Color(0xFF6B7280)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(48.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = Color(0xFF1A1344)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    if (isLoading) "Creating..." else "Create",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(48.dp)
            ) {
                Text(
                    "Cancel",
                    color = Color.White.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp
                )
            }
        }
    )
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
