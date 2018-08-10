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

    fun addEntry(entry: DataEntry) {
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
                    hist = TrackerApp.sRepository.getAllEntry()
                    monthHist = TrackerApp.sRepository.getAllEntryBetweenDate()
                }
                else ->{
                     hist = TrackerApp.sRepository.getOperation(accountId)
                     monthHist = TrackerApp.sRepository.getAllEntryBetweenDateAccount(accountId)
                }
            }
            monthHistory.postValue(monthHist)
            history.postValue(hist)
            updatePieDataSet(hist)
            updateMonthPieDataSet(monthHist)
        }
    }

    fun getLiveBaseEntry(): LiveData<List<DataEntry>> = TrackerApp.sRepository.getLiveBaseEntry()

    fun getAllTemplates():LiveData<List<TemplateEntry>> = TrackerApp.sRepository.getAllTemplates()


    private fun updatePieDataSet(list: List<DataEntry>) {
        val pieDataSet = createPieDataSet(list)
        mainPieData.postValue(pieDataSet)
    }

    private fun updateMonthPieDataSet(list: List<DataEntry>){
        val pieDataSet = createPieDataSet(list)
        monthPieData.postValue(pieDataSet)
    }

    fun deleteEntry(dataEntry: DataEntry) {
        launch(CommonPool) {
            TrackerApp.sRepository.deleteEntry(dataEntry)
            updateHistory(dataEntry.accountId)
        }
    }

    fun deleteTemplate(templateEntry: TemplateEntry) =
            launch(CommonPool){TrackerApp.sRepository.deleteTemplate(templateEntry)}
}