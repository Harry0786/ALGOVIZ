package com.algoviz.plus.ui.studyrooms.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.algoviz.plus.domain.model.Message
import com.algoviz.plus.domain.model.MessageType
import com.algoviz.plus.ui.notifications.ActiveChatRoomTracker
import com.algoviz.plus.ui.notifications.InAppNotification
import com.algoviz.plus.ui.notifications.InAppNotificationCenter
import com.algoviz.plus.ui.studyrooms.chat.state.ChatRoomUiState
import com.algoviz.plus.ui.studyrooms.chat.viewmodel.ChatRoomViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

private object ChatThemeColors {
    val BackgroundTop = Color(0xFF251B5E)
    val BackgroundMid = Color(0xFF2D1B69)
    val BackgroundBottom = Color(0xFF3D2080)
    val SurfacePrimary = Color(0xFF120B35)
    val SurfaceSecondary = Color(0xFF2D1B69)
    val AccentMint = Color(0xFF5EEAD4)
    val AccentMintDark = Color(0xFF14B8A6)
    val AccentIndigo = Color(0xFF4F46E5)
    val AccentIndigoLight = Color(0xFF6366F1)
    val TextSecondary = Color(0xFF9CA3AF)
    val Error = Color(0xFFEF4444)
    val SnackbarSurface = Color(0xFF21124E)
    val OwnMessageBubble = Color(0xFF0F766E)
    val SystemMessage = Color(0xFFFDE68A)
}

private val ReceiverSenderPalette: List<Pair<Color, Color>> = listOf(
    Color(0xFF60A5FA) to Color(0xFF3B82F6),
    Color(0xFFF472B6) to Color(0xFFEC4899),
    Color(0xFF34D399) to Color(0xFF10B981),
    Color(0xFFFBBF24) to Color(0xFFF59E0B),
    Color(0xFFA78BFA) to Color(0xFF8B5CF6),
    Color(0xFF22D3EE) to Color(0xFF06B6D4),
    Color(0xFFFB7185) to Color(0xFFF43F5E),
    Color(0xFF4ADE80) to Color(0xFF22C55E)
)

private fun receiverSenderAccent(userId: String): Pair<Color, Color> {
    val index = (userId.hashCode() and Int.MAX_VALUE) % ReceiverSenderPalette.size
    return ReceiverSenderPalette[index]
}

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
    var showMemberList by remember { mutableStateOf(false) }
    var showCodeSnippetDialog by remember { mutableStateOf(false) }
    var showDeleteGroupConfirm by remember { mutableStateOf(false) }
    val currentRoomId = (uiState as? ChatRoomUiState.Success)?.room?.id

    DisposableEffect(currentRoomId) {
        ActiveChatRoomTracker.setActiveRoom(currentRoomId)
        onDispose {
            ActiveChatRoomTracker.setActiveRoom(null)
        }
    }
    
    // Track message count to only scroll on new messages, not on every state change
    var lastMessageCount by remember { mutableStateOf(0) }

    // Show error when message sending fails
    LaunchedEffect(sendMessageError) {
        sendMessageError?.let { error ->
            InAppNotificationCenter.post(
                InAppNotification(
                    title = "Message not sent",
                    message = error,
                    type = com.algoviz.plus.ui.notifications.InAppNotificationType.Error,
                    groupKey = "chat_message_errors",
                    dedupeKey = "chat_error:$error"
                )
            )
            viewModel.clearSendMessageError()
        }
    }

    LaunchedEffect(deleteRoomError) {
        deleteRoomError?.let { error ->
            InAppNotificationCenter.post(
                InAppNotification(
                    title = "Group action failed",
                    message = error,
                    type = com.algoviz.plus.ui.notifications.InAppNotificationType.Error,
                    groupKey = "chat_group_errors",
                    dedupeKey = "chat_delete_error:$error"
                )
            )
            viewModel.clearDeleteRoomError()
        }
    }

    LaunchedEffect(roomDeleted) {
        if (roomDeleted) {
            showMemberList = false
            showDeleteGroupConfirm = false
            InAppNotificationCenter.post(
                InAppNotification(
                    title = "Group removed",
                    message = "This study group was deleted successfully.",
                    type = com.algoviz.plus.ui.notifications.InAppNotificationType.Success,
                    groupKey = "chat_group_success",
                    dedupeKey = "chat_group_deleted:${currentRoomId ?: "unknown"}"
                )
            )
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

    Scaffold(
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
                                        tint = ChatThemeColors.AccentMint
                                    )
                                    Text(
                                        text = "${state.room.memberCount} members",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 12.sp,
                                        color = ChatThemeColors.TextSecondary
                                    )
                                    Text(
                                        text = "•",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 12.sp,
                                        color = ChatThemeColors.TextSecondary
                                    )
                                    Text(
                                        text = state.room.category.displayName,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 12.sp,
                                        color = ChatThemeColors.TextSecondary
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
                            containerColor = ChatThemeColors.SurfacePrimary,
                            titleContentColor = Color.White,
                            navigationIconContentColor = ChatThemeColors.AccentMint,
                            actionIconContentColor = ChatThemeColors.AccentMint
                        ),
                        modifier = Modifier
                            .shadow(10.dp)
                            .background(ChatThemeColors.SurfacePrimary)
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
                            containerColor = ChatThemeColors.SurfacePrimary,
                            titleContentColor = Color.White,
                            navigationIconContentColor = ChatThemeColors.AccentMint
                        ),
                        modifier = Modifier
                            .shadow(10.dp)
                            .background(ChatThemeColors.SurfacePrimary)
                    )
                }
            }

            HorizontalDivider(
                color = Color.White.copy(alpha = 0.12f),
                thickness = 1.dp
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            ChatThemeColors.BackgroundTop,
                            ChatThemeColors.BackgroundMid,
                            ChatThemeColors.BackgroundBottom
                        )
                    )
                )
        ) {
            when (val state = uiState) {
                is ChatRoomUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is ChatRoomUiState.Success -> {
                    val typingUsers = state.members
                        .filter { it.userId != state.currentUserId && it.isTyping }
                        .map { it.userName }
                        .distinct()
                    val timelineItems = remember(state.messages) {
                        buildChatTimeline(state.messages)
                    }
                    val senderAccentByUserId = remember(state.members, state.currentUserId) {
                        buildRoomSenderAccentMap(
                            memberIds = state.members.map { it.userId },
                            currentUserId = state.currentUserId
                        )
                    }

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
                                                                ChatThemeColors.AccentMint.copy(alpha = 0.2f),
                                                                ChatThemeColors.AccentIndigoLight.copy(alpha = 0.2f)
                                                            )
                                                        )
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Forum,
                                                    contentDescription = null,
                                                    tint = ChatThemeColors.AccentMint,
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
                                                color = ChatThemeColors.TextSecondary,
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
                            items(
                                items = timelineItems,
                                key = { item ->
                                    when (item) {
                                        is ChatTimelineItem.DateHeader -> "date-${item.dayStartMillis}"
                                        is ChatTimelineItem.MessageItem -> item.message.id
                                    }
                                }
                            ) { item ->
                                when (item) {
                                    is ChatTimelineItem.DateHeader -> {
                                        ChatDateSeparator(dayStartMillis = item.dayStartMillis)
                                    }
                                    is ChatTimelineItem.MessageItem -> {
                                        MessageBubble(
                                            message = item.message,
                                            isOwnMessage = item.message.userId == state.currentUserId,
                                            senderAccent = senderAccentByUserId[item.message.userId]
                                        )
                                    }
                                }
                            }
                        }

                        TypingIndicator(
                            typingUsers = typingUsers,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
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
                            tint = ChatThemeColors.Error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = state.message,
                            color = ChatThemeColors.Error,
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
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ChatThemeColors.AccentMint,
                                    contentColor = ChatThemeColors.SurfacePrimary
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Retry")
                            }
                            Button(
                                onClick = onBackClick,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ChatThemeColors.SurfaceSecondary,
                                    contentColor = Color.White
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Back")
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
            val creatorName = successState.members
                .firstOrNull { member -> member.userId == successState.room.createdBy }
                ?.userName
                ?: if (successState.room.createdBy == successState.currentUserId) "You" else "Unknown member"

            ModalBottomSheet(
                onDismissRequest = { showMemberList = false },
                containerColor = ChatThemeColors.SurfacePrimary
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
                                        colors = listOf(ChatThemeColors.AccentMint, ChatThemeColors.AccentMintDark)
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
                            color = ChatThemeColors.TextSecondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                    
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                    
                    // Room info
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        RoomInfoItem(
                            icon = Icons.Default.Info,
                            label = "Category",
                            value = successState.room.category.displayName
                        )
                        RoomInfoItem(
                            icon = Icons.Default.AdminPanelSettings,
                            label = "Creator",
                            value = creatorName
                        )
                        RoomInfoItem(
                            icon = Icons.Default.Group,
                            label = "Members",
                            value = "${successState.members.size} member${if (successState.members.size != 1) "s" else ""}"
                        )
                        RoomInfoItem(
                            icon = Icons.Default.Schedule,
                            label = "Created",
                            value = formatDateTime(successState.room.createdAt)
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
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .matchParentSize()
                                            .clip(CircleShape)
                                            .background(ChatThemeColors.AccentMint),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = member.userName.firstOrNull()?.uppercase() ?: "?",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = member.userName,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        color = Color.White
                                    )
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
            containerColor = ChatThemeColors.SurfacePrimary,
            titleContentColor = Color.White,
            textContentColor = Color(0xFFC7D2FE),
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
                    Text("Cancel", color = Color(0xFF5EEAD4))
                }
            }
        )
    }
    
    // Code Snippet Dialog
    if (showCodeSnippetDialog) {
        var codeInput by remember { mutableStateOf("") }
        
        AlertDialog(
            onDismissRequest = { showCodeSnippetDialog = false },
            containerColor = ChatThemeColors.SurfacePrimary,
            titleContentColor = Color.White,
            textContentColor = Color(0xFFC7D2FE),
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
                        color = ChatThemeColors.AccentMint,
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
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = ChatThemeColors.SurfaceSecondary.copy(alpha = 0.5f),
                            unfocusedContainerColor = ChatThemeColors.SurfaceSecondary.copy(alpha = 0.35f),
                            focusedBorderColor = ChatThemeColors.AccentMint,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.25f),
                            focusedLabelColor = ChatThemeColors.AccentMint,
                            unfocusedLabelColor = ChatThemeColors.TextSecondary,
                            cursorColor = ChatThemeColors.AccentMint
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
                        containerColor = ChatThemeColors.AccentMint,
                        contentColor = ChatThemeColors.SurfacePrimary
                    )
                ) {
                    Text("Send")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCodeSnippetDialog = false }) {
                    Text("Cancel", color = ChatThemeColors.AccentMint)
                }
            }
        )
    }
}

private sealed interface ChatTimelineItem {
    data class DateHeader(val dayStartMillis: Long) : ChatTimelineItem
    data class MessageItem(val message: Message) : ChatTimelineItem
}

private fun buildChatTimeline(messages: List<Message>): List<ChatTimelineItem> {
    if (messages.isEmpty()) return emptyList()

    val items = mutableListOf<ChatTimelineItem>()
    var previousDayStart: Long? = null

    messages.forEach { message ->
        val dayStart = startOfDayMillis(message.timestamp)
        if (previousDayStart != dayStart) {
            items += ChatTimelineItem.DateHeader(dayStartMillis = dayStart)
            previousDayStart = dayStart
        }
        items += ChatTimelineItem.MessageItem(message)
    }

    return items
}

private fun startOfDayMillis(timestamp: Long): Long {
    val calendar = Calendar.getInstance().apply {
        timeInMillis = timestamp
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return calendar.timeInMillis
}

@Composable
private fun ChatDateSeparator(dayStartMillis: Long) {
    val label = remember(dayStartMillis) { formatChatDayLabel(dayStartMillis) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Surface(
            color = Color(0xFF31206F).copy(alpha = 0.88f),
            shape = RoundedCornerShape(999.dp),
            tonalElevation = 2.dp,
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
        ) {
            Text(
                text = label,
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
    }
}

private fun formatChatDayLabel(dayStartMillis: Long): String {
    val nowDayStart = startOfDayMillis(System.currentTimeMillis())
    val diffDays = ((nowDayStart - dayStartMillis) / 86_400_000L).toInt()

    return when (diffDays) {
        0 -> "Today"
        1 -> "Yesterday"
        else -> SimpleDateFormat("EEE, dd MMM", Locale.getDefault()).format(Date(dayStartMillis))
    }
}

@Composable
private fun TypingIndicator(
    typingUsers: List<String>,
    modifier: Modifier = Modifier
) {
    var dots by remember { mutableStateOf("") }

    LaunchedEffect(typingUsers.isNotEmpty()) {
        if (!typingUsers.isNotEmpty()) {
            dots = ""
            return@LaunchedEffect
        }

        while (true) {
            dots = when (dots) {
                "" -> "."
                "." -> ".."
                ".." -> "..."
                else -> ""
            }
            kotlinx.coroutines.delay(350)
        }
    }

    AnimatedVisibility(
        visible = typingUsers.isNotEmpty(),
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier = modifier
    ) {
        val typingText = when {
            typingUsers.size == 1 -> "${typingUsers.first()} is typing$dots"
            typingUsers.size == 2 -> "${typingUsers[0]} and ${typingUsers[1]} are typing$dots"
            else -> "${typingUsers.size} people are typing$dots"
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = null,
                tint = ChatThemeColors.AccentMint,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = typingText,
                color = ChatThemeColors.AccentMint,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
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
        color = ChatThemeColors.SurfaceSecondary.copy(alpha = 0.5f)
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
                    .background(ChatThemeColors.AccentMint),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = ChatThemeColors.SurfacePrimary,
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
                    color = ChatThemeColors.TextSecondary
                )
            }
        }
    }
}

@Composable
fun MessageBubble(
    message: Message,
    isOwnMessage: Boolean,
    senderAccent: Pair<Color, Color>? = null
) {
    val (senderAccentLight, senderAccentDark) = senderAccent ?: receiverSenderAccent(message.userId)
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
                    color = senderAccentLight
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
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(senderAccentLight, senderAccentDark)
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
                color = if (isOwnMessage) ChatThemeColors.OwnMessageBubble else ChatThemeColors.SurfaceSecondary,
                modifier = Modifier.widthIn(max = 300.dp),
                shadowElevation = 4.dp,
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = if (isOwnMessage) 
                        ChatThemeColors.AccentMint.copy(alpha = 0.35f) 
                    else 
                        senderAccentDark.copy(alpha = 0.35f)
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
                            color = if (message.type == MessageType.SYSTEM) ChatThemeColors.SystemMessage
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
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(ChatThemeColors.AccentMint, ChatThemeColors.AccentMintDark)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Y",
                            color = ChatThemeColors.SurfacePrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
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
                color = ChatThemeColors.TextSecondary
            )
            if (message.edited) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "(edited)",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 9.sp,
                    color = ChatThemeColors.TextSecondary
                )
            }
        }
    }
}

private fun buildRoomSenderAccentMap(
    memberIds: List<String>,
    currentUserId: String
): Map<String, Pair<Color, Color>> {
    val receiverIds = memberIds
        .filter { it != currentUserId }
        .distinct()

    if (receiverIds.size <= 2) {
        return emptyMap()
    }

    return receiverIds.mapIndexed { index, userId ->
        userId to ReceiverSenderPalette[index % ReceiverSenderPalette.size]
    }.toMap()
}

@Composable
fun CodeSnippetCard(
    code: String,
    isOwnMessage: Boolean
) {
    val codeContent = code.trim()
    val detectedLanguage = remember(codeContent) { detectProgrammingLanguage(codeContent) }

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isOwnMessage) Color(0xFF0B3F38) else Color(0xFF1F2937),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.25f))
                    .padding(horizontal = 10.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Code,
                    contentDescription = null,
                    tint = ChatThemeColors.AccentMint,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = detectedLanguage.uppercase(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = ChatThemeColors.AccentMint,
                    fontFamily = FontFamily.Monospace
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(10.dp)
            ) {
                codeContent.lines().forEachIndexed { lineIndex, line ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = (lineIndex + 1).toString().padStart(3),
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF94A3B8),
                            textAlign = TextAlign.End
                        )
                        Text(
                            text = line.ifEmpty { " " },
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFFE5E7EB)
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
        color = ChatThemeColors.SurfacePrimary,
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
                        color = ChatThemeColors.TextSecondary
                    ) 
                },
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = ChatThemeColors.SurfaceSecondary.copy(alpha = 0.5f),
                    unfocusedContainerColor = ChatThemeColors.SurfaceSecondary.copy(alpha = 0.3f),
                    focusedBorderColor = ChatThemeColors.AccentMint,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = ChatThemeColors.AccentMint
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
                    tint = ChatThemeColors.AccentMint,
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
                    ChatThemeColors.AccentMint 
                else 
                    Color(0xFF374151),
                contentColor = if (value.trim().isNotEmpty()) 
                    ChatThemeColors.SurfacePrimary 
                else 
                    Color(0xFF6B7280)
            ) {
                if (isSending) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = ChatThemeColors.SurfacePrimary,
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

private fun formatDateTime(timestamp: Long): String {
    return SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(timestamp))
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
                tint = ChatThemeColors.AccentMint,
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
    value: String,
    detail: String? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = ChatThemeColors.SurfaceSecondary.copy(alpha = 0.3f),
                shape = RoundedCornerShape(10.dp)
            )
            .padding(12.dp)
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = ChatThemeColors.AccentMint,
            modifier = Modifier.size(24.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 12.sp,
                color = ChatThemeColors.TextSecondary,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = value,
                fontSize = 14.sp,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
            detail?.let {
                Text(
                    text = it,
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.88f)
                )
            }
        }
    }
}
