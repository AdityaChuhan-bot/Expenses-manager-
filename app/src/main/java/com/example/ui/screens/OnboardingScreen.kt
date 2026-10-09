package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MoneyCalculations
import com.example.ui.theme.CashMoneyColor
import com.example.ui.theme.OnlineMoneyColor
import com.example.ui.theme.ReserveShieldColor
import com.example.ui.theme.StatusRed

@Composable
fun OnboardingScreen(
    onCompleteSetup: (online: Double, cash: Double, reserve: Double) -> Unit
) {
    var onlineText by remember { mutableStateOf("100") }
    var cashText by remember { mutableStateOf("200") }
    var reserveText by remember { mutableStateOf("200") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val online = onlineText.toDoubleOrNull() ?: 0.0
    val cash = cashText.toDoubleOrNull() ?: 0.0
    val reserve = reserveText.toDoubleOrNull() ?: 0.0

    val total = (online + cash).coerceAtLeast(0.0)
    val spendableCash = (cash - reserve).coerceAtLeast(0.0)
    val normalSpendable = (online + spendableCash).coerceAtLeast(0.0)
    val dateInfo = MoneyCalculations.getMonthDateInfo()
    val safeDaily = if (dateInfo.remainingDaysIncludingToday > 0) normalSpendable / dateInfo.remainingDaysIncludingToday else 0.0

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp, vertical = 24.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .size(64.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Payments,
                contentDescription = "PocketFlow Logo",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Welcome to PocketFlow",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Personal money manager built for irregular pocket money. Set your initial balances to begin.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Step 1: Online Money
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = null,
                        tint = OnlineMoneyColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Step 1: Online Money",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "Bank accounts, UPI, digital wallets currently available.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = onlineText,
                    onValueChange = { onlineText = it; errorMessage = null },
                    label = { Text("Online Money (₹)") },
                    prefix = { Text("₹ ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("onboarding_input_online")
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Step 2: Liquid Cash
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AttachMoney,
                        contentDescription = null,
                        tint = CashMoneyColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Step 2: Liquid Cash",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "Physical notes & coins currently with you.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = cashText,
                    onValueChange = { cashText = it; errorMessage = null },
                    label = { Text("Liquid Cash (₹)") },
                    prefix = { Text("₹ ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("onboarding_input_cash")
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Step 3: Emergency Reserve
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = ReserveShieldColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Step 3: Emergency Reserve",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "How much cash should remain locked & protected from daily spending.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = reserveText,
                    onValueChange = { reserveText = it; errorMessage = null },
                    label = { Text("Emergency Reserve (₹)") },
                    prefix = { Text("₹ ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("onboarding_input_reserve")
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Live Calculation Result Preview
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Live Calculation Preview",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total Money:", style = MaterialTheme.typography.bodyMedium)
                    Text(MoneyCalculations.formatCurrency(total), fontWeight = FontWeight.Bold)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Emergency Reserve:", style = MaterialTheme.typography.bodyMedium)
                    Text(MoneyCalculations.formatCurrency(reserve), fontWeight = FontWeight.Bold, color = ReserveShieldColor)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Normal Spendable:", style = MaterialTheme.typography.bodyMedium)
                    Text(MoneyCalculations.formatCurrency(normalSpendable), fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Safe Daily Spending (${dateInfo.remainingDaysIncludingToday} days left):", style = MaterialTheme.typography.bodySmall)
                    Text("${MoneyCalculations.formatCurrencyInt(safeDaily)}/day", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        errorMessage?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = it, color = StatusRed, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                val parsedOnline = onlineText.toDoubleOrNull()
                val parsedCash = cashText.toDoubleOrNull()
                val parsedReserve = reserveText.toDoubleOrNull()

                if (parsedOnline == null || parsedOnline < 0.0) {
                    errorMessage = "Please enter a valid online amount"
                    return@Button
                }
                if (parsedCash == null || parsedCash < 0.0) {
                    errorMessage = "Please enter a valid cash amount"
                    return@Button
                }
                if (parsedReserve == null || parsedReserve < 0.0) {
                    errorMessage = "Please enter a valid reserve amount"
                    return@Button
                }

                onCompleteSetup(parsedOnline, parsedCash, parsedReserve)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("btn_complete_onboarding"),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(
                text = "Enter Dashboard →",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
