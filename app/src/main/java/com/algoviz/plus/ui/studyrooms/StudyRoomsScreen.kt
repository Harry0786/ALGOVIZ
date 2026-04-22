package com.algoviz.plus.ui.studyrooms

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.algoviz.plus.BuildConfig
import com.algoviz.plus.R
import com.algoviz.plus.domain.model.RoomMember
import com.algoviz.plus.domain.model.StudyRoom
import com.algoviz.plus.ui.studyrooms.state.StudyRoomAction
import com.algoviz.plus.ui.studyrooms.state.StudyRoomsUiState
import com.algoviz.plus.ui.studyrooms.viewmodel.StudyRoomsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StudyRoomsScreen(
    onRoomClick: (String) -> Unit,
    onCreateRoomClick: () -> Unit = {},
    onBackClick: () -> Unit,
    viewModel: StudyRoomsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }

    val rooms = (uiState as? StudyRoomsUiState.Success)?.rooms.orEmpty()
    val myRooms = (uiState as? StudyRoomsUiState.Success)?.myRooms.orEmpty()
    val loadingRoomId = (uiState as? StudyRoomsUiState.Success)?.loadingRoomId
    val roomMembersByRoom = (uiState as? StudyRoomsUiState.Success)?.roomMembersByRoom.orEmpty()
    val onlineFriends = (uiState as? StudyRoomsUiState.Success)?.onlineFriends.orEmpty()

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.bg1),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.76f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 16.dp)
                .padding(top = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick, modifier = Modifier.size(40.dp)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = "Algorithm Arena",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Study Rooms",
                color = Color(0xFFF1F1F4),
                fontSize = 30.sp,
                lineHeight = 34.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Join or create a sanctuary for focused learning.",
                    color = Color(0xFFB7B7BF),
                    fontSize = 15.sp,
                    lineHeight = 21.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )

                Surface(
                    modifier = Modifier
                        .padding(start = 10.dp)
                        .height(46.dp)
                        .width(126.dp)
                        .clickable { onCreateRoomClick() },
                    color = Color(0xFFF2F2F2),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "+",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF1A1A1E)
                        )
                        Spacer(modifier = Modifier.width(7.dp))
                        Text(
                            text = "Create",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1A1A1E)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    viewModel.onAction(StudyRoomAction.SearchRooms(it))
                },
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color(0xFF9C9CA5)
                    )
                },
                placeholder = {
                    Text(
                        text = "Search rooms...",
                        color = Color(0xFF9C9CA5),
                        fontSize = 17.sp
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(20.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF28282D),
                    unfocusedContainerColor = Color(0xFF28282D),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            when (val state = uiState) {
                is StudyRoomsUiState.Loading -> {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color.White)
                    }
                }

                is StudyRoomsUiState.Error -> {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Text(
                            text = state.message,
                            color = Color(0xFFFF7A7A),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                is StudyRoomsUiState.Success -> {
                    StudyRoomsList(
                        rooms = rooms,
                        myRooms = myRooms,
                        roomMembersByRoom = roomMembersByRoom,
                        onlineFriends = onlineFriends,
                        onRoomClick = onRoomClick,
                        onJoinRoom = { roomId -> viewModel.onAction(StudyRoomAction.JoinRoom(roomId)) },
                        loadingRoomId = loadingRoomId,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun StudyRoomsList(
    rooms: List<StudyRoom>,
    myRooms: List<StudyRoom>,
    roomMembersByRoom: Map<String, List<RoomMember>>,
    onlineFriends: List<RoomMember>,
    onRoomClick: (String) -> Unit,
    onJoinRoom: (String) -> Unit,
    loadingRoomId: String?,
    modifier: Modifier = Modifier
) {
    val roomOrder = remember(rooms, myRooms) {
        val ordered = linkedMapOf<String, StudyRoom>()
        myRooms.forEach { ordered[it.id] = it }
        rooms.forEach { ordered[it.id] = it }
        ordered.values.toList()
    }

    if (roomOrder.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "No rooms available",
                color = Color.White,
                fontSize = 18.sp
            )
        }
        return
    }

    val joinedIds = remember(myRooms) { myRooms.map { it.id }.toSet() }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 20.dp)
    ) {
        items(roomOrder, key = { it.id }) { room ->
            val isJoined = room.id in joinedIds
            StudyRoomListCard(
                room = room,
                isJoined = isJoined,
                members = roomMembersByRoom[room.id].orEmpty(),
                isLoading = loadingRoomId == room.id,
                onClick = {
                    if (isJoined) {
                        onRoomClick(room.id)
                    } else {
                        onJoinRoom(room.id)
                    }
                }
            )
        }

        item {
            FriendsOnlineSection(onlineFriends = onlineFriends)
        }
    }
}

@Composable
private fun StudyRoomListCard(
    room: StudyRoom,
    isJoined: Boolean,
    members: List<RoomMember>,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    val roomAvatarUrl = members.firstOrNull()?.let { member ->
        member.avatarUrl?.takeUnless { it.isBlank() } ?: deterministicAvatarUrl(member.userId)
    } ?: deterministicAvatarUrl(room.createdBy)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(196.dp)
            .clickable(enabled = !isLoading, onClick = onClick),
        color = Color(0x4C111116),
        shape = RoundedCornerShape(22.dp),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (roomAvatarUrl.isNullOrBlank()) {
                Image(
                    painter = painterResource(id = R.drawable.bg5),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                AsyncImage(
                    model = roomAvatarUrl,
                    contentDescription = "Room avatar",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    error = painterResource(id = R.drawable.bg5),
                    fallback = painterResource(id = R.drawable.bg5)
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xA6040407))
            )

            Surface(
                modifier = Modifier
                    .padding(start = 16.dp, top = 14.dp),
                color = Color(0x55323238),
                shape = RoundedCornerShape(50)
            ) {
                Text(
                    text = room.category.displayName.uppercase(Locale.getDefault()),
                    color = Color(0xFFD4D4D8),
                    fontSize = 12.sp,
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp)
                )
            }

            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 12.dp, end = 14.dp),
                horizontalArrangement = Arrangement.spacedBy((-8).dp)
            ) {
                val previewMembers = members
                    .sortedByDescending { member -> member.isOnline }
                    .take(2)

                previewMembers.forEach { member ->
                    AvatarBadge(
                        userId = member.userId,
                        name = member.userName,
                        avatarUrl = member.avatarUrl,
                        isOnline = member.isOnline,
                        modifier = Modifier.size(38.dp)
                    )
                }

                val extra = (room.memberCount - previewMembers.size).coerceAtLeast(0)
                if (extra > 0) {
                    Surface(
                        modifier = Modifier.size(38.dp),
                        shape = CircleShape,
                        color = Color(0xAA8C8C93),
                        tonalElevation = 0.dp,
                        shadowElevation = 0.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "+$extra",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 16.dp, end = 16.dp, bottom = 14.dp)
            ) {
                Text(
                    text = room.name,
                    color = Color(0xFFF5F5F7),
                    fontSize = 34.sp,
                    lineHeight = 38.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = room.description,
                    color = Color(0xFFC1C1C7),
                    fontSize = 16.sp,
                    lineHeight = 22.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    Text(
                        text = "${room.memberCount}/${room.maxMembers} members",
                        color = Color(0xFFD1D1D6),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = room.lastMessageAt?.let { formatTime(it) } ?: "Weekly",
                        color = Color(0xFFA9A9B2),
                        fontSize = 14.sp
                    )
                }
            }

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 16.dp, bottom = 16.dp)
                        .size(22.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = if (isJoined) "Open room" else "Join room",
                    tint = Color(0xFFEAEAF0),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 16.dp, bottom = 16.dp)
                        .size(26.dp)
                )
            }
        }
    }
}

@Composable
private fun AvatarBadge(
    userId: String,
    name: String,
    avatarUrl: String?,
    isOnline: Boolean,
    modifier: Modifier = Modifier
) {
    val fallbackUrl = remember(userId) { deterministicAvatarUrl(userId) }
    var resolvedUrl by remember(userId, avatarUrl) {
        mutableStateOf(avatarUrl?.takeUnless { it.isBlank() } ?: fallbackUrl)
    }

    Box(
        modifier = modifier
            .clip(CircleShape)
            .border(width = 2.dp, color = Color(0xFF2B2B2F), shape = CircleShape)
    ) {
        AsyncImage(
            model = resolvedUrl,
            contentDescription = name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            error = painterResource(id = R.drawable.user),
            fallback = painterResource(id = R.drawable.user),
            onError = {
                if (resolvedUrl != fallbackUrl) {
                    resolvedUrl = fallbackUrl
                }
            }
        )

        if (isOnline) {
            Box(
                modifier = Modifier
                    .size(11.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF37D67A))
                    .border(1.5.dp, Color(0xFF0D0D11), CircleShape)
                    .align(Alignment.BottomEnd)
            )
        }
    }
}

@Composable
private fun FriendsOnlineSection(onlineFriends: List<RoomMember>) {
    val shownFriends = onlineFriends.take(6)
    val emptyState = shownFriends.isEmpty()

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        color = Color(0x550B0B0E),
        shape = RoundedCornerShape(22.dp),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp)
        ) {
            Text(
                text = "Friends Online",
                color = Color(0xFFF2F2F5),
                fontSize = 19.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
            )

            if (emptyState) {
                Text(
                    text = "No friends online right now",
                    color = Color(0xFFA3A3AA),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(start = 4.dp, bottom = 10.dp)
                )
            }

            shownFriends.forEach { friend ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AvatarBadge(
                        userId = friend.userId,
                        name = friend.userName,
                        avatarUrl = friend.avatarUrl,
                        isOnline = friend.isOnline,
                        modifier = Modifier.size(44.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = friend.userName,
                            color = Color(0xFFE7E7EB),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "ONLINE",
                            color = Color(0xFFA3A3AA),
                            fontSize = 11.sp,
                            letterSpacing = 1.2.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                color = Color(0xFF8B8B90),
                shape = RoundedCornerShape(24.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "View All Friends",
                        color = Color(0xFFF5F5F7),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

private fun deterministicAvatarUrl(userId: String): String {
    val baseUrl = BuildConfig.SUPABASE_URL.trimEnd('/')
    return "$baseUrl/storage/v1/object/public/Algoviz/profile_images/$userId.jpg"
}

private fun formatTime(timestamp: Long): String {
    return SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(timestamp))
}
