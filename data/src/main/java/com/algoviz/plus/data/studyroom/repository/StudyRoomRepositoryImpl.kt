package com.algoviz.plus.data.studyroom.repository

import com.algoviz.plus.data.studyroom.mapper.StudyRoomMapper
import com.algoviz.plus.data.studyroom.model.MessageDto
import com.algoviz.plus.data.studyroom.model.StudyRoomDto
import com.algoviz.plus.data.studyroom.remote.FirebaseStudyRoomDataSource
import com.algoviz.plus.domain.model.Message
import com.algoviz.plus.domain.model.RoomMember
import com.algoviz.plus.domain.model.StudyRoom
import com.algoviz.plus.domain.model.UserPresence
import com.algoviz.plus.domain.repository.StudyRoomRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StudyRoomRepositoryImpl @Inject constructor(
    private val dataSource: FirebaseStudyRoomDataSource
) : StudyRoomRepository {
    
    override fun getAllRooms(): Flow<List<StudyRoom>> {
        return dataSource.observeAllRooms().map { rooms ->
            rooms.map { StudyRoomMapper.toDomain(it) }
        }
    }
    
    override fun getRoomsByCategory(category: String): Flow<List<StudyRoom>> {
        return dataSource.observeRoomsByCategory(category).map { rooms ->
            rooms.map { StudyRoomMapper.toDomain(it) }
        }
    }
    
    override fun getRoomById(roomId: String): Flow<StudyRoom?> {
        return dataSource.observeRoomById(roomId).map { room ->
            room?.let { StudyRoomMapper.toDomain(it) }
        }
    }
    
    override fun getMyRooms(userId: String): Flow<List<StudyRoom>> {
        return dataSource.observeMyRooms(userId).map { rooms ->
            rooms.map { StudyRoomMapper.toDomain(it) }
        }
    }
    
    override suspend fun createRoom(
        name: String,
        description: String,
        category: String,
        createdBy: String,
        creatorName: String
    ): Result<String> {
        val roomDto = StudyRoomDto(
            name = name,
            description = description,
            category = category,
            createdBy = createdBy,
            isActive = true,
            memberCount = 0
        )
        return dataSource.createRoom(roomDto, creatorName)
    }
    
    override suspend fun joinRoom(roomId: String, userId: String, userName: String): Result<Unit> {
        return dataSource.joinRoom(roomId, userId, userName)
    }
    
    override suspend fun leaveRoom(roomId: String, userId: String): Result<Unit> {
        return dataSource.leaveRoom(roomId, userId)
    }
    
    override fun getRoomMessages(roomId: String, limit: Int): Flow<List<Message>> {
        return dataSource.observeRoomMessages(roomId, limit).map { messages ->
            messages.map { StudyRoomMapper.messageToDomain(it) }
        }
    }
    
    override suspend fun sendMessage(
        roomId: String,
        userId: String,
        userName: String,
        content: String,
        type: String,
        codeLanguage: String?,
        replyToId: String?
    ): Result<String> {
        val messageDto = MessageDto(
            roomId = roomId,
            userId = userId,
            userName = userName,
            content = content,
            type = type,
            codeLanguage = codeLanguage,
            replyToId = replyToId
        )
        return dataSource.sendMessage(messageDto)
    }
    
    override suspend fun deleteMessage(roomId: String, messageId: String): Result<Unit> {
        return dataSource.deleteMessage(roomId, messageId)
    }
    
    override suspend fun editMessage(roomId: String, messageId: String, newContent: String): Result<Unit> {
        return dataSource.editMessage(roomId, messageId, newContent)
    }
    
    override fun getRoomMembers(roomId: String): Flow<List<RoomMember>> {
        return dataSource.observeRoomMembers(roomId).map { members ->
            members.map { StudyRoomMapper.memberToDomain(it) }
        }
    }
    
    override fun getUserPresence(userId: String): Flow<UserPresence?> {
        return dataSource.observeUserPresence(userId).map { presence ->
            presence?.let { StudyRoomMapper.presenceToDomain(it) }
        }
    }
    
    override suspend fun updateUserPresence(userId: String, isOnline: Boolean): Result<Unit> {
        return dataSource.updateUserPresence(userId, isOnline)
    }
    
    override fun searchRooms(query: String): Flow<List<StudyRoom>> {
        return dataSource.searchRooms(query).map { rooms ->
            rooms.map { StudyRoomMapper.toDomain(it) }
        }
    }
}
