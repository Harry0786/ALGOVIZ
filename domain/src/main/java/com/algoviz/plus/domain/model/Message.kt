package com.algoviz.plus.domain.model

data class Message(
    val id: String,
    val roomId: String,
    val userId: String,
    val userName: String,
    val content: String,
    val type: MessageType,
    val timestamp: Long,
    val edited: Boolean = false,
    val editedAt: Long? = null,
    // For code snippets
    val codeLanguage: String? = null,
    // For replies
    val replyToId: String? = null,
    val replyToContent: String? = null
)

enum class MessageType {
    TEXT,
    CODE,
    IMAGE,
    FILE,
    AUDIO,
    SYSTEM
}

data class RoomMember(
    val userId: String,
    val userName: String,
    val joinedAt: Long,
    val isOnline: Boolean = false,
    val lastSeenAt: Long? = null,
    val unreadCount: Int = 0
)

data class UserPresence(
    val userId: String,
    val isOnline: Boolean,
    val lastSeenAt: Long
)
