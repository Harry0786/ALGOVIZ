package com.algoviz.plus.domain.usecase

import com.algoviz.plus.domain.repository.StudyRoomRepository
import javax.inject.Inject

class JoinStudyRoomUseCase @Inject constructor(
    private val repository: StudyRoomRepository
) {
    suspend operator fun invoke(
        roomId: String,
        userId: String,
        userName: String
    ): Result<Unit> {
        return repository.joinRoom(roomId, userId, userName)
    }
}
