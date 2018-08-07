package uz.firefly.tracker

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.graphics.Color
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import uz.firefly.tracker.room.DataEntry
import uz.firefly.tracker.util.BalanceManager
import uz.firefly.tracker.util.Type

class MainViewModel : ViewModel() {

    val history: MutableLiveData<List<DataEntry>> = MutableLiveData()
    val pieData: MutableLiveData<PieDataSet> = MutableLiveData()

    fun addOperation(entry: DataEntry) {
        launch(CommonPool) {
            TrackerApp.sRepository.insertEntry(entry)
        }
    }

    fun updateHistory(accountId: Int) {
        when (accountId) {
            R.id.total_account -> launch(CommonPool) {
                val hist = TrackerApp.sRepository.getDataEntries()
                history.postValue(hist)
                updatePieDataSet(hist)
            }
            else -> launch(CommonPool) {
                val hist = TrackerApp.sRepository.getOperation(accountId)
                history.postValue(hist)
                updatePieDataSet(hist)
            }
        }
    }

    fun getLiveBase(): LiveData<List<DataEntry>> = TrackerApp.sRepository.getLiveBase()

    private fun updatePieDataSet(list: List<DataEntry>) {
        val entries = mutableListOf<PieEntry>()
        list.filter { it.type == Type.EXPENSE }.groupBy { it.categoryId }.forEach { entry ->
            val count = BalanceManager.calculateExpense(entry.value)
            if (count != 0.00f) {
                val categoryId = entry.value.first().categoryId
                val category = TrackerApp.sRepository.expensesCategories.find { it.id == categoryId }
                entries.add(PieEntry(count, category?.title))
            }
        }
        val materialColors = intArrayOf(
                Color.parseColor("#2ECC71"),
                Color.parseColor("#F1C40F"),
                Color.parseColor("#E74C3C"),
                Color.parseColor("#3498DB"),
                Color.parseColor("#FF5722"),
                Color.parseColor("#607D8B"),
                Color.parseColor("#7B1FA2"),
                Color.parseColor("#E91E63")
        )
        val pieDataSet = PieDataSet(entries, "").apply {
            valueTextSize = 10f
            colors = materialColors.asList()
            sliceSpace = 3f
        }
        pieData.postValue(pieDataSet)
    }

    fun deleteItem(dataEntry: DataEntry) {
        launch(CommonPool){
            TrackerApp.sRepository.deleteItem(dataEntry)
            updateHistory(dataEntry.accountId)
        }
    }
}