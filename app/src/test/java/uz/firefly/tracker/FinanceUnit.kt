package uz.firefly.tracker

import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal
import android.media.VolumeShaper
import uz.firefly.tracker.room.DataEntry
import uz.firefly.tracker.util.BalanceManager
import uz.firefly.tracker.util.Type
import java.math.RoundingMode
import java.util.*

class FinanceUnit{
    private val list:List<DataEntry> = listOf(
            DataEntry(null, Type.INCOME, BigDecimal(1.51), Currency.getInstance("RUB"), 1, R.id.total_account, Date()),
            DataEntry(null, Type.EXPENSE, BigDecimal(1.51), Currency.getInstance("RUB"), 1, R.id.total_account,Date()),
            DataEntry(null, Type.INCOME, BigDecimal(1.51), Currency.getInstance("RUB"), 1, R.id.total_account,Date()),
            DataEntry(null, Type.EXPENSE, BigDecimal(1.51), Currency.getInstance("RUB"), 1, R.id.total_account,Date())
            )
    private val list2:List<DataEntry> = listOf(
            DataEntry(null, Type.INCOME, BigDecimal(1.51), Currency.getInstance("RUB"), 1, R.id.total_account,Date()),
            DataEntry(null, Type.EXPENSE, BigDecimal(1.51), Currency.getInstance("USD"), 1, R.id.total_account,Date())
    )

    private val list3:List<DataEntry> = listOf(
            DataEntry(null, Type.INCOME, BigDecimal(1.512681), Currency.getInstance("RUB"), 1, R.id.total_account,Date()),
            DataEntry(null, Type.INCOME, BigDecimal(1.512681), Currency.getInstance("RUB"), 1, R.id.total_account,Date())
            )
    @Test
    fun testCalculateBalance1() {
        val reslt = BalanceManager.calculateBalance(list)
        assertEquals(reslt, BigDecimal(0.00).setScale(2, RoundingMode.HALF_EVEN))
    }
    @Test
    fun testCalculateBalance2() {
        try {
            BalanceManager.calculateBalance(list2)
        } catch (e: IllegalArgumentException) {
            assertEquals(e.message, "Only one currency in list")
        }
    }

    @Test
    fun testCalculateBalance3() {
        val result = BalanceManager.calculateBalance(list3)
        assertEquals(result,BigDecimal(3.02).setScale(2,RoundingMode.HALF_EVEN))
    }

    @Test
    fun testCalculateExpense(){
        val result = BalanceManager.calculateExpense(list.filter { it.type == Type.EXPENSE })
        assertEquals(result, 3.02f)
    }
}