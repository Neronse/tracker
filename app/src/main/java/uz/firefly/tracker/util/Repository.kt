package uz.firefly.tracker.util

import uz.firefly.tracker.R
import java.math.BigDecimal
import java.util.*

// TODO Репозиторий, поддержка локализации
data class Category(val id: Int, val title: String)

// TODO Репозиторий, динамические иконки
data class Account(val icon: Int, val title: Int)

data class Entry(val type: Type, val amount: BigDecimal, val currency: Currency, val categoryId: Int, val accountId: Int) {

    enum class Type {
        EXPENSE, INCOME
    }


}

object Repository {

    // TODO Репозиторий
    val accounts = arrayOf(
            Account(R.drawable.ic_donut_large_black_24dp, R.string.total),
            Account(R.drawable.ic_account_balance_wallet_black_24dp, R.string.cash),
            Account(R.drawable.ic_credit_card_black_24dp, R.string.card),
            Account(R.drawable.ic_web_black_24dp, R.string.yandex_money))

    // TODO Репозиторий
    val expensesCategories = arrayOf(
            Category(0, "Дом"),
            Category(1, "Еда"),
            Category(2, "Долги"),
            Category(3, "Транспорт"),
            Category(4, "Счета и услуги"),
            Category(5, "Личные расходы"),
            Category(6, "Сбережения"),
            Category(7, "Другие расходы")
    )

    // TODO Репозиторий
    val incomesCategories = arrayOf(
            Category(-1, "Зарплата"),
            Category(-2, "Стипендия"),
            Category(-3, "Подарки"),
            Category(-4, "Алименты"),
            Category(-5, "Дивиденды"),
            Category(-6, "Пенсия"),
            Category(-7, "Пособия"),
            Category(-8, "Другие доходы")
    )

    val operations = mutableListOf(
            Entry(Entry.Type.EXPENSE, BigDecimal(2028.23), Currency.getInstance("USD"), 1, 1),
            Entry(Entry.Type.INCOME, BigDecimal(4033.87), Currency.getInstance("USD"), -1, 1)
    )

}
