package uz.firefly.tracker.fragment

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import org.jetbrains.anko.*
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.support.v4.ctx
import uz.firefly.tracker.R
import uz.firefly.tracker.util.Entry
import uz.firefly.tracker.util.Repository
import java.math.RoundingMode

class HistoryFragment : BaseFragment() {

    private lateinit var contentView: HistoryFragmentView


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        contentView = HistoryFragmentView()
        return contentView.createView(AnkoContext.create(ctx, this))
    }
}

private class HistoryFragmentView : AnkoComponent<HistoryFragment> {

    override fun createView(ui: AnkoContext<HistoryFragment>) = with(ui) {
        recyclerView {
            lparams(matchParent, matchParent)
            layoutManager = LinearLayoutManager(ctx)
            adapter = OperationsAdapter(Repository.operations.reversed())
        }
    }

    fun resetAdapter() {
    }

    class OperationsAdapter(val entries: List<Entry>) : RecyclerView.Adapter<OperationsAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
                ViewHolder(OperationView().createView(AnkoContext.create(parent.context, parent)))

        override fun getItemCount(): Int = entries.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) =
                holder.bind(entries[position])


        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

            val subtitle = itemView.find<TextView>(R.id.info)
            val title = itemView.find<TextView>(R.id.title)
            val amount = itemView.find<TextView>(R.id.amount)

            fun bind(entry: Entry) {
                // TODO WTF
                title.text = entry.type.name
                amount.text = entry.amount.setScale(2, RoundingMode.HALF_EVEN).toString()
                subtitle.text = when (entry.type) {
                    Entry.Type.INCOME -> Repository.incomesCategories[-entry.categoryId - 1].title
                    Entry.Type.EXPENSE -> Repository.expensesCategories[entry.categoryId].title
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
                    id = R.id.amount
                    textSize = 16.0f
                }
            }
        }
    }


}