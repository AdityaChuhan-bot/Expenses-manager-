package com.example.data.repository

import com.example.data.AppDatabase
import com.example.data.entity.ExpectedIncomeEntity
import com.example.data.entity.MoneySource
import com.example.data.entity.TransactionEntity
import com.example.data.entity.TransactionType
import com.example.data.entity.UserSettingsEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.Calendar

class MoneyRepository(private val database: AppDatabase) {

    private val transactionDao = database.transactionDao()
    private val expectedIncomeDao = database.expectedIncomeDao()
    private val userSettingsDao = database.userSettingsDao()

    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()
    val allExpectedIncomes: Flow<List<ExpectedIncomeEntity>> = expectedIncomeDao.getAllExpectedIncomes()
    val userSettings: Flow<UserSettingsEntity?> = userSettingsDao.getSettingsFlow()

    suspend fun getSettingsSync(): UserSettingsEntity = withContext(Dispatchers.IO) {
        userSettingsDao.getSettings() ?: UserSettingsEntity()
    }

    suspend fun saveInitialSetup(
        online: Double,
        cash: Double,
        emergencyReserve: Double
    ) = withContext(Dispatchers.IO) {
        val current = getSettingsSync()
        val updated = current.copy(
            onlineBalance = online,
            cashBalance = cash,
            emergencyReserve = emergencyReserve,
            isSetupCompleted = true,
            lastActiveTimestamp = System.currentTimeMillis()
        )
        userSettingsDao.insertOrUpdate(updated)
    }

    suspend fun updateEmergencyReserve(newReserve: Double) = withContext(Dispatchers.IO) {
        val current = getSettingsSync()
        val updated = current.copy(
            emergencyReserve = newReserve.coerceAtLeast(0.0),
            lastActiveTimestamp = System.currentTimeMillis()
        )
        userSettingsDao.insertOrUpdate(updated)
    }

    suspend fun updateBalancesManually(
        newOnline: Double,
        newCash: Double,
        newReserve: Double
    ) = withContext(Dispatchers.IO) {
        val current = getSettingsSync()
        val updated = current.copy(
            onlineBalance = newOnline.coerceAtLeast(0.0),
            cashBalance = newCash.coerceAtLeast(0.0),
            emergencyReserve = newReserve.coerceAtLeast(0.0),
            lastActiveTimestamp = System.currentTimeMillis()
        )
        userSettingsDao.insertOrUpdate(updated)
    }

    suspend fun addMoney(
        source: MoneySource,
        amount: Double,
        category: String,
        note: String
    ): Long = withContext(Dispatchers.IO) {
        val current = getSettingsSync()
        val updatedSettings = when (source) {
            MoneySource.ONLINE -> current.copy(
                onlineBalance = current.onlineBalance + amount,
                lastActiveTimestamp = System.currentTimeMillis()
            )
            MoneySource.CASH -> current.copy(
                cashBalance = current.cashBalance + amount,
                lastActiveTimestamp = System.currentTimeMillis()
            )
            MoneySource.NONE -> current
        }
        userSettingsDao.insertOrUpdate(updatedSettings)

        val transaction = TransactionEntity(
            type = TransactionType.INCOME,
            source = source,
            amount = amount,
            category = category.ifBlank { "Income" },
            note = note,
            timestamp = System.currentTimeMillis()
        )
        transactionDao.insertTransaction(transaction)
    }

    suspend fun addExpense(
        source: MoneySource,
        amount: Double,
        category: String,
        note: String
    ): Long = withContext(Dispatchers.IO) {
        val current = getSettingsSync()
        val updatedSettings = when (source) {
            MoneySource.ONLINE -> current.copy(
                onlineBalance = (current.onlineBalance - amount).coerceAtLeast(0.0),
                lastActiveTimestamp = System.currentTimeMillis()
            )
            MoneySource.CASH -> current.copy(
                cashBalance = (current.cashBalance - amount).coerceAtLeast(0.0),
                lastActiveTimestamp = System.currentTimeMillis()
            )
            MoneySource.NONE -> current
        }
        userSettingsDao.insertOrUpdate(updatedSettings)

        val transaction = TransactionEntity(
            type = TransactionType.EXPENSE,
            source = source,
            amount = amount,
            category = category.ifBlank { "Expense" },
            note = note,
            timestamp = System.currentTimeMillis()
        )
        transactionDao.insertTransaction(transaction)
    }

    suspend fun releaseEmergencyCash(
        amount: Double,
        note: String
    ): Long = withContext(Dispatchers.IO) {
        val current = getSettingsSync()
        val actualRelease = amount.coerceAtMost(current.emergencyReserve).coerceAtLeast(0.0)
        val newReserve = (current.emergencyReserve - actualRelease).coerceAtLeast(0.0)

        val updatedSettings = current.copy(
            emergencyReserve = newReserve,
            lastActiveTimestamp = System.currentTimeMillis()
        )
        userSettingsDao.insertOrUpdate(updatedSettings)

        val transaction = TransactionEntity(
            type = TransactionType.EMERGENCY_RELEASE,
            source = MoneySource.NONE,
            amount = actualRelease,
            category = "Emergency Cash",
            note = if (note.isBlank()) "Released ₹${actualRelease.toInt()} from Emergency Reserve" else note,
            timestamp = System.currentTimeMillis()
        )
        transactionDao.insertTransaction(transaction)
    }

    suspend fun deleteTransaction(transaction: TransactionEntity) = withContext(Dispatchers.IO) {
        val current = getSettingsSync()
        var newOnline = current.onlineBalance
        var newCash = current.cashBalance
        var newReserve = current.emergencyReserve

        when (transaction.type) {
            TransactionType.INCOME -> {
                when (transaction.source) {
                    MoneySource.ONLINE -> newOnline = (newOnline - transaction.amount).coerceAtLeast(0.0)
                    MoneySource.CASH -> newCash = (newCash - transaction.amount).coerceAtLeast(0.0)
                    MoneySource.NONE -> Unit
                }
            }
            TransactionType.EXPENSE -> {
                when (transaction.source) {
                    MoneySource.ONLINE -> newOnline += transaction.amount
                    MoneySource.CASH -> newCash += transaction.amount
                    MoneySource.NONE -> Unit
                }
            }
            TransactionType.EMERGENCY_RELEASE -> {
                newReserve += transaction.amount
            }
        }

        userSettingsDao.insertOrUpdate(
            current.copy(
                onlineBalance = newOnline,
                cashBalance = newCash,
                emergencyReserve = newReserve,
                lastActiveTimestamp = System.currentTimeMillis()
            )
        )
        transactionDao.deleteTransaction(transaction)
    }

    suspend fun updateTransaction(
        oldTransaction: TransactionEntity,
        newTransaction: TransactionEntity
    ) = withContext(Dispatchers.IO) {
        val current = getSettingsSync()
        var newOnline = current.onlineBalance
        var newCash = current.cashBalance
        var newReserve = current.emergencyReserve

        // Step 1: Revert old
        when (oldTransaction.type) {
            TransactionType.INCOME -> {
                when (oldTransaction.source) {
                    MoneySource.ONLINE -> newOnline = (newOnline - oldTransaction.amount).coerceAtLeast(0.0)
                    MoneySource.CASH -> newCash = (newCash - oldTransaction.amount).coerceAtLeast(0.0)
                    MoneySource.NONE -> Unit
                }
            }
            TransactionType.EXPENSE -> {
                when (oldTransaction.source) {
                    MoneySource.ONLINE -> newOnline += oldTransaction.amount
                    MoneySource.CASH -> newCash += oldTransaction.amount
                    MoneySource.NONE -> Unit
                }
            }
            TransactionType.EMERGENCY_RELEASE -> {
                newReserve += oldTransaction.amount
            }
        }

        // Step 2: Apply new
        when (newTransaction.type) {
            TransactionType.INCOME -> {
                when (newTransaction.source) {
                    MoneySource.ONLINE -> newOnline += newTransaction.amount
                    MoneySource.CASH -> newCash += newTransaction.amount
                    MoneySource.NONE -> Unit
                }
            }
            TransactionType.EXPENSE -> {
                when (newTransaction.source) {
                    MoneySource.ONLINE -> newOnline = (newOnline - newTransaction.amount).coerceAtLeast(0.0)
                    MoneySource.CASH -> newCash = (newCash - newTransaction.amount).coerceAtLeast(0.0)
                    MoneySource.NONE -> Unit
                }
            }
            TransactionType.EMERGENCY_RELEASE -> {
                newReserve = (newReserve - newTransaction.amount).coerceAtLeast(0.0)
            }
        }

        userSettingsDao.insertOrUpdate(
            current.copy(
                onlineBalance = newOnline,
                cashBalance = newCash,
                emergencyReserve = newReserve,
                lastActiveTimestamp = System.currentTimeMillis()
            )
        )
        transactionDao.updateTransaction(newTransaction)
    }

    suspend fun addExpectedIncome(
        amount: Double,
        expectedDateMillis: Long,
        source: String,
        note: String
    ): Long = withContext(Dispatchers.IO) {
        val entity = ExpectedIncomeEntity(
            amount = amount,
            expectedDateMillis = expectedDateMillis,
            source = source.ifBlank { "Expected" },
            note = note,
            isReceived = false
        )
        expectedIncomeDao.insertExpectedIncome(entity)
    }

    suspend fun markExpectedIncomeReceived(
        item: ExpectedIncomeEntity,
        receiveSource: MoneySource
    ) = withContext(Dispatchers.IO) {
        // Add as real money
        addMoney(
            source = receiveSource,
            amount = item.amount,
            category = item.source,
            note = if (item.note.isNotBlank()) "Expected: ${item.note}" else "Received from expected income"
        )
        expectedIncomeDao.deleteExpectedIncome(item)
    }

    suspend fun deleteExpectedIncome(item: ExpectedIncomeEntity) = withContext(Dispatchers.IO) {
        expectedIncomeDao.deleteExpectedIncome(item)
    }

    suspend fun updateReminderSettings(
        middayEnabled: Boolean,
        middayHour: Int,
        middayMinute: Int,
        eveningEnabled: Boolean,
        eveningHour: Int,
        eveningMinute: Int
    ) = withContext(Dispatchers.IO) {
        val current = getSettingsSync()
        val updated = current.copy(
            middayReminderEnabled = middayEnabled,
            middayReminderHour = middayHour,
            middayReminderMinute = middayMinute,
            eveningReminderEnabled = eveningEnabled,
            eveningReminderHour = eveningHour,
            eveningReminderMinute = eveningMinute
        )
        userSettingsDao.insertOrUpdate(updated)
    }

    suspend fun hasTransactionsToday(): Boolean = withContext(Dispatchers.IO) {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startOfDay = cal.timeInMillis

        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        val endOfDay = cal.timeInMillis

        val todayTransactions = transactionDao.getTransactionsBetweenSync(startOfDay, endOfDay)
        todayTransactions.isNotEmpty()
    }

    suspend fun resetAllData() = withContext(Dispatchers.IO) {
        transactionDao.deleteAllTransactions()
        expectedIncomeDao.deleteAll()
        userSettingsDao.insertOrUpdate(UserSettingsEntity())
    }
}
