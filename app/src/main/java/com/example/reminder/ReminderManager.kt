package com.example.reminder

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import com.example.data.AppDatabase
import com.example.data.repository.MoneyRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

object ReminderManager {

    const val CHANNEL_ID = "pocketflow_daily_reminders"
    const val CHANNEL_NAME = "Daily Money Reminders"

    const val ACTION_ADD_MONEY = "com.example.pocketflow.ACTION_ADD_MONEY"
    const val ACTION_ADD_EXPENSE = "com.example.pocketflow.ACTION_ADD_EXPENSE"
    const val ACTION_NO_TRANSACTIONS = "com.example.pocketflow.ACTION_NO_TRANSACTIONS"

    const val EXTRA_REMINDER_TYPE = "reminder_type"
    const val TYPE_MIDDAY = "midday"
    const val TYPE_EVENING = "evening"

    const val NOTIFICATION_ID_MIDDAY = 1001
    const val NOTIFICATION_ID_EVENING = 1002

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                importance
            ).apply {
                description = "Reminders to keep your cash and online balance up to date"
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun scheduleAllReminders(context: Context) {
        val repository = MoneyRepository(AppDatabase.getInstance(context))
        CoroutineScope(Dispatchers.IO).launch {
            val settings = repository.getSettingsSync()
            if (settings.middayReminderEnabled) {
                scheduleDailyAlarm(
                    context = context,
                    type = TYPE_MIDDAY,
                    hour = settings.middayReminderHour,
                    minute = settings.middayReminderMinute,
                    requestCode = 2001
                )
            } else {
                cancelAlarm(context, 2001, TYPE_MIDDAY)
            }

            if (settings.eveningReminderEnabled) {
                scheduleDailyAlarm(
                    context = context,
                    type = TYPE_EVENING,
                    hour = settings.eveningReminderHour,
                    minute = settings.eveningReminderMinute,
                    requestCode = 2002
                )
            } else {
                cancelAlarm(context, 2002, TYPE_EVENING)
            }
        }
    }

    fun scheduleDailyAlarm(
        context: Context,
        type: String,
        hour: Int,
        minute: Int,
        requestCode: Int
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, ReminderBroadcastReceiver::class.java).apply {
            putExtra(EXTRA_REMINDER_TYPE, type)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        } catch (_: SecurityException) {
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }

    fun cancelAlarm(context: Context, requestCode: Int, type: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderBroadcastReceiver::class.java).apply {
            putExtra(EXTRA_REMINDER_TYPE, type)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    fun showReminderNotification(
        context: Context,
        type: String,
        hasTransactionsToday: Boolean
    ) {
        createNotificationChannel(context)

        val isMidday = type == TYPE_MIDDAY
        val notificationId = if (isMidday) NOTIFICATION_ID_MIDDAY else NOTIFICATION_ID_EVENING

        val title = if (isMidday) "💰 Money check" else "📊 End-of-day money check"
        val message = if (hasTransactionsToday) {
            "Balance updated recently. You're good. 👍"
        } else {
            if (isMidday) {
                "Have you spent or received any money today? Update your balance."
            } else {
                "Did you spend or receive any money today? Update your balance before ending the day."
            }
        }

        // Tap open app intent
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            3001,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Add Expense quick action
        val addExpenseIntent = Intent(context, MainActivity::class.java).apply {
            action = ACTION_ADD_EXPENSE
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val addExpensePendingIntent = PendingIntent.getActivity(
            context,
            3002,
            addExpenseIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Add Money quick action
        val addMoneyIntent = Intent(context, MainActivity::class.java).apply {
            action = ACTION_ADD_MONEY
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val addMoneyPendingIntent = PendingIntent.getActivity(
            context,
            3003,
            addMoneyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(openAppPendingIntent)
            .setAutoCancel(true)
            .addAction(0, "− Add Expense", addExpensePendingIntent)
            .addAction(0, "+ Add Money", addMoneyPendingIntent)

        if (!isMidday) {
            // End-of-day extra action: "No transactions today"
            val noTxIntent = Intent(context, NotificationActionReceiver::class.java).apply {
                action = ACTION_NO_TRANSACTIONS
                putExtra("notification_id", notificationId)
            }
            val noTxPendingIntent = PendingIntent.getBroadcast(
                context,
                3004,
                noTxIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(0, "No transactions today", noTxPendingIntent)
        }

        try {
            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
        } catch (_: SecurityException) {
            // Handle notification permission not granted gracefully
        }
    }
}
