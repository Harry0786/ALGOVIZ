package com.algoviz.plus.domain.usecase

import com.algoviz.plus.domain.repository.StudyRoomRepository
import javax.inject.Inject

class DeleteRoomUseCase @Inject constructor(
    private val repository: StudyRoomRepository
) {
    suspend operator fun invoke(
        roomId: String,
        requesterId: String,
        requesterName: String
    ): Result<Unit> {
        return repository.deleteRoom(
            roomId = roomId,
            requesterId = requesterId,
            requesterName = requesterName
        )
    }
}
