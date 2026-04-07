package com.algoviz.plus.ui.studyrooms

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.algoviz.plus.domain.model.RoomCategory
import com.algoviz.plus.ui.notifications.InAppNotification
import com.algoviz.plus.ui.notifications.InAppNotificationCenter
import com.algoviz.plus.ui.studyrooms.state.CreateRoomEvent
import com.algoviz.plus.ui.studyrooms.state.StudyRoomAction
import com.algoviz.plus.ui.studyrooms.viewmodel.StudyRoomsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRoomScreen(
    onBackClick: () -> Unit,
    onRoomCreated: () -> Unit,
    viewModel: StudyRoomsViewModel = hiltViewModel()
) {
    val createRoomEvent by viewModel.createRoomEvent.collectAsStateWithLifecycle()

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(RoomCategory.GENERAL) }
    var maxMembers by remember { mutableStateOf(50f) }
    var isPrivate by remember { mutableStateOf(false) }

    val isLoading = createRoomEvent is CreateRoomEvent.Loading

    LaunchedEffect(createRoomEvent) {
        when (val event = createRoomEvent) {
            is CreateRoomEvent.Success -> {
                viewModel.clearCreateRoomEvent()
                onRoomCreated()
            }
            is CreateRoomEvent.Error -> {
                InAppNotificationCenter.post(
                    InAppNotification(
                        title = "Room creation failed",
                        message = event.message,
                        type = com.algoviz.plus.ui.notifications.InAppNotificationType.Error,
                        groupKey = "create_room_errors",
                        dedupeKey = "create_room_error:${event.message}"
                    )
                )
                viewModel.clearCreateRoomEvent()
            }
            else -> {}
        }
    }

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFF0B0B0D), Color(0xFF141418), Color(0xFF1A1A1F))
    )

    Scaffold(
        modifier = Modifier.background(backgroundBrush),
        containerColor = Color.Transparent,
        topBar = {
            Surface(color = Color(0xFF0B0B0D).copy(alpha = 0.95f)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { if (!isLoading) onBackClick() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Text(
                        text = "Create Study Room",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush)
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Header icon
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(Color(0xFFF3F4F6), Color(0xFF14B8A6))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Group,
                                contentDescription = null,
                                tint = Color(0xFF0B0B0D),
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                }

                // Room Name
                item {
                    SectionLabel("Room Name")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = name,
                        onValueChange = { if (it.length <= 50) name = it },
                        placeholder = { Text("e.g. Binary Search Study Group", color = Color.White.copy(alpha = 0.4f)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading,
                        isError = name.isNotEmpty() && name.length < 3,
                        supportingText = {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                if (name.isNotEmpty() && name.length < 3)
                                    Text("Minimum 3 characters", color = Color(0xFFEF4444), fontSize = 12.sp)
                                else
                                    Spacer(modifier = Modifier.weight(1f))
                                Text("${name.length}/50", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = roomFieldColors()
                    )
                }

                // Description
                item {
                    SectionLabel("Description")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = description,
                        onValueChange = { if (it.length <= 300) description = it },
                        placeholder = { Text("What will members study in this room?", color = Color.White.copy(alpha = 0.4f)) },
                        minLines = 3,
                        maxLines = 5,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading,
                        isError = description.isNotEmpty() && description.length < 10,
                        supportingText = {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                if (description.isNotEmpty() && description.length < 10)
                                    Text("Minimum 10 characters", color = Color(0xFFEF4444), fontSize = 12.sp)
                                else
                                    Spacer(modifier = Modifier.weight(1f))
                                Text("${description.length}/300", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = roomFieldColors()
                    )
                }

                // Category
                item {
                    SectionLabel("Category")
                    Spacer(modifier = Modifier.height(10.dp))
                    CategoryChipGrid(
                        selected = selectedCategory,
                        onSelect = { selectedCategory = it },
                        enabled = !isLoading
                    )
                }

                // Max Members
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SectionLabel("Max Members")
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFF3F4F6).copy(alpha = 0.15f))
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = maxMembers.toInt().toString(),
                                color = Color(0xFFF3F4F6),
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Slider(
                        value = maxMembers,
                        onValueChange = { maxMembers = it },
                        valueRange = 2f..100f,
                        steps = 97,
                        enabled = !isLoading,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFFF3F4F6),
                            activeTrackColor = Color(0xFFF3F4F6),
                            inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("2", color = Color.White.copy(alpha = 0.4f), fontSize = 12.sp)
                        Text("100", color = Color.White.copy(alpha = 0.4f), fontSize = 12.sp)
                    }
                }

                // Private / Public Toggle
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.07f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = !isLoading) { isPrivate = !isPrivate }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isPrivate) Icons.Default.Lock else Icons.Default.LockOpen,
                                contentDescription = null,
                                tint = if (isPrivate) Color(0xFFFBBF24) else Color(0xFFF3F4F6),
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isPrivate) "Private Room" else "Public Room",
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = if (isPrivate)
                                        "Only invited members can join"
                                    else
                                        "Anyone can discover and join",
                                    color = Color.White.copy(alpha = 0.55f),
                                    fontSize = 13.sp
                                )
                            }
                            Switch(
                                checked = isPrivate,
                                onCheckedChange = { if (!isLoading) isPrivate = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFFFBBF24),
                                    uncheckedThumbColor = Color.White,
                                    uncheckedTrackColor = Color(0xFFF3F4F6)
                                )
                            )
                        }
                    }
                }

                // Create Button
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    val canCreate = name.length >= 3 && description.length >= 10 && !isLoading
                    Button(
                        onClick = {
                            viewModel.onAction(
                                StudyRoomAction.CreateRoom(
                                    name = name.trim(),
                                    description = description.trim(),
                                    category = selectedCategory.name,
                                    maxMembers = maxMembers.toInt(),
                                    isPrivate = isPrivate
                                )
                            )
                        },
                        enabled = canCreate,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF3F4F6),
                            contentColor = Color(0xFF0B0B0D),
                            disabledContainerColor = Color(0xFF374151),
                            disabledContentColor = Color(0xFF6B7280)
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.dp,
                                color = Color(0xFF0B0B0D)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Creating...", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        } else {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Create Room", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        color = Color(0xFFF3F4F6),
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.5.sp
    )
}

@Composable
private fun CategoryChipGrid(
    selected: RoomCategory,
    onSelect: (RoomCategory) -> Unit,
    enabled: Boolean
) {
    val categories = RoomCategory.entries
    val rowSize = 2
    val rows = categories.chunked(rowSize)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                row.forEach { category ->
                    val isSelected = category == selected
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isSelected) Color(0xFFF3F4F6).copy(alpha = 0.2f)
                                else Color.White.copy(alpha = 0.07f)
                            )
                            .border(
                                width = if (isSelected) 1.5.dp else 0.5.dp,
                                color = if (isSelected) Color(0xFFF3F4F6) else Color.White.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable(enabled = enabled) { onSelect(category) }
                            .padding(horizontal = 10.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = category.displayName,
                            color = if (isSelected) Color(0xFFF3F4F6) else Color.White.copy(alpha = 0.75f),
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            maxLines = 2,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            lineHeight = 16.sp
                        )
                    }
                }
                if (row.size < rowSize) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun roomFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedBorderColor = Color(0xFFF3F4F6),
    unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
    focusedLabelColor = Color(0xFFF3F4F6),
    unfocusedLabelColor = Color.White.copy(alpha = 0.6f),
    cursorColor = Color(0xFFF3F4F6),
    errorBorderColor = Color(0xFFEF4444),
    errorLabelColor = Color(0xFFEF4444),
    focusedContainerColor = Color(0xFF141418).copy(alpha = 0.3f),
    unfocusedContainerColor = Color(0xFF141418).copy(alpha = 0.2f)
)
