package com.matthaigh27.chatgptwrapper

import android.Manifest
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.util.Log
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.matthaigh27.chatgptwrapper.utils.Constants
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import java.util.Random

class MyApplication : Application() {

    private var mFCMToken: String = String()
    private var mUUID: String = String()
    override fun onCreate() {
        super.onCreate()

        initToken()
        // on below line we are getting device id.
        mUUID = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        appContext = applicationContext as MyApplication
    }

    private fun initToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(Constants.TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            /**
             * Get new FCM registration token
             */

            /**
             * Get new FCM registration token
             */
            mFCMToken = task.result
            Log.d(Constants.TAG, mFCMToken)
        })
    }

    fun getFCMToken(): String {
        return this.mFCMToken
    }

    fun getUUID(): String {
        return this.mUUID
    }

    /**
     * this shows system notification with message
     * @param message to be shown with system notification
     */
    fun showNotification(message: String) {
        val notificationId: Int = Random().nextInt()
        val channelId = "chat_message"

        val builder = NotificationCompat.Builder(this, channelId)
        builder.setSmallIcon(R.drawable.ic_notification)
        builder.setContentTitle(Constants.TAG)
        builder.setContentText(message)
        builder.setStyle(
            NotificationCompat.BigTextStyle().bigText(
                message
            )
        )
        builder.priority = NotificationCompat.PRIORITY_DEFAULT
        builder.setAutoCancel(true)

        val channelName: CharSequence = "Chat Message"
        val channelDescription = "This notification channel is used for chat message notifications"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(channelId, channelName, importance)
        channel.description = channelDescription
        val notificationManager = getSystemService(
            NotificationManager::class.java
        )
        notificationManager.createNotificationChannel(channel)
        val notificationManagerCompat = NotificationManagerCompat.from(this)
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        notificationManagerCompat.notify(notificationId, builder.build())
    }

    companion object {
        lateinit var appContext: MyApplication
    }
}