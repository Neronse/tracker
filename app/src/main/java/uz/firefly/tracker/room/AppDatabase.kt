package uz.firefly.tracker.room

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import android.content.Context
import uz.firefly.tracker.TrackerApp


@Database(entities = arrayOf(DataEntry::class, TemplateEntry::class), version = 1)
@TypeConverters (EntryConverter::class)

abstract class AppDatabase :RoomDatabase() {
    abstract fun operationDao():OperationDao

    companion object {
        private val database:AppDatabase by lazy (LazyThreadSafetyMode.SYNCHRONIZED){
            Room.databaseBuilder(TrackerApp.getApplication(),AppDatabase::class.java, "tracker.db").build()
        }
        fun getDatabaseInstance(): AppDatabase =  database

    }
}

