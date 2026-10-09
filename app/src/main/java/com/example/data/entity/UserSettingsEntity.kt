package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_settings")
data class UserSettingsEntity(
    @PrimaryKey
    val id: Int = 1,
    val onlineBalance: Double = 0.0,
    val cashBalance: Double = 0.0,
    val emergencyReserve: Double = 0.0,
    val isSetupCompleted: Boolean = false,
    val middayReminderEnabled: Boolean = true,
    val middayReminderHour: Int = 14,
    val middayReminderMinute: Int = 0,
    val eveningReminderEnabled: Boolean = true,
    val eveningReminderHour: Int = 21,
    val eveningReminderMinute: Int = 0,
    val lastActiveTimestamp: Long = System.currentTimeMillis()
)
