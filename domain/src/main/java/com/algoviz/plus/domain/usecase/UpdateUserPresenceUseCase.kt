package com.algoviz.plus.domain.usecase

import com.algoviz.plus.domain.repository.StudyRoomRepository
import javax.inject.Inject

class UpdateUserPresenceUseCase @Inject constructor(
    private val repository: StudyRoomRepository
) {
    suspend operator fun invoke(userId: String, isOnline: Boolean): Result<Unit> {
        return repository.updateUserPresence(userId, isOnline)
    }
}