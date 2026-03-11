package com.algoviz.plus.fcm

import android.app.ActivityManager
import android.content.Context
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.auth.FirebaseAuth
import timber.log.Timber

class AlgoVizMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Timber.d("FCM Token: $token")
        // Send token to backend
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Timber.d("Message received from: ${message.from}")

        if (!isAppInForeground()) {
            Timber.d("Skipping push display because app is not in foreground")
            return
        }

        val title = message.notification?.title
            ?: message.data["title"]
            ?: "New study room message"
        val body = message.notification?.body
            ?: message.data["body"]
            ?: message.data["message"]
            ?: "Open AlgoViz to read the latest chat update."
        val roomId = message.data["roomId"]
        val senderId = message.data["senderId"]
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        if (senderId != null && senderId == currentUserId) {
            Timber.d("Skipping self-notification for senderId=%s", senderId)
            return
        }

        Timber.d("Notification: $title - $body")

        if (message.data.isNotEmpty()) {
            Timber.d("Message data: ${message.data}")
        }

        StudyRoomNotificationHelper.showChatNotification(
            context = this,
            title = title,
            body = body,
            roomId = roomId
        )
    }

    private fun isAppInForeground(): Boolean {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager ?: return false
        val runningProcesses = activityManager.runningAppProcesses ?: return false
        val processName = packageName

        return runningProcesses.any {
            it.processName == processName &&
                it.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
        }
    }
}
