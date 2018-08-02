package uz.firefly.tracker

import android.app.Application
import uz.firefly.tracker.util.Repository

class TrackerApp : Application() {


    override fun onCreate() {
        super.onCreate()
        sInstance = this;
    }

    companion object {
        val SCALE_BD = 2
        private lateinit var sInstance: TrackerApp;

        val sRepository: Repository by lazy {  Repository(TrackerApp.sInstance.applicationContext) }

        fun getApplication(): TrackerApp = sInstance
    }
}