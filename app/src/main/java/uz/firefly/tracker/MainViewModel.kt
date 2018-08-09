package uz.firefly.tracker

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.github.mikephil.charting.data.PieDataSet
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import uz.firefly.tracker.room.DataEntry
import uz.firefly.tracker.room.TemplateEntry
import uz.firefly.tracker.util.createPieDataSet

class MainViewModel : ViewModel() {

    val history: MutableLiveData<List<DataEntry>> = MutableLiveData()
    val monthHistory: MutableLiveData<List<DataEntry>> = MutableLiveData()
    val mainPieData: MutableLiveData<PieDataSet> = MutableLiveData()
    val monthPieData: MutableLiveData<PieDataSet> = MutableLiveData()

    fun addOperation(entry: DataEntry) {
        launch(CommonPool) {
            TrackerApp.sRepository.insertEntry(entry)
        }
    }

    fun addTemplate(templateEntry: TemplateEntry){
        launch(CommonPool){
            TrackerApp.sRepository.insetTemplate(templateEntry)
        }
    }

    fun updateHistory(accountId: Int){
        launch(CommonPool){
            val hist: List<DataEntry>
            val monthHist: List<DataEntry>
            when(accountId){
                R.id.total_account -> {
                    hist = TrackerApp.sRepository.getDataEntries()
                    monthHist = TrackerApp.sRepository.getAllBetweenDate()
                }
                else ->{
                     hist = TrackerApp.sRepository.getOperation(accountId)
                     monthHist = TrackerApp.sRepository.getOperationBetweenDate(accountId)
                }
            }
            monthHistory.postValue(monthHist)
            history.postValue(hist)
            updatePieDataSet(hist)
            updateMonthPieDataSet(monthHist)
        }
    }

    fun getLiveBase(): LiveData<List<DataEntry>> = TrackerApp.sRepository.getLiveBase()

    fun getAllTemplates():LiveData<List<TemplateEntry>> = TrackerApp.sRepository.getAllTemplates()


    private fun updatePieDataSet(list: List<DataEntry>) {
        val pieDataSet = createPieDataSet(list)
        mainPieData.postValue(pieDataSet)
    }

    private fun updateMonthPieDataSet(list: List<DataEntry>){
        val pieDataSet = createPieDataSet(list)
        monthPieData.postValue(pieDataSet)
    }

    fun deleteItem(dataEntry: DataEntry) {
        launch(CommonPool) {
            TrackerApp.sRepository.deleteItem(dataEntry)
            updateHistory(dataEntry.accountId)
        }
    }

    fun deleteTemplate(templateEntry: TemplateEntry) =
            launch(CommonPool){TrackerApp.sRepository.deleteTemplate(templateEntry)}
}