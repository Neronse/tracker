package uz.firefly.tracker.util

import android.graphics.Color
import android.support.annotation.ColorInt
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import uz.firefly.tracker.TrackerApp
import uz.firefly.tracker.room.DataEntry

fun createPieDataSet(list: List<DataEntry>): PieDataSet {
    val entries = mutableListOf<PieEntry>()
    list.filter { it.type == Type.EXPENSE }
            .groupBy { it.categoryId }
            .forEach { entry ->
                val count = BalanceManager.calculateExpense(entry.value)
                if (count != 0.00f) {
                    val categoryId = entry.value.first().categoryId
                    val category = TrackerApp.sRepository.expensesCategories.find { it.id == categoryId }
                    entries.add(PieEntry(count, category?.title))
                }
            }
    val materialColors = intArrayOf(
            Color.parseColor("#2ECC71"),
            Color.parseColor("#5EF2DC"),
            Color.parseColor("#E74C3C"),
            Color.parseColor("#3498DB"),
            Color.parseColor("#FF5722"),
            Color.parseColor("#607D8B"),
            Color.parseColor("#7B1FA2"),
            Color.parseColor("#E91E63")
    )
    return PieDataSet(entries, "").apply {
        valueTextSize = 10f
        colors = materialColors.asList()
        sliceSpace = 3f
    }
}

fun generateSpannableTitle(str: String, @ColorInt color: Int): SpannableString {
    val ss = SpannableString(str)
    ss.setSpan(RelativeSizeSpan(1.7f), 0, str.length, 0);
    ss.setSpan(ForegroundColorSpan(color), 0, str.length - 9, 0)
    return ss
}