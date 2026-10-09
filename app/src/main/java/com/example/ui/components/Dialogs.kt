package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.MoneySource
import com.example.data.entity.TransactionEntity
import com.example.data.entity.TransactionType
import com.example.model.MoneyCalculations
import com.example.ui.theme.CashMoneyColor
import com.example.ui.theme.OnlineMoneyColor
import com.example.ui.theme.ReserveShieldColor
import com.example.ui.theme.StatusRed

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddMoneyDialog(
    currentOnline: Double,
    currentCash: Double,
    onDismiss: () -> Unit,
    onConfirm: (source: MoneySource, amount: Double, sourceTag: String, note: String) -> Unit
) {
    var selectedSource by remember { mutableStateOf(MoneySource.ONLINE) }
    var amountText by remember { mutableStateOf("") }
    var selectedTag by remember { mutableStateOf("Pocket money") }
    var noteText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val tags = listOf("Pocket money", "Parents", "College", "Travel", "Other")

    val amount = amountText.toDoubleOrNull() ?: 0.0
    val newBalance = if (selectedSource == MoneySource.ONLINE) currentOnline + amount else currentCash + amount

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add Money",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Where did you receive the money?",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedSource == MoneySource.ONLINE,
                        onClick = { selectedSource = MoneySource.ONLINE },
                        label = { Text("Online (UPI/Bank)") },
                        leadingIcon = {
                            Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chip_add_money_online")
                    )
                    FilterChip(
                        selected = selectedSource == MoneySource.CASH,
                        onClick = { selectedSource = MoneySource.CASH },
                        label = { Text("Cash") },
                        leadingIcon = {
                            Icon(Icons.Default.AttachMoney, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chip_add_money_cash")
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        errorMessage = null
                    },
                    label = { Text("Amount (₹)") },
                    placeholder = { Text("e.g. 500") },
                    prefix = { Text("₹ ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    isError = errorMessage != null,
                    supportingText = errorMessage?.let { { Text(it, color = StatusRed) } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_add_money_amount")
                )

                if (amount > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "New ${if (selectedSource == MoneySource.ONLINE) "Online" else "Cash"} balance: ${MoneyCalculations.formatCurrency(newBalance)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Source / Note",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    tags.forEach { tag ->
                        FilterChip(
                            selected = selectedTag == tag,
                            onClick = { selectedTag = tag },
                            label = { Text(tag, fontSize = 12.sp) },
                            modifier = Modifier.testTag("chip_source_$tag")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("Optional Note") },
                    placeholder = { Text("e.g. Received from Dad") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_add_money_note")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsed = amountText.toDoubleOrNull()
                    if (parsed == null || parsed <= 0.0) {
                        errorMessage = "Please enter a valid amount greater than 0"
                        return@Button
                    }
                    onConfirm(selectedSource, parsed, selectedTag, noteText)
                },
                modifier = Modifier.testTag("btn_confirm_add_money")
            ) {
                Text("+ Add Money")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("btn_cancel_add_money")
            ) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddExpenseDialog(
    currentOnline: Double,
    currentCash: Double,
    onDismiss: () -> Unit,
    onConfirm: (source: MoneySource, amount: Double, category: String, note: String) -> Unit
) {
    var selectedSource by remember { mutableStateOf(MoneySource.ONLINE) }
    var amountText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Food") }
    var noteText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val categories = listOf("Food", "Transport", "College", "Entertainment", "Shopping", "Other")

    val amount = amountText.toDoubleOrNull() ?: 0.0
    val maxAvailable = if (selectedSource == MoneySource.ONLINE) currentOnline else currentCash

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add Expense",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Paid via:",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedSource == MoneySource.ONLINE,
                        onClick = { selectedSource = MoneySource.ONLINE },
                        label = { Text("Online (${MoneyCalculations.formatCurrency(currentOnline)})") },
                        leadingIcon = {
                            Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chip_expense_online")
                    )
                    FilterChip(
                        selected = selectedSource == MoneySource.CASH,
                        onClick = { selectedSource = MoneySource.CASH },
                        label = { Text("Cash (${MoneyCalculations.formatCurrency(currentCash)})") },
                        leadingIcon = {
                            Icon(Icons.Default.AttachMoney, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chip_expense_cash")
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        errorMessage = null
                    },
                    label = { Text("Amount (₹)") },
                    placeholder = { Text("e.g. 50") },
                    prefix = { Text("₹ ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    isError = errorMessage != null,
                    supportingText = errorMessage?.let { { Text(it, color = StatusRed) } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_expense_amount")
                )

                // Overdraft warning
                if (amount > maxAvailable) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Warning",
                            tint = StatusRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Exceeds available ${if (selectedSource == MoneySource.ONLINE) "Online" else "Cash"} balance (${MoneyCalculations.formatCurrency(maxAvailable)})!",
                            style = MaterialTheme.typography.bodySmall,
                            color = StatusRed,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else if (amount > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Remaining in ${if (selectedSource == MoneySource.ONLINE) "Online" else "Cash"}: ${MoneyCalculations.formatCurrency(maxAvailable - amount)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 12.sp) },
                            modifier = Modifier.testTag("chip_category_$cat")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("Optional Note") },
                    placeholder = { Text("e.g. Lunch with friends") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_expense_note")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsed = amountText.toDoubleOrNull()
                    if (parsed == null || parsed <= 0.0) {
                        errorMessage = "Please enter a valid amount greater than 0"
                        return@Button
                    }
                    if (parsed > maxAvailable) {
                        errorMessage = "Cannot exceed available ${if (selectedSource == MoneySource.ONLINE) "Online" else "Cash"} balance (${MoneyCalculations.formatCurrency(maxAvailable)})"
                        return@Button
                    }
                    onConfirm(selectedSource, parsed, selectedCategory, noteText)
                },
                modifier = Modifier.testTag("btn_confirm_add_expense")
            ) {
                Text("− Add Expense")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("btn_cancel_add_expense")
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ReleaseEmergencyDialog(
    emergencyReserve: Double,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double, note: String) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val amount = amountText.toDoubleOrNull() ?: 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Use Emergency Cash",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = ReserveShieldColor
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Release protected physical cash reserve into spendable cash.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Current Reserve:",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = MoneyCalculations.formatCurrency(emergencyReserve),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ReserveShieldColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        errorMessage = null
                    },
                    label = { Text("Amount to Release (₹)") },
                    placeholder = { Text("e.g. 100") },
                    prefix = { Text("₹ ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    isError = errorMessage != null,
                    supportingText = errorMessage?.let { { Text(it, color = StatusRed) } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_release_amount")
                )

                if (amount > 0 && amount <= emergencyReserve) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "New Reserve: ${MoneyCalculations.formatCurrency(emergencyReserve - amount)}\nNormal Spendable Money will increase by ${MoneyCalculations.formatCurrency(amount)}.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("Reason (Optional)") },
                    placeholder = { Text("e.g. Urgent bus fare / medical") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_release_note")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsed = amountText.toDoubleOrNull()
                    if (parsed == null || parsed <= 0.0) {
                        errorMessage = "Please enter an amount greater than 0"
                        return@Button
                    }
                    if (parsed > emergencyReserve) {
                        errorMessage = "Cannot release more than current reserve (${MoneyCalculations.formatCurrency(emergencyReserve)})"
                        return@Button
                    }
                    onConfirm(parsed, noteText)
                },
                colors = ButtonDefaults.buttonColors(containerColor = ReserveShieldColor),
                modifier = Modifier.testTag("btn_confirm_release_emergency")
            ) {
                Text("Release Cash")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("btn_cancel_release_emergency")
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun EditReserveDialog(
    currentReserve: Double,
    onDismiss: () -> Unit,
    onConfirm: (newReserve: Double) -> Unit
) {
    var amountText by remember { mutableStateOf(if (currentReserve > 0) currentReserve.toInt().toString() else "") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Set Emergency Reserve",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Define how much physical cash should remain protected and untouched for emergencies.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        errorMessage = null
                    },
                    label = { Text("Emergency Reserve (₹)") },
                    placeholder = { Text("e.g. 200") },
                    prefix = { Text("₹ ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    isError = errorMessage != null,
                    supportingText = errorMessage?.let { { Text(it, color = StatusRed) } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_edit_reserve")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsed = amountText.toDoubleOrNull()
                    if (parsed == null || parsed < 0.0) {
                        errorMessage = "Please enter a valid positive number"
                        return@Button
                    }
                    onConfirm(parsed)
                },
                modifier = Modifier.testTag("btn_confirm_save_reserve")
            ) {
                Text("Save Reserve")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("btn_cancel_edit_reserve")
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun CalculationExplanationDialog(
    online: Double,
    cash: Double,
    reserve: Double,
    spendableCash: Double,
    normalSpendable: Double,
    remainingDays: Int,
    safeDaily: Double,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "How Budget Is Calculated",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "The app NEVER divides your total money across days. Only your Normal Spendable Money is used, keeping your emergency cash 100% protected.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "1. Spendable Cash",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Cash (${MoneyCalculations.formatCurrency(cash)}) − Reserve (${MoneyCalculations.formatCurrency(reserve)}) = ${MoneyCalculations.formatCurrency(spendableCash)}",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "2. Normal Spendable Money",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Online (${MoneyCalculations.formatCurrency(online)}) + Spendable Cash (${MoneyCalculations.formatCurrency(spendableCash)}) = ${MoneyCalculations.formatCurrency(normalSpendable)}",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "3. Safe Daily Spending",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "${MoneyCalculations.formatCurrency(normalSpendable)} ÷ $remainingDays remaining days = ${MoneyCalculations.formatCurrency(safeDaily)}/day (displayed as ${MoneyCalculations.formatCurrencyInt(safeDaily)}/day)",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier.testTag("btn_close_explanation")
            ) {
                Text("Got It")
            }
        }
    )
}

@Composable
fun AddExpectedMoneyDialog(
    onDismiss: () -> Unit,
    onConfirm: (amount: Double, source: String, note: String) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var sourceText by remember { mutableStateOf("Parents") }
    var noteText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val presetSources = listOf("Parents", "Pocket Money", "College Reimbursement", "Relative", "Other")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add Expected Future Money",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "⚠️ Expected money is NOT added to your current balance until it actually arrives.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.padding(10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        errorMessage = null
                    },
                    label = { Text("Expected Amount (₹)") },
                    placeholder = { Text("e.g. 500") },
                    prefix = { Text("₹ ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    isError = errorMessage != null,
                    supportingText = errorMessage?.let { { Text(it, color = StatusRed) } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_expected_amount")
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Expected Source",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presetSources.take(3).forEach { s ->
                        FilterChip(
                            selected = sourceText == s,
                            onClick = { sourceText = s },
                            label = { Text(s, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("Note / Expected Date (Optional)") },
                    placeholder = { Text("e.g. By 15th October") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_expected_note")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsed = amountText.toDoubleOrNull()
                    if (parsed == null || parsed <= 0.0) {
                        errorMessage = "Please enter an amount greater than 0"
                        return@Button
                    }
                    onConfirm(parsed, sourceText, noteText)
                },
                modifier = Modifier.testTag("btn_confirm_add_expected")
            ) {
                Text("Save Expected")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("btn_cancel_add_expected")
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun EditTransactionDialog(
    transaction: TransactionEntity,
    onDismiss: () -> Unit,
    onConfirm: (updated: TransactionEntity) -> Unit
) {
    var amountText by remember { mutableStateOf(transaction.amount.toString()) }
    var categoryText by remember { mutableStateOf(transaction.category) }
    var noteText by remember { mutableStateOf(transaction.note) }
    var source by remember { mutableStateOf(transaction.source) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Edit Transaction",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (transaction.type != TransactionType.EMERGENCY_RELEASE) {
                    Text(
                        text = "Form:",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = source == MoneySource.ONLINE,
                            onClick = { source = MoneySource.ONLINE },
                            label = { Text("Online") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = source == MoneySource.CASH,
                            onClick = { source = MoneySource.CASH },
                            label = { Text("Cash") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        errorMessage = null
                    },
                    label = { Text("Amount (₹)") },
                    prefix = { Text("₹ ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    isError = errorMessage != null,
                    supportingText = errorMessage?.let { { Text(it, color = StatusRed) } },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = categoryText,
                    onValueChange = { categoryText = it },
                    label = { Text("Category / Source") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("Note") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsed = amountText.toDoubleOrNull()
                    if (parsed == null || parsed <= 0.0) {
                        errorMessage = "Please enter an amount greater than 0"
                        return@Button
                    }
                    val updated = transaction.copy(
                        amount = parsed,
                        source = source,
                        category = categoryText.ifBlank { transaction.category },
                        note = noteText
                    )
                    onConfirm(updated)
                }
            ) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
