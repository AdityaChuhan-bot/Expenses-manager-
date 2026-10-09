package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.MoneySource
import com.example.data.entity.TransactionEntity
import com.example.data.entity.TransactionType
import com.example.model.MoneyCalculations
import com.example.model.SpendingPaceStatus
import com.example.ui.components.BalanceDistinctionCard
import com.example.ui.components.EmergencyReserveCard
import com.example.ui.components.SafeDailySpendingCard
import com.example.ui.theme.CashMoneyColor
import com.example.ui.theme.OnlineMoneyColor
import com.example.ui.theme.ReserveShieldColor
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusRed
import com.example.ui.theme.StatusYellow
import com.example.ui.viewmodel.MoneyUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    state: MoneyUiState,
    onAddMoneyClick: () -> Unit,
    onAddExpenseClick: () -> Unit,
    onReleaseEmergencyClick: () -> Unit,
    onAdjustReserveClick: () -> Unit,
    onExplainCalculationClick: () -> Unit,
    onViewAllTransactionsClick: () -> Unit,
    onTransactionClick: (TransactionEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val overview = state.overview
    val recentTransactions = state.transactions.take(5)

    val dateFormat = SimpleDateFormat("EEEE, d MMMM", Locale.getDefault())
    val todayString = dateFormat.format(Date())

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Header & Current Date
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "PocketFlow",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = todayString,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Spending Status Pill
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = when (overview.paceStatus) {
                            SpendingPaceStatus.ON_TRACK -> StatusGreen.copy(alpha = 0.15f)
                            SpendingPaceStatus.SLIGHTLY_HIGH -> StatusYellow.copy(alpha = 0.15f)
                            SpendingPaceStatus.TOO_FAST -> StatusRed.copy(alpha = 0.15f)
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(overview.paceStatus.icon, fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = overview.paceStatus.label,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = when (overview.paceStatus) {
                                    SpendingPaceStatus.ON_TRACK -> StatusGreen
                                    SpendingPaceStatus.SLIGHTLY_HIGH -> StatusYellow
                                    SpendingPaceStatus.TOO_FAST -> StatusRed
                                }
                            )
                        }
                    }
                }
            }
        }

        // 1. Safe Daily Spending Hero Card
        item {
            SafeDailySpendingCard(
                overview = overview,
                onExplainClick = onExplainCalculationClick
            )
        }

        // 2. Large Primary Action Buttons: + Add Money and - Add Expense
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onAddMoneyClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp)
                        .testTag("btn_dashboard_add_money"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Money",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Add Money",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = onAddExpenseClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp)
                        .testTag("btn_dashboard_add_expense"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Add Expense",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Add Expense",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 3. Balance Distinction Card (Total vs Spendable vs Online vs Cash vs Reserve)
        item {
            BalanceDistinctionCard(overview = overview)
        }

        // 4. Emergency Reserve Card
        item {
            EmergencyReserveCard(
                emergencyReserve = overview.emergencyReserve,
                cashBalance = overview.cashBalance,
                spendableCash = overview.spendableCash,
                onReleaseClick = onReleaseEmergencyClick,
                onAdjustClick = onAdjustReserveClick
            )
        }

        // 5. Recent Transactions Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Transactions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                TextButton(
                    onClick = onViewAllTransactionsClick,
                    modifier = Modifier.testTag("btn_view_all_transactions")
                ) {
                    Text("View all")
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            }
        }

        if (recentTransactions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No transactions yet",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap '+ Add Money' or '− Add Expense' above to get started.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(recentTransactions, key = { it.id }) { tx ->
                TransactionRowItem(
                    transaction = tx,
                    onClick = { onTransactionClick(tx) }
                )
            }
        }
    }
}

@Composable
fun TransactionRowItem(
    transaction: TransactionEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val timeFormat = SimpleDateFormat("d MMM, h:mm a", Locale.getDefault())
    val formattedTime = timeFormat.format(Date(transaction.timestamp))

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("tx_item_${transaction.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Icon badge
                val (bgColor, iconVector, iconTint) = when (transaction.type) {
                    TransactionType.INCOME -> Triple(
                        MaterialTheme.colorScheme.primaryContainer,
                        if (transaction.source == MoneySource.ONLINE) Icons.Default.AccountBalanceWallet else Icons.Default.AttachMoney,
                        MaterialTheme.colorScheme.primary
                    )
                    TransactionType.EXPENSE -> Triple(
                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                        if (transaction.source == MoneySource.ONLINE) Icons.Default.AccountBalanceWallet else Icons.Default.AttachMoney,
                        MaterialTheme.colorScheme.error
                    )
                    TransactionType.EMERGENCY_RELEASE -> Triple(
                        ReserveShieldColor.copy(alpha = 0.15f),
                        Icons.Default.LockOpen,
                        ReserveShieldColor
                    )
                }

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(bgColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = iconVector,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = transaction.category,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    val sourceLabel = when (transaction.source) {
                        MoneySource.ONLINE -> "Online"
                        MoneySource.CASH -> "Cash"
                        MoneySource.NONE -> "Emergency Cash"
                    }
                    val subtitle = if (transaction.note.isNotBlank()) {
                        "$sourceLabel · ${transaction.note}"
                    } else {
                        "$sourceLabel · $formattedTime"
                    }
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Amount
            val amountText = when (transaction.type) {
                TransactionType.INCOME -> "+ ${MoneyCalculations.formatCurrency(transaction.amount)}"
                TransactionType.EXPENSE -> "− ${MoneyCalculations.formatCurrency(transaction.amount)}"
                TransactionType.EMERGENCY_RELEASE -> "🔓 ${MoneyCalculations.formatCurrency(transaction.amount)}"
            }
            val amountColor = when (transaction.type) {
                TransactionType.INCOME -> MaterialTheme.colorScheme.primary
                TransactionType.EXPENSE -> MaterialTheme.colorScheme.error
                TransactionType.EMERGENCY_RELEASE -> ReserveShieldColor
            }

            Text(
                text = amountText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = amountColor
            )
        }
    }
}
