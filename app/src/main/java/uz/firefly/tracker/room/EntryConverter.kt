package uz.firefly.tracker.room

import android.arch.persistence.room.TypeConverter
import uz.firefly.tracker.TrackerApp
import uz.firefly.tracker.util.Type
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*

class EntryConverter {
    @TypeConverter
    fun fromLong(value: Long): BigDecimal {
        return BigDecimal(value).divide(BigDecimal(100))
    }

    @TypeConverter
    fun toLong(bigDecimal: BigDecimal): Long {
        return bigDecimal.multiply(BigDecimal(100)).toLong()
    }

    @TypeConverter
    fun fromCurrency(currency: Currency):String = currency.currencyCode

    @TypeConverter
    fun toCurrency(currencyCode:String):Currency = Currency.getInstance(currencyCode)

    @TypeConverter
    fun fromType(type: Type): String = type.toString()

    @TypeConverter
    fun toType(type: String): Type = Type.valueOf(type)

    @TypeConverter
    fun fromDate(date:Date): Long = date.time

    @TypeConverter
    fun toDate(date: Long):Date = Date(date)
}
