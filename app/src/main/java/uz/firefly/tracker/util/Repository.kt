package uz.firefly.tracker.util

import android.arch.lifecycle.LiveData
import android.content.Context
import org.jetbrains.anko.defaultSharedPreferences
import uz.firefly.tracker.R
import uz.firefly.tracker.fragment.currentCurrency
import uz.firefly.tracker.room.AppDatabase
import uz.firefly.tracker.room.DataEntry
import java.util.*

// TODO Репозиторий, поддержка локализации
data class Category(val id: Int, val title: String)

// TODO Репозиторий, динамические иконки
data class Account(val icon: Int, val title: Int, val id: Int)

/*   val operations = mutableListOf(
           Entry(Type.EXPENSE, BigDecimal(2028.23), Currency.getInstance("USD"), 1, 1),
           Entry(Type.INCOME, BigDecimal(4033.87), Currency.getInstance("USD"), -1, 1)
   )*/

/*data class Entry(val type: Type,
                 val amount: BigDecimal,
                 val currency: Currency,
                 val categoryId: Int,
                 val accountId: Int)*/

enum class Type {
    EXPENSE, INCOME
}

class Repository(context: Context) {
    val database: AppDatabase
    val appContext = context

    init {
        database = AppDatabase.getDatabaseInstance()
    }

    val expensesCategories = arrayOf(
            Category(0, context.getString(R.string.Home)),
            Category(1, context.getString(R.string.food)),
            Category(2, context.getString(R.string.debts)),
            Category(3, context.getString(R.string.transport)),
            Category(4, context.getString(R.string.accounts)),
            Category(5, context.getString(R.string.personalExpense)),
            Category(6, context.getString(R.string.savings)),
            Category(7, context.getString(R.string.otherExpense))
    )

    val incomesCategories = arrayOf(
            Category(-1, context.getString(R.string.salary)),
            Category(-2, context.getString(R.string.grants)),
            Category(-3, context.getString(R.string.gifts)),
            Category(-4, context.getString(R.string.alimony)),
            Category(-5, context.getString(R.string.dividend)),
            Category(-6, context.getString(R.string.pension)),
            Category(-7, context.getString(R.string.benefits)),
            Category(-8, context.getString(R.string.otherIncome))
    )
    val accounts = arrayOf(
            Account(R.drawable.ic_donut_large_black_24dp, R.string.total, R.id.total_account),
            Account(R.drawable.ic_account_balance_wallet_black_24dp, R.string.cash, R.id.cash_account),
            Account(R.drawable.ic_credit_card_black_24dp, R.string.card, R.id.card_account),
            Account(R.drawable.ic_web_black_24dp, R.string.yandex_money, R.id.yamoney_account))

    fun getDataEntries(): List<DataEntry> =
        database.operationDao().getAll(getCurrencyState())



    fun insertEntry(entry: DataEntry) = database.operationDao().insert(entry)

    fun getOperation(accountId: Int): List<DataEntry> =
            database.operationDao().getOperation(getCurrencyState(), accountId)



    private fun getCurrencyState(): Currency {
        val currentCurrencySetting = appContext.defaultSharedPreferences.getInt(currentCurrency, R.id.rub)
        return when (currentCurrencySetting) {
            R.id.usd -> Currency.getInstance(usd)
            R.id.rub -> Currency.getInstance(rub)
            else -> Currency.getInstance(rub)
        }
    }
}
