package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.entity.ExpectedIncomeEntity
import com.example.data.entity.MoneySource
import com.example.data.entity.TransactionEntity
import com.example.data.entity.TransactionType
import com.example.data.entity.UserSettingsEntity
import com.example.data.repository.MoneyRepository
import com.example.model.MoneyCalculations
import com.example.model.MoneyOverview
import com.example.reminder.ReminderManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class MoneyUiState(
    val isLoading: Boolean = true,
    val isSetupCompleted: Boolean = false,
    val overview: MoneyOverview = MoneyOverview(),
    val transactions: List<TransactionEntity> = emptyList(),
    val expectedIncomes: List<ExpectedIncomeEntity> = emptyList(),
    val totalExpectedIncome: Double = 0.0,
    val moneyAfterExpectedIncome: Double = 0.0,
    val dailyAllowanceAfterExpectedIncome: Double = 0.0,
    val middayReminderEnabled: Boolean = true,
    val middayHour: Int = 14,
    val middayMinute: Int = 0,
    val eveningReminderEnabled: Boolean = true,
    val eveningHour: Int = 21,
    val eveningMinute: Int = 0,
    val userSettings: UserSettingsEntity = UserSettingsEntity()
)

class MoneyViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MoneyRepository(AppDatabase.getInstance(application))

    // Interactive Projection scenario state
    val scenarioDailySpending = MutableStateFlow(20.0)

    val uiState: StateFlow<MoneyUiState> = combine(
        repository.userSettings,
        repository.allTransactions,
        repository.allExpectedIncomes
    ) { settings, transactions, expectedIncomes ->
        val userSettings = settings ?: UserSettingsEntity()

        // Calculate this month's spending
        val cal = Calendar.getInstance()
        val currentMonth = cal.get(Calendar.MONTH)
        val currentYear = cal.get(Calendar.YEAR)

        val monthSpent = transactions
            .filter { tx ->
                if (tx.type != TransactionType.EXPENSE) return@filter false
                val txCal = Calendar.getInstance().apply { timeInMillis = tx.timestamp }
                txCal.get(Calendar.MONTH) == currentMonth && txCal.get(Calendar.YEAR) == currentYear
            }
            .sumOf { it.amount }

        val overview = MoneyCalculations.computeOverview(
            online = userSettings.onlineBalance,
            cash = userSettings.cashBalance,
            emergencyReserve = userSettings.emergencyReserve,
            spentThisMonth = monthSpent
        )

        val pendingExpected = expectedIncomes.filter { !it.isReceived }
        val totalExpected = pendingExpected.sumOf { it.amount }
        val moneyAfterExpected = overview.totalMoney + totalExpected
        val spendableAfterExpected = overview.normalSpendableMoney + totalExpected
        val dailyAfterExpected = if (overview.daysRemaining > 0) spendableAfterExpected / overview.daysRemaining else 0.0

        MoneyUiState(
            isLoading = false,
            isSetupCompleted = userSettings.isSetupCompleted,
            overview = overview,
            transactions = transactions,
            expectedIncomes = expectedIncomes,
            totalExpectedIncome = totalExpected,
            moneyAfterExpectedIncome = moneyAfterExpected,
            dailyAllowanceAfterExpectedIncome = dailyAfterExpected,
            middayReminderEnabled = userSettings.middayReminderEnabled,
            middayHour = userSettings.middayReminderHour,
            middayMinute = userSettings.middayReminderMinute,
            eveningReminderEnabled = userSettings.eveningReminderEnabled,
            eveningHour = userSettings.eveningReminderHour,
            eveningMinute = userSettings.eveningReminderMinute,
            userSettings = userSettings
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MoneyUiState()
    )

    fun saveFirstTimeSetup(online: Double, cash: Double, emergencyReserve: Double) {
        viewModelScope.launch {
            repository.saveInitialSetup(online, cash, emergencyReserve)
            ReminderManager.scheduleAllReminders(getApplication())
        }
    }

    fun addMoney(source: MoneySource, amount: Double, category: String, note: String) {
        if (amount <= 0.0) return
        viewModelScope.launch {
            repository.addMoney(source, amount, category, note)
        }
    }

    fun addExpense(source: MoneySource, amount: Double, category: String, note: String) {
        if (amount <= 0.0) return
        viewModelScope.launch {
            repository.addExpense(source, amount, category, note)
        }
    }

    fun updateEmergencyReserve(newReserve: Double) {
        viewModelScope.launch {
            repository.updateEmergencyReserve(newReserve)
        }
    }

    fun releaseEmergencyCash(amount: Double, note: String) {
        if (amount <= 0.0) return
        viewModelScope.launch {
            repository.releaseEmergencyCash(amount, note)
        }
    }

    fun updateBalancesManually(online: Double, cash: Double, reserve: Double) {
        viewModelScope.launch {
            repository.updateBalancesManually(online, cash, reserve)
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    fun updateTransaction(oldTransaction: TransactionEntity, newTransaction: TransactionEntity) {
        viewModelScope.launch {
            repository.updateTransaction(oldTransaction, newTransaction)
        }
    }

    fun addExpectedIncome(amount: Double, dateMillis: Long, source: String, note: String) {
        if (amount <= 0.0) return
        viewModelScope.launch {
            repository.addExpectedIncome(amount, dateMillis, source, note)
        }
    }

    fun markExpectedIncomeReceived(item: ExpectedIncomeEntity, receiveSource: MoneySource) {
        viewModelScope.launch {
            repository.markExpectedIncomeReceived(item, receiveSource)
        }
    }

    fun deleteExpectedIncome(item: ExpectedIncomeEntity) {
        viewModelScope.launch {
            repository.deleteExpectedIncome(item)
        }
    }

    fun updateReminderSettings(
        middayEnabled: Boolean,
        middayHour: Int,
        middayMinute: Int,
        eveningEnabled: Boolean,
        eveningHour: Int,
        eveningMinute: Int
    ) {
        viewModelScope.launch {
            repository.updateReminderSettings(
                middayEnabled,
                middayHour,
                middayMinute,
                eveningEnabled,
                eveningHour,
                eveningMinute
            )
            ReminderManager.scheduleAllReminders(getApplication())
        }
    }

    fun triggerTestNotification(type: String) {
        viewModelScope.launch {
            val hasTx = repository.hasTransactionsToday()
            ReminderManager.showReminderNotification(getApplication(), type, hasTx)
        }
    }

    fun resetAllData() {
        viewModelScope.launch {
            repository.resetAllData()
        }
    }
}
