package uz.firefly.tracker.util

import uz.firefly.tracker.room.DataEntry

import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*

const val usd = "USD"
const val rub = "RUB"

fun BigDecimal.toUsd(exchangeRate: BigDecimal) = divide(exchangeRate, 2, RoundingMode.HALF_EVEN)

fun BigDecimal.toRub(exchangeRate: BigDecimal) = multiply(exchangeRate).setScale(2,RoundingMode.HALF_EVEN)

object BalanceManager {

    fun total(exchangeRate: BigDecimal, operations: Iterable<DataEntry>): BigDecimal =
            operations.fold(BigDecimal(0)) { total, entry ->
                val amount = when (entry.currency.currencyCode) {
                    usd -> entry.amount.toRub(exchangeRate)
                    rub -> entry.amount
                    else -> throw IllegalArgumentException()
                }
                when (entry.type) {
                    Type.EXPENSE -> total.minus(amount)
                    Type.INCOME -> total.plus(amount)
                }
            }
    fun calculateExpense(list: List<DataEntry>):Float {
        if (list.isEmpty()) return 0.00f
        return list.fold(BigDecimal(0)) { totalExpense, operation ->
            totalExpense.plus(operation.amount)
        }.toFloat()
    }

    fun calculateBalance(list: List<DataEntry>): BigDecimal {
        if (list.isEmpty()) return BigDecimal(0).setScale(2,RoundingMode.HALF_EVEN)
        val currentCurrency: Currency = list.first().currency
        var balance: BigDecimal = BigDecimal(0).setScale(2, RoundingMode.HALF_EVEN)
        list.forEach {
            if (currentCurrency != it.currency) throw IllegalArgumentException("Only one currency in list")

            when(it.type){
                Type.INCOME -> balance += it.amount.setScale(2, RoundingMode.HALF_EVEN)
                Type.EXPENSE -> balance -= it.amount.setScale(2,RoundingMode.HALF_EVEN)
            }  }
        return balance
    }
}