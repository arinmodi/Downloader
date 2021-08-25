package com.example.socialmediadownloader.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import java.util.*


class DeviceBootReciver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent!!.action!!.equals("android.intent.action.BOOT_COMPLETED")) {
            val alarmIntent = Intent(context, MyNewIntentService::class.java)
            val pendingIntent: PendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, 0)
            val manager = context!!.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val calendar: Calendar = Calendar.getInstance()
            calendar.setTimeInMillis(System.currentTimeMillis())
            calendar.set(Calendar.HOUR_OF_DAY, 7)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 1)
            manager.setRepeating(
                AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, pendingIntent
            )
        }
    }
}