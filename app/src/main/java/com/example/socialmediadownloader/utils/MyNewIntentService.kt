package com.example.socialmediadownloader.utils

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.socialmediadownloader.activities.MainActivity


class MyNewIntentService : BroadcastReceiver() {

    private val NOTIFICATION_ID = 3
    private val CHANNEL_ID  = "NOTIFY"



    override fun onReceive(context: Context?, intent: Intent?) {
        if(context != null){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Files Notification",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                manager.createNotificationChannel(channel)
            }

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)

            val contentView = RemoteViews("com.example.socialmediadownloader", com.example.socialmediadownloader.R.layout.notification_layout)

            builder.setSmallIcon(com.example.socialmediadownloader.R.drawable.bell)
            builder.setPriority(NotificationCompat.PRIORITY_DEFAULT)
            builder.setStyle(NotificationCompat.DecoratedCustomViewStyle())
            builder.setAutoCancel(true)
            builder.setContent(contentView)


            val notifyIntent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(context, 2, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT)

            builder.setContentIntent(pendingIntent)
            val notificationCompat: Notification = builder.build()
            val managerCompat = NotificationManagerCompat.from(context)
            managerCompat.notify(NOTIFICATION_ID, notificationCompat)
        }
    }

}