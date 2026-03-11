package com.algoviz.plus.domain.usecase

import com.algoviz.plus.domain.model.RoomMember
import com.algoviz.plus.domain.repository.StudyRoomRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

class GetRoomMembersUseCase @Inject constructor(
    private val repository: StudyRoomRepository
) {
    operator fun invoke(roomId: String): Flow<List<RoomMember>> {
        return repository.getRoomMembers(roomId).flatMapLatest { members ->
            if (members.isEmpty()) {
                flowOf(emptyList())
            } else {
                combine(
                    members.map { member ->
                        repository.getUserPresence(member.userId)
                    }
                ) { presences ->
                    members.mapIndexed { index, member ->
                        val presence = presences[index]
                        member.copy(
                            isOnline = presence?.isOnline ?: false,
                            lastSeenAt = presence?.lastSeenAt ?: member.lastSeenAt
                        )
                    }
                }
            }
        }
    }
}
