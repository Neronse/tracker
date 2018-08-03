package uz.firefly.tracker

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
import java.math.BigDecimal

class MainViewModel : ViewModel() {

    val history: MutableLiveData<List<DataEntry>> = MutableLiveData()
    val pieData: MutableLiveData<PieDataSet> = MutableLiveData()
    val balance: MutableLiveData<BigDecimal> = MutableLiveData()

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

    fun updateBalance(){
        launch(CommonPool) {
            val hist = TrackerApp.sRepository.getDataEntries()
            balance.postValue( BalanceManager.calculateBalance(hist))
        }
    }


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
                rgb("#2ecc71"),
                rgb("#f1c40f"),
                rgb("#e74c3c"),
                rgb("#3498db"),
                rgb("#FF5722"),
                rgb("#607D8B"),
                rgb("#7B1FA2"),
                rgb("#E91E63"))
        val pieDataSet = PieDataSet(entries, "").apply {
            valueTextSize = 10f
            colors = materialColors.asList()
            sliceSpace = 3f
        }
        pieData.postValue(pieDataSet)
    }

    private fun rgb(hex: String): Int {
        val color = java.lang.Long.parseLong(hex.replace("#", ""), 16).toInt()
        val r = color shr 16 and 0xFF
        val g = color shr 8 and 0xFF
        val b = color shr 0 and 0xFF
        return Color.rgb(r, g, b)
    }

}