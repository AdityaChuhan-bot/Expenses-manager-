package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.ExpectedIncomeEntity
import com.example.data.entity.MoneySource
import com.example.model.MoneyCalculations
import com.example.model.SpendingPaceStatus
import com.example.ui.theme.ReserveShieldColor
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusRed
import com.example.ui.theme.StatusYellow
import com.example.ui.viewmodel.MoneyUiState

@Composable
fun ProjectionsScreen(
    state: MoneyUiState,
    onAddExpectedMoneyClick: () -> Unit,
    onMarkExpectedReceived: (ExpectedIncomeEntity, MoneySource) -> Unit,
    onDeleteExpectedMoney: (ExpectedIncomeEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val overview = state.overview
    var customDailyRate by remember { mutableDoubleStateOf(20.0) }
    var itemToReceive by remember { mutableStateOf<ExpectedIncomeEntity?>(null) }
    var receiveSource by remember { mutableStateOf(MoneySource.ONLINE) }

    val hypotheticalResult = remember(overview.normalSpendableMoney, overview.emergencyReserve, customDailyRate, overview.daysRemaining) {
        MoneyCalculations.projectHypothetical(
            normalSpendable = overview.normalSpendableMoney,
            emergencyReserve = overview.emergencyReserve,
            dailyRate = customDailyRate,
            remainingDays = overview.daysRemaining
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("projections_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Screen Header
        item {
            Column {
                Text(
                    text = "Projections & Analysis",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Spending pace, month-end projections, and expected future cash flow",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 1. Monthly Spending Analysis Card (Requirement 9)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("spending_analysis_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "This Month's Spending",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        // Status Badge
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = when (overview.paceStatus) {
                                SpendingPaceStatus.ON_TRACK -> StatusGreen.copy(alpha = 0.15f)
                                SpendingPaceStatus.SLIGHTLY_HIGH -> StatusYellow.copy(alpha = 0.15f)
                                SpendingPaceStatus.TOO_FAST -> StatusRed.copy(alpha = 0.15f)
                            }
                        ) {
                            Text(
                                text = "${overview.paceStatus.icon} ${overview.paceStatus.label}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = when (overview.paceStatus) {
                                    SpendingPaceStatus.ON_TRACK -> StatusGreen
                                    SpendingPaceStatus.SLIGHTLY_HIGH -> StatusYellow
                                    SpendingPaceStatus.TOO_FAST -> StatusRed
                                },
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Spent so far", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(MoneyCalculations.formatCurrency(overview.spentThisMonth), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Average daily", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${MoneyCalculations.formatCurrency(overview.actualDailyPace)}/d", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("Recommended daily", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${MoneyCalculations.formatCurrency(overview.recommendedDailyPace)}/d", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }

        // 2. Month-End Projection Banner (Requirement 10)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("month_end_projection_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoGraph,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Month-End Projection",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "If you continue at your current spending rate (${MoneyCalculations.formatCurrency(overview.actualDailyPace)}/day):",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                text = "Expected remaining spendable:",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                            val spendableProj = overview.projectedMonthEndSpendable
                            Text(
                                text = MoneyCalculations.formatCurrency(spendableProj),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (spendableProj >= 0) MaterialTheme.colorScheme.onPrimaryContainer else StatusRed
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "+ Protected Reserve: ${MoneyCalculations.formatCurrency(overview.emergencyReserve)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                            )
                            Text(
                                text = "Total: ${MoneyCalculations.formatCurrency(overview.projectedMonthEndTotal)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        }

        // 3. Interactive Scenario Simulator (Requirement 10)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("scenario_simulator_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Hypothetical Scenario Simulator",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "See how different daily spending rates affect your month-end balance.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Preset chips: ₹10, ₹20, ₹50
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(10.0, 20.0, 50.0).forEach { rate ->
                            FilterChip(
                                selected = customDailyRate == rate,
                                onClick = { customDailyRate = rate },
                                label = { Text("What if ₹${rate.toInt()}/day?") },
                                modifier = Modifier.testTag("chip_rate_${rate.toInt()}")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Slider & input
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Slider(
                            value = customDailyRate.toFloat(),
                            onValueChange = { customDailyRate = it.toDouble() },
                            valueRange = 0f..200f,
                            steps = 39,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("slider_daily_rate")
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "₹${customDailyRate.toInt()}/day",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Projected remaining spendable:", style = MaterialTheme.typography.bodyMedium)
                                val remainingSpendable = hypotheticalResult.first
                                Text(
                                    text = MoneyCalculations.formatCurrency(remainingSpendable),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (remainingSpendable >= 0) MaterialTheme.colorScheme.primary else StatusRed
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total including Emergency Reserve:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = MoneyCalculations.formatCurrency(hypotheticalResult.second),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. Expected Future Money Section (Requirement 11)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("expected_money_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Expected Future Money",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.tertiaryContainer,
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Text(
                                    text = "Not currently available",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Button(
                            onClick = onAddExpectedMoneyClick,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("btn_add_expected_money")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add")
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Expected Money", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(MoneyCalculations.formatCurrency(state.totalExpectedIncome), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Current Money", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(MoneyCalculations.formatCurrency(overview.totalMoney), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("After Expected", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(MoneyCalculations.formatCurrency(state.moneyAfterExpectedIncome), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }

                    if (state.totalExpectedIncome > 0) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "✨ When received, your daily spending allowance will jump from ${MoneyCalculations.formatCurrencyInt(overview.safeDailySpending)}/day to ${MoneyCalculations.formatCurrencyInt(state.dailyAllowanceAfterExpectedIncome)}/day!",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
            }
        }

        // Expected incomes list
        val pendingItems = state.expectedIncomes.filter { !it.isReceived }
        if (pendingItems.isNotEmpty()) {
            item {
                Text(
                    text = "Pending Expected Cash Flows",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            items(pendingItems, key = { it.id }) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.source, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text(MoneyCalculations.formatCurrency(item.amount), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                            if (item.note.isNotBlank()) {
                                Text(item.note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            FilledTonalButton(
                                onClick = { itemToReceive = item },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("btn_receive_expected_${item.id}")
                            ) {
                                Text("Mark Received", fontSize = 12.sp)
                            }

                            IconButton(onClick = { onDeleteExpectedMoney(item) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = StatusRed)
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal when marking expected money as received: Where did you receive it? (Online or Cash)
    itemToReceive?.let { item ->
        AlertDialog(
            onDismissRequest = { itemToReceive = null },
            title = { Text("Money Received!") },
            text = {
                Column {
                    Text("Where did you receive the ${MoneyCalculations.formatCurrency(item.amount)} from ${item.source}?")
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = receiveSource == MoneySource.ONLINE,
                            onClick = { receiveSource = MoneySource.ONLINE },
                            label = { Text("Online (Bank/UPI)") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = receiveSource == MoneySource.CASH,
                            onClick = { receiveSource = MoneySource.CASH },
                            label = { Text("Cash") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val currentItem = item
                        itemToReceive = null
                        onMarkExpectedReceived(currentItem, receiveSource)
                    },
                    modifier = Modifier.testTag("btn_confirm_receive_source")
                ) {
                    Text("Add to Balance")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { itemToReceive = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
