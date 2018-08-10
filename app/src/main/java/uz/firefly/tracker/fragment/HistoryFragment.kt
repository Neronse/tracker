package uz.firefly.tracker.fragment

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.util.DiffUtil
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import org.jetbrains.anko.*
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.support.v4.find
import uz.firefly.tracker.MainViewModel
import uz.firefly.tracker.R
import uz.firefly.tracker.TrackerApp
import uz.firefly.tracker.room.DataEntry
import uz.firefly.tracker.util.OperationDiffUtil
import uz.firefly.tracker.util.Type
import java.math.RoundingMode
import java.text.DateFormat

class HistoryFragment : BaseFragment() {

    private lateinit var contentView: HistoryFragmentView
    private lateinit var model: MainViewModel


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        contentView = HistoryFragmentView()
        return contentView.createView(AnkoContext.create(ctx, this))
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        model = ViewModelProviders.of(activity!!).get(MainViewModel::class.java)
        val list = find<RecyclerView>(R.id.recycler)
        list.adapter = HistoryFragmentView.OperationsAdapter(mutableListOf()) { dataEntry: DataEntry ->  showAlertDeleteDialog(dataEntry)}
        val recyclerAdapter = list.adapter as HistoryFragmentView.OperationsAdapter
        model.history.observe(this, Observer { it ->
            if(it != null) {
                val sorted = it.sortedByDescending { it.date }
                val operationDiffUtil = OperationDiffUtil(recyclerAdapter.entries, sorted)
                val diffResult = DiffUtil.calculateDiff(operationDiffUtil)
                recyclerAdapter.setData(sorted)
                diffResult.dispatchUpdatesTo(recyclerAdapter)
            }
        })

    }

    private fun showAlertDeleteDialog(dataEntry: DataEntry) {

        alert {
            customView {
                verticalLayout {
                    lparams(wrapContent, wrapContent)
                    padding = dip(16)
                    gravity = Gravity.CENTER_HORIZONTAL

                    textView {
                        textResource = R.string.deleteOperationAccess
                        textSize = 20.0f
                        textColorResource = R.color.secondary
                        gravity = Gravity.CENTER_HORIZONTAL
                        typeface = ResourcesCompat.getFont(ctx, R.font.roboto_condensed_regular)
                    }.lparams(wrapContent, wrapContent)
                }
            }
            yesButton {
                model.deleteEntry(dataEntry)
            }
            noButton {  }
        }.show()
    }
}

private class HistoryFragmentView : AnkoComponent<HistoryFragment> {

    override fun createView(ui: AnkoContext<HistoryFragment>) = with(ui) {
        recyclerView {
            id = R.id.recycler
            lparams(matchParent, matchParent)
            layoutManager = LinearLayoutManager(ctx)
        }
    }


    fun resetAdapter() {
    }

    class OperationsAdapter(val entries: MutableList<DataEntry>, val clickListener: (DataEntry) -> Unit) : RecyclerView.Adapter<OperationsAdapter.ViewHolder>() {

        fun setData(entries: List<DataEntry>) {
                this.entries.clear()
                this.entries.addAll(entries )
            }


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
                ViewHolder(OperationView().createView(AnkoContext.create(parent.context, parent)))

        override fun getItemCount(): Int = entries.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) =
                holder.bind(entries[position],clickListener)


        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

            val subtitle = itemView.find<TextView>(R.id.info)
            val title = itemView.find<TextView>(R.id.title)
            val amount = itemView.find<TextView>(R.id.amount)
            val date = itemView.find<TextView>(R.id.dateTextHist)

            fun bind(entry: DataEntry, clickListener: (DataEntry) -> Unit) {
                when (entry.type) {
                    Type.INCOME -> {
                        title.text = TrackerApp.getApplication().applicationContext.getString(R.string.incomes)
                        subtitle.text = TrackerApp.sRepository.incomesCategories[-entry.categoryId - 1].title
                        val count = "+${entry.amount.setScale(2, RoundingMode.HALF_EVEN)}"
                        amount.text = count
                    }
                    Type.EXPENSE -> {
                        title.text = TrackerApp.getApplication().applicationContext.getString(R.string.expenses)
                        subtitle.text = TrackerApp.sRepository.expensesCategories[entry.categoryId].title
                        val count = "-${entry.amount.setScale(2, RoundingMode.HALF_EVEN)}"
                        amount.text = count
                    }
                }
                date.text = DateFormat.getDateInstance().format(entry.date)
                itemView.setOnLongClickListener {

                    clickListener(entry)
                    true
                }
            }
        }
    }

    class OperationView : AnkoComponent<ViewGroup> {


        override fun createView(ui: AnkoContext<ViewGroup>) = with(ui) {
            linearLayout {
                lparams(matchParent, wrapContent)
                horizontalPadding = dip(16)
                verticalPadding = dip(4)
                gravity = Gravity.CENTER_VERTICAL
                minimumHeight = dip(60)
                verticalLayout {
                    textView {
                        id = R.id.title
                    }

                    textView {
                        id = R.id.info
                    }
                }.lparams(0, wrapContent, 1.0f)

                textView {
                    id = R.id.dateTextHist
                    textSize = 16.0f
                    horizontalPadding = dip(16)
                }

                textView {
                    id = R.id.amount
                    textSize = 16.0f
                }
            }
        }
    }


}