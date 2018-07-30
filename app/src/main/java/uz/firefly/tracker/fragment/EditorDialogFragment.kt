package uz.firefly.tracker.fragment

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
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Spinner
import org.jetbrains.anko.*
import org.jetbrains.anko.appcompat.v7.themedToolbar
import org.jetbrains.anko.design.textInputEditText
import org.jetbrains.anko.design.textInputLayout
import org.jetbrains.anko.sdk15.coroutines.textChangedListener
import org.jetbrains.anko.support.v4.ctx
import uz.firefly.tracker.R
import uz.firefly.tracker.util.*
import java.math.BigDecimal
import java.util.*

class EditorFragment : BaseFragment() {

    private lateinit var contentView: EditorDialogFragmentView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        contentView = EditorDialogFragmentView()
        return contentView.createView(AnkoContext.create(ctx, this))
    }

    fun createOperation(type: Entry.Type, amount: BigDecimal, currency: String, categoryId: Int, accountId: Int) {
        val entry = Entry(type, amount, Currency.getInstance(currency), categoryId, accountId)
        Repository.operations.add(entry)
    }

}

private class EditorDialogFragmentView : AnkoComponent<EditorFragment> {

    lateinit var accountSpinner: Spinner
    lateinit var categorySpinner: Spinner
    lateinit var currencySpinner: Spinner

    lateinit var typeView: RadioGroup
    lateinit var amountView: EditText

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
                                typeView.checkedRadioButtonId == R.id.incomes -> Entry.Type.INCOME
                                else -> Entry.Type.EXPENSE
                            }
                            val amount = BigDecimal(amountView.text.toString())
                            val currency = currencySpinner.selectedItem.toString()
                            val categoryId = (categorySpinner.selectedItem as CategoryHolder).value.id
                            val accountId = accountSpinner.selectedItemPosition - 1
                            owner.createOperation(type, amount, currency, categoryId, accountId)
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


                    headerTextView(R.string.currency)

                    currencySpinner = spinner {
                        adapter = object : ArrayAdapter<String>(ctx, android.R.layout.simple_list_item_1,
                                android.R.id.text1, arrayOf(usd, rub)) {
                            override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
                                return super.getView(position, convertView, parent).also {
                                    it.setPadding(padding, padding, padding, padding)
                                }
                            }
                        }
                    }.lparams(matchParent, wrapContent)

                    headerTextView(R.string.category)

                    categorySpinner = spinner().lparams(matchParent, wrapContent)

                    headerTextView(R.string.account)

                    accountSpinner = spinner {
                        adapter = object : ArrayAdapter<String>(ctx, android.R.layout.simple_list_item_1,
                                android.R.id.text1, Repository.accounts
                                .slice(1 until Repository.accounts.size)
                                .map { ctx.getString(it.title) }) {
                            override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
                                return super.getView(position, convertView, parent).also {
                                    it.setPadding(padding, padding, padding, padding)
                                }
                            }
                        }
                    }.lparams(matchParent, wrapContent)
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
            R.id.incomes -> Repository.incomesCategories
            R.id.expenses -> Repository.expensesCategories
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

}