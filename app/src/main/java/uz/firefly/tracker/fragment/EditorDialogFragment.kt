package uz.firefly.tracker.fragment

import android.arch.lifecycle.ViewModelProviders
import android.os.Build
import android.os.Bundle
import android.support.annotation.StringRes
import android.support.v4.content.ContextCompat
import android.support.v4.content.res.ResourcesCompat
import android.text.InputType
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.work.Data
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import org.jetbrains.anko.*
import org.jetbrains.anko.appcompat.v7.themedToolbar
import org.jetbrains.anko.design.textInputEditText
import org.jetbrains.anko.design.textInputLayout
import org.jetbrains.anko.sdk15.coroutines.textChangedListener
import org.jetbrains.anko.support.v4.ctx
import uz.firefly.tracker.MainViewModel
import uz.firefly.tracker.R
import uz.firefly.tracker.TrackerApp
import uz.firefly.tracker.room.DataEntry
import uz.firefly.tracker.util.*
import java.math.BigDecimal
import java.text.DateFormat
import java.util.*
import java.util.concurrent.TimeUnit

const val TYPE = "type"
const val AMOUNT = "amount"
const val CURRENCY = "currency"
const val CATEGORY_ID = "categoryId"
const val ACCOUNT_ID = "accountId"

class EditorFragment : BaseFragment() {

    private lateinit var contentView: EditorDialogFragmentView
    private val model by lazy { ViewModelProviders.of(this).get(MainViewModel::class.java) }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        contentView = EditorDialogFragmentView()
        return contentView.createView(AnkoContext.create(ctx, this))
    }

    fun createOperation(type: Type, amount: BigDecimal, currency: Currency, categoryId: Int, accountId: Int, date:Date) {
        model.addOperation(DataEntry(null, type, amount, currency, categoryId, accountId, date))
    }

}

private class EditorDialogFragmentView : AnkoComponent<EditorFragment> {

    lateinit var accountSpinner: Spinner
    lateinit var categorySpinner: Spinner

    lateinit var typeView: RadioGroup
    lateinit var amountView: EditText
    lateinit var checkBox: CheckBox
    lateinit var dateTextView: TextView

    class CategoryHolder(val value: Category) {
        override fun toString(): String = value.title
    }

    override fun createView(ui: AnkoContext<EditorFragment>) = with(ui) {
        relativeLayout {
            lparams(matchParent, matchParent)

            val robotoCondensed = ResourcesCompat.getFont(ctx, R.font.roboto_condensed_regular)

            val toolbar = themedToolbar(R.style.ToolbarTheme) {
                id = R.id.toolbar
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    elevation = 0.0f
                    stateListAnimator = null
                    outlineProvider = null
                }
                backgroundColorResource = R.color.white
                textView(R.string.add) {
                    textColorResource = R.color.secondary
                    textSize = 20.0f
                    typeface = robotoCondensed
                }.lparams {
                    gravity = Gravity.CENTER
                }
                setNavigationIcon(R.drawable.ic_close_24dp)
                setNavigationOnClickListener { owner.requireFragmentManager().popBackStack() }

                inflateMenu(R.menu.editor)
                setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.done -> {
                            val type = when {
                                typeView.checkedRadioButtonId == R.id.incomes -> Type.INCOME
                                else -> Type.EXPENSE
                            }
                            val amount = BigDecimal(amountView.text.toString())
                            val currency = when (ctx.defaultSharedPreferences.getInt(currentCurrency, R.id.rub)) {
                                R.id.rub -> Currency.getInstance(rub)
                                R.id.usd -> Currency.getInstance(usd)
                                else -> Currency.getInstance(rub)
                            }
                            val categoryId = (categorySpinner.selectedItem as CategoryHolder).value.id
                            val accountId = when (accountSpinner.selectedItem.toString()) {
                                ctx.getString(R.string.cash) -> R.id.cash_account
                                ctx.getString(R.string.card) -> R.id.card_account
                                ctx.getString(R.string.yandex_money) -> R.id.yamoney_account
                                else -> R.id.cash_account
                            }
                            val date = DateFormat.getDateInstance().parse(dateTextView.text.toString())
                            if (checkBox.isChecked) {
                                val data: Data = Data.Builder()
                                        .putString(TYPE, type.toString())
                                        .putString(AMOUNT, amount.toPlainString())
                                        .putString(CURRENCY, currency.currencyCode)
                                        .putInt(CATEGORY_ID, categoryId)
                                        .putInt(ACCOUNT_ID, accountId)
                                        .build()
                                val periodicWorkRequest = PeriodicWorkRequest.Builder(RegularOperationWorker::class.java, 30, TimeUnit.DAYS)
                                        .setInputData(data)
                                        .build()
                                WorkManager.getInstance().enqueue(periodicWorkRequest)

                            } else {
                                owner.createOperation(type, amount, currency, categoryId, accountId, date)
                            }
                            owner.requireFragmentManager().popBackStack()
                            true
                        }
                        else -> false
                    }
                }
            }.lparams(matchParent, wrapContent)

            scrollView {
                verticalLayout {
                    padding = dip(16)

                    typeView = radioGroup {
                        orientation = RadioGroup.HORIZONTAL
                        dividerDrawable = ContextCompat.getDrawable(ctx, R.drawable.header_divider)
                        showDividers = RadioGroup.SHOW_DIVIDER_MIDDLE
                        radioButton {
                            id = R.id.incomes
                            textResource = R.string.incomes
                            typeface = robotoCondensed
                        }

                        radioButton {
                            id = R.id.expenses
                            textResource = R.string.expenses
                            typeface = robotoCondensed
                        }
                        check(R.id.expenses)

                        setOnCheckedChangeListener { _, _ -> updateCategorySpinner() }
                    }.lparams(wrapContent, wrapContent) {
                        gravity = Gravity.CENTER_HORIZONTAL
                    }

                    textInputLayout {
                        typeface = robotoCondensed

                        amountView = textInputEditText {
                            hintResource = R.string.amount
                            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL

                            textChangedListener {

                                afterTextChanged {
                                    toolbar.menu.findItem(R.id.done).isEnabled = length() > 0
                                }

                            }
                        }
                    }.lparams(matchParent, wrapContent)


                    val padding = dip(4)

                    fun headerTextView(@StringRes titleRes: Int) = textView(titleRes) {
                        typeface = robotoCondensed
                        textColorResource = R.color.accent
                        textSize = 12.0f
                        horizontalPadding = padding
                    }

                    headerTextView(R.string.category)

                    categorySpinner = spinner().lparams(matchParent, wrapContent)

                    headerTextView(R.string.account)

                    accountSpinner = spinner {
                        adapter = object : ArrayAdapter<String>(ctx, android.R.layout.simple_list_item_1,
                                android.R.id.text1, TrackerApp.sRepository.accounts
                                .slice(1 until TrackerApp.sRepository.accounts.size)
                                .map { ctx.getString(it.title) }) {
                            override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
                                return super.getView(position, convertView, parent).also {
                                    it.setPadding(padding, padding, padding, padding)
                                }
                            }
                        }
                    }.lparams(matchParent, wrapContent)

                    relativeLayout{
                        dateTextView = textView {
                            id = R.id.date
                            text = DateFormat.getDateInstance().format(Date())
                            textAppearance = android.R.style.TextAppearance_Holo_Widget_TextView
                            typeface = robotoCondensed

                        }.lparams(wrapContent, wrapContent){
                            centerInParent()
                        }
                        button{
                            textResource = R.string.chooseDate
                            id = R.id.datePickBtn

                            setOnClickListener {
                                alert {
                                    isCancelable = false
                                    lateinit var datePicker: DatePicker
                                    customView {
                                        verticalLayout {
                                            datePicker = datePicker {
                                                maxDate = System.currentTimeMillis()
                                            }
                                        }
                                    }
                                    yesButton {
                                        setNewDate(datePicker)
                                    }
                                    noButton { }
                                }.show()
                            }
                        }.lparams(wrapContent, wrapContent){
                            alignParentRight()
                        }

                    }.lparams(matchParent, wrapContent)


                    checkBox = checkBox {
                        textResource = R.string.parametrs
                        typeface = robotoCondensed
                        isChecked = false
                    }
                }


            }.lparams(matchParent, wrapContent) {
                alignParentBottom()
                bottomOf(R.id.toolbar)
            }

            view {
                backgroundResource = R.drawable.dropshadow
            }.lparams(matchParent, dip(2)) {
                bottomOf(R.id.toolbar)
            }

            updateCategorySpinner()
        }
    }

    fun updateCategorySpinner() {
        val categories = when (typeView.checkedRadioButtonId) {
            R.id.incomes -> TrackerApp.sRepository.incomesCategories
            R.id.expenses -> TrackerApp.sRepository.expensesCategories
            else -> throw RuntimeException("invalid checkedRadioButtonId")
        }
        categorySpinner.adapter = object : ArrayAdapter<CategoryHolder>(categorySpinner.context,
                android.R.layout.simple_list_item_1, android.R.id.text1,
                categories.map { CategoryHolder(it) }) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
                return super.getView(position, convertView, parent).also {
                    val padding = it.context.dip(4)
                    it.setPadding(padding, padding, padding, padding)
                }
            }
        }
    }

    fun setNewDate(datePicker:DatePicker){
        val calendar = Calendar.getInstance()
        calendar.set(datePicker.year, datePicker.month, datePicker.dayOfMonth)
        dateTextView.text = DateFormat.getDateInstance().format(calendar.time)
    }
}