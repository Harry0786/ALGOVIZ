package com.algoviz.plus.domain.usecase

import com.algoviz.plus.domain.repository.StudyRoomRepository
import javax.inject.Inject

class LeaveRoomUseCase @Inject constructor(
    private val repository: StudyRoomRepository
) {
    suspend operator fun invoke(
        roomId: String,
        userId: String
    ): Result<Unit> {
        return repository.leaveRoom(roomId, userId)
    }
}
