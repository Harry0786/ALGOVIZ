package com.algoviz.plus.data.studyroom.mapper

import com.algoviz.plus.data.studyroom.model.MessageDto
import com.algoviz.plus.data.studyroom.model.RoomMemberDto
import com.algoviz.plus.data.studyroom.model.StudyRoomDto
import com.algoviz.plus.data.studyroom.model.UserPresenceDto
import com.algoviz.plus.domain.model.Message
import com.algoviz.plus.domain.model.MessageType
import com.algoviz.plus.domain.model.RoomCategory
import com.algoviz.plus.domain.model.RoomMember
import com.algoviz.plus.domain.model.StudyRoom
import com.algoviz.plus.domain.model.UserPresence

object StudyRoomMapper {
    
    fun toDomain(dto: StudyRoomDto): StudyRoom {
        return StudyRoom(
            id = dto.id,
            name = dto.name,
            description = dto.description,
            category = RoomCategory.fromStorageCategory(dto.category),
            createdBy = dto.createdBy,
            createdAt = dto.createdAt,
            memberCount = dto.memberCount,
            maxMembers = dto.maxMembers,
            isPrivate = dto.isPrivate,
            lastMessageAt = dto.lastMessageAt,
            lastMessage = dto.lastMessage,
            isActive = dto.isActive
        )
    }
    
    fun toDto(studyRoom: StudyRoom): StudyRoomDto {
        return StudyRoomDto(
            id = studyRoom.id,
            name = studyRoom.name,
            description = studyRoom.description,
            category = studyRoom.category.name,
            createdBy = studyRoom.createdBy,
            createdAt = studyRoom.createdAt,
            memberCount = studyRoom.memberCount,
            maxMembers = studyRoom.maxMembers,
            isPrivate = studyRoom.isPrivate,
            lastMessageAt = studyRoom.lastMessageAt,
            lastMessage = studyRoom.lastMessage,
            isActive = studyRoom.isActive
        )
    }
    
    fun messageToDomain(dto: MessageDto): Message {
        return Message(
            id = dto.id,
            roomId = dto.roomId,
            userId = dto.userId,
            userName = dto.userName,
            content = dto.content,
            type = MessageType.values().find { it.name == dto.type } ?: MessageType.TEXT,
            timestamp = dto.timestamp,
            edited = dto.edited,
            editedAt = dto.editedAt,
            codeLanguage = dto.codeLanguage,
            replyToId = dto.replyToId,
            replyToContent = dto.replyToContent
        )
    }
    
    fun messageToDto(message: Message): MessageDto {
        return MessageDto(
            id = message.id,
            roomId = message.roomId,
            userId = message.userId,
            userName = message.userName,
            content = message.content,
            type = message.type.name,
            timestamp = message.timestamp,
            edited = message.edited,
            editedAt = message.editedAt,
            codeLanguage = message.codeLanguage,
            replyToId = message.replyToId,
            replyToContent = message.replyToContent
        )
    }
    
    fun memberToDomain(dto: RoomMemberDto): RoomMember {
        return RoomMember(
            userId = dto.userId,
            userName = dto.userName,
            joinedAt = dto.joinedAt,
            isOnline = dto.isOnline,
            lastSeenAt = dto.lastSeenAt,
            unreadCount = dto.unreadCount,
            isTyping = dto.isTyping,
            typingAt = dto.typingAt
        )
    }
    
    fun memberToDto(member: RoomMember): RoomMemberDto {
        return RoomMemberDto(
            userId = member.userId,
            userName = member.userName,
            joinedAt = member.joinedAt,
            isOnline = member.isOnline,
            lastSeenAt = member.lastSeenAt,
            unreadCount = member.unreadCount,
            isTyping = member.isTyping,
            typingAt = member.typingAt
        )
    }
    
    fun presenceToDomain(dto: UserPresenceDto): UserPresence {
        return UserPresence(
            userId = dto.userId,
            isOnline = dto.isOnline,
            lastSeenAt = dto.lastSeenAt
        )
    }
    
    fun presenceToDto(presence: UserPresence): UserPresenceDto {
        return UserPresenceDto(
            userId = presence.userId,
            isOnline = presence.isOnline,
            lastSeenAt = presence.lastSeenAt
        )
    }
}
