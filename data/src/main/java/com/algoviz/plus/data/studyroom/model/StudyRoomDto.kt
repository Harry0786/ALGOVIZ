package com.algoviz.plus.data.studyroom.model

data class StudyRoomDto(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val category: String = "",
    val createdBy: String = "",
    val createdAt: Long = 0L,
    val memberCount: Int = 0,
    val maxMembers: Int = 50,
    val isPrivate: Boolean = false,
    val lastMessageAt: Long? = null,
    val lastMessage: String? = null,
    val isActive: Boolean = true
)

data class MessageDto(
    val id: String = "",
    val roomId: String = "",
    val userId: String = "",
    val userName: String = "",
    val content: String = "",
    val type: String = "TEXT",
    val timestamp: Long = 0L,
    val edited: Boolean = false,
    val editedAt: Long? = null,
    val codeLanguage: String? = null,
    val replyToId: String? = null,
    val replyToContent: String? = null
)

data class RoomMemberDto(
    val userId: String = "",
    val userName: String = "",
    val joinedAt: Long = 0L,
    val isOnline: Boolean = false,
    val lastSeenAt: Long? = null,
    val unreadCount: Int = 0,
    val isTyping: Boolean = false,
    val typingAt: Long? = null
)

data class UserPresenceDto(
    val userId: String = "",
    val isOnline: Boolean = false,
    val lastSeenAt: Long = 0L
)
