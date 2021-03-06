package uz.firefly.tracker.util

import android.arch.lifecycle.LiveData
import android.content.Context
import android.util.Log
import org.jetbrains.anko.defaultSharedPreferences
import uz.firefly.tracker.R
import uz.firefly.tracker.fragment.currentCurrency
import uz.firefly.tracker.room.AppDatabase
import uz.firefly.tracker.room.DataEntry
import uz.firefly.tracker.room.TemplateEntry
import java.text.DateFormat
import java.util.*

data class Category(val id: Int, val title: String)

data class Account(val icon: Int, val title: Int, val id: Int)

enum class Type {
    EXPENSE, INCOME
}

const val FROM = 0
const val TO = 1

class Repository(context: Context) {
    private val database: AppDatabase = AppDatabase.getDatabaseInstance()
    private val appContext = context

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

    fun getAllEntry(): List<DataEntry> =
            database.operationDao().getAllEntry(getCurrencyState())

    fun getLiveBaseEntry(): LiveData<List<DataEntry>> = database.operationDao().getLiveBaseEntry(getCurrencyState())

    fun insertEntry(entry: DataEntry) = database.operationDao().insertEntry(entry)

    fun getAllEntryBetweenDateAccount(accountId: Int): List<DataEntry> {
        val list = getDateFromTo()
        return database.operationDao().getAllEntryBetweenDateAccount(list[FROM], list[TO], getCurrencyState(), accountId)
    }

    fun getAllEntryBetweenDate(): List<DataEntry> {
        val list = getDateFromTo()
        return database.operationDao().getAllEntryBetweenDate(list[FROM], list[TO], getCurrencyState())
    }

    fun getOperation(accountId: Int): List<DataEntry> =
            database.operationDao().getOperation(getCurrencyState(), accountId)

    fun deleteEntry(dataEntry: DataEntry) = database.operationDao().deleteEntry(dataEntry)

    fun deleteTemplate(templateEntry: TemplateEntry) = database.operationDao().deleteTemplate(templateEntry)

    fun getAllTemplates():LiveData<List<TemplateEntry>> = database.operationDao().getAllTemplates(getCurrencyState())

    fun insetTemplate(templateEntry: TemplateEntry) = database.operationDao().insertTemplate(templateEntry)

    private fun getCurrencyState(): Currency {
        val currentCurrencySetting = appContext.defaultSharedPreferences.getInt(currentCurrency, R.id.rub)
        return when (currentCurrencySetting) {
            R.id.usd -> Currency.getInstance(usd)
            R.id.rub -> Currency.getInstance(rub)
            else -> Currency.getInstance(rub)
        }
    }

    private fun getDateFromTo(): List<Date> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 0)
        val endDate = Date()
        val fromDate: Date = calendar.time
        Log.d("LOG_TAG", "endDate ${DateFormat.getDateInstance().format(endDate)}")
        Log.d("LOG_TAG", "fromDate ${DateFormat.getDateInstance().format(fromDate)}")
        return listOf(fromDate, endDate)
    }
}
