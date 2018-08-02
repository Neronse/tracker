package uz.firefly.tracker

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import uz.firefly.tracker.room.DataEntry

class MainViewModel : ViewModel() {

    val history: MutableLiveData<List<DataEntry>> = MutableLiveData()
    val itemAdded: MutableLiveData<DataEntry> = MutableLiveData()

    fun addOperation(entry: DataEntry) {
        launch(CommonPool) {
            TrackerApp.sRepository.insertEntry(entry)
           // itemAdded.postValue(entry)
        }
    }

    fun updateHistory(accountId: Int) {
        when (accountId) {
            R.id.total_account -> launch(CommonPool) { history.postValue(TrackerApp.sRepository.getDataEntries()) }
            else -> launch(CommonPool) { history.postValue(TrackerApp.sRepository.getOperation(accountId)) }
        }
    }

}