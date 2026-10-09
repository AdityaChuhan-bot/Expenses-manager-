package com.example.ui.screens

import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import com.example.model.MoneyCalculations
import com.example.reminder.ReminderManager
import com.example.ui.theme.ReserveShieldColor
import com.example.ui.theme.StatusRed
import com.example.ui.viewmodel.MoneyUiState

@Composable
fun SettingsScreen(
    state: MoneyUiState,
    onUpdateEmergencyReserve: (Double) -> Unit,
    onUpdateBalancesManually: (Double, Double, Double) -> Unit,
    onUpdateReminders: (middayEnabled: Boolean, middayHour: Int, middayMin: Int, eveningEnabled: Boolean, eveningHour: Int, eveningMin: Int) -> Unit,
    onTriggerTestNotification: (String) -> Unit,
    onResetAllData: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showAdjustBalancesDialog by remember { mutableStateOf(false) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }
    var showEditReserveDialog by remember { mutableStateOf(false) }

    var middayEnabled by remember(state.middayReminderEnabled) { mutableStateOf(state.middayReminderEnabled) }
    var middayHour by remember(state.middayHour) { mutableStateOf(state.middayHour) }
    var middayMinute by remember(state.middayMinute) { mutableStateOf(state.middayMinute) }

    var eveningEnabled by remember(state.eveningReminderEnabled) { mutableStateOf(state.eveningReminderEnabled) }
    var eveningHour by remember(state.eveningHour) { mutableStateOf(state.eveningHour) }
    var eveningMinute by remember(state.eveningMinute) { mutableStateOf(state.eveningMinute) }

    fun formatTime(hour: Int, min: Int): String {
        val amPm = if (hour >= 12) "PM" else "AM"
        val h = if (hour % 12 == 0) 12 else hour % 12
        return "%d:%02d %s".format(h, min, amPm)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("settings_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Settings & Preferences",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Manage your emergency cash reserve, reminders, and balances",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 1. Emergency Cash Reserve Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_reserve_card"),
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = ReserveShieldColor)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Emergency Reserve", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }

                        Text(
                            text = MoneyCalculations.formatCurrency(state.overview.emergencyReserve),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = ReserveShieldColor
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "This amount of physical cash is shielded from daily budget spending. You can change it anytime; balances update immediately.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = { showEditReserveDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_settings_change_reserve")
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Change Reserve Amount")
                    }
                }
            }
        }

        // 2. Daily Balance Reminders (Requirement 14 & 15)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_reminders_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Daily Balance Reminders", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }

                    Text(
                        text = "Local on-device notifications to keep your irregular cash and online balances accurate.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Midday Reminder
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Midday Reminder", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            Text(
                                text = "Time: ${formatTime(middayHour, middayMinute)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TextButton(
                                onClick = {
                                    TimePickerDialog(
                                        context,
                                        { _, hourOfDay, minute ->
                                            middayHour = hourOfDay
                                            middayMinute = minute
                                            onUpdateReminders(middayEnabled, middayHour, middayMinute, eveningEnabled, eveningHour, eveningMinute)
                                        },
                                        middayHour,
                                        middayMinute,
                                        false
                                    ).show()
                                }
                            ) {
                                Text("Change")
                            }

                            Switch(
                                checked = middayEnabled,
                                onCheckedChange = {
                                    middayEnabled = it
                                    onUpdateReminders(it, middayHour, middayMinute, eveningEnabled, eveningHour, eveningMinute)
                                },
                                modifier = Modifier.testTag("switch_midday_reminder")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Evening Reminder
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("End-of-Day Reminder", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            Text(
                                text = "Time: ${formatTime(eveningHour, eveningMinute)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TextButton(
                                onClick = {
                                    TimePickerDialog(
                                        context,
                                        { _, hourOfDay, minute ->
                                            eveningHour = hourOfDay
                                            eveningMinute = minute
                                            onUpdateReminders(middayEnabled, middayHour, middayMinute, eveningEnabled, eveningHour, eveningMinute)
                                        },
                                        eveningHour,
                                        eveningMinute,
                                        false
                                    ).show()
                                }
                            ) {
                                Text("Change")
                            }

                            Switch(
                                checked = eveningEnabled,
                                onCheckedChange = {
                                    eveningEnabled = it
                                    onUpdateReminders(middayEnabled, middayHour, middayMinute, it, eveningHour, eveningMinute)
                                },
                                modifier = Modifier.testTag("switch_evening_reminder")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Test notification buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                onTriggerTestNotification(ReminderManager.TYPE_MIDDAY)
                                Toast.makeText(context, "Midday reminder notification triggered!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_test_midday_notification")
                        ) {
                            Text("Test Midday", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                onTriggerTestNotification(ReminderManager.TYPE_EVENING)
                                Toast.makeText(context, "Evening reminder notification triggered!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_test_evening_notification")
                        ) {
                            Text("Test Evening", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // 3. Manual Balance Reconciliation
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Tune, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Balance Reconciliation", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }

                    Text(
                        text = "Need to sync after finding cash or reconciling bank balance? Adjust your baseline numbers directly.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = { showAdjustBalancesDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_manual_balance_adjust")
                    ) {
                        Text("Reconcile Balances Manually")
                    }
                }
            }
        }

        // 4. Privacy & Offline Guarantee (Requirement 20)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("100% Private & Offline-First", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "• All money data remains securely on your local device.\n• No account required, no cloud backend.\n• No analytics, trackers, or external API transmissions.\n• Works completely without internet connection.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // 5. Reset App Data
        item {
            Button(
                onClick = { showResetConfirmDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("btn_reset_all_data")
            ) {
                Icon(Icons.Default.RestartAlt, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Reset All Data & Start Fresh", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
            }
        }
    }

    // Change Emergency Reserve Dialog
    if (showEditReserveDialog) {
        var newReserveText by remember { mutableStateOf(state.overview.emergencyReserve.toInt().toString()) }
        AlertDialog(
            onDismissRequest = { showEditReserveDialog = false },
            title = { Text("Update Emergency Reserve") },
            text = {
                OutlinedTextField(
                    value = newReserveText,
                    onValueChange = { newReserveText = it },
                    label = { Text("Protected Cash Reserve (₹)") },
                    prefix = { Text("₹ ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsed = newReserveText.toDoubleOrNull() ?: 0.0
                        onUpdateEmergencyReserve(parsed)
                        showEditReserveDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showEditReserveDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Manual balance adjustment dialog
    if (showAdjustBalancesDialog) {
        var onlineInput by remember { mutableStateOf(state.overview.onlineBalance.toString()) }
        var cashInput by remember { mutableStateOf(state.overview.cashBalance.toString()) }
        var reserveInput by remember { mutableStateOf(state.overview.emergencyReserve.toString()) }

        AlertDialog(
            onDismissRequest = { showAdjustBalancesDialog = false },
            title = { Text("Manual Balance Adjustment") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = onlineInput,
                        onValueChange = { onlineInput = it },
                        label = { Text("Online Balance (₹)") },
                        prefix = { Text("₹ ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = cashInput,
                        onValueChange = { cashInput = it },
                        label = { Text("Liquid Cash Balance (₹)") },
                        prefix = { Text("₹ ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = reserveInput,
                        onValueChange = { reserveInput = it },
                        label = { Text("Emergency Reserve (₹)") },
                        prefix = { Text("₹ ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val o = onlineInput.toDoubleOrNull() ?: state.overview.onlineBalance
                        val c = cashInput.toDoubleOrNull() ?: state.overview.cashBalance
                        val r = reserveInput.toDoubleOrNull() ?: state.overview.emergencyReserve
                        onUpdateBalancesManually(o, c, r)
                        showAdjustBalancesDialog = false
                    }
                ) {
                    Text("Apply Changes")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showAdjustBalancesDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Reset confirmation dialog
    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            title = { Text("Reset Everything?") },
            text = { Text("This will permanently clear all transactions, balances, and expectations, returning to the onboarding setup.") },
            confirmButton = {
                Button(
                    onClick = {
                        onResetAllData()
                        showResetConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Yes, Reset")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showResetConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
