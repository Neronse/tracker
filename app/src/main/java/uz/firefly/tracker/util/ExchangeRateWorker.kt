package uz.firefly.tracker.util

import androidx.work.Worker
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.apply
import org.jetbrains.anko.defaultSharedPreferences
import org.jetbrains.anko.info
import org.json.JSONObject

const val usdRub = "USD_RUB"
const val rubUsd = "RUB_USD"

class ExchangeRateWorker : Worker(), AnkoLogger {

    override fun doWork(): Result {
        val client = OkHttpClient()
        val url = "http://free.currencyconverterapi.com/api/v6/convert?q=$usdRub&compact=ultra"
        val request = Request.Builder().url(url).build()
        try {
            val response = client.newCall(request).execute()
            response.body()?.string()?.let {
                val data = JSONObject(it)
                val exchangeRate = data.getDouble(usdRub).toString()
                applicationContext.defaultSharedPreferences.apply {
                    putString(usdRub, exchangeRate)
                }
                info { exchangeRate }
                return Result.SUCCESS
            }
        } catch (e: Exception) {
            info { e }
        }
        return Result.RETRY
    }

}