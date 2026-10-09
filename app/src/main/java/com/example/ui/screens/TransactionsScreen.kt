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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.ui.theme.ReserveShieldColor
import com.example.ui.theme.StatusRed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TransactionsScreen(
    transactions: List<TransactionEntity>,
    onEditTransaction: (TransactionEntity) -> Unit,
    onDeleteTransaction: (TransactionEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") }
    var transactionToDelete by remember { mutableStateOf<TransactionEntity?>(null) }
    var selectedTransactionForDetails by remember { mutableStateOf<TransactionEntity?>(null) }

    val filteredTransactions = remember(transactions, searchQuery, selectedFilter) {
        transactions.filter { tx ->
            val matchesFilter = when (selectedFilter) {
                "INCOME" -> tx.type == TransactionType.INCOME
                "EXPENSE" -> tx.type == TransactionType.EXPENSE
                "EMERGENCY" -> tx.type == TransactionType.EMERGENCY_RELEASE
                else -> true
            }
            val matchesSearch = searchQuery.isBlank() ||
                    tx.category.contains(searchQuery, ignoreCase = true) ||
                    tx.note.contains(searchQuery, ignoreCase = true)
            matchesFilter && matchesSearch
        }
    }

    // Group transactions by date
    val groupedTransactions = remember(filteredTransactions) {
        val dateHeaderFormat = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault())
        filteredTransactions.groupBy { tx ->
            dateHeaderFormat.format(Date(tx.timestamp))
        }
    }

    // Totals for current view
    val totalIn = filteredTransactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    val totalOut = filteredTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("transactions_screen")
    ) {
        // Header
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(
                text = "Transaction History",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by category or note...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("tx_search_bar"),
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Filter Chips
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = selectedFilter == "ALL",
                        onClick = { selectedFilter = "ALL" },
                        label = { Text("All (${transactions.size})") },
                        modifier = Modifier.testTag("filter_all")
                    )
                }
                item {
                    FilterChip(
                        selected = selectedFilter == "INCOME",
                        onClick = { selectedFilter = "INCOME" },
                        label = { Text("Income") },
                        modifier = Modifier.testTag("filter_income")
                    )
                }
                item {
                    FilterChip(
                        selected = selectedFilter == "EXPENSE",
                        onClick = { selectedFilter = "EXPENSE" },
                        label = { Text("Expenses") },
                        modifier = Modifier.testTag("filter_expense")
                    )
                }
                item {
                    FilterChip(
                        selected = selectedFilter == "EMERGENCY",
                        onClick = { selectedFilter = "EMERGENCY" },
                        label = { Text("Reserve Releases") },
                        modifier = Modifier.testTag("filter_emergency")
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Inflow vs Outflow Mini-Strip
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total In: ${MoneyCalculations.formatCurrency(totalIn)}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Total Out: ${MoneyCalculations.formatCurrency(totalOut)}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        // List
        if (filteredTransactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "No matching transactions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Recorded income and expenses will appear here.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                groupedTransactions.forEach { (dateHeader, txList) ->
                    item {
                        Text(
                            text = dateHeader,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 10.dp, bottom = 2.dp)
                        )
                    }

                    items(txList, key = { it.id }) { tx ->
                        TransactionRowItem(
                            transaction = tx,
                            onClick = { selectedTransactionForDetails = tx }
                        )
                    }
                }
            }
        }
    }

    // Transaction Details & Actions Sheet / Dialog
    selectedTransactionForDetails?.let { tx ->
        val timeFormat = SimpleDateFormat("EEEE, d MMMM yyyy, h:mm a", Locale.getDefault())
        val formattedTime = timeFormat.format(Date(tx.timestamp))

        AlertDialog(
            onDismissRequest = { selectedTransactionForDetails = null },
            title = {
                Text(
                    text = "Transaction Details",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = tx.category,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    val amountFormatted = when (tx.type) {
                        TransactionType.INCOME -> "+ ${MoneyCalculations.formatCurrency(tx.amount)}"
                        TransactionType.EXPENSE -> "− ${MoneyCalculations.formatCurrency(tx.amount)}"
                        TransactionType.EMERGENCY_RELEASE -> "Unlocked: ${MoneyCalculations.formatCurrency(tx.amount)}"
                    }
                    Text(
                        text = amountFormatted,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = when (tx.type) {
                            TransactionType.INCOME -> MaterialTheme.colorScheme.primary
                            TransactionType.EXPENSE -> MaterialTheme.colorScheme.error
                            TransactionType.EMERGENCY_RELEASE -> ReserveShieldColor
                        }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text("Source: ${tx.source.name}", style = MaterialTheme.typography.bodyMedium)
                    Text("Date: $formattedTime", style = MaterialTheme.typography.bodyMedium)
                    if (tx.note.isNotBlank()) {
                        Text("Note: ${tx.note}", style = MaterialTheme.typography.bodyMedium)
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Editing or deleting will automatically recalculate balances and budget projections.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val toEdit = tx
                        selectedTransactionForDetails = null
                        onEditTransaction(toEdit)
                    },
                    modifier = Modifier.testTag("btn_action_edit_tx")
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        val toDelete = tx
                        selectedTransactionForDetails = null
                        transactionToDelete = toDelete
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusRed),
                    modifier = Modifier.testTag("btn_action_delete_tx")
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete")
                }
            }
        )
    }

    // Delete confirmation dialog
    transactionToDelete?.let { tx ->
        AlertDialog(
            onDismissRequest = { transactionToDelete = null },
            title = { Text("Delete Transaction?") },
            text = {
                Text("Are you sure you want to delete '${tx.category}' (${MoneyCalculations.formatCurrency(tx.amount)})? All balances will be recalculated.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteTransaction(tx)
                        transactionToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("btn_confirm_delete_tx")
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { transactionToDelete = null }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}
