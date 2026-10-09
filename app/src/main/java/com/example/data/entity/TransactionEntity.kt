package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransactionType {
    INCOME,
    EXPENSE,
    EMERGENCY_RELEASE
}

enum class MoneySource {
    ONLINE,
    CASH,
    NONE
}

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: TransactionType,
    val source: MoneySource,
    val amount: Double,
    val category: String,
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
