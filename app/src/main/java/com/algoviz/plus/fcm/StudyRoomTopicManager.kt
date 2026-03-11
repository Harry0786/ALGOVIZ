package com.algoviz.plus.fcm

import com.google.firebase.messaging.FirebaseMessaging
import timber.log.Timber

object StudyRoomTopicManager {

    private fun topicForRoom(roomId: String): String {
        return "study_room_${roomId.replace(Regex("[^A-Za-z0-9_-]"), "_")}"
    }

    fun subscribeToRoom(roomId: String) {
        FirebaseMessaging.getInstance().subscribeToTopic(topicForRoom(roomId))
            .addOnSuccessListener {
                Timber.d("Subscribed to room topic for %s", roomId)
            }
            .addOnFailureListener { error ->
                Timber.w(error, "Failed to subscribe to room topic for %s", roomId)
            }
    }

    fun unsubscribeFromRoom(roomId: String) {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topicForRoom(roomId))
            .addOnSuccessListener {
                Timber.d("Unsubscribed from room topic for %s", roomId)
            }
            .addOnFailureListener { error ->
                Timber.w(error, "Failed to unsubscribe from room topic for %s", roomId)
            }
    }
}
