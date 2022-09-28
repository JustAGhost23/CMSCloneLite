package com.example.cmsclonelite.notification

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_MUTABLE
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.cmsclonelite.MainActivity
import com.example.cmsclonelite.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class NotificationMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.v("CloudMessage", "From ${message.from}")
        if (message.data.isNotEmpty()) {
            Log.v("CloudMessage", "Message Data ${message.data}")
        }
        message.data.let {
            Log.v("CloudMessage", "Message Data Body ${it["body"]}")
            Log.v("CloudMessage", "Message Data Title  ${it["title"]}")
            showNotificationOnStatusBar(it)
        }
        if (message.notification != null) {
            Log.v("CloudMessage", "Notification ${message.notification}")
            Log.v("CloudMessage", "Notification Title ${message.notification!!.title}")
            Log.v("CloudMessage", "Notification Body ${message.notification!!.body}")
        }
    }
    private fun showNotificationOnStatusBar(data: Map<String, String>) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags= Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        intent.putExtra("title",data["title"])
        intent.putExtra("body",data["body"])
        val requestCode = System.currentTimeMillis().toInt()
        val pendingIntent : PendingIntent = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getActivity(this, requestCode,intent, FLAG_MUTABLE)
        }else{
            PendingIntent.getActivity(this, requestCode, intent, PendingIntent.FLAG_CANCEL_CURRENT)
        }

        val builder = NotificationCompat.Builder(this,"Global")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(data["title"])
            .setContentText(data["body"])
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setStyle(
                NotificationCompat.BigTextStyle().bigText((data["body"]))
            )
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(this)){
            notify(requestCode,builder.build())
        }
    }
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM",token)
        saveGCMToken(token)
    }
    private fun saveGCMToken(fcmToken: String) {
        val sharedPrefs = this
            .getSharedPreferences("PREFERENCES", Context.MODE_PRIVATE)
        sharedPrefs.edit().putString("fcmToken", fcmToken).apply()
    }
}