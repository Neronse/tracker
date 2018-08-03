package uz.firefly.tracker.room

import android.arch.persistence.room.*
import uz.firefly.tracker.util.Type
import java.math.BigDecimal
import java.util.*

@Entity(tableName = "entrys")
data class DataEntry(
        @PrimaryKey(autoGenerate = true)
        val id: Int?,
        @ColumnInfo(name = "type")val type: Type,
        @ColumnInfo(name = "amount")val amount: BigDecimal,
        @ColumnInfo(name = "currency")val currency: Currency,
        @ColumnInfo(name = "categoryid") val categoryId:Int,
        @ColumnInfo(name = "accountid")val accountId:Int
)
