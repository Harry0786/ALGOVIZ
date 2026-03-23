package com.algoviz.plus.data.studyroom.remote

import com.algoviz.plus.data.studyroom.model.MessageDto
import com.algoviz.plus.data.studyroom.model.RoomMemberDto
import com.algoviz.plus.data.studyroom.model.StudyRoomDto
import com.algoviz.plus.data.studyroom.model.UserPresenceDto
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseStudyRoomDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private fun parseMillis(value: Any?): Long? {
        return when (value) {
            null -> null
            is Long -> value
            is Int -> value.toLong()
            is Double -> value.toLong()
            is Float -> value.toLong()
            is Number -> value.toLong()
            is com.google.firebase.Timestamp -> value.toDate().time
            is java.util.Date -> value.time
            is String -> value.toLongOrNull()
            else -> null
        }
    }

    companion object {
        private const val ROOMS_COLLECTION = "study_rooms"
        private const val MESSAGES_COLLECTION = "messages"
        private const val MEMBERS_COLLECTION = "members"
        private const val PRESENCE_COLLECTION = "user_presence"
    }
    
    // Room operations
    fun observeAllRooms(): Flow<List<StudyRoomDto>> = callbackFlow {
        val listener = firestore.collection(ROOMS_COLLECTION)
            .whereEqualTo("isActive", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val rooms = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(StudyRoomDto::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                // Sort by lastMessageAt on client side to avoid composite index
                val sortedRooms = rooms.sortedByDescending { it.lastMessageAt }
                trySend(sortedRooms)
            }
        awaitClose { listener.remove() }
    }
    
    fun observeRoomsByCategory(category: String): Flow<List<StudyRoomDto>> = callbackFlow {
        val listener = firestore.collection(ROOMS_COLLECTION)
            .whereEqualTo("category", category)
            .whereEqualTo("isActive", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val rooms = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(StudyRoomDto::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                // Sort by lastMessageAt on client side to avoid composite index
                val sortedRooms = rooms.sortedByDescending { it.lastMessageAt }
                trySend(sortedRooms)
            }
        awaitClose { listener.remove() }
    }
    
    fun observeRoomById(roomId: String): Flow<StudyRoomDto?> = callbackFlow {
        val listener = firestore.collection(ROOMS_COLLECTION)
            .document(roomId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val room = snapshot?.toObject(StudyRoomDto::class.java)?.copy(id = snapshot.id)
                trySend(room)
            }
        awaitClose { listener.remove() }
    }
    
    fun observeMyRooms(userId: String): Flow<List<StudyRoomDto>> = callbackFlow {
        // First, get all rooms and filter on client side to avoid needing composite index
        val listener = firestore.collection(ROOMS_COLLECTION)
            .whereEqualTo("isActive", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val allRooms = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(StudyRoomDto::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                
                if (allRooms.isEmpty()) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                
                // Launch coroutine to check membership asynchronously
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val myRooms = allRooms.filter { room ->
                            try {
                                val memberDoc = firestore.collection(ROOMS_COLLECTION)
                                    .document(room.id)
                                    .collection(MEMBERS_COLLECTION)
                                    .document(userId)
                                    .get()
                                    .await()
                                memberDoc.exists()
                            } catch (e: Exception) {
                                false // If check fails, assume not a member
                            }
                        }
                        trySend(myRooms)
                    } catch (e: Exception) {
                        // If filtering fails, send empty list
                        trySend(emptyList())
                    }
                }
            }
        awaitClose { listener.remove() }
    }

    fun observeUnreadCounts(userId: String): Flow<Map<String, Int>> = callbackFlow {
        val unreadByRoom = mutableMapOf<String, Int>()
        val memberListeners = mutableMapOf<String, ListenerRegistration>()

        val roomsListener = firestore.collection(ROOMS_COLLECTION)
            .whereEqualTo("isActive", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val activeRoomIds = snapshot?.documents?.map { it.id }?.toSet() ?: emptySet()

                val removedRoomIds = memberListeners.keys - activeRoomIds
                removedRoomIds.forEach { roomId ->
                    memberListeners.remove(roomId)?.remove()
                    unreadByRoom.remove(roomId)
                }

                val newRoomIds = activeRoomIds - memberListeners.keys
                newRoomIds.forEach { roomId ->
                    val memberListener = firestore.collection(ROOMS_COLLECTION)
                        .document(roomId)
                        .collection(MEMBERS_COLLECTION)
                        .document(userId)
                        .addSnapshotListener { memberSnapshot, memberError ->
                            if (memberError != null) {
                                return@addSnapshotListener
                            }

                            if (memberSnapshot != null && memberSnapshot.exists()) {
                                unreadByRoom[roomId] = (memberSnapshot.getLong("unreadCount") ?: 0L).toInt()
                            } else {
                                unreadByRoom.remove(roomId)
                            }

                            trySend(unreadByRoom.toMap())
                        }
                    memberListeners[roomId] = memberListener
                }

                trySend(unreadByRoom.toMap())
            }
        awaitClose {
            roomsListener.remove()
            memberListeners.values.forEach { it.remove() }
            memberListeners.clear()
        }
    }
    
    suspend fun createRoom(roomDto: StudyRoomDto, creatorName: String): Result<String> = runCatching {
        val docRef = firestore.collection(ROOMS_COLLECTION).document()
        val roomWithId = roomDto.copy(
            id = docRef.id, 
            createdAt = System.currentTimeMillis(),
            isActive = true  // Explicitly ensure isActive is set
        )
        
        // Explicitly set the document with all fields to ensure isActive is written
        docRef.set(
            mapOf(
                "id" to roomWithId.id,
                "name" to roomWithId.name,
                "description" to roomWithId.description,
                "category" to roomWithId.category,
                "createdBy" to roomWithId.createdBy,
                "createdAt" to roomWithId.createdAt,
                "memberCount" to roomWithId.memberCount,
                "maxMembers" to roomWithId.maxMembers,
                "isPrivate" to roomWithId.isPrivate,
                "isActive" to roomWithId.isActive,
                "lastMessageAt" to roomWithId.lastMessageAt,
                "lastMessage" to roomWithId.lastMessage
            )
        ).await()
        
        // Add creator as first member with proper userName
        joinRoom(docRef.id, roomDto.createdBy, creatorName).getOrThrow()
        
        docRef.id
    }
    
    suspend fun joinRoom(roomId: String, userId: String, userName: String): Result<Unit> = runCatching {
        val memberRef = firestore.collection(ROOMS_COLLECTION)
            .document(roomId)
            .collection(MEMBERS_COLLECTION)
            .document(userId)
        
        // Check if already a member to avoid duplicate joins
        val existingMember = memberRef.get().await()
        if (existingMember.exists()) {
            return@runCatching // Already a member, no action needed
        }
        
        // Check room capacity using actual member count from members collection
        val actualMemberCount = getActualMemberCount(roomId)
        val roomDoc = firestore.collection(ROOMS_COLLECTION)
            .document(roomId)
            .get()
            .await()
        
        val maxMembers = roomDoc.getLong("maxMembers")?.toInt() ?: 50
        if (actualMemberCount >= maxMembers) {
            throw IllegalStateException("Room is at capacity (${maxMembers} members)")
        }
        
        val memberDto = RoomMemberDto(
            userId = userId,
            userName = userName,
            joinedAt = System.currentTimeMillis(),
            isOnline = true,
            lastSeenAt = System.currentTimeMillis(),
            unreadCount = 0,
            isTyping = false,
            typingAt = null
        )
        memberRef.set(memberDto).await()
        
        // Update member count with error handling
        try {
            firestore.collection(ROOMS_COLLECTION)
                .document(roomId)
                .update("memberCount", com.google.firebase.firestore.FieldValue.increment(1))
                .await()
        } catch (e: Exception) {
            // If count update fails, remove the member to maintain consistency
            memberRef.delete().await()
            throw e
        }
    }
    
    suspend fun leaveRoom(roomId: String, userId: String): Result<Unit> = runCatching {
        val memberRef = firestore.collection(ROOMS_COLLECTION)
            .document(roomId)
            .collection(MEMBERS_COLLECTION)
            .document(userId)
        
        // Check if member exists before attempting to leave
        val memberDoc = memberRef.get().await()
        if (!memberDoc.exists()) {
            return@runCatching // Not a member, no action needed
        }
        
        memberRef.delete().await()
        
        // Update member count with bounds check
        try {
            val roomDoc = firestore.collection(ROOMS_COLLECTION)
                .document(roomId)
                .get()
                .await()
            
            val currentCount = roomDoc.getLong("memberCount") ?: 0
            if (currentCount > 0) {
                firestore.collection(ROOMS_COLLECTION)
                    .document(roomId)
                    .update("memberCount", com.google.firebase.firestore.FieldValue.increment(-1))
                    .await()
            }
        } catch (e: Exception) {
            // Log error but don't fail the leave operation since member is already removed
            // The count might be slightly off but will be corrected on next operation
        }
    }

    // Helper: Get actual member count from members collection to verify room capacity
    private suspend fun getActualMemberCount(roomId: String): Int = runCatching {
        val querySnapshot = firestore.collection(ROOMS_COLLECTION)
            .document(roomId)
            .collection(MEMBERS_COLLECTION)
            .get()
            .await()
        querySnapshot.size()
    }.getOrDefault(0)

    // Public function to sync and fix member count if it's inconsistent with actual members
    suspend fun syncMemberCount(roomId: String): Result<Unit> = runCatching {
        val actualCount = getActualMemberCount(roomId)
        firestore.collection(ROOMS_COLLECTION)
            .document(roomId)
            .update("memberCount", actualCount)
            .await()
    }

    suspend fun markRoomAsRead(roomId: String, userId: String): Result<Unit> = runCatching {
        val memberRef = firestore.collection(ROOMS_COLLECTION)
            .document(roomId)
            .collection(MEMBERS_COLLECTION)
            .document(userId)

        memberRef.set(
            mapOf(
                "userId" to userId,
                "lastSeenAt" to System.currentTimeMillis(),
                "unreadCount" to 0,
                "isOnline" to true,
                "isTyping" to false,
                "typingAt" to null
            ),
            com.google.firebase.firestore.SetOptions.merge()
        ).await()
    }

    suspend fun deleteRoom(roomId: String, requesterId: String, requesterName: String): Result<Unit> = runCatching {
        val roomRef = firestore.collection(ROOMS_COLLECTION).document(roomId)
        val roomSnapshot = roomRef.get().await()

        if (!roomSnapshot.exists()) {
            throw IllegalStateException("Group not found")
        }

        val createdBy = roomSnapshot.getString("createdBy")
        if (createdBy != requesterId) {
            throw IllegalStateException("Only the group creator can delete this group")
        }

        // WhatsApp-style notice: create a system message visible to current members.
        val messageRef = roomRef.collection(MESSAGES_COLLECTION).document()
        val now = System.currentTimeMillis()
        val systemContent = "This group was deleted by $requesterName"
        val systemMessage = MessageDto(
            id = messageRef.id,
            roomId = roomId,
            userId = requesterId,
            userName = "System",
            content = systemContent,
            type = "SYSTEM",
            timestamp = now
        )
        messageRef.set(systemMessage).await()

        val membersSnapshot = roomRef.collection(MEMBERS_COLLECTION).get().await()
        membersSnapshot.documents.forEach { memberDoc ->
            memberDoc.reference.delete().await()
        }

        roomRef.update(
            mapOf(
                "isActive" to false,
                "memberCount" to 0,
                "lastMessage" to systemContent,
                "lastMessageAt" to now
            )
        ).await()
    }

    suspend fun addMemberByAdmin(
        roomId: String,
        adminId: String,
        targetUserId: String,
        targetUserName: String
    ): Result<Unit> = runCatching {
        require(targetUserId.isNotBlank()) { "Member user id is required" }
        require(targetUserName.isNotBlank()) { "Member name is required" }

        val roomRef = firestore.collection(ROOMS_COLLECTION).document(roomId)
        val roomSnapshot = roomRef.get().await()
        if (!roomSnapshot.exists()) {
            throw IllegalStateException("Group not found")
        }

        val createdBy = roomSnapshot.getString("createdBy")
        if (createdBy != adminId) {
            throw IllegalStateException("Only group admin can add members")
        }

        val isActive = roomSnapshot.getBoolean("isActive") ?: true
        if (!isActive) {
            throw IllegalStateException("Cannot add members to an inactive group")
        }

        val memberRef = roomRef.collection(MEMBERS_COLLECTION).document(targetUserId)
        val existingMember = memberRef.get().await()
        if (existingMember.exists()) {
            throw IllegalStateException("Member already exists in this group")
        }

        val actualMemberCount = getActualMemberCount(roomId)
        val maxMembers = roomSnapshot.getLong("maxMembers")?.toInt() ?: 50
        if (actualMemberCount >= maxMembers) {
            throw IllegalStateException("Room is at capacity (${maxMembers} members)")
        }

        val now = System.currentTimeMillis()
        val memberDto = RoomMemberDto(
            userId = targetUserId,
            userName = targetUserName,
            joinedAt = now,
            isOnline = false,
            lastSeenAt = now,
            unreadCount = 0,
            isTyping = false,
            typingAt = null
        )
        memberRef.set(memberDto).await()

        roomRef.update("memberCount", FieldValue.increment(1)).await()

        val systemMessageRef = roomRef.collection(MESSAGES_COLLECTION).document()
        val systemContent = "$targetUserName was added to the group"
        systemMessageRef.set(
            MessageDto(
                id = systemMessageRef.id,
                roomId = roomId,
                userId = adminId,
                userName = "System",
                content = systemContent,
                type = "SYSTEM",
                timestamp = now
            )
        ).await()

        roomRef.update(
            mapOf(
                "lastMessage" to systemContent,
                "lastMessageAt" to now
            )
        ).await()
    }

    suspend fun removeMemberByAdmin(
        roomId: String,
        adminId: String,
        targetUserId: String
    ): Result<Unit> = runCatching {
        require(targetUserId.isNotBlank()) { "Member user id is required" }

        val roomRef = firestore.collection(ROOMS_COLLECTION).document(roomId)
        val roomSnapshot = roomRef.get().await()
        if (!roomSnapshot.exists()) {
            throw IllegalStateException("Group not found")
        }

        val createdBy = roomSnapshot.getString("createdBy")
        if (createdBy != adminId) {
            throw IllegalStateException("Only group admin can remove members")
        }
        if (targetUserId == adminId) {
            throw IllegalStateException("Admin cannot remove themselves")
        }

        val memberRef = roomRef.collection(MEMBERS_COLLECTION).document(targetUserId)
        val memberSnapshot = memberRef.get().await()
        if (!memberSnapshot.exists()) {
            throw IllegalStateException("Member not found in this group")
        }

        val removedName = memberSnapshot.getString("userName") ?: "A member"
        memberRef.delete().await()

        val now = System.currentTimeMillis()
        roomRef.update("memberCount", FieldValue.increment(-1)).await()

        val systemMessageRef = roomRef.collection(MESSAGES_COLLECTION).document()
        val systemContent = "$removedName was removed from the group"
        systemMessageRef.set(
            MessageDto(
                id = systemMessageRef.id,
                roomId = roomId,
                userId = adminId,
                userName = "System",
                content = systemContent,
                type = "SYSTEM",
                timestamp = now
            )
        ).await()

        roomRef.update(
            mapOf(
                "lastMessage" to systemContent,
                "lastMessageAt" to now
            )
        ).await()
    }
    
    // Message operations
    fun observeRoomMessages(roomId: String, limit: Int): Flow<List<MessageDto>> = callbackFlow {
        val listener = firestore.collection(ROOMS_COLLECTION)
            .document(roomId)
            .collection(MESSAGES_COLLECTION)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .limitToLast(limit.toLong())
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val messages = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(MessageDto::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(messages)
            }
        awaitClose { listener.remove() }
    }
    
    suspend fun sendMessage(messageDto: MessageDto): Result<String> = runCatching {
        val roomRef = firestore.collection(ROOMS_COLLECTION).document(messageDto.roomId)
        val messageRef = roomRef.collection(MESSAGES_COLLECTION).document()
        
        val messageWithId = messageDto.copy(
            id = messageRef.id,
            timestamp = System.currentTimeMillis()
        )
        
        // Save message first
        messageRef.set(messageWithId).await()
        
        // Update room's last message (best effort, don't fail if this fails)
        try {
            roomRef.update(
                mapOf(
                    "lastMessage" to messageDto.content.take(100),
                    "lastMessageAt" to messageWithId.timestamp
                )
            ).await()

            // Increment unread counters for other members; reset sender counter.
            val membersSnapshot = roomRef.collection(MEMBERS_COLLECTION).get().await()
            val batch = firestore.batch()
            membersSnapshot.documents.forEach { member ->
                if (member.id == messageDto.userId) {
                    batch.set(
                        member.reference,
                        mapOf(
                            "userId" to messageDto.userId,
                            "unreadCount" to 0,
                            "lastSeenAt" to messageWithId.timestamp,
                            "isOnline" to true,
                            "isTyping" to false,
                            "typingAt" to null
                        ),
                        com.google.firebase.firestore.SetOptions.merge()
                    )
                } else {
                    batch.update(member.reference, "unreadCount", FieldValue.increment(1))
                }
            }
            batch.commit().await()
        } catch (e: Exception) {
            // Log but don't throw - message was saved successfully
        }
        
        messageRef.id
    }
    
    suspend fun deleteMessage(roomId: String, messageId: String): Result<Unit> = runCatching {
        firestore.collection(ROOMS_COLLECTION)
            .document(roomId)
            .collection(MESSAGES_COLLECTION)
            .document(messageId)
            .delete()
            .await()
    }
    
    suspend fun editMessage(roomId: String, messageId: String, newContent: String): Result<Unit> = runCatching {
        firestore.collection(ROOMS_COLLECTION)
            .document(roomId)
            .collection(MESSAGES_COLLECTION)
            .document(messageId)
            .update(
                mapOf(
                    "content" to newContent,
                    "edited" to true,
                    "editedAt" to System.currentTimeMillis()
                )
            )
            .await()
    }
    
    // Member operations
    fun observeRoomMembers(roomId: String): Flow<List<RoomMemberDto>> = callbackFlow {
        val presenceListeners = mutableMapOf<String, ListenerRegistration>()
        val memberByUserId = mutableMapOf<String, RoomMemberDto>()
        val presenceByUserId = mutableMapOf<String, UserPresenceDto?>()
        val staleAfterMs = 120_000L
        val typingStaleAfterMs = 5_000L

        fun mergedMembers(): List<RoomMemberDto> {
            val now = System.currentTimeMillis()
            return memberByUserId.values.map { member ->
                val presence = presenceByUserId[member.userId]
                val isTypingFresh = member.isTyping && member.typingAt?.let { now - it <= typingStaleAfterMs } == true
                val presenceOnline = presence?.isOnline == true
                val memberOnline = member.isOnline
                val presenceRecent = presence?.lastSeenAt?.let { now - it <= staleAfterMs } == true
                val memberRecent = member.lastSeenAt?.let { now - it <= staleAfterMs } == true
                val resolvedOnline = presenceOnline || memberOnline || presenceRecent || memberRecent || isTypingFresh
                val lastSeen = maxOf(
                    presence?.lastSeenAt ?: 0L,
                    member.lastSeenAt ?: 0L,
                    member.joinedAt
                ).takeIf { it > 0L }

                member.copy(
                    isOnline = resolvedOnline,
                    lastSeenAt = lastSeen,
                    isTyping = isTypingFresh,
                    typingAt = if (isTypingFresh) member.typingAt else null
                )
            }.sortedBy { it.userName.lowercase() }
        }

        val listener = firestore.collection(ROOMS_COLLECTION)
            .document(roomId)
            .collection(MEMBERS_COLLECTION)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val latestMembers = snapshot?.documents?.mapNotNull { doc ->
                    val userId = doc.getString("userId")?.takeIf { it.isNotBlank() } ?: doc.id
                    val userName = doc.getString("userName") ?: return@mapNotNull null
                    val joinedAt = parseMillis(doc.get("joinedAt")) ?: System.currentTimeMillis()
                    val isOnline = doc.getBoolean("isOnline") ?: false
                    val lastSeenAt = parseMillis(doc.get("lastSeenAt"))
                    val unreadCount = (doc.getLong("unreadCount") ?: 0L).toInt()
                    val isTyping = doc.getBoolean("isTyping") ?: false
                    val typingAt = parseMillis(doc.get("typingAt"))

                    RoomMemberDto(
                        userId = userId,
                        userName = userName,
                        joinedAt = joinedAt,
                        isOnline = isOnline,
                        lastSeenAt = lastSeenAt,
                        unreadCount = unreadCount,
                        isTyping = isTyping,
                        typingAt = typingAt
                    )
                } ?: emptyList()

                val latestUserIds = latestMembers.map { it.userId }.toSet()

                memberByUserId.clear()
                latestMembers.forEach { member ->
                    memberByUserId[member.userId] = member
                }

                val removedUserIds = presenceListeners.keys - latestUserIds
                removedUserIds.forEach { userId ->
                    presenceListeners.remove(userId)?.remove()
                    presenceByUserId.remove(userId)
                }

                val newUserIds = latestUserIds - presenceListeners.keys
                newUserIds.forEach { userId ->
                    val presenceListener = firestore.collection(PRESENCE_COLLECTION)
                        .document(userId)
                        .addSnapshotListener { presenceSnapshot, _ ->
                            val presence = if (presenceSnapshot != null && presenceSnapshot.exists()) {
                                val isOnline = presenceSnapshot.getBoolean("isOnline") ?: false
                                val lastSeenAt = parseMillis(presenceSnapshot.get("lastSeenAt")) ?: 0L
                                UserPresenceDto(
                                    userId = userId,
                                    isOnline = isOnline,
                                    lastSeenAt = lastSeenAt
                                )
                            } else {
                                null
                            }
                            presenceByUserId[userId] = presence
                            trySend(mergedMembers())
                        }
                    presenceListeners[userId] = presenceListener
                }

                trySend(mergedMembers())
            }

        val freshnessTickerJob = CoroutineScope(Dispatchers.Default).launch {
            while (true) {
                kotlinx.coroutines.delay(3_000L)
                trySend(mergedMembers())
            }
        }
        awaitClose {
            freshnessTickerJob.cancel()
            listener.remove()
            presenceListeners.values.forEach { it.remove() }
            presenceListeners.clear()
            memberByUserId.clear()
            presenceByUserId.clear()
        }
    }
    
    fun observeUserPresence(userId: String): Flow<UserPresenceDto?> = callbackFlow {
        val listener = firestore.collection(PRESENCE_COLLECTION)
            .document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val presence = if (snapshot != null && snapshot.exists()) {
                    val isOnline = snapshot.getBoolean("isOnline") ?: false
                    val lastSeenAt = parseMillis(snapshot.get("lastSeenAt")) ?: 0L
                    UserPresenceDto(
                        userId = userId,
                        isOnline = isOnline,
                        lastSeenAt = lastSeenAt
                    )
                } else {
                    null
                }
                trySend(presence)
            }
        awaitClose { listener.remove() }
    }
    
    suspend fun updateUserPresence(userId: String, isOnline: Boolean): Result<Unit> = runCatching {
        val presenceDto = UserPresenceDto(
            userId = userId,
            isOnline = isOnline,
            lastSeenAt = System.currentTimeMillis()
        )
        firestore.collection(PRESENCE_COLLECTION)
            .document(userId)
            .set(presenceDto)
            .await()
    }

    suspend fun updateMemberPresence(roomId: String, userId: String, isOnline: Boolean): Result<Unit> = runCatching {
        val memberRef = firestore.collection(ROOMS_COLLECTION)
            .document(roomId)
            .collection(MEMBERS_COLLECTION)
            .document(userId)

        memberRef.set(
            mapOf(
                "userId" to userId,
                "isOnline" to isOnline,
                "lastSeenAt" to System.currentTimeMillis()
            ),
            com.google.firebase.firestore.SetOptions.merge()
        ).await()
    }

    suspend fun updateTypingStatus(roomId: String, userId: String, isTyping: Boolean): Result<Unit> = runCatching {
        val memberRef = firestore.collection(ROOMS_COLLECTION)
            .document(roomId)
            .collection(MEMBERS_COLLECTION)
            .document(userId)

        val now = System.currentTimeMillis()

        memberRef.set(
            mapOf(
                "userId" to userId,
                "isOnline" to true,
                "isTyping" to isTyping,
                "typingAt" to now,
                "lastSeenAt" to now
            ),
            com.google.firebase.firestore.SetOptions.merge()
        ).await()
    }
    
    // Search
    fun searchRooms(query: String): Flow<List<StudyRoomDto>> = callbackFlow {
        val searchQuery = query.lowercase()
        val listener = firestore.collection(ROOMS_COLLECTION)
            .whereEqualTo("isActive", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val rooms = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(StudyRoomDto::class.java)?.copy(id = doc.id)
                }?.filter {
                    it.name.lowercase().contains(searchQuery) ||
                    it.description.lowercase().contains(searchQuery) ||
                    it.category.lowercase().contains(searchQuery)
                } ?: emptyList()
                trySend(rooms)
            }
        awaitClose { listener.remove() }
    }
}
