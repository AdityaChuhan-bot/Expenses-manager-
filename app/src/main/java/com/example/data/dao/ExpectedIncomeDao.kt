package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.ExpectedIncomeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpectedIncomeDao {
    @Query("SELECT * FROM expected_incomes ORDER BY expectedDateMillis ASC")
    fun getAllExpectedIncomes(): Flow<List<ExpectedIncomeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpectedIncome(item: ExpectedIncomeEntity): Long

    @Update
    suspend fun updateExpectedIncome(item: ExpectedIncomeEntity)

    @Delete
    suspend fun deleteExpectedIncome(item: ExpectedIncomeEntity)

    @Query("DELETE FROM expected_incomes")
    suspend fun deleteAll()
}
