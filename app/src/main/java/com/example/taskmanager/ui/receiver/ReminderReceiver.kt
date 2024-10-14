package com.example.taskmanager.ui.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.taskmanager.R
import android.util.Log
import android.widget.Toast


class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val taskTitle = intent.getStringExtra("taskTitle")

        // Log or Toast to check if the reminder is triggered
        Toast.makeText(context, "Reminder received for task: $taskTitle", Toast.LENGTH_SHORT).show()

        // Check if notification permission is granted for Android 13 and above
        if (ActivityCompat.checkSelfPermission(
                context, android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED) {

            // Create notification
            val builder = NotificationCompat.Builder(context, "task_manager_channel")
                .setSmallIcon(R.drawable.ic_notification)  // Ensure this icon exists
                .setContentTitle("Task Reminder")
                .setContentText("Reminder for task: $taskTitle")
                .setPriority(NotificationCompat.PRIORITY_HIGH) // Priority for the notification
                .setAutoCancel(true)
                .setVibrate(longArrayOf(0, 500, 500, 500))  // Vibration pattern
                .setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI) // Sound

            // Show the notification
            with(NotificationManagerCompat.from(context)) {
                notify(taskTitle.hashCode(), builder.build())
            }
        } else {
            // Handle the case where the POST_NOTIFICATIONS permission is not granted
            Log.e("ReminderReceiver", "Notification permission not granted")
        }
    }
}
