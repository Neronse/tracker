package uz.firefly.tracker.room

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import uz.firefly.tracker.util.Type
import java.math.BigDecimal
import java.util.*

@Entity(tableName = "templates")
data class TemplateEntry(
        @PrimaryKey(autoGenerate = false) @ColumnInfo(name = "template")
        val id: String,
        @ColumnInfo(name = "type") val type: Type,
        @ColumnInfo(name = "amount") val amount: BigDecimal,
        @ColumnInfo(name = "currency") val currency: Currency,
        @ColumnInfo(name = "categoryid") val categoryId: Int,
        @ColumnInfo(name = "accountid") val accountId: Int,
        @ColumnInfo(name = "date") val date: Date
)
