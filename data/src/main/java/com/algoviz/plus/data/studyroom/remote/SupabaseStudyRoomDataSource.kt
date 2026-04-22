package com.algoviz.plus.data.studyroom.remote

import com.algoviz.plus.core.common.utils.UserIdentityUtils
import com.algoviz.plus.data.studyroom.model.MessageDto
import com.algoviz.plus.data.studyroom.model.RoomMemberDto
import com.algoviz.plus.data.studyroom.model.StudyRoomDto
import com.algoviz.plus.data.studyroom.model.UserPresenceDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.realtime.PostgresChangeFilter
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.realtime
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupabaseStudyRoomDataSource @Inject constructor(
    private val supabaseClient: SupabaseClient
) {
    @Serializable
    private data class StudyRoomRow(
        val id: String,
        val name: String,
        val description: String,
        val category: String,
        @SerialName("created_by") val createdBy: String,
        @SerialName("created_at") val createdAt: Long,
        @SerialName("member_count") val memberCount: Int,
        @SerialName("max_members") val maxMembers: Int,
        @SerialName("is_private") val isPrivate: Boolean,
        @SerialName("last_message_at") val lastMessageAt: Long? = null,
        @SerialName("last_message") val lastMessage: String? = null,
        @SerialName("is_active") val isActive: Boolean = true
    )

    @Serializable
    private data class RoomMemberRow(
        @SerialName("room_id") val roomId: String,
        @SerialName("user_id") val userId: String,
        @SerialName("user_name") val userName: String,
        @SerialName("joined_at") val joinedAt: Long,
        @SerialName("is_online") val isOnline: Boolean = false,
        @SerialName("last_seen_at") val lastSeenAt: Long? = null,
        @SerialName("unread_count") val unreadCount: Int = 0,
        @SerialName("is_typing") val isTyping: Boolean = false,
        @SerialName("typing_at") val typingAt: Long? = null
    )

    @Serializable
    private data class MessageRow(
        val id: String,
        @SerialName("room_id") val roomId: String,
        @SerialName("user_id") val userId: String,
        @SerialName("user_name") val userName: String,
        val content: String,
        val type: String,
        val timestamp: Long,
        val edited: Boolean = false,
        @SerialName("edited_at") val editedAt: Long? = null,
        @SerialName("code_language") val codeLanguage: String? = null,
        @SerialName("reply_to_id") val replyToId: String? = null,
        @SerialName("reply_to_content") val replyToContent: String? = null
    )

    @Serializable
    private data class PresenceRow(
        @SerialName("user_id") val userId: String,
        @SerialName("is_online") val isOnline: Boolean,
        @SerialName("last_seen_at") val lastSeenAt: Long
    )

    @Serializable
    private data class UserProfileAvatarRow(
        @SerialName("user_id") val userId: String,
        @SerialName("avatar_url") val avatarUrl: String? = null,
        val name: String? = null,
        val username: String? = null,
        val email: String? = null
    )

    @Serializable
    private data class UserProfileAvatarByIdRow(
        val id: String,
        @SerialName("avatar_url") val avatarUrl: String? = null,
        val name: String? = null,
        val username: String? = null,
        val email: String? = null
    )

    private companion object {
        const val STALE_MEMBER_ONLINE_MS = 120_000L
        const val STALE_TYPING_MS = 5_000L
    }

    private val roomsTable = supabaseClient.postgrest["study_rooms"]
    private val membersTable = supabaseClient.postgrest["study_room_members"]
    private val messagesTable = supabaseClient.postgrest["study_room_messages"]
    private val presenceTable = supabaseClient.postgrest["user_presence"]
    private val userProfilesTable = supabaseClient.postgrest["user_profiles"]
    private val supabaseBaseUrl: String by lazy {
        runCatching { supabaseClient.supabaseUrl }
            .getOrNull()
            ?.trim()
            ?.trimEnd('/')
            .orEmpty()
    }

    private fun StudyRoomRow.toDto(): StudyRoomDto = StudyRoomDto(
        id = id,
        name = name,
        description = description,
        category = category,
        createdBy = createdBy,
        createdAt = createdAt,
        memberCount = memberCount,
        maxMembers = maxMembers,
        isPrivate = isPrivate,
        lastMessageAt = lastMessageAt,
        lastMessage = lastMessage,
        isActive = isActive
    )

    private fun RoomMemberRow.toDto(avatarUrl: String? = null): RoomMemberDto = RoomMemberDto(
        userId = userId,
        userName = userName,
        avatarUrl = avatarUrl,
        joinedAt = joinedAt,
        isOnline = isOnline,
        lastSeenAt = lastSeenAt,
        unreadCount = unreadCount,
        isTyping = isTyping,
        typingAt = typingAt
    )

    private fun MessageRow.toDto(): MessageDto = MessageDto(
        id = id,
        roomId = roomId,
        userId = userId,
        userName = userName,
        content = content,
        type = type,
        timestamp = timestamp,
        edited = edited,
        editedAt = editedAt,
        codeLanguage = codeLanguage,
        replyToId = replyToId,
        replyToContent = replyToContent
    )

    private fun PresenceRow.toDto(): UserPresenceDto = UserPresenceDto(
        userId = userId,
        isOnline = isOnline,
        lastSeenAt = lastSeenAt
    )

    private fun <T> pollingFlow(intervalMs: Long = 2000L, block: suspend () -> T): Flow<T> {
        return flow {
            while (currentCoroutineContext().isActive) {
                emit(block())
                delay(intervalMs)
            }
        }.distinctUntilChanged()
    }

    private fun normalizeMemberAvatarUrl(raw: String?): String? {
        val normalized = UserIdentityUtils.normalizeAvatarUrl(
            raw = raw,
            supabaseUrl = supabaseBaseUrl
        ) ?: return null

        val lower = normalized.lowercase()
        if (lower.startsWith("algoviz/")) {
            return "$supabaseBaseUrl/storage/v1/object/public/$normalized"
        }

        return normalized
    }

    private fun fallbackMemberAvatarUrl(userId: String): String {
        return UserIdentityUtils.buildPublicAvatarUrl(
            objectPath = "profile_images/$userId.jpg",
            version = null,
            supabaseUrl = supabaseBaseUrl
        )
    }

    @OptIn(SupabaseExperimental::class)
    private fun <T> observeReloadOnChanges(
        channelName: String,
        vararg watchers: Pair<String, PostgresChangeFilter.() -> Unit>,
        fetcher: suspend () -> T
    ): Flow<T> = channelFlow {
        val channel = supabaseClient.channel(channelName)
        val watcherJobs = watchers.map { (table, filterBlock) ->
            launch {
                channel.postgresChangeFlow<PostgresAction>("public") {
                    this.table = table
                    filterBlock()
                }.collect {
                    trySend(fetcher())
                }
            }
        }
        trySend(fetcher())
        val subscribeJob = launch {
            channel.subscribe()
        }

        awaitClose {
            watcherJobs.forEach { it.cancel() }
            subscribeJob.cancel()
            launch {
                supabaseClient.realtime.removeChannel(channel)
            }
        }
    }

    private suspend fun loadProfileByUserIds(userIds: List<String>): Map<String, UserProfileAvatarRow> {
        if (userIds.isEmpty()) return emptyMap()

        return runCatching {
            userProfilesTable.select {
                filter {
                    isIn("user_id", userIds)
                }
            }.decodeList<UserProfileAvatarRow>().associateBy { it.userId }
        }.getOrElse {
            runCatching {
                userProfilesTable.select {
                    filter {
                        isIn("id", userIds)
                    }
                }.decodeList<UserProfileAvatarByIdRow>()
                    .associate { row ->
                        row.id to UserProfileAvatarRow(
                            userId = row.id,
                            avatarUrl = row.avatarUrl,
                            name = row.name,
                            username = row.username,
                            email = row.email
                        )
                    }
            }.getOrElse { emptyMap() }
        }
    }

    private suspend fun buildRoomMemberDtos(members: List<RoomMemberRow>): List<RoomMemberDto> {
        val userIds = members.map { it.userId }.distinct()
        val presenceByUserId = if (userIds.isEmpty()) {
            emptyMap()
        } else {
            runCatching {
                presenceTable.select {
                    filter {
                        isIn("user_id", userIds)
                    }
                }.decodeList<PresenceRow>().associateBy { it.userId }
            }.getOrElse { emptyMap() }
        }

        val profileByUserId = loadProfileByUserIds(userIds)
        val now = System.currentTimeMillis()

        return members.map { member ->
            val presence = presenceByUserId[member.userId]
            val typingFresh = member.isTyping && member.typingAt?.let { now - it <= STALE_TYPING_MS } == true
            val presenceOnline = presence?.isOnline == true
            val memberRecent = member.lastSeenAt?.let { now - it <= STALE_MEMBER_ONLINE_MS } == true
            val resolvedOnline = when (presence?.isOnline) {
                true -> true
                false -> typingFresh
                null -> typingFresh || memberRecent || presenceOnline
            }
            val profile = profileByUserId[member.userId]
            val avatarUrl = normalizeMemberAvatarUrl(profile?.avatarUrl)
                ?: fallbackMemberAvatarUrl(member.userId)
            val resolvedName = profile?.name?.takeUnless { it.isNullOrBlank() }
                ?: profile?.username?.takeUnless { it.isNullOrBlank() }
                ?: profile?.email?.substringBefore('@')?.takeUnless { it.isNullOrBlank() }
                ?: member.userName.ifBlank { "AlgoViz User" }

            member.copy(
                userName = resolvedName,
                isOnline = resolvedOnline,
                lastSeenAt = maxOf(
                    presence?.lastSeenAt ?: 0L,
                    member.lastSeenAt ?: 0L,
                    member.joinedAt
                ).takeIf { it > 0L },
                isTyping = typingFresh,
                typingAt = if (typingFresh) member.typingAt else null
            ).toDto(avatarUrl)
        }.sortedBy { it.userName.lowercase() }
    }

    private suspend fun fetchRoomMemberDtos(roomId: String): List<RoomMemberDto> {
        val members = membersTable.select {
            filter {
                eq("room_id", roomId)
            }
        }.decodeList<RoomMemberRow>()

        return buildRoomMemberDtos(members)
    }

    private suspend fun getRoomRow(roomId: String): StudyRoomRow? {
        return roomsTable.select {
            filter {
                eq("id", roomId)
            }
            limit(1)
        }.decodeSingleOrNull<StudyRoomRow>()
    }

    private suspend fun getActualMemberCount(roomId: String): Int {
        return membersTable.select {
            filter {
                eq("room_id", roomId)
            }
        }.decodeList<RoomMemberRow>().size
    }

    private suspend fun syncMemberCountInternal(roomId: String) {
        val actualCount = getActualMemberCount(roomId)
        roomsTable.update(
            buildJsonObject {
                put("member_count", actualCount)
            }
        ) {
            filter {
                eq("id", roomId)
            }
        }
    }

    fun observeAllRooms(): Flow<List<StudyRoomDto>> = observeReloadOnChanges(
        channelName = "study-rooms-all",
        "study_rooms" to {},
        "study_room_members" to {},
        "study_room_messages" to {}
    ) {
        roomsTable.select {
            filter {
                eq("is_active", true)
            }
            order("last_message_at", Order.DESCENDING)
        }.decodeList<StudyRoomRow>()
            .map { it.toDto() }
    }

    fun observeRoomsByCategory(category: String): Flow<List<StudyRoomDto>> = observeReloadOnChanges(
        channelName = "study-rooms-category-$category",
        "study_rooms" to {},
        "study_room_members" to {},
        "study_room_messages" to {}
    ) {
        roomsTable.select {
            filter {
                eq("category", category)
                eq("is_active", true)
            }
            order("last_message_at", Order.DESCENDING)
        }.decodeList<StudyRoomRow>()
            .map { it.toDto() }
    }

    fun observeRoomById(roomId: String): Flow<StudyRoomDto?> = observeReloadOnChanges(
        channelName = "study-room-$roomId",
        "study_rooms" to {
            filter("id", FilterOperator.EQ, roomId)
        },
        "study_room_members" to {
            filter("room_id", FilterOperator.EQ, roomId)
        },
        "study_room_messages" to {
            filter("room_id", FilterOperator.EQ, roomId)
        }
    ) {
        getRoomRow(roomId)?.toDto()
    }

    fun observeMyRooms(userId: String): Flow<List<StudyRoomDto>> = observeReloadOnChanges(
        channelName = "study-rooms-mine-$userId",
        "study_room_members" to {
            filter("user_id", FilterOperator.EQ, userId)
        },
        "study_rooms" to {},
        "study_room_messages" to {}
    ) {
        val memberships = membersTable.select {
            filter {
                eq("user_id", userId)
            }
        }.decodeList<RoomMemberRow>()

        val roomIds = memberships.map { it.roomId }.distinct()
        if (roomIds.isEmpty()) {
            emptyList()
        } else {
            roomsTable.select {
                filter {
                    eq("is_active", true)
                    isIn("id", roomIds)
                }
                order("last_message_at", Order.DESCENDING)
            }.decodeList<StudyRoomRow>().map { it.toDto() }
        }
    }

    fun observeUnreadCounts(userId: String): Flow<Map<String, Int>> = observeReloadOnChanges(
        channelName = "study-rooms-unread-$userId",
        "study_room_members" to {
            filter("user_id", FilterOperator.EQ, userId)
        }
    ) {
        membersTable.select {
            filter {
                eq("user_id", userId)
            }
        }.decodeList<RoomMemberRow>()
            .associate { it.roomId to it.unreadCount }
    }

    suspend fun createRoom(roomDto: StudyRoomDto, creatorName: String): Result<String> = runCatching {
        val roomId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()

        roomsTable.insert(
            buildJsonObject {
                put("id", roomId)
                put("name", roomDto.name)
                put("description", roomDto.description)
                put("category", roomDto.category)
                put("created_by", roomDto.createdBy)
                put("created_at", now)
                put("member_count", 0)
                put("max_members", roomDto.maxMembers)
                put("is_private", roomDto.isPrivate)
                put("is_active", true)
            }
        )

        joinRoom(roomId, roomDto.createdBy, creatorName).getOrThrow()
        roomId
    }

    suspend fun joinRoom(roomId: String, userId: String, userName: String): Result<Unit> = runCatching {
        val room = getRoomRow(roomId) ?: throw IllegalStateException("Group not found")
        val existing = membersTable.select {
            filter {
                eq("room_id", roomId)
                eq("user_id", userId)
            }
            limit(1)
        }.decodeSingleOrNull<RoomMemberRow>()

        if (existing != null) return@runCatching

        val memberCount = getActualMemberCount(roomId)
        if (memberCount >= room.maxMembers) {
            throw IllegalStateException("Room is at capacity (${room.maxMembers} members)")
        }

        val now = System.currentTimeMillis()
        membersTable.insert(
            buildJsonObject {
                put("room_id", roomId)
                put("user_id", userId)
                put("user_name", userName)
                put("joined_at", now)
                put("is_online", true)
                put("last_seen_at", now)
                put("unread_count", 0)
                put("is_typing", false)
            }
        )

        syncMemberCountInternal(roomId)
    }

    suspend fun leaveRoom(roomId: String, userId: String): Result<Unit> = runCatching {
        membersTable.delete {
            filter {
                eq("room_id", roomId)
                eq("user_id", userId)
            }
        }
        syncMemberCountInternal(roomId)
    }

    suspend fun syncMemberCount(roomId: String): Result<Unit> = runCatching {
        syncMemberCountInternal(roomId)
    }

    suspend fun markRoomAsRead(roomId: String, userId: String): Result<Unit> = runCatching {
        val existing = membersTable.select {
            filter {
                eq("room_id", roomId)
                eq("user_id", userId)
            }
            limit(1)
        }.decodeSingleOrNull<RoomMemberRow>()

        val now = System.currentTimeMillis()
        if (existing == null) {
            membersTable.insert(
                buildJsonObject {
                    put("room_id", roomId)
                    put("user_id", userId)
                    put("user_name", userId)
                    put("joined_at", now)
                    put("is_online", true)
                    put("last_seen_at", now)
                    put("unread_count", 0)
                    put("is_typing", false)
                }
            )
        } else {
            membersTable.update(
                buildJsonObject {
                    put("last_seen_at", now)
                    put("is_online", true)
                    put("unread_count", 0)
                    put("is_typing", false)
                }
            ) {
                filter {
                    eq("room_id", roomId)
                    eq("user_id", userId)
                }
            }
        }
    }

    suspend fun deleteRoom(roomId: String, requesterId: String, requesterName: String): Result<Unit> = runCatching {
        val room = getRoomRow(roomId) ?: throw IllegalStateException("Group not found")
        if (room.createdBy != requesterId) {
            throw IllegalStateException("Only the group creator can delete this group")
        }

        val now = System.currentTimeMillis()
        val systemContent = "This group was deleted by $requesterName"

        messagesTable.insert(
            buildJsonObject {
                put("id", UUID.randomUUID().toString())
                put("room_id", roomId)
                put("user_id", requesterId)
                put("user_name", "System")
                put("content", systemContent)
                put("type", "SYSTEM")
                put("timestamp", now)
            }
        )

        membersTable.delete {
            filter {
                eq("room_id", roomId)
            }
        }

        roomsTable.update(
            buildJsonObject {
                put("is_active", false)
                put("member_count", 0)
                put("last_message", systemContent)
                put("last_message_at", now)
            }
        ) {
            filter {
                eq("id", roomId)
            }
        }
    }

    suspend fun addMemberByAdmin(
        roomId: String,
        adminId: String,
        targetUserId: String,
        targetUserName: String
    ): Result<Unit> = runCatching {
        require(targetUserId.isNotBlank()) { "Member user id is required" }
        require(targetUserName.isNotBlank()) { "Member name is required" }

        val room = getRoomRow(roomId) ?: throw IllegalStateException("Group not found")
        if (room.createdBy != adminId) throw IllegalStateException("Only group admin can add members")
        if (!room.isActive) throw IllegalStateException("Cannot add members to an inactive group")

        val existing = membersTable.select {
            filter {
                eq("room_id", roomId)
                eq("user_id", targetUserId)
            }
            limit(1)
        }.decodeSingleOrNull<RoomMemberRow>()

        if (existing != null) throw IllegalStateException("Member already exists in this group")

        val currentCount = getActualMemberCount(roomId)
        if (currentCount >= room.maxMembers) {
            throw IllegalStateException("Room is at capacity (${room.maxMembers} members)")
        }

        val now = System.currentTimeMillis()
        membersTable.insert(
            buildJsonObject {
                put("room_id", roomId)
                put("user_id", targetUserId)
                put("user_name", targetUserName)
                put("joined_at", now)
                put("is_online", false)
                put("last_seen_at", now)
                put("unread_count", 0)
                put("is_typing", false)
            }
        )

        syncMemberCountInternal(roomId)

        val systemContent = "$targetUserName was added to the group"
        messagesTable.insert(
            buildJsonObject {
                put("id", UUID.randomUUID().toString())
                put("room_id", roomId)
                put("user_id", adminId)
                put("user_name", "System")
                put("content", systemContent)
                put("type", "SYSTEM")
                put("timestamp", now)
            }
        )

        roomsTable.update(
            buildJsonObject {
                put("last_message", systemContent)
                put("last_message_at", now)
            }
        ) {
            filter {
                eq("id", roomId)
            }
        }
    }

    suspend fun removeMemberByAdmin(
        roomId: String,
        adminId: String,
        targetUserId: String
    ): Result<Unit> = runCatching {
        require(targetUserId.isNotBlank()) { "Member user id is required" }

        val room = getRoomRow(roomId) ?: throw IllegalStateException("Group not found")
        if (room.createdBy != adminId) throw IllegalStateException("Only group admin can remove members")
        if (targetUserId == adminId) throw IllegalStateException("Admin cannot remove themselves")

        val targetMember = membersTable.select {
            filter {
                eq("room_id", roomId)
                eq("user_id", targetUserId)
            }
            limit(1)
        }.decodeSingleOrNull<RoomMemberRow>() ?: throw IllegalStateException("Member not found in this group")

        membersTable.delete {
            filter {
                eq("room_id", roomId)
                eq("user_id", targetUserId)
            }
        }

        syncMemberCountInternal(roomId)

        val now = System.currentTimeMillis()
        val systemContent = "${targetMember.userName} was removed from the group"
        messagesTable.insert(
            buildJsonObject {
                put("id", UUID.randomUUID().toString())
                put("room_id", roomId)
                put("user_id", adminId)
                put("user_name", "System")
                put("content", systemContent)
                put("type", "SYSTEM")
                put("timestamp", now)
            }
        )

        roomsTable.update(
            buildJsonObject {
                put("last_message", systemContent)
                put("last_message_at", now)
            }
        ) {
            filter {
                eq("id", roomId)
            }
        }
    }

    fun observeRoomMessages(roomId: String, limit: Int): Flow<List<MessageDto>> = pollingFlow(1200L) {
        messagesTable.select {
            filter {
                eq("room_id", roomId)
            }
            order("timestamp", Order.DESCENDING)
            limit(limit.toLong())
        }.decodeList<MessageRow>()
            .sortedBy { it.timestamp }
            .map { it.toDto() }
    }

    suspend fun sendMessage(messageDto: MessageDto): Result<String> = runCatching {
        val messageId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()

        messagesTable.insert(
            buildJsonObject {
                put("id", messageId)
                put("room_id", messageDto.roomId)
                put("user_id", messageDto.userId)
                put("user_name", messageDto.userName)
                put("content", messageDto.content)
                put("type", messageDto.type)
                put("timestamp", now)
                put("edited", false)
                messageDto.codeLanguage?.let { put("code_language", it) }
                messageDto.replyToId?.let { put("reply_to_id", it) }
                messageDto.replyToContent?.let { put("reply_to_content", it) }
            }
        )

        roomsTable.update(
            buildJsonObject {
                put("last_message", messageDto.content.take(100))
                put("last_message_at", now)
            }
        ) {
            filter {
                eq("id", messageDto.roomId)
            }
        }

        val members = membersTable.select {
            filter {
                eq("room_id", messageDto.roomId)
            }
        }.decodeList<RoomMemberRow>()

        members.forEach { member ->
            if (member.userId == messageDto.userId) {
                membersTable.update(
                    buildJsonObject {
                        put("unread_count", 0)
                        put("last_seen_at", now)
                        put("is_online", true)
                        put("is_typing", false)
                        put("typing_at", now)
                    }
                ) {
                    filter {
                        eq("room_id", messageDto.roomId)
                        eq("user_id", member.userId)
                    }
                }
            } else {
                membersTable.update(
                    buildJsonObject {
                        put("unread_count", member.unreadCount + 1)
                    }
                ) {
                    filter {
                        eq("room_id", messageDto.roomId)
                        eq("user_id", member.userId)
                    }
                }
            }
        }

        messageId
    }

    suspend fun deleteMessage(roomId: String, messageId: String): Result<Unit> = runCatching {
        messagesTable.delete {
            filter {
                eq("room_id", roomId)
                eq("id", messageId)
            }
        }
    }

    suspend fun editMessage(roomId: String, messageId: String, newContent: String): Result<Unit> = runCatching {
        messagesTable.update(
            buildJsonObject {
                put("content", newContent)
                put("edited", true)
                put("edited_at", System.currentTimeMillis())
            }
        ) {
            filter {
                eq("room_id", roomId)
                eq("id", messageId)
            }
        }
    }

    @OptIn(SupabaseExperimental::class)
    fun observeRoomMembers(roomId: String): Flow<List<RoomMemberDto>> = channelFlow {
        val channel = supabaseClient.channel("room-members-$roomId")
        val reloadTrigger = launch {
            merge(
                channel.postgresChangeFlow<PostgresAction>("public") {
                    table = "study_room_members"
                    filter("room_id", io.github.jan.supabase.postgrest.query.filter.FilterOperator.EQ, roomId)
                },
                channel.postgresChangeFlow<PostgresAction>("public") {
                    table = "user_presence"
                }
            ).collect {
                trySend(fetchRoomMemberDtos(roomId))
            }
        }

        trySend(fetchRoomMemberDtos(roomId))
        val subscribeJob = launch {
            channel.subscribe()
        }

        awaitClose {
            reloadTrigger.cancel()
            subscribeJob.cancel()
            launch {
                supabaseClient.realtime.removeChannel(channel)
            }
        }
    }

    @OptIn(SupabaseExperimental::class)
    fun observeUserPresence(userId: String): Flow<UserPresenceDto?> = channelFlow {
        val channel = supabaseClient.channel("user-presence-$userId")
        val reloadTrigger = launch {
            channel.postgresChangeFlow<PostgresAction>("public") {
                table = "user_presence"
                filter("user_id", io.github.jan.supabase.postgrest.query.filter.FilterOperator.EQ, userId)
            }.collect {
                trySend(fetchUserPresence(userId))
            }
        }

        trySend(fetchUserPresence(userId))
        val subscribeJob = launch {
            channel.subscribe()
        }

        awaitClose {
            reloadTrigger.cancel()
            subscribeJob.cancel()
            launch {
                supabaseClient.realtime.removeChannel(channel)
            }
        }
    }

    private suspend fun fetchUserPresence(userId: String): UserPresenceDto? {
        return presenceTable.select {
            filter {
                eq("user_id", userId)
            }
            limit(1)
        }.decodeSingleOrNull<PresenceRow>()?.toDto()
    }

    suspend fun updateUserPresence(userId: String, isOnline: Boolean): Result<Unit> = runCatching {
        val now = System.currentTimeMillis()
        val existing = presenceTable.select {
            filter {
                eq("user_id", userId)
            }
            limit(1)
        }.decodeSingleOrNull<PresenceRow>()

        if (existing == null) {
            presenceTable.insert(
                buildJsonObject {
                    put("user_id", userId)
                    put("is_online", isOnline)
                    put("last_seen_at", now)
                }
            )
        } else {
            presenceTable.update(
                buildJsonObject {
                    put("is_online", isOnline)
                    put("last_seen_at", now)
                }
            ) {
                filter {
                    eq("user_id", userId)
                }
            }
        }
    }

    suspend fun updateMemberPresence(roomId: String, userId: String, isOnline: Boolean): Result<Unit> = runCatching {
        membersTable.update(
            buildJsonObject {
                put("is_online", isOnline)
                put("last_seen_at", System.currentTimeMillis())
            }
        ) {
            filter {
                eq("room_id", roomId)
                eq("user_id", userId)
            }
        }
    }

    suspend fun updateTypingStatus(roomId: String, userId: String, isTyping: Boolean): Result<Unit> = runCatching {
        val now = System.currentTimeMillis()
        membersTable.update(
            buildJsonObject {
                put("is_online", true)
                put("is_typing", isTyping)
                put("typing_at", now)
                put("last_seen_at", now)
            }
        ) {
            filter {
                eq("room_id", roomId)
                eq("user_id", userId)
            }
        }
    }

    fun searchRooms(query: String): Flow<List<StudyRoomDto>> = pollingFlow(2000L) {
        val normalized = query.trim().lowercase()
        if (normalized.isBlank()) {
            emptyList()
        } else {
            roomsTable.select {
                filter {
                    eq("is_active", true)
                }
            }.decodeList<StudyRoomRow>()
                .filter {
                    it.name.lowercase().contains(normalized) ||
                        it.description.lowercase().contains(normalized) ||
                        it.category.lowercase().contains(normalized)
                }
                .map { it.toDto() }
        }
    }
}
