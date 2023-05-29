package com.matthaigh27.chatgptwrapper.services

import android.content.Intent
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.matthaigh27.chatgptwrapper.utils.Constants.TAG

class MessageService : FirebaseMessagingService() {

    /**
     * this function is called when langchain server pushs notification into FCM
     * @param remoteMessage is sent by FCM when langchain server pushs notification
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        if (remoteMessage.notification != null) {
            Log.d(
                TAG, "Message Notification Body: " + remoteMessage.notification!!.body
            )

            /** intent for sending broadcast to ChatActivity */
            val intent = Intent()
            intent.action = "android.intent.action.MAIN"
            intent.putExtra("message", remoteMessage.notification!!.body)

            /** send broadcast to ChatActivity
             * So ChatActivity can receive remoteMessage from this service */
            sendBroadcast(intent)
        }
    }
}