package com.algoviz.plus.domain.usecase

import com.algoviz.plus.domain.model.StudyRoom
import com.algoviz.plus.domain.repository.StudyRoomRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetStudyRoomsUseCase @Inject constructor(
    private val repository: StudyRoomRepository
) {
    operator fun invoke(): Flow<List<StudyRoom>> {
        return repository.getAllRooms()
    }
    
    fun byCategory(category: String): Flow<List<StudyRoom>> {
        return repository.getRoomsByCategory(category)
    }
    
    fun roomById(roomId: String): Flow<StudyRoom?> {
        return repository.getRoomById(roomId)
    }
    
    fun myRooms(userId: String): Flow<List<StudyRoom>> {
        return repository.getMyRooms(userId)
    }

    fun unreadCounts(userId: String): Flow<Map<String, Int>> {
        return repository.getUnreadCounts(userId)
    }

    suspend fun markRoomAsRead(roomId: String, userId: String): Result<Unit> {
        return repository.markRoomAsRead(roomId, userId)
    }

    suspend fun setMemberPresence(roomId: String, userId: String, isOnline: Boolean): Result<Unit> {
        return repository.updateMemberPresence(roomId, userId, isOnline)
    }

    suspend fun setTypingStatus(roomId: String, userId: String, isTyping: Boolean): Result<Unit> {
        return repository.updateTypingStatus(roomId, userId, isTyping)
    }

    suspend fun addMemberByAdmin(
        roomId: String,
        adminId: String,
        targetUserId: String,
        targetUserName: String
    ): Result<Unit> {
        return repository.addMemberByAdmin(roomId, adminId, targetUserId, targetUserName)
    }

    suspend fun removeMemberByAdmin(roomId: String, adminId: String, targetUserId: String): Result<Unit> {
        return repository.removeMemberByAdmin(roomId, adminId, targetUserId)
    }
    
    fun search(query: String): Flow<List<StudyRoom>> {
        return repository.searchRooms(query)
    }
}
