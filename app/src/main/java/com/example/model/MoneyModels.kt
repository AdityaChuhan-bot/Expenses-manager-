package com.example.model

import java.util.Calendar

data class MoneyOverview(
    val totalMoney: Double = 0.0,
    val onlineBalance: Double = 0.0,
    val cashBalance: Double = 0.0,
    val emergencyReserve: Double = 0.0,
    val spendableCash: Double = 0.0,
    val normalSpendableMoney: Double = 0.0,
    val daysRemaining: Int = 1,
    val safeDailySpending: Double = 0.0,
    val spentThisMonth: Double = 0.0,
    val actualDailyPace: Double = 0.0,
    val recommendedDailyPace: Double = 0.0,
    val paceStatus: SpendingPaceStatus = SpendingPaceStatus.ON_TRACK,
    val projectedMonthEndSpendable: Double = 0.0,
    val projectedMonthEndTotal: Double = 0.0
)

enum class SpendingPaceStatus(val label: String, val icon: String) {
    ON_TRACK("On Track", "🟢"),
    SLIGHTLY_HIGH("Slightly High", "🟡"),
    TOO_FAST("Spending Too Fast", "🔴")
}

data class MonthDateInfo(
    val year: Int,
    val month: Int, // 0-based Calendar.MONTH
    val currentDay: Int,
    val totalDaysInMonth: Int,
    val remainingDaysIncludingToday: Int
)

object MoneyCalculations {

    fun getMonthDateInfo(calendar: Calendar = Calendar.getInstance()): MonthDateInfo {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
        val totalDaysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val remainingDays = (totalDaysInMonth - currentDay + 1).coerceAtLeast(1)

        return MonthDateInfo(
            year = year,
            month = month,
            currentDay = currentDay,
            totalDaysInMonth = totalDaysInMonth,
            remainingDaysIncludingToday = remainingDays
        )
    }

    fun computeOverview(
        online: Double,
        cash: Double,
        emergencyReserve: Double,
        spentThisMonth: Double,
        dateInfo: MonthDateInfo = getMonthDateInfo()
    ): MoneyOverview {
        val totalMoney = (online + cash).coerceAtLeast(0.0)
        val spendableCash = (cash - emergencyReserve).coerceAtLeast(0.0)
        val normalSpendable = (online + spendableCash).coerceAtLeast(0.0)

        val remainingDays = dateInfo.remainingDaysIncludingToday
        val safeDaily = if (remainingDays > 0) normalSpendable / remainingDays else 0.0

        val daysElapsed = dateInfo.currentDay.coerceAtLeast(1)
        val actualDailyPace = spentThisMonth / daysElapsed
        val recommendedDailyPace = safeDaily

        val paceStatus = when {
            normalSpendable <= 0.0 && spentThisMonth > 0 -> SpendingPaceStatus.TOO_FAST
            actualDailyPace <= recommendedDailyPace -> SpendingPaceStatus.ON_TRACK
            actualDailyPace <= recommendedDailyPace * 1.25 -> SpendingPaceStatus.SLIGHTLY_HIGH
            else -> SpendingPaceStatus.TOO_FAST
        }

        // Projected remaining spendable if spending continues at actual daily pace
        val projectedFutureSpending = actualDailyPace * remainingDays
        val projectedMonthEndSpendable = normalSpendable - projectedFutureSpending
        val projectedMonthEndTotal = totalMoney - projectedFutureSpending

        return MoneyOverview(
            totalMoney = totalMoney,
            onlineBalance = online,
            cashBalance = cash,
            emergencyReserve = emergencyReserve,
            spendableCash = spendableCash,
            normalSpendableMoney = normalSpendable,
            daysRemaining = remainingDays,
            safeDailySpending = safeDaily,
            spentThisMonth = spentThisMonth,
            actualDailyPace = actualDailyPace,
            recommendedDailyPace = recommendedDailyPace,
            paceStatus = paceStatus,
            projectedMonthEndSpendable = projectedMonthEndSpendable,
            projectedMonthEndTotal = projectedMonthEndTotal
        )
    }

    fun projectHypothetical(
        normalSpendable: Double,
        emergencyReserve: Double,
        dailyRate: Double,
        remainingDays: Int
    ): Pair<Double, Double> {
        val futureSpending = dailyRate * remainingDays
        val projectedSpendable = normalSpendable - futureSpending
        val projectedTotal = (normalSpendable - futureSpending) + emergencyReserve
        return Pair(projectedSpendable, projectedTotal)
    }

    fun formatCurrency(amount: Double): String {
        return if (amount % 1.0 == 0.0) {
            "₹%,d".format(amount.toLong())
        } else {
            "₹%,.2f".format(amount)
        }
    }

    fun formatCurrencyInt(amount: Double): String {
        return "₹%,d".format(amount.toLong())
    }
}
