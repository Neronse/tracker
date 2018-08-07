package uz.firefly.tracker.util

import android.support.v7.util.DiffUtil
import uz.firefly.tracker.room.DataEntry

class OperationDiffUtil(private val oldList: List<DataEntry>, private val newList: List<DataEntry>) : DiffUtil.Callback() {


    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            oldList[oldItemPosition].id == newList[newItemPosition].id

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val newDataEntry = newList[newItemPosition]
        val oldDataEntry = oldList[oldItemPosition]

        return oldDataEntry.type == newDataEntry.type &&
                oldDataEntry.amount == newDataEntry.amount &&
                oldDataEntry.accountId == newDataEntry.accountId &&
                oldDataEntry.currency == newDataEntry.currency &&
                oldDataEntry.categoryId == newDataEntry.categoryId &&
                oldDataEntry.date == newDataEntry.date
    }

}