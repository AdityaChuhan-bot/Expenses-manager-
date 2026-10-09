package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.entity.ExpectedIncomeEntity
import com.example.data.entity.MoneySource
import com.example.data.entity.TransactionEntity
import com.example.reminder.ReminderManager
import com.example.ui.components.AddExpenseDialog
import com.example.ui.components.AddExpectedMoneyDialog
import com.example.ui.components.AddMoneyDialog
import com.example.ui.components.CalculationExplanationDialog
import com.example.ui.components.EditReserveDialog
import com.example.ui.components.EditTransactionDialog
import com.example.ui.components.MainTab
import com.example.ui.components.PocketFlowBottomBar
import com.example.ui.components.ReleaseEmergencyDialog
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.ProjectionsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TransactionsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MoneyViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MoneyViewModel by viewModels()

    // Intent quick action triggers
    private var initialAction by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        ReminderManager.createNotificationChannel(this)
        handleIntent(intent)

        setContent {
            MyApplicationTheme {
                // Request POST_NOTIFICATIONS permission on Android 13+
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val permissionLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.RequestPermission()
                    ) { isGranted ->
                        if (isGranted) {
                            ReminderManager.scheduleAllReminders(this@MainActivity)
                        }
                    }

                    LaunchedEffect(Unit) {
                        if (ContextCompat.checkSelfPermission(
                                this@MainActivity,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            ReminderManager.scheduleAllReminders(this@MainActivity)
                        }
                    }
                } else {
                    LaunchedEffect(Unit) {
                        ReminderManager.scheduleAllReminders(this@MainActivity)
                    }
                }

                PocketFlowApp(
                    viewModel = viewModel,
                    initialAction = initialAction,
                    onActionHandled = { initialAction = null }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val action = intent?.action
        when (action) {
            ReminderManager.ACTION_ADD_MONEY -> initialAction = "add_money"
            ReminderManager.ACTION_ADD_EXPENSE -> initialAction = "add_expense"
        }
    }
}

@Composable
fun PocketFlowApp(
    viewModel: MoneyViewModel,
    initialAction: String?,
    onActionHandled: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var currentTab by remember { mutableStateOf(MainTab.DASHBOARD) }

    // Dialog & sheet states
    var showAddMoneyDialog by remember { mutableStateOf(false) }
    var showAddExpenseDialog by remember { mutableStateOf(false) }
    var showReleaseReserveDialog by remember { mutableStateOf(false) }
    var showAdjustReserveDialog by remember { mutableStateOf(false) }
    var showExplanationDialog by remember { mutableStateOf(false) }
    var showAddExpectedMoneyDialog by remember { mutableStateOf(false) }
    var transactionToEdit by remember { mutableStateOf<TransactionEntity?>(null) }

    // Respond to notification quick actions
    LaunchedEffect(initialAction) {
        if (initialAction != null) {
            when (initialAction) {
                "add_money" -> showAddMoneyDialog = true
                "add_expense" -> showAddExpenseDialog = true
            }
            onActionHandled()
        }
    }

    if (state.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    // First time setup check
    if (!state.isSetupCompleted) {
        Box(modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing)) {
            OnboardingScreen(
                onCompleteSetup = { online, cash, reserve ->
                    viewModel.saveFirstTimeSetup(online, cash, reserve)
                }
            )
        }
        return
    }

    // Back button behavior: if on a secondary tab, navigate back to Dashboard
    BackHandler(enabled = currentTab != MainTab.DASHBOARD) {
        currentTab = MainTab.DASHBOARD
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        bottomBar = {
            PocketFlowBottomBar(
                currentTab = currentTab,
                onTabSelected = { currentTab = it }
            )
        }
    ) { innerPadding ->
        AnimatedContent(
            targetState = currentTab,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "tab_transition",
            modifier = Modifier.padding(innerPadding)
        ) { tab ->
            when (tab) {
                MainTab.DASHBOARD -> DashboardScreen(
                    state = state,
                    onAddMoneyClick = { showAddMoneyDialog = true },
                    onAddExpenseClick = { showAddExpenseDialog = true },
                    onReleaseEmergencyClick = { showReleaseReserveDialog = true },
                    onAdjustReserveClick = { showAdjustReserveDialog = true },
                    onExplainCalculationClick = { showExplanationDialog = true },
                    onViewAllTransactionsClick = { currentTab = MainTab.TRANSACTIONS },
                    onTransactionClick = { transactionToEdit = it }
                )

                MainTab.TRANSACTIONS -> TransactionsScreen(
                    transactions = state.transactions,
                    onEditTransaction = { transactionToEdit = it },
                    onDeleteTransaction = { viewModel.deleteTransaction(it) }
                )

                MainTab.PROJECTIONS -> ProjectionsScreen(
                    state = state,
                    onAddExpectedMoneyClick = { showAddExpectedMoneyDialog = true },
                    onMarkExpectedReceived = { item, source ->
                        viewModel.markExpectedIncomeReceived(item, source)
                    },
                    onDeleteExpectedMoney = { viewModel.deleteExpectedIncome(it) }
                )

                MainTab.SETTINGS -> SettingsScreen(
                    state = state,
                    onUpdateEmergencyReserve = { viewModel.updateEmergencyReserve(it) },
                    onUpdateBalancesManually = { o, c, r -> viewModel.updateBalancesManually(o, c, r) },
                    onUpdateReminders = { midEn, midH, midM, eveEn, eveH, eveM ->
                        viewModel.updateReminderSettings(midEn, midH, midM, eveEn, eveH, eveM)
                    },
                    onTriggerTestNotification = { type -> viewModel.triggerTestNotification(type) },
                    onResetAllData = { viewModel.resetAllData() }
                )
            }
        }
    }

    // Add Money Dialog
    if (showAddMoneyDialog) {
        AddMoneyDialog(
            currentOnline = state.overview.onlineBalance,
            currentCash = state.overview.cashBalance,
            onDismiss = { showAddMoneyDialog = false },
            onConfirm = { source, amount, tag, note ->
                viewModel.addMoney(source, amount, tag, note)
                showAddMoneyDialog = false
            }
        )
    }

    // Add Expense Dialog
    if (showAddExpenseDialog) {
        AddExpenseDialog(
            currentOnline = state.overview.onlineBalance,
            currentCash = state.overview.cashBalance,
            onDismiss = { showAddExpenseDialog = false },
            onConfirm = { source, amount, category, note ->
                viewModel.addExpense(source, amount, category, note)
                showAddExpenseDialog = false
            }
        )
    }

    // Release Emergency Reserve Dialog
    if (showReleaseReserveDialog) {
        ReleaseEmergencyDialog(
            emergencyReserve = state.overview.emergencyReserve,
            onDismiss = { showReleaseReserveDialog = false },
            onConfirm = { amount, note ->
                viewModel.releaseEmergencyCash(amount, note)
                showReleaseReserveDialog = false
            }
        )
    }

    // Adjust Emergency Reserve Dialog
    if (showAdjustReserveDialog) {
        EditReserveDialog(
            currentReserve = state.overview.emergencyReserve,
            onDismiss = { showAdjustReserveDialog = false },
            onConfirm = { newReserve ->
                viewModel.updateEmergencyReserve(newReserve)
                showAdjustReserveDialog = false
            }
        )
    }

    // Explanation Dialog
    if (showExplanationDialog) {
        val o = state.overview
        CalculationExplanationDialog(
            online = o.onlineBalance,
            cash = o.cashBalance,
            reserve = o.emergencyReserve,
            spendableCash = o.spendableCash,
            normalSpendable = o.normalSpendableMoney,
            remainingDays = o.daysRemaining,
            safeDaily = o.safeDailySpending,
            onDismiss = { showExplanationDialog = false }
        )
    }

    // Add Expected Money Dialog
    if (showAddExpectedMoneyDialog) {
        AddExpectedMoneyDialog(
            onDismiss = { showAddExpectedMoneyDialog = false },
            onConfirm = { amount, source, note ->
                viewModel.addExpectedIncome(amount, System.currentTimeMillis(), source, note)
                showAddExpectedMoneyDialog = false
            }
        )
    }

    // Edit Transaction Dialog
    transactionToEdit?.let { tx ->
        EditTransactionDialog(
            transaction = tx,
            onDismiss = { transactionToEdit = null },
            onConfirm = { updated ->
                viewModel.updateTransaction(tx, updated)
                transactionToEdit = null
            }
        )
    }
}
