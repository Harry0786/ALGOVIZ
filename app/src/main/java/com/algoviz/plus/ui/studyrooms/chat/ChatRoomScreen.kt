package com.algoviz.plus.ui.studyrooms.chat

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.algoviz.plus.domain.model.Message
import com.algoviz.plus.domain.model.MessageType
import com.algoviz.plus.ui.studyrooms.chat.state.ChatRoomUiState
import com.algoviz.plus.ui.studyrooms.chat.viewmodel.ChatRoomViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatRoomScreen(
    onBackClick: () -> Unit,
    viewModel: ChatRoomViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val messageInput by viewModel.messageInput.collectAsStateWithLifecycle()
    val sendMessageError by viewModel.sendMessageError.collectAsStateWithLifecycle()
    val isSending by viewModel.isSending.collectAsStateWithLifecycle()
    val isDeletingRoom by viewModel.isDeletingRoom.collectAsStateWithLifecycle()
    val deleteRoomError by viewModel.deleteRoomError.collectAsStateWithLifecycle()
    val roomDeleted by viewModel.roomDeleted.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var showMemberList by remember { mutableStateOf(false) }
    var showCodeSnippetDialog by remember { mutableStateOf(false) }
    var showDeleteGroupConfirm by remember { mutableStateOf(false) }
    
    // Track message count to only scroll on new messages, not on every state change
    var lastMessageCount by remember { mutableStateOf(0) }
    var lastNotifiedMessageId by remember { mutableStateOf<String?>(null) }
    var lastIncomingAlertAt by remember { mutableLongStateOf(0L) }
    val incomingAlertCooldownMs = 2500L


    
    // Show error when message sending fails
    LaunchedEffect(sendMessageError) {
        sendMessageError?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearSendMessageError()
        }
    }

    LaunchedEffect(deleteRoomError) {
        deleteRoomError?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearDeleteRoomError()
        }
    }

    LaunchedEffect(roomDeleted) {
        if (roomDeleted) {
            showMemberList = false
            showDeleteGroupConfirm = false
            snackbarHostState.showSnackbar("Group deleted")
            viewModel.clearRoomDeletedEvent()
            onBackClick()
        }
    }
    
    // Auto-scroll to bottom when new messages arrive
    val messageCount = (uiState as? ChatRoomUiState.Success)?.messages?.size ?: 0
    LaunchedEffect(messageCount) {
        if (messageCount == 0) {
            lastMessageCount = 0
            return@LaunchedEffect
        }

        if (lastMessageCount == 0) {
            listState.scrollToItem(messageCount - 1)
        } else if (messageCount > lastMessageCount) {
            listState.animateScrollToItem(messageCount - 1)
        }
        lastMessageCount = messageCount
    }

    LaunchedEffect(uiState) {
        val state = uiState as? ChatRoomUiState.Success ?: return@LaunchedEffect
        val latestMessage = state.messages.lastOrNull() ?: return@LaunchedEffect

        if (lastNotifiedMessageId == null) {
            lastNotifiedMessageId = latestMessage.id
            return@LaunchedEffect
        }

        val now = System.currentTimeMillis()
        if (
            latestMessage.id != lastNotifiedMessageId &&
            latestMessage.userId != state.currentUserId &&
            now - lastIncomingAlertAt >= incomingAlertCooldownMs
        ) {
            snackbarHostState.showSnackbar("${latestMessage.userName}: ${latestMessage.content.take(80)}")
            lastIncomingAlertAt = now
        }

        lastNotifiedMessageId = latestMessage.id
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            when (val state = uiState) {
                is ChatRoomUiState.Success -> {
                    TopAppBar(
                        title = {
                            Column {
                                Text(
                                    text = state.room.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Group,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = Color(0xFF5EEAD4)
                                    )
                                    Text(
                                        text = "${state.room.memberCount} members",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 12.sp,
                                        color = Color(0xFF9CA3AF)
                                    )
                                    Text(
                                        text = "•",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 12.sp,
                                        color = Color(0xFF9CA3AF)
                                    )
                                    Text(
                                        text = state.room.category.displayName,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 12.sp,
                                        color = Color(0xFF9CA3AF)
                                    )
                                }
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = onBackClick) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                            }
                        },
                        actions = {
                            IconButton(onClick = { showMemberList = true }) {
                                Icon(Icons.Default.Person, "Members")
                            }
                        },
        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color(0xFF1A1344),
                            titleContentColor = Color.White,
                            navigationIconContentColor = Color(0xFF5EEAD4),
                            actionIconContentColor = Color(0xFF5EEAD4)
                        ),
                        modifier = Modifier.shadow(8.dp)
                    )
                }
                else -> {
                    TopAppBar(
                        title = { Text("Chat Room") },
                        navigationIcon = {
                            IconButton(onClick = onBackClick) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color(0xFF1A1344),
                            titleContentColor = Color.White,
                            navigationIconContentColor = Color(0xFF5EEAD4)
                        ),
                        modifier = Modifier.shadow(8.dp)
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFF0D0B1E))
        ) {
            when (val state = uiState) {
                is ChatRoomUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is ChatRoomUiState.Success -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Messages list
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            state = listState,
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (state.messages.isEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 120.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(16.dp),
                                            modifier = Modifier.padding(24.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(80.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        brush = Brush.linearGradient(
                                                            colors = listOf(
                                                                Color(0xFF5EEAD4).copy(alpha = 0.2f),
                                                                Color(0xFF6366F1).copy(alpha = 0.2f)
                                                            )
                                                        )
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Forum,
                                                    contentDescription = null,
                                                    tint = Color(0xFF5EEAD4),
                                                    modifier = Modifier.size(40.dp)
                                                )
                                            }
                                            Text(
                                                text = "Start the Discussion!",
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                            Text(
                                                text = "Share ideas, ask questions, or discuss algorithms.\nUse code snippets, media, and voice messages!",
                                                fontSize = 13.sp,
                                                color = Color(0xFF9CA3AF),
                                                textAlign = TextAlign.Center,
                                                lineHeight = 18.sp
                                            )
                                            
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                                modifier = Modifier.padding(top = 8.dp)
                                            ) {
                                                FeatureChip(
                                                    icon = Icons.Default.Code,
                                                    text = "Code"
                                                )
                                                FeatureChip(
                                                    icon = Icons.Default.Image,
                                                    text = "Media"
                                                )
                                                FeatureChip(
                                                    icon = Icons.Default.Mic,
                                                    text = "Voice"
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            items(state.messages) { message ->
                                MessageBubble(
                                    message = message,
                                    isOwnMessage = message.userId == state.currentUserId
                                )
                            }
                        }
                        
                        // Message input
                        MessageInputBar(
                            modifier = Modifier
                                .navigationBarsPadding()
                                .imePadding(),
                            value = messageInput,
                            onValueChange = { viewModel.updateMessageInput(it) },
                            onSend = { viewModel.sendMessage() },
                            onCodeSnippetClick = { showCodeSnippetDialog = true },
                            isSending = isSending
                        )
                    }
                }
                is ChatRoomUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = "Error",
                            modifier = Modifier.size(48.dp),
                            tint = Color(0xFFEF4444)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = state.message,
                            color = Color(0xFFEF4444),
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(horizontal = 24.dp)
                        ) {
                            Button(
                                onClick = { viewModel.retryLoadChatRoom() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5EEAD4)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Retry", color = Color.White)
                            }
                            Button(
                                onClick = onBackClick,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C7280)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Back", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Member List Dialog
    if (showMemberList) {
        val successState = (uiState as? ChatRoomUiState.Success)
        if (successState != null) {
            ModalBottomSheet(
                onDismissRequest = { showMemberList = false },
                containerColor = Color(0xFF1A1344)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Room header
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(Color(0xFF5EEAD4), Color(0xFF14B8A6))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = successState.room.name.firstOrNull()?.uppercase() ?: "?",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 32.sp
                            )
                        }
                        
                        Text(
                            text = successState.room.name,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        
                        Text(
                            text = successState.room.description.ifBlank { "No description" },
                            fontSize = 13.sp,
                            color = Color(0xFF9CA3AF),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                    
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                    
                    // Room info
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        RoomInfoItem(
                            icon = Icons.Default.Info,
                            label = "Room Settings",
                            value = "Category: ${successState.room.category.displayName}"
                        )
                        RoomInfoItem(
                            icon = Icons.Default.AdminPanelSettings,
                            label = "Creator",
                            value = if (successState.room.createdBy == successState.currentUserId) {
                                "You"
                            } else {
                                successState.room.createdBy
                            }
                        )
                        RoomInfoItem(
                            icon = Icons.Default.Group,
                            label = "Members",
                            value = "${successState.members.size} member${if (successState.members.size != 1) "s" else ""}"
                        )
                        RoomInfoItem(
                            icon = Icons.Default.Schedule,
                            label = "Created",
                            value = formatMessageTime(successState.room.createdAt)
                        )
                    }

                    if (successState.room.createdBy == successState.currentUserId) {
                        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                        Text(
                            text = "Group Actions",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                        Button(
                            onClick = { showDeleteGroupConfirm = true },
                            enabled = !isDeletingRoom,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFDC2626),
                                contentColor = Color.White,
                                disabledContainerColor = Color(0xFF7F1D1D),
                                disabledContentColor = Color.White.copy(alpha = 0.7f)
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (isDeletingRoom) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(if (isDeletingRoom) "Deleting Group..." else "Delete Group")
                        }
                    }
                    
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                    
                    // Members section
                    Text(
                        text = "Members (${successState.members.size})",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    
                    // Member list - using Column instead of LazyColumn to avoid nested scrollables
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        successState.members.forEach { member ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        color = Color(0xFF2D1B69).copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .padding(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF5EEAD4)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = member.userName.firstOrNull()?.uppercase() ?: "?",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = member.userName,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        color = Color.White
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (member.isOnline) Color(0xFF10B981) else Color.Gray
                                                )
                                        )
                                        Text(
                                            text = if (member.isOnline) "Online" else "Offline",
                                            fontSize = 11.sp,
                                            color = if (member.isOnline) Color(0xFF10B981) else Color.Gray
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }

    if (showDeleteGroupConfirm) {
        AlertDialog(
            onDismissRequest = {
                if (!isDeletingRoom) {
                    showDeleteGroupConfirm = false
                }
            },
            title = { Text("Delete Group") },
            text = {
                Text(
                    "This will remove the group for all members and send a final system message notification. This action cannot be undone."
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.deleteRoom() },
                    enabled = !isDeletingRoom,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFDC2626),
                        contentColor = Color.White
                    )
                ) {
                    Text(if (isDeletingRoom) "Deleting..." else "Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteGroupConfirm = false },
                    enabled = !isDeletingRoom
                ) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Code Snippet Dialog
    if (showCodeSnippetDialog) {
        var codeInput by remember { mutableStateOf("") }
        
        AlertDialog(
            onDismissRequest = { showCodeSnippetDialog = false },
            title = {
                Text(
                    "Send Code Snippet",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Language will be auto-detected",
                        fontSize = 12.sp,
                        color = Color(0xFF5EEAD4),
                        fontWeight = FontWeight.Medium
                    )
                    
                    OutlinedTextField(
                        value = codeInput,
                        onValueChange = { codeInput = it },
                        label = { Text("Code") },
                        placeholder = { Text("Paste your code here...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        maxLines = 10,
                        textStyle = LocalTextStyle.current.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (codeInput.isNotBlank()) {
                            val detectedLang = detectProgrammingLanguage(codeInput)
                            val formattedMessage = "```$detectedLang\\n$codeInput\\n```"
                            viewModel.sendMessage(type = "CODE", contentOverride = formattedMessage)
                            showCodeSnippetDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF5EEAD4)
                    )
                ) {
                    Text("Send")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCodeSnippetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun AttachmentOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF2D1B69).copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF5EEAD4)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = Color(0xFF1A1344),
                    modifier = Modifier.size(24.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = Color.White
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = Color(0xFF9CA3AF)
                )
            }
        }
    }
}

@Composable
fun MessageBubble(
    message: Message,
    isOwnMessage: Boolean
) {
    val isCodeSnippet =
        message.type == MessageType.CODE ||
            (message.content.startsWith("```") && message.content.endsWith("```"))
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 12.dp)
    ) {
        // Username for receiver messages - outside bubble
        if (!isOwnMessage) {
            Row(
                modifier = Modifier.padding(start = 44.dp, bottom = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = message.userName,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    color = Color(0xFF5EEAD4)
                )
            }
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (isOwnMessage) Arrangement.End else Arrangement.Start
        ) {
            // Receiver message - avatar on left
            if (!isOwnMessage) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF6366F1), Color(0xFF4F46E5))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = message.userName.firstOrNull()?.uppercase() ?: "?",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
            
            // Message bubble content - styling differs for sender/receiver
            Surface(
                shape = RoundedCornerShape(
                    topStart = if (isOwnMessage) 12.dp else 0.dp,
                    topEnd = if (isOwnMessage) 0.dp else 12.dp,
                    bottomStart = 12.dp,
                    bottomEnd = 12.dp
                ),
                color = if (isOwnMessage) Color(0xFF0D9B6B) else Color(0xFF2D1B69),
                modifier = Modifier.widthIn(max = 300.dp),
                shadowElevation = 4.dp,
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = if (isOwnMessage) 
                        Color(0xFF10B981).copy(alpha = 0.3f) 
                    else 
                        Color(0xFF4F46E5).copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    horizontalAlignment = if (isOwnMessage) Alignment.End else Alignment.Start
                ) {
                    // Message content
                    if (isCodeSnippet) {
                        CodeSnippetCard(
                            code = message.content.removeSurrounding("```").trim(),
                            isOwnMessage = isOwnMessage
                        )
                    } else {
                        Text(
                            text = message.content,
                            color = if (message.type == MessageType.SYSTEM) Color(0xFFFDE68A)
                            else if (isOwnMessage) Color.White
                            else Color.White.copy(alpha = 0.95f),
                            style = MaterialTheme.typography.bodyMedium,
                            fontSize = 13.sp,
                            lineHeight = 19.sp
                        )
                    }
                }
            }
            
            // Sender message - avatar on right
            if (isOwnMessage) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF5EEAD4), Color(0xFF14B8A6))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Y",
                        color = Color(0xFF1A1344),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
        
        // Timestamp and edited indicator outside bubble
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp, start = if (isOwnMessage) 0.dp else 44.dp, end = if (isOwnMessage) 44.dp else 0.dp),
            horizontalArrangement = if (isOwnMessage) Arrangement.End else Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = formatMessageTime(message.timestamp),
                style = MaterialTheme.typography.bodySmall,
                fontSize = 9.sp,
                color = Color(0xFF9CA3AF)
            )
            if (message.edited) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "(edited)",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 9.sp,
                    color = Color(0xFF9CA3AF)
                )
            }
        }
    }
}

@Composable
fun CodeSnippetCard(
    code: String,
    isOwnMessage: Boolean
) {
    // Auto-detect language from code content
    val detectedLanguage = detectProgrammingLanguage(code)
    val lines = code.lines()
    val codeContent = if (lines.isNotEmpty() && lines.first().length < 20 && !lines.first().contains(" ")) {
        // First line looks like a language identifier, remove it
        lines.drop(1).joinToString("\n")
    } else {
        code
    }
    
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF1A1A2E),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 400.dp),
        shadowElevation = 2.dp,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = Color(0xFF5EEAD4).copy(alpha = 0.3f)
        )
    ) {
        Column {
            // Language header
            Surface(
                color = if (isOwnMessage) Color(0xFF0D9B6B).copy(alpha = 0.3f) 
                       else Color(0xFF4F46E5).copy(alpha = 0.3f),
                shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Code,
                        contentDescription = null,
                        tint = Color(0xFF5EEAD4),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = detectedLanguage.uppercase(),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF5EEAD4),
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
            
            // Code content with horizontal scrolling
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 350.dp)
                    .verticalScroll(rememberScrollState())
                    .horizontalScroll(rememberScrollState())
                    .padding(12.dp)
            ) {
                codeContent.lines().forEachIndexed { lineIndex, line ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 1.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Line number
                        Text(
                            text = (lineIndex + 1).toString().padStart(3),
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF6B7280),
                            modifier = Modifier.padding(end = 4.dp),
                            textAlign = TextAlign.End
                        )
                        // Code line
                        Text(
                            text = line.ifEmpty { " " },
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFFE5E7EB),
                            lineHeight = 20.sp,
                            modifier = Modifier.wrapContentWidth()
                        )
                    }
                }
            }
        }
    }
}

// Helper function to auto-detect programming language
private fun detectProgrammingLanguage(code: String): String {
    return when {
        code.contains("fun ") || code.contains("class ") || code.contains("val ") -> "kotlin"
        code.contains("public class") || code.contains("import java") -> "java"
        code.contains("def ") || code.contains("import ") && code.contains(":") -> "python"
        code.contains("function") || code.contains("const ") || code.contains("let ") -> "javascript"
        code.contains("#include") || code.contains("int main") -> "cpp"
        code.contains("<?php") -> "php"
        code.contains("SELECT") || code.contains("INSERT") -> "sql"
        code.contains("<html") || code.contains("<!DOCTYPE") -> "html"
        code.contains("{") && code.contains("}") -> "json"
        else -> "code"
    }
}

@Composable
fun MessageInputBar(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    onCodeSnippetClick: () -> Unit,
    isSending: Boolean
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(8.dp),
        color = Color(0xFF1A1344),
        tonalElevation = 8.dp
    ) {
        // Main input row with code button integrated
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                placeholder = { 
                    Text(
                        "Type a message...",
                        color = Color(0xFF9CA3AF)
                    ) 
                },
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF2D1B69).copy(alpha = 0.5f),
                    unfocusedContainerColor = Color(0xFF2D1B69).copy(alpha = 0.3f),
                    focusedBorderColor = Color(0xFF5EEAD4),
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color(0xFF5EEAD4)
                ),
                maxLines = 5,
                minLines = 1
            )
            
            // Code snippet button beside send button
            IconButton(
                onClick = onCodeSnippetClick,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    Icons.Default.Code,
                    contentDescription = "Code Snippet",
                    tint = Color(0xFF5EEAD4),
                    modifier = Modifier.size(24.dp)
                )
            }
            
            FloatingActionButton(
                onClick = {
                    if (value.trim().isNotEmpty() && !isSending) {
                        onSend()
                    }
                },
                modifier = Modifier.size(48.dp),
                containerColor = if (value.trim().isNotEmpty()) 
                    Color(0xFF5EEAD4) 
                else 
                    Color(0xFF374151),
                contentColor = if (value.trim().isNotEmpty()) 
                    Color(0xFF1A1344) 
                else 
                    Color(0xFF6B7280)
            ) {
                if (isSending) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color(0xFF1A1344),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

private fun formatMessageTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60_000 -> "Just now"
        diff < 3_600_000 -> "${diff / 60_000}m"
        diff < 86_400_000 -> SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
        else -> SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(timestamp))
    }
}

@Composable
fun FeatureChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = Color(0xFF5EEAD4),
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = text,
                fontSize = 12.sp,
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun RoomInfoItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFF2D1B69).copy(alpha = 0.3f),
                shape = RoundedCornerShape(10.dp)
            )
            .padding(12.dp)
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = Color(0xFF5EEAD4),
            modifier = Modifier.size(24.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color(0xFF9CA3AF),
                fontWeight = FontWeight.Medium
            )
            Text(
                text = value,
                fontSize = 14.sp,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
