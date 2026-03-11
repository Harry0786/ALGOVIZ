package com.algoviz.plus.domain.usecase

import com.algoviz.plus.domain.model.RoomMember
import com.algoviz.plus.domain.repository.StudyRoomRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRoomMembersUseCase @Inject constructor(
    private val repository: StudyRoomRepository
) {
    operator fun invoke(roomId: String): Flow<List<RoomMember>> {
        return repository.getRoomMembers(roomId)
    }
}
