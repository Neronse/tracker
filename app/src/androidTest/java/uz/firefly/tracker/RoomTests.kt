package uz.firefly.tracker

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.arch.persistence.room.Room
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import junit.framework.Assert.assertEquals
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import uz.firefly.tracker.room.AppDatabase
import uz.firefly.tracker.room.DataEntry
import uz.firefly.tracker.room.OperationDao
import uz.firefly.tracker.room.TemplateEntry
import uz.firefly.tracker.util.BalanceManager
import uz.firefly.tracker.util.Type
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit


@RunWith(AndroidJUnit4::class)
class RoomTests {
    private lateinit var operationDao: OperationDao
    private lateinit var appDatabase: AppDatabase


    val list = listOf(
            DataEntry(null, Type.INCOME, BigDecimal(1.25).setScale(2, RoundingMode.HALF_EVEN), Currency.getInstance("RUB"), 1, 1, Date()),
            DataEntry(null, Type.INCOME, BigDecimal(1.25).setScale(2, RoundingMode.HALF_EVEN), Currency.getInstance("RUB"), 1, 1, Date()),
            DataEntry(null, Type.INCOME, BigDecimal(1.25).setScale(2, RoundingMode.HALF_EVEN), Currency.getInstance("RUB"), 1, 1, Date()),
            DataEntry(null, Type.INCOME, BigDecimal(1.25).setScale(2, RoundingMode.HALF_EVEN), Currency.getInstance("RUB"), 1, 1, Date()),

            DataEntry(null, Type.INCOME, BigDecimal(1.25).setScale(2, RoundingMode.HALF_EVEN), Currency.getInstance("USD"), 1, 1, Date()),
            DataEntry(null, Type.INCOME, BigDecimal(1.25).setScale(2, RoundingMode.HALF_EVEN), Currency.getInstance("USD"), 1, 1, Date()),

            DataEntry(null, Type.INCOME, BigDecimal(1.25).setScale(2, RoundingMode.HALF_EVEN), Currency.getInstance("RUB"), 1, 2, Date()),
            DataEntry(null, Type.INCOME, BigDecimal(1.25).setScale(2, RoundingMode.HALF_EVEN), Currency.getInstance("RUB"), 1, 2, Date())
    )
    val template = TemplateEntry("Template",Type.INCOME,BigDecimal(1.25).setScale(2, RoundingMode.HALF_EVEN),Currency.getInstance("RUB"),1,1,Date())

    @Before
    fun createDb() {
        val context = InstrumentationRegistry.getContext()
        appDatabase = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
                .fallbackToDestructiveMigration().build()
        operationDao = appDatabase.operationDao()
    }

    @After
    fun closeDb() {
        appDatabase.close()
    }

    @Test
    fun testInsertDataEntryItem() {
        val dataEntry = DataEntry(1,
                Type.INCOME,
                BigDecimal(1.25).setScale(2, RoundingMode.HALF_EVEN),
                Currency.getInstance("RUB"),
                1,
                1,
                Date()
        )
        operationDao.insertEntry(dataEntry)
        val list: List<DataEntry> = operationDao.getAllEntry(Currency.getInstance("RUB"))
        assertEquals(list[0], dataEntry)
        operationDao.deleteAllEntries()
    }

    @Test
    fun testGetOperation() {
        list.forEach { operationDao.insertEntry(it) }
        val list = operationDao.getOperation(Currency.getInstance("RUB"), 1)
        val balance = BalanceManager.calculateBalance(list)
        assertEquals(balance, BigDecimal(5.00).setScale(2, RoundingMode.HALF_EVEN))
    }


    @Test
    fun testGetLiveBase() {
        list.forEach { operationDao.insertEntry(it) }
        val data = operationDao.getLiveBaseEntry(Currency.getInstance("RUB"))
        val balance = BalanceManager.calculateBalance(data.getCurrentValue()!!)
        assertEquals(balance, BigDecimal(7.50).setScale(2,RoundingMode.HALF_EVEN))
    }

    @Test
    fun testInsertTemplates() {
        operationDao.insertTemplate(template)
        val temp = operationDao.getAllTemplates(Currency.getInstance("RUB"))
        assertEquals(temp.getCurrentValue()!![0].id, template.id)
    }

    @Test
    fun testGetOperationBetweenDate(){
        val firstDate = Calendar.getInstance()
        firstDate.set(2018, 1,1)
        val secondDate = Calendar.getInstance()
        secondDate.set(2018, 5, 10)
        val thirdDate = Calendar.getInstance()
        thirdDate.set(2018, 9, 15)

        val list = listOf(
                DataEntry(null, Type.INCOME, BigDecimal(1.25).setScale(2, RoundingMode.HALF_EVEN), Currency.getInstance("RUB"), 1, 1, firstDate.time),
                DataEntry(null, Type.INCOME, BigDecimal(1.25).setScale(2, RoundingMode.HALF_EVEN), Currency.getInstance("RUB"), 1, 1, secondDate.time),
                DataEntry(null, Type.INCOME, BigDecimal(1.25).setScale(2, RoundingMode.HALF_EVEN), Currency.getInstance("RUB"), 1, 1, thirdDate.time)
        )
        val fromDate = Calendar.getInstance()
        fromDate.set(2018, 2, 15)
        val toDate = Calendar.getInstance()
        toDate.set(2018, 8, 15)

        list.forEach { operationDao.insertEntry(it) }
        val item = operationDao.getAllEntryBetweenDateAccount(fromDate.time, toDate.time, Currency.getInstance("RUB"), 1)
        assertEquals(item.size, 1)
        val item2 = operationDao.getAllEntryBetweenDate(fromDate.time, toDate.time, Currency.getInstance("RUB"))
        assertEquals(item2.size, 1)
    }

    @Test
    fun testDeleteEntry(){
        val entry = DataEntry(1, Type.INCOME, BigDecimal(1.25).setScale(2, RoundingMode.HALF_EVEN), Currency.getInstance("RUB"), 1, 1,Date())
        operationDao.insertEntry(entry)
        operationDao.deleteEntry(entry)
        val items = operationDao.getAllEntry(Currency.getInstance("RUB"))
        assertEquals(items.size, 0)
    }

    @Test
    fun testDeleteTemplate(){
        operationDao.insertTemplate(template)
        operationDao.deleteTemplate(template)
        val items = operationDao.getAllTemplates(Currency.getInstance("RUB"))
        assertEquals(items.getCurrentValue()!!.size, 0)
    }

    @Test
    fun testDeleteAllEntries(){
        list.forEach { operationDao.insertEntry(it) }
        operationDao.deleteAllEntries()
        val items = operationDao.getAllEntry(Currency.getInstance("RUB"))
        assertEquals(items.size, 0)
    }
}

//потому что livedata ленивая, а я нет :(
private fun <T> LiveData<T>.getCurrentValue(): T? {
    var value: T? = null
    val latch = CountDownLatch(1)
    val obs = Observer<T> {
        value = it
        latch.countDown()
    }
    observeForever(obs)
    latch.await(2, TimeUnit.SECONDS)
    return value
}

