package com.algoviz.plus.fcm

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
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
        
        message.notification?.let {
            Timber.d("Notification: ${it.title} - ${it.body}")
            // Handle notification
        }
        
        message.data.isNotEmpty().let {
            Timber.d("Message data: ${message.data}")
            // Handle data payload
        }
    }
}
