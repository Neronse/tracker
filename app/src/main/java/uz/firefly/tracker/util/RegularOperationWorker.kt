package uz.firefly.tracker.util

import androidx.work.Worker
import uz.firefly.tracker.fragment.*
import uz.firefly.tracker.room.AppDatabase
import uz.firefly.tracker.room.DataEntry
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DateFormat
import java.util.*

class RegularOperationWorker : Worker() {

    override fun doWork(): Result {
        val type = inputData.getString(TYPE)
        val amount = inputData.getString(AMOUNT)
        val currency = inputData.getString(CURRENCY)
        val categoryId = inputData.getInt(CATEGORY_ID, 0)
        val accountId = inputData.getInt(ACCOUNT_ID, 0)
        if (type != null && amount != null && currency != null) {
            AppDatabase.getDatabaseInstance().operationDao().insert(DataEntry(
                    null,
                    Type.valueOf(type),
                    BigDecimal(amount).setScale(2, RoundingMode.HALF_EVEN),
                    Currency.getInstance(currency),
                    categoryId,
                    accountId,
                    Calendar.getInstance().time
            ))
        }
        return Result.SUCCESS
    }
}