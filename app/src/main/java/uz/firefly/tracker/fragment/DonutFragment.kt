package uz.firefly.tracker.fragment

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.content.res.ResourcesCompat
import android.view.*
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import org.jetbrains.anko.*
import org.jetbrains.anko.custom.ankoView
import org.jetbrains.anko.support.v4.ctx
import uz.firefly.tracker.MainViewModel
import uz.firefly.tracker.R

class DonutFragment : BaseFragment() {
    private lateinit var contentView: DonutFragmentView
    private val model by lazy { ViewModelProviders.of(activity!!).get(MainViewModel::class.java) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        contentView = DonutFragmentView()
        return contentView.createView(AnkoContext.create(ctx, this))
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        model.pieData.observe(this, Observer {
            contentView.pieChart.data = PieData(it)
            contentView.pieChart.invalidate()
        })
    }
}


private class DonutFragmentView : AnkoComponent<DonutFragment> {
    lateinit var pieChart: PieChart

    override fun createView(ui: AnkoContext<DonutFragment>) = with(ui) {
        frameLayout {
            val typeFc = ResourcesCompat.getFont(ctx, R.font.roboto_condensed_regular)

            pieChart = pieChart {
                id = R.id.pieChart
                legend.verticalAlignment = Legend.LegendVerticalAlignment.TOP
                legend.horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
                legend.orientation = Legend.LegendOrientation.HORIZONTAL
                legend.textSize = 15f
                legend.formSize = 12f
                legend.typeface = typeFc
                legend.isWordWrapEnabled = true
                setEntryLabelTextSize(10f)
                setEntryLabelTypeface(typeFc)
                holeRadius = 15f
                transparentCircleRadius = 20f
                description.isEnabled = false
                dragDecelerationFrictionCoef = 0.95f
                isDrawHoleEnabled = false
                animateX(1400)
            }.lparams(matchParent, matchParent) {
                gravity = Gravity.CENTER
            }
        }
    }
}

inline fun ViewManager.pieChart(): PieChart = pieChart() {}
inline fun ViewManager.pieChart(init: (@AnkoViewDslMarker PieChart).() -> Unit): PieChart {
    return ankoView({ PieChart(it) }, theme = 0) { init() }
}