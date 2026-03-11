package com.algoviz.plus.domain.usecase

import com.algoviz.plus.domain.model.Message
import com.algoviz.plus.domain.repository.StudyRoomRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRoomMessagesUseCase @Inject constructor(
    private val repository: StudyRoomRepository
) {
    operator fun invoke(roomId: String, limit: Int = 50): Flow<List<Message>> {
        return repository.getRoomMessages(roomId, limit)
    }
}
