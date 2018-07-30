package uz.firefly.tracker

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import androidx.work.*
import org.jetbrains.anko.*
import uz.firefly.tracker.fragment.MainFragment
import uz.firefly.tracker.util.ExchangeRateWorker
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MainActivityView().setContentView(this)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().add(R.id.container, MainFragment()).commit()
        }
    }

    override fun onStart() {
        super.onStart()
        WorkManager.getInstance().enqueue(OneTimeWorkRequest.Builder(ExchangeRateWorker::class.java)
                .setBackoffCriteria(BackoffPolicy.LINEAR, 10, TimeUnit.SECONDS)
                .setConstraints(Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build())
                .build())
    }

}

internal class MainActivityView : AnkoComponent<MainActivity> {
    override fun createView(ui: AnkoContext<MainActivity>) = with(ui) {
        frameLayout {
            id = R.id.container
            lparams(matchParent, matchParent)
        }
    }

}
