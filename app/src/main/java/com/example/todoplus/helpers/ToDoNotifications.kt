package com.example.todoplus.helpers

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.todoplus.MainActivity
import com.example.todoplus.R

const val channelID = "toDoChannel"
const val titleExtra = "task_title"
const val detailsExtra = "task_details"
const val notificationIDExtra = "task_id"

class ToDoNotifications : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        println("ID: " + intent!!.getIntExtra(notificationIDExtra, -1))
        val mainActivityIntent = Intent(context, MainActivity::class.java)
        val activityIntent = PendingIntent.getActivity(context, -2, mainActivityIntent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)

        val notification = NotificationCompat.Builder(context, channelID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(intent?.getStringExtra(titleExtra))
            .setContentText(intent?.getStringExtra(detailsExtra))
            .setContentIntent(activityIntent)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(intent!!.getIntExtra(notificationIDExtra, -1), notification)
    }
}