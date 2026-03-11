package com.algoviz.plus.data.studyroom.remote

import com.algoviz.plus.data.studyroom.model.MessageDto
import com.algoviz.plus.data.studyroom.model.RoomMemberDto
import com.algoviz.plus.data.studyroom.model.StudyRoomDto
import com.algoviz.plus.data.studyroom.model.UserPresenceDto
import com.google.firebase.firestore.FirebaseFirestore
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
        
        val memberDto = RoomMemberDto(
            userId = userId,
            userName = userName,
            joinedAt = System.currentTimeMillis(),
            isOnline = true
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
        val listener = firestore.collection(ROOMS_COLLECTION)
            .document(roomId)
            .collection(MEMBERS_COLLECTION)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val members = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(RoomMemberDto::class.java)
                } ?: emptyList()
                trySend(members)
            }
        awaitClose { listener.remove() }
    }
    
    fun observeUserPresence(userId: String): Flow<UserPresenceDto?> = callbackFlow {
        val listener = firestore.collection(PRESENCE_COLLECTION)
            .document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val presence = snapshot?.toObject(UserPresenceDto::class.java)
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
