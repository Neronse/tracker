package uz.firefly.tracker.room

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy.REPLACE
import android.arch.persistence.room.Query
import java.util.*

@Dao
interface OperationDao {
    @Query("SELECT * FROM entrys  WHERE currency = :currency ")
    fun getAllEntry(currency: Currency): List<DataEntry>

    @Query("SELECT * FROM entrys  WHERE currency = :currency ")
    fun getLiveBaseEntry(currency: Currency): LiveData<List<DataEntry>>

    @Insert(onConflict = REPLACE)
    fun insertEntry(dataEntry: DataEntry)

    @Query("DELETE from entrys")
    fun deleteAllEntries()

    @Query("SELECT * FROM entrys WHERE currency = :currency AND accountid = :accountid")
    fun getOperation(currency: Currency, accountid:Int): List<DataEntry>

    @Query("SELECT * FROM entrys WHERE date BETWEEN :from and :to AND currency = :currency AND accountid = :accountid")
    fun getAllEntryBetweenDateAccount(from:Date, to:Date, currency: Currency, accountid: Int): List<DataEntry>

    @Query("SELECT * FROM entrys WHERE date BETWEEN :from and :to AND currency = :currency ")
    fun getAllEntryBetweenDate(from:Date, to:Date, currency: Currency): List<DataEntry>

    @Delete
    fun deleteEntry(dataEntry: DataEntry)

    @Query("SELECT * FROM templates WHERE currency = :currency")
    fun getAllTemplates(currency: Currency): LiveData<List<TemplateEntry>>

    @Insert(onConflict = REPLACE)
    fun insertTemplate(templateEntry: TemplateEntry)

    @Delete
    fun deleteTemplate(templateEntry: TemplateEntry)

}