package com.algoviz.plus.domain.usecase

import com.algoviz.plus.domain.repository.StudyRoomRepository
import javax.inject.Inject

class CreateRoomUseCase @Inject constructor(
    private val repository: StudyRoomRepository
) {
    suspend operator fun invoke(
        name: String,
        description: String,
        category: String,
        createdBy: String,
        creatorName: String
    ): Result<String> {
        return repository.createRoom(
            name = name,
            description = description,
            category = category,
            createdBy = createdBy,
            creatorName = creatorName
        )
    }
}
