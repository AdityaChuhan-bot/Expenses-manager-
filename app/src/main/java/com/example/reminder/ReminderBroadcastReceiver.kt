package com.example.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.AppDatabase
import com.example.data.repository.MoneyRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val type = intent.getStringExtra(ReminderManager.EXTRA_REMINDER_TYPE) ?: ReminderManager.TYPE_MIDDAY

        val repository = MoneyRepository(AppDatabase.getInstance(context))
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val settings = repository.getSettingsSync()
                val isEnabled = if (type == ReminderManager.TYPE_MIDDAY) {
                    settings.middayReminderEnabled
                } else {
                    settings.eveningReminderEnabled
                }

                if (isEnabled) {
                    val hasTransactions = repository.hasTransactionsToday()
                    ReminderManager.showReminderNotification(context, type, hasTransactions)

                    // Re-schedule for next day at configured time
                    val hour = if (type == ReminderManager.TYPE_MIDDAY) settings.middayReminderHour else settings.eveningReminderHour
                    val minute = if (type == ReminderManager.TYPE_MIDDAY) settings.middayReminderMinute else settings.eveningReminderMinute
                    val reqCode = if (type == ReminderManager.TYPE_MIDDAY) 2001 else 2002

                    ReminderManager.scheduleDailyAlarm(context, type, hour, minute, reqCode)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
