package uz.firefly.tracker.room

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy.REPLACE
import android.arch.persistence.room.Query
import java.util.*

@Dao
interface OperationDao {
    @Query("SELECT * FROM entrys  WHERE currency = :currency ")
    fun getAll(currency: Currency): List<DataEntry>

    @Query("SELECT * FROM entrys  WHERE currency = :currency ")
    fun getLiveBase(currency: Currency): LiveData<List<DataEntry>>

    @Insert(onConflict = REPLACE)
    fun insert(dataEntry: DataEntry)

    @Query("DELETE from entrys")
    fun deleteAll()

    @Query("SELECT * FROM entrys WHERE currency = :currency AND accountid = :accountid")
    fun getOperation(currency: Currency, accountid:Int): List<DataEntry>

}