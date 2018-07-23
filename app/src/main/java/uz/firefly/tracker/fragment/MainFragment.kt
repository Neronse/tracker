package uz.firefly.tracker.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import org.jetbrains.anko.*
import org.jetbrains.anko.appcompat.v7.themedToolbar
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.support.v4.ctx
import uz.firefly.tracker.BuildConfig
import uz.firefly.tracker.R
import uz.firefly.tracker.util.BalanceManager
import uz.firefly.tracker.util.Entry
import uz.firefly.tracker.util.toUsd
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

class MainFragment : BaseFragment() {

    lateinit var totalRub: TextView
    lateinit var totalUsd: TextView

    lateinit var incomesRub: TextView
    lateinit var incomesUsd: TextView

    lateinit var expensesRub: TextView
    lateinit var expensesUsd: TextView

    companion object {
        val expenses = mutableListOf(Entry(Entry.Type.EXPENSE, BigDecimal(2028.23), Currency.getInstance("USD")))
        val incomes = mutableListOf(Entry(Entry.Type.INCOME, BigDecimal(4033.87), Currency.getInstance("USD")))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return MainFragmentView().createView(AnkoContext.create(ctx, this))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        incomesRub = view.findViewById(R.id.incomes_rub)
        incomesUsd = view.findViewById(R.id.incomes_usd)

        expensesRub = view.findViewById(R.id.expenses_rub)
        expensesUsd = view.findViewById(R.id.expenses_usd)

        totalRub = view.findViewById(R.id.total_rub)
        totalUsd = view.findViewById(R.id.total_usd)
    }

    override fun onStart() {
        super.onStart()
        update()
    }

    fun BigDecimal.format(): String {
        val symbols = DecimalFormatSymbols(Locale.getDefault()).apply {
            groupingSeparator = ' '
            decimalSeparator = '.'
        }
        return DecimalFormat("###,###.00", symbols).format(this)
    }

    fun update() {
        val exp = BalanceManager.total(expenses).abs()
        val inc = BalanceManager.total(incomes)

        val bal = inc.minus(exp)


        incomesUsd.text = getString(R.string.usd_format, inc.toUsd().setScale(2, RoundingMode.HALF_EVEN).format())
        incomesRub.text = getString(R.string.rub_format, inc.setScale(2, RoundingMode.HALF_EVEN).format())

        expensesUsd.text = getString(R.string.usd_format, exp.toUsd().setScale(2, RoundingMode.HALF_EVEN).format())
        expensesRub.text = getString(R.string.rub_format, exp.setScale(2, RoundingMode.HALF_EVEN).format())

        totalUsd.text = getString(R.string.usd_format, bal.toUsd().setScale(2, RoundingMode.HALF_EVEN).format())
        totalRub.text = getString(R.string.rub_format, bal.setScale(2, RoundingMode.HALF_EVEN).format())
    }

    fun showAbout() {
        val version = getString(R.string.version)
        val buildNumber = getString(R.string.build)
        val message = """
            $version: ${BuildConfig.VERSION_NAME}
            $buildNumber: ${BuildConfig.VERSION_CODE}
        """.trimIndent()
        alert {
            customView {
                verticalLayout {
                    lparams(wrapContent, wrapContent)
                    padding = dip(16)
                    gravity = Gravity.CENTER_HORIZONTAL
                    imageView(R.mipmap.ic_launcher_round) {

                    }

                    textView(R.string.app_name) {
                        textSize = 20.0f
                        textColorResource = R.color.gray900
                        gravity = Gravity.CENTER_HORIZONTAL
                    }.lparams(wrapContent, wrapContent)

                    textView(message) {
                        textSize = 18.0f
                        textColorResource = R.color.gray800
                        gravity = Gravity.CENTER_HORIZONTAL
                    }.lparams(wrapContent, wrapContent)
                }

            }

        }.show()
    }

    fun showSettings() {
        fragmentManager
                ?.beginTransaction()
                ?.replace(R.id.container, SettingsFragment())
                ?.addToBackStack(null)
                ?.commit()
    }

}

internal class MainFragmentView : AnkoComponent<MainFragment> {

    override fun createView(ui: AnkoContext<MainFragment>) = with(ui) {
        verticalLayout {

            val shadowColor = Color.argb(0x80, 0, 0, 0)

            themedToolbar(R.style.ToolbarTheme) {
                backgroundColorResource = R.color.white

                textView(R.string.app_name) {
                    textSize = 20.0f
                    textColorResource = R.color.gray800
                }.lparams {
                    gravity = Gravity.CENTER_HORIZONTAL
                }

                inflateMenu(R.menu.main)
                setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.about -> {
                            owner.showAbout()
                            true
                        }
                        R.id.settings -> {
                            owner.showSettings()
                            true
                        }
                        else -> false
                    }
                }
            }

            verticalLayout {
                backgroundColorResource = R.color.lightRed
                padding = dip(16)

                textView(R.string.expenses) {
                    allCaps = true
                    textColorResource = R.color.gray50
                    textSize = 16.0f
                }
                textView("$2028.23") {
                    id = R.id.expenses_usd
                    textColorResource = R.color.white
                    textSize = 28.0f
                    setShadowLayer(1.0f, 1.0f, 1.0f, shadowColor)
                }

                textView("128 735,81 RUB") {
                    id = R.id.expenses_rub
                    textColorResource = R.color.white
                    textSize = 24.0f
                    setShadowLayer(1.0f, 1.0f, 1.0f, shadowColor)
                }

            }.lparams(weight = 0.7f, width = matchParent)

            verticalLayout {
                backgroundColorResource = R.color.lightBlue
                padding = dip(16)

                textView(R.string.incomes) {
                    allCaps = true
                    textColorResource = R.color.gray50
                    textSize = 16.0f
                }
                textView("$4033.87") {
                    id = R.id.incomes_usd
                    textColorResource = R.color.white
                    textSize = 28.0f
                    setShadowLayer(1.0f, 1.0f, 1.0f, shadowColor)
                }

                textView("256 037,80 RUB") {
                    id = R.id.incomes_rub
                    textColorResource = R.color.white
                    textSize = 24.0f
                    setShadowLayer(1.0f, 1.0f, 1.0f, shadowColor)
                }

            }.lparams(weight = 0.7f, width = matchParent)

            verticalLayout {
                padding = dip(16)

                textView(R.string.balance) {
                    allCaps = true
                    textSize = 16.0f
                    textColorResource = R.color.gray900

                }

                textView("$2005.64") {
                    id = R.id.total_usd
                    textColorResource = R.color.black
                    textSize = 28.0f
                }

                textView("127 301,99 RUB") {
                    id = R.id.total_rub
                    textColorResource = R.color.black
                    textSize = 24.0f
                }

            }.lparams(weight = 0.7f, width = matchParent)

            linearLayout {
                frameLayout {
                    backgroundColorResource = R.color.red

                    view {
                        backgroundColorResource = R.color.white
                    }.lparams(dip(56), dip(2), gravity = Gravity.CENTER)

                    setOnClickListener {
                        MainFragment.expenses.add(Entry(Entry.Type.EXPENSE,
                                BigDecimal(Math.random() * 1000.0f),
                                Currency.getInstance("USD"))
                        )
                        owner.update()
                    }
                }.lparams(weight = 1.0f, height = matchParent, width = 0)

                frameLayout {
                    backgroundColorResource = R.color.blue

                    view {
                        backgroundColorResource = R.color.white
                    }.lparams(dip(2), dip(56), gravity = Gravity.CENTER)

                    view {
                        backgroundColorResource = R.color.white
                    }.lparams(dip(56), dip(2), gravity = Gravity.CENTER)

                    setOnClickListener {
                        MainFragment.incomes.add(Entry(Entry.Type.INCOME,
                                BigDecimal(Math.random() * 1000.0f),
                                Currency.getInstance("USD"))
                        )
                        owner.update()
                    }
                }.lparams(weight = 1.0f, height = matchParent, width = 0)
            }.lparams(weight = 1.2f, width = matchParent)
        }
    }

}