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
    
    fun search(query: String): Flow<List<StudyRoom>> {
        return repository.searchRooms(query)
    }
}
