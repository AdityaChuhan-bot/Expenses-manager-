package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expected_incomes")
data class ExpectedIncomeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val expectedDateMillis: Long,
    val source: String,
    val note: String = "",
    val isReceived: Boolean = false
)
