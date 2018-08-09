package uz.firefly.tracker.fragment

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.support.v4.content.res.ResourcesCompat
import android.text.Spannable
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import org.jetbrains.anko.*
import org.jetbrains.anko.support.v4.ctx
import uz.firefly.tracker.MainViewModel
import uz.firefly.tracker.R
import uz.firefly.tracker.util.BalanceManager
import uz.firefly.tracker.util.Type
import uz.firefly.tracker.util.generateSpannableTitle
import java.text.SimpleDateFormat
import java.util.*

class DummyFragment : BaseFragment() {

    private lateinit var contentView: DummyFragmentView
    private lateinit var model: MainViewModel


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        contentView = DummyFragmentView()
        return contentView.createView(AnkoContext.create(ctx, this))
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        model = ViewModelProviders.of(activity!!).get(MainViewModel::class.java)
        model.monthPieData.observe(this, Observer {
            contentView.pieChart.data = PieData(it)
            contentView.pieChart.invalidate()
        })
        model.monthHistory.observe(this, Observer { it ->

            if (it != null) {
                var totalInc: String = getString(R.string.noData)
                var totalExp: String = getString(R.string.noData)
                var delta: String = getString(R.string.noData)
                it.groupBy { it.type }.forEach {
                    when (it.key) {
                        Type.INCOME -> {
                            val incBalance = BalanceManager.calculateBalance(it.value)
                            totalInc = "${getString(R.string.incomes)} = $incBalance"
                        }
                        Type.EXPENSE -> {
                            val expBalance = BalanceManager.calculateBalance(it.value)
                            totalExp = "${getString(R.string.expenses)} = $expBalance"
                        }
                    }
                }
                val balance = BalanceManager.calculateBalance(it)
                delta = "${getString(R.string.diff)} = $balance"
                contentView.totalInc.text = totalInc
                contentView.totalExp.text = totalExp
                contentView.delta.text = delta

            }
        })
    }
}

private class DummyFragmentView : AnkoComponent<DummyFragment> {

    lateinit var totalInc: TextView
    lateinit var totalExp: TextView
    lateinit var delta: TextView
    lateinit var pieChart: PieChart


    override fun createView(ui: AnkoContext<DummyFragment>) = with(ui) {


        relativeLayout {
            val typeFc = ResourcesCompat.getFont(ctx, R.font.roboto_condensed_regular)

            lparams(matchParent, matchParent)
            linearLayout {
                id = R.id.dateTitle
                textView {
                    typeface = typeFc
                    horizontalPadding = dip(16)
                    verticalPadding = dip(4)
                    textSize = 20f
                    textResource = R.string.statistics

                }.lparams(wrapContent, wrapContent)
                textView {
                    id = R.id.date
                    typeface = typeFc
                    horizontalPadding = dip(4)
                    verticalPadding = dip(4)
                    textSize = 20f
                    text = getSpannableDate()
                }.lparams(wrapContent, wrapContent)
            }.lparams(wrapContent, wrapContent) {
                centerHorizontally()
                alignParentTop()
            }

            verticalLayout {
                id = R.id.statisticsBlock
                totalInc = textView {
                    typeface = typeFc
                    horizontalPadding = dip(16)
                    verticalPadding = dip(4)
                    textSize = 18f
                    textResource = R.string.noData
                }.lparams(wrapContent, wrapContent)
                totalExp = textView {
                    typeface = typeFc
                    horizontalPadding = dip(16)
                    verticalPadding = dip(4)
                    textSize = 18f
                    textResource = R.string.noData
                }.lparams(wrapContent, wrapContent)
                delta = textView {
                    typeface = typeFc
                    horizontalPadding = dip(16)
                    verticalPadding = dip(4)
                    textSize = 18f
                    textResource = R.string.noData
                }.lparams(wrapContent, wrapContent)

                pieChart = pieChart {
                    id = R.id.pieChart
                    legend.verticalAlignment = Legend.LegendVerticalAlignment.TOP
                    legend.horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
                    legend.orientation = Legend.LegendOrientation.HORIZONTAL
                    legend.textSize = 15f//dp
                    legend.formSize = 12f//dp
                    legend.typeface = typeFc
                    legend.isWordWrapEnabled = true
                    setEntryLabelTextSize(10f)//sp
                    setEntryLabelTypeface(typeFc)
                    setHoleColor(resources.getColor(R.color.yellow))
                    centerText = generateSpannableTitle(ctx.getString(R.string.app_name), resources.getColor(R.color.accent))
                    holeRadius = 58f//проценты  от радиуса всей диаграммы
                    transparentCircleRadius = 61f//проценты
                    description.isEnabled = false
                    dragDecelerationFrictionCoef = 0.95f
                    isDrawHoleEnabled = true
                    isRotationEnabled = false
                    setTransparentCircleColor(Color.WHITE)
                    setTransparentCircleAlpha(110)
                    animateX(1400)
                }.lparams(matchParent, matchParent) {
                    gravity = Gravity.CENTER
                }

            }.lparams(matchParent, matchParent) {
                below(R.id.dateTitle)
            }


        }
    }


    private fun getSpannableDate(): SpannableString {
        val sdf = SimpleDateFormat("MMMM yyyy", Resources.getSystem().configuration.locale)
        val stringDate = sdf.format(Date())
        val ss = SpannableString(stringDate)
        ss.setSpan(UnderlineSpan(), 0, stringDate.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        return ss
    }


}