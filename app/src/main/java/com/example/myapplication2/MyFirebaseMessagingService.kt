package com.example.myapplication2

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlin.random.Random

class MyFirebaseMessagingService: FirebaseMessagingService() {

    private val channelId= "12345"

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val intent= Intent(this,MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val manager= getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId= Random.nextInt()

        createNotificationChannel(manager)

        val intent1= PendingIntent.getActivities(this,0, arrayOf(intent), PendingIntent.FLAG_IMMUTABLE)

        val Notification= NotificationCompat.Builder(this,channelId)
            .setContentTitle(message.data["title"])
            .setContentText(message.data["body"])
            .setSmallIcon(R.drawable.ic_lchat_logo_foreground)
            .setAutoCancel(true)
            .setContentIntent(intent1)
            .build()

        manager.notify(notificationId,Notification)
    }

    private fun createNotificationChannel(manager: NotificationManager){
        val channel= NotificationChannel(channelId,"ABCD",NotificationManager.IMPORTANCE_HIGH).apply {
            description="New Chat"
            enableLights(true)
        }
        manager.createNotificationChannel(channel)
    }
}