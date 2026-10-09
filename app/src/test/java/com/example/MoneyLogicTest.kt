package com.example

import com.example.data.entity.MoneySource
import com.example.data.entity.TransactionEntity
import com.example.data.entity.TransactionType
import com.example.model.MoneyCalculations
import com.example.model.MonthDateInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class MoneyLogicTest {

    @Test
    fun testCoreMoneyModelAndOct8Scenario() {
        // Oct 8, 2026
        val dateInfo = MonthDateInfo(
            year = 2026,
            month = Calendar.OCTOBER,
            currentDay = 8,
            totalDaysInMonth = 31,
            remainingDaysIncludingToday = 24
        )

        // Baseline: Online = 100, Cash = 200, Emergency Reserve = 200
        var online = 100.0
        var cash = 200.0
        var reserve = 200.0
        var spentThisMonth = 0.0

        var overview = MoneyCalculations.computeOverview(
            online = online,
            cash = cash,
            emergencyReserve = reserve,
            spentThisMonth = spentThisMonth,
            dateInfo = dateInfo
        )

        assertEquals(300.0, overview.totalMoney, 0.01)
        assertEquals(200.0, overview.emergencyReserve, 0.01)
        assertEquals(0.0, overview.spendableCash, 0.01)
        assertEquals(100.0, overview.normalSpendableMoney, 0.01)
        assertEquals(24, overview.daysRemaining)

        // Safe Daily Spending: 100 / 24 = 4.16666... ≈ 4.17
        assertEquals(4.166, overview.safeDailySpending, 0.01)
        assertEquals("₹4", MoneyCalculations.formatCurrencyInt(overview.safeDailySpending))

        // Step 1: Add ₹500 online
        online += 500.0
        assertEquals(600.0, online, 0.01)

        overview = MoneyCalculations.computeOverview(online, cash, reserve, spentThisMonth, dateInfo)
        assertEquals(800.0, overview.totalMoney, 0.01)
        assertEquals(600.0, overview.normalSpendableMoney, 0.01)
        assertEquals(25.0, overview.safeDailySpending, 0.01) // 600 / 24 = 25

        // Step 2: Spend ₹50 online
        online -= 50.0
        spentThisMonth += 50.0
        assertEquals(550.0, online, 0.01)

        overview = MoneyCalculations.computeOverview(online, cash, reserve, spentThisMonth, dateInfo)
        assertEquals(750.0, overview.totalMoney, 0.01)
        assertEquals(550.0, overview.normalSpendableMoney, 0.01)
        assertEquals(22.916, overview.safeDailySpending, 0.01) // 550 / 24 = 22.916

        // Step 3: Spend ₹20 cash
        cash -= 20.0
        spentThisMonth += 20.0
        assertEquals(180.0, cash, 0.01)

        overview = MoneyCalculations.computeOverview(online, cash, reserve, spentThisMonth, dateInfo)
        assertEquals(730.0, overview.totalMoney, 0.01)
        // cash is 180, reserve is 200 -> spendable cash = max(0, 180 - 200) = 0
        assertEquals(0.0, overview.spendableCash, 0.01)
        assertEquals(550.0, overview.normalSpendableMoney, 0.01)

        // Step 4: Add ₹300 cash
        cash += 300.0
        assertEquals(480.0, cash, 0.01)

        overview = MoneyCalculations.computeOverview(online, cash, reserve, spentThisMonth, dateInfo)
        assertEquals(1030.0, overview.totalMoney, 0.01)
        // cash is 480, reserve is 200 -> spendable cash is 280
        assertEquals(280.0, overview.spendableCash, 0.01)
        assertEquals(830.0, overview.normalSpendableMoney, 0.01) // 550 + 280 = 830

        // Step 5: Change emergency reserve to ₹250
        reserve = 250.0
        overview = MoneyCalculations.computeOverview(online, cash, reserve, spentThisMonth, dateInfo)
        assertEquals(230.0, overview.spendableCash, 0.01) // 480 - 250 = 230
        assertEquals(780.0, overview.normalSpendableMoney, 0.01) // 550 + 230 = 780

        // Step 6: Release ₹50 emergency cash
        // Releasing ₹50 from emergency reserve reduces reserve to 200 and unlocks ₹50 into spendable cash
        reserve -= 50.0
        assertEquals(200.0, reserve, 0.01)

        overview = MoneyCalculations.computeOverview(online, cash, reserve, spentThisMonth, dateInfo)
        assertEquals(280.0, overview.spendableCash, 0.01) // 480 - 200 = 280
        assertEquals(830.0, overview.normalSpendableMoney, 0.01) // 550 + 280 = 830
        assertEquals(34.583, overview.safeDailySpending, 0.01) // 830 / 24 = 34.58

        // Verify mathematical consistency
        assertEquals(overview.totalMoney, online + cash, 0.01)
        assertEquals(overview.normalSpendableMoney, online + (cash - reserve), 0.01)
    }

    @Test
    fun testHypotheticalScenarios() {
        // Given normal spendable = 100, remaining days = 24
        val (remSpendable10, remTotal10) = MoneyCalculations.projectHypothetical(
            normalSpendable = 100.0,
            emergencyReserve = 200.0,
            dailyRate = 10.0,
            remainingDays = 24
        )
        // Future spend = 240. Remaining spendable = 100 - 240 = -140
        assertEquals(-140.0, remSpendable10, 0.01)
        assertEquals(60.0, remTotal10, 0.01) // -140 + 200 = 60

        val (remSpendable2, remTotal2) = MoneyCalculations.projectHypothetical(
            normalSpendable = 100.0,
            emergencyReserve = 200.0,
            dailyRate = 2.0,
            remainingDays = 24
        )
        // Future spend = 48. Remaining spendable = 100 - 48 = 52
        assertEquals(52.0, remSpendable2, 0.01)
        assertEquals(252.0, remTotal2, 0.01)
    }
}
