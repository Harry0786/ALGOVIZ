package com.algoviz.plus.domain.repository

import com.algoviz.plus.domain.model.Message
import com.algoviz.plus.domain.model.RoomMember
import com.algoviz.plus.domain.model.StudyRoom
import com.algoviz.plus.domain.model.UserPresence
import kotlinx.coroutines.flow.Flow

interface StudyRoomRepository {
    // Room operations
    fun getAllRooms(): Flow<List<StudyRoom>>
    fun getRoomsByCategory(category: String): Flow<List<StudyRoom>>
    fun getRoomById(roomId: String): Flow<StudyRoom?>
    fun getMyRooms(userId: String): Flow<List<StudyRoom>>
    suspend fun createRoom(
        name: String,
        description: String,
        category: String,
        createdBy: String,
        creatorName: String,
        maxMembers: Int = 50,
        isPrivate: Boolean = false
    ): Result<String>
    suspend fun joinRoom(roomId: String, userId: String, userName: String): Result<Unit>
    suspend fun leaveRoom(roomId: String, userId: String): Result<Unit>
    suspend fun deleteRoom(roomId: String, requesterId: String, requesterName: String): Result<Unit>
    
    // Message operations
    fun getRoomMessages(roomId: String, limit: Int = 50): Flow<List<Message>>
    suspend fun sendMessage(
        roomId: String,
        userId: String,
        userName: String,
        content: String,
        type: String = "TEXT",
        codeLanguage: String? = null,
        replyToId: String? = null
    ): Result<String>
    suspend fun deleteMessage(roomId: String, messageId: String): Result<Unit>
    suspend fun editMessage(roomId: String, messageId: String, newContent: String): Result<Unit>
    
    // Member operations
    fun getRoomMembers(roomId: String): Flow<List<RoomMember>>
    fun getUserPresence(userId: String): Flow<UserPresence?>
    suspend fun updateUserPresence(userId: String, isOnline: Boolean): Result<Unit>
    
    // Search
    fun searchRooms(query: String): Flow<List<StudyRoom>>
}
