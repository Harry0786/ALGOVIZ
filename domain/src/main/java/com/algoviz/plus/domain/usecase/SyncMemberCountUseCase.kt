package com.algoviz.plus.domain.usecase

import com.algoviz.plus.domain.repository.StudyRoomRepository
import javax.inject.Inject

class SyncMemberCountUseCase @Inject constructor(
    private val repository: StudyRoomRepository
) {
    suspend operator fun invoke(roomId: String): Result<Unit> {
        return repository.syncMemberCount(roomId)
    }
}
