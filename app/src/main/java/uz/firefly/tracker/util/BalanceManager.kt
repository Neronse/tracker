package uz.firefly.tracker.util

import uz.firefly.tracker.util.Entry.Type.EXPENSE
import uz.firefly.tracker.util.Entry.Type.INCOME
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*

data class Entry(val type: Type, val amount: BigDecimal, val currency: Currency) {

    enum class Type {
        EXPENSE, INCOME
    }
}

private val exchangeRate = BigDecimal(63.47)

fun BigDecimal.toUsd() = divide(exchangeRate, 2, RoundingMode.HALF_EVEN)

fun BigDecimal.toRub() = multiply(exchangeRate)

object BalanceManager {

    fun total(operations: Iterable<Entry>): BigDecimal =
            operations.fold(BigDecimal(0)) { total, entry ->
                val amount = when (entry.currency.currencyCode) {
                    "USD" -> entry.amount.toRub()
                    "RUB" -> entry.amount
                    else -> throw IllegalArgumentException()
                }
                when (entry.type) {
                    EXPENSE -> total.minus(amount)
                    INCOME -> total.plus(amount)
                }
            }
}