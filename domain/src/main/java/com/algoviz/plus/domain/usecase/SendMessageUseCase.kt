package com.algoviz.plus.domain.usecase

import com.algoviz.plus.domain.repository.StudyRoomRepository
import javax.inject.Inject

class SendMessageUseCase @Inject constructor(
    private val repository: StudyRoomRepository
) {
    suspend operator fun invoke(
        roomId: String,
        userId: String,
        userName: String,
        content: String,
        type: String = "TEXT",
        codeLanguage: String? = null,
        replyToId: String? = null
    ): Result<String> {
        if (content.isBlank()) {
            return Result.failure(IllegalArgumentException("Message cannot be empty"))
        }
        return repository.sendMessage(
            roomId = roomId,
            userId = userId,
            userName = userName,
            content = content,
            type = type,
            codeLanguage = codeLanguage,
            replyToId = replyToId
        )
    }
}
