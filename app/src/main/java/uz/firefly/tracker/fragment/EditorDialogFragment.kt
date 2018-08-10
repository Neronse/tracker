package uz.firefly.tracker.fragment

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
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
import org.jetbrains.anko.support.v4.find
import org.jetbrains.anko.support.v4.toast
import uz.firefly.tracker.MainViewModel
import uz.firefly.tracker.R
import uz.firefly.tracker.TrackerApp
import uz.firefly.tracker.room.DataEntry
import uz.firefly.tracker.room.TemplateEntry
import uz.firefly.tracker.util.*
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

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

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val listView = find<ListView>(R.id.templateList)
        listView.adapter = EditorDialogFragmentView.TemplateAdapter(context!!)

        listView.setOnItemClickListener { adapterView, view, position, id ->
            val templateEntry = (listView.adapter as EditorDialogFragmentView.TemplateAdapter).getItem(position)
            val dataEntry = DataEntry(null,
                    templateEntry.type,
                    templateEntry.amount,
                    templateEntry.currency,
                    templateEntry.categoryId,
                    templateEntry.accountId,
                    Date())
            createOperation(dataEntry)
            toast("${getString(R.string.added)}  ${templateEntry.id}")
        }

        listView.setOnItemLongClickListener { adapterView, view, position, id ->
            val templateEntry = (listView.adapter as EditorDialogFragmentView.TemplateAdapter).getItem(position)
            deleteTemplate(templateEntry)
            true
        }
        model.getAllTemplates().observe(this, Observer {
            if (it != null) {
                (listView.adapter as EditorDialogFragmentView.TemplateAdapter).updateTemplates(it)
            }
        })
    }


    fun createOperation(dataEntry: DataEntry) {
        model.addEntry(dataEntry)
    }

    fun createTemplate(templateEntry: TemplateEntry) {
        model.addTemplate(templateEntry)
    }

    fun deleteTemplate(templateEntry: TemplateEntry){
        model.deleteTemplate(templateEntry)
    }

}

private class EditorDialogFragmentView : AnkoComponent<EditorFragment> {

    lateinit var accountSpinner: Spinner
    lateinit var categorySpinner: Spinner

    lateinit var typeView: RadioGroup
    lateinit var amountView: EditText
    lateinit var checkBox: CheckBox
    lateinit var dateTextView: TextView
    private lateinit var templateId: EditText

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
                            val entry = collectData()
                            if (checkBox.isChecked) {
                                val data: Data = Data.Builder()
                                        .putString(TYPE, entry.type.toString())
                                        .putString(AMOUNT, entry.amount.toPlainString())
                                        .putString(CURRENCY, entry.currency.currencyCode)
                                        .putInt(CATEGORY_ID, entry.categoryId)
                                        .putInt(ACCOUNT_ID, entry.accountId)
                                        .build()
                                val periodicWorkRequest = PeriodicWorkRequest.Builder(RegularOperationWorker::class.java, 30, TimeUnit.DAYS)
                                        .setInputData(data)
                                        .build()
                                WorkManager.getInstance().enqueue(periodicWorkRequest)
                            } else {
                                owner.createOperation(entry)
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


                    val paddingConst = dip(4)

                    fun headerTextView(@StringRes titleRes: Int) = textView(titleRes) {
                        typeface = robotoCondensed
                        textColorResource = R.color.accent
                        textSize = 12.0f
                        horizontalPadding = paddingConst
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
                                    it.setPadding(paddingConst, paddingConst, paddingConst, paddingConst)
                                }
                            }
                        }
                    }.lparams(matchParent, wrapContent)

                    relativeLayout {
                        dateTextView = textView {
                            id = R.id.date
                            text = DateFormat.getDateInstance().format(Date())
                            textAppearance = android.R.style.TextAppearance_Holo_Widget_TextView
                            typeface = robotoCondensed

                        }.lparams(wrapContent, wrapContent) {
                            centerInParent()
                        }
                        themedButton(theme = R.style.Widget_MaterialComponents_Button_TextButton) {
                            textResource = R.string.chooseDate
                            id = R.id.datePickBtn

                            setOnClickListener { it ->
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
                        }.lparams(wrapContent, wrapContent) {
                            alignParentRight()
                        }

                    }.lparams(matchParent, wrapContent)


                    checkBox = checkBox {
                        textResource = R.string.parametrs
                        typeface = robotoCondensed
                        isChecked = false
                    }
                    themedButton(theme = R.style.Widget_MaterialComponents_Button_TextButton) {
                        id = R.id.templateBtn
                        textResource = R.string.createTemplate
                        setOnClickListener { it ->
                            if (amountView.text.isNotBlank()) {
                                alert {
                                    customView {
                                        verticalLayout {
                                            padding = dip(16)
                                            lparams(wrapContent, wrapContent)
                                            gravity = Gravity.CENTER_HORIZONTAL
                                            textView {
                                                textResource = R.string.enterTemplateId
                                                textSize = 20.0f
                                                textColorResource = R.color.secondary
                                                typeface = ResourcesCompat.getFont(ctx, R.font.roboto_condensed_regular)
                                            }.lparams(wrapContent, wrapContent)
                                            templateId = editText {
                                                inputType = InputType.TYPE_CLASS_TEXT
                                                hint = context.getString(R.string.template)
                                            }.lparams(matchParent, wrapContent)
                                        }
                                    }
                                    yesButton {
                                        val dataEntry = collectData()
                                        val id: String = templateId.text.toString()
                                        if (id.isNotBlank()) {
                                            owner.createTemplate(getTemplateEntry(id, dataEntry))
                                        } else owner.createTemplate(getTemplateEntry(context.getString(R.string.template), dataEntry))
                                    }
                                    noButton { }
                                }.show()

                            } else {
                                toast(context.getString(R.string.fillFields))
                            }
                        }
                    }

                    listView {
                        id = R.id.templateList
                    }.lparams(matchParent, dip(250))

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

    fun collectData(): DataEntry {
        val type = when {
            typeView.checkedRadioButtonId == R.id.incomes -> Type.INCOME
            else -> Type.EXPENSE
        }
        val amount = BigDecimal(amountView.text.toString())
        val currency = when (typeView.context.defaultSharedPreferences.getInt(currentCurrency, R.id.rub)) {
            R.id.rub -> Currency.getInstance(rub)
            R.id.usd -> Currency.getInstance(usd)
            else -> Currency.getInstance(rub)
        }
        val categoryId = (categorySpinner.selectedItem as CategoryHolder).value.id
        val accountId = when (accountSpinner.selectedItem.toString()) {
            accountSpinner.context.getString(R.string.cash) -> R.id.cash_account
            accountSpinner.context.getString(R.string.card) -> R.id.card_account
            accountSpinner.context.getString(R.string.yandex_money) -> R.id.yamoney_account
            else -> R.id.cash_account
        }
        val date = DateFormat.getDateInstance().parse(dateTextView.text.toString())
        return DataEntry(null, type, amount, currency, categoryId, accountId, date)
    }

    fun getTemplateEntry(id: String, dataEntry: DataEntry): TemplateEntry =
            TemplateEntry(id, dataEntry.type, dataEntry.amount, dataEntry.currency, dataEntry.categoryId, dataEntry.accountId, dataEntry.date)

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

    fun setNewDate(datePicker: DatePicker) {
        val calendar = Calendar.getInstance()
        calendar.set(datePicker.year, datePicker.month, datePicker.dayOfMonth)
        dateTextView.text = DateFormat.getDateInstance().format(calendar.time)
    }

    class TemplateAdapter(context: Context) : ArrayAdapter<Any>(context, 0) {
        private var templates: List<TemplateEntry> = ArrayList()

        fun updateTemplates(list: List<TemplateEntry>) {
            templates = list
            notifyDataSetChanged()
        }

        override fun getCount(): Int = templates.size
        override fun getItem(position: Int): TemplateEntry = templates[position]
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val template = templates[position]
            val holder: TemplateVH
            val retView: View
            if (convertView == null) {
                retView = TemplateListItem().createView(AnkoContext.create(parent.context, parent))
                holder = TemplateVH()
                holder.templateTitle = retView.findViewById(R.id.templateTitle)
                holder.categoryInfo = retView.findViewById(R.id.templateInfo)
                holder.templateAmount = retView.findViewById(R.id.templateAmount)
                retView.tag = holder
            } else {
                holder = convertView.tag as TemplateVH
                retView = convertView
            }
            holder.templateTitle?.text = template.id

            when (template.type) {
                Type.INCOME -> {
                    holder.categoryInfo?.text = TrackerApp.sRepository.incomesCategories[-template.categoryId - 1].title
                    val amount = "+${template.amount.setScale(2, RoundingMode.HALF_EVEN)}"
                    holder.templateAmount?.text = amount
                }
                Type.EXPENSE -> {
                    holder.categoryInfo?.text = TrackerApp.sRepository.expensesCategories[template.categoryId].title
                    val amount = "-${template.amount.setScale(2, RoundingMode.HALF_EVEN)}"
                    holder.templateAmount?.text = amount
                }
            }
            return retView
        }
    }

    class TemplateVH {
        var templateTitle: TextView? = null
        var categoryInfo: TextView? = null
        var templateAmount: TextView? = null
    }

    class TemplateListItem : AnkoComponent<ViewGroup> {
        override fun createView(ui: AnkoContext<ViewGroup>) = with(ui) {
            linearLayout {
                id = R.id.templateLineLayout
                lparams(matchParent, wrapContent)
                horizontalPadding = dip(16)
                verticalPadding = dip(4)
                gravity = Gravity.CENTER_VERTICAL
                minimumHeight = dip(60)
                verticalLayout {
                    textView {
                        id = R.id.templateTitle
                        textSize = 16.0f
                    }
                    textView {
                        id = R.id.templateInfo
                    }
                }.lparams(0, wrapContent, 1.0f)

                textView {
                    id = R.id.templateAmount
                    textSize = 16.0f
                }
            }
        }
    }
}