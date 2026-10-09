package com.example.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat

class NotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ReminderManager.ACTION_NO_TRANSACTIONS) {
            val notificationId = intent.getIntExtra("notification_id", ReminderManager.NOTIFICATION_ID_EVENING)
            NotificationManagerCompat.from(context).cancel(notificationId)
            Toast.makeText(context, "Recorded: No transactions today. Balance unchanged.", Toast.LENGTH_SHORT).show()
        }
    }
}
