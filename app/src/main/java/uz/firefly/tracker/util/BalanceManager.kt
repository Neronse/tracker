package uz.firefly.tracker.util

import uz.firefly.tracker.util.Entry.Type.EXPENSE
import uz.firefly.tracker.util.Entry.Type.INCOME
import java.math.BigDecimal
import java.math.RoundingMode

const val usd = "USD"
const val rub = "RUB"

fun BigDecimal.toUsd(exchangeRate: BigDecimal) = divide(exchangeRate, 2, RoundingMode.HALF_EVEN)

fun BigDecimal.toRub(exchangeRate: BigDecimal) = multiply(exchangeRate)

object BalanceManager {

    fun total(exchangeRate: BigDecimal, operations: Iterable<Entry>): BigDecimal =
            operations.fold(BigDecimal(0)) { total, entry ->
                val amount = when (entry.currency.currencyCode) {
                    usd -> entry.amount.toRub(exchangeRate)
                    rub -> entry.amount
                    else -> throw IllegalArgumentException()
                }
                when (entry.type) {
                    EXPENSE -> total.minus(amount)
                    INCOME -> total.plus(amount)
                }
            }
}