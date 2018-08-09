@file:Suppress("EXPERIMENTAL_FEATURE_WARNING")

package uz.firefly.tracker.fragment

import android.animation.LayoutTransition
import android.annotation.SuppressLint
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.support.design.internal.BottomNavigationItemView
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.content.res.ResourcesCompat
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import org.jetbrains.anko.*
import org.jetbrains.anko.collections.forEachWithIndex
import org.jetbrains.anko.design.bottomNavigationView
import org.jetbrains.anko.design.floatingActionButton
import org.jetbrains.anko.sdk15.coroutines.onClick
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.support.v4.defaultSharedPreferences
import uz.firefly.tracker.MainViewModel
import uz.firefly.tracker.R
import uz.firefly.tracker.TrackerApp
import uz.firefly.tracker.util.BalanceManager
import uz.firefly.tracker.util.toRub
import uz.firefly.tracker.util.toUsd
import uz.firefly.tracker.util.usdRub
import java.math.BigDecimal
import java.math.RoundingMode

const val EMPTY_EXCHANGE_RATE = "0"

class MainFragment : BaseFragment() {

    private lateinit var contentView: MainFragmentView
    private val model by lazy { ViewModelProviders.of(activity!!).get(MainViewModel::class.java) }
    val isTablet = resources.getBoolean(R.bool.isTablet)


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        contentView = MainFragmentView()
        return contentView.createView(AnkoContext.create(ctx, this))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            if (isTablet) {
                setTabletContentFragments()
            } else {
                setCurrentPage(R.id.balance)
            }
        }
        setCurrentAccount(0)
        model.updateHistory(R.id.total_account)
        model.getLiveBase().observe(this, Observer {
            if (it != null) {
                val total = BalanceManager.calculateBalance(it)
                val balance = "${getString(R.string.balance)} ${total.toPlainString()}"
                val exchangeRate = (defaultSharedPreferences.getString(usdRub, EMPTY_EXCHANGE_RATE))
                if (exchangeRate != EMPTY_EXCHANGE_RATE) {
                    val convertBalance = when (defaultSharedPreferences.getInt(currentCurrency, R.id.rub)) {
                        R.id.rub -> total.toUsd(BigDecimal(exchangeRate).setScale(2, RoundingMode.HALF_EVEN))
                        R.id.usd -> total.toRub(BigDecimal(exchangeRate).setScale(2, RoundingMode.HALF_EVEN))
                        else -> throw RuntimeException("convert fail")
                    }
                    val convertString = "${getString(R.string.converted)} ${convertBalance.toPlainString()}"
                    contentView.exchangeBalance.text = convertString
                }
                contentView.balance.text = balance
            }
        })
    }


    fun setCurrentAccount(index: Int) {
        contentView.setCurrentAccount(index)
    }

    fun updateDataForFragments(accountId: Int) {
        model.updateHistory(accountId)
    }

    private fun setTabletContentFragments() {
        childFragmentManager.beginTransaction()
                .add(R.id.donutContent, DonutFragment())
                .add(R.id.historyContent, HistoryFragment())
                .add(R.id.dummyContent, DummyFragment())
                .commit()
    }

    private fun setContentFragment(fragment: Fragment) {
        childFragmentManager.beginTransaction()
                .replace(R.id.content, fragment)
                .commit()
    }

    override fun onStart() {
        super.onStart()
        contentView.updateExchangeRate()
        defaultSharedPreferences.registerOnSharedPreferenceChangeListener(listener)
    }

    override fun onStop() {
        super.onStop()
        defaultSharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
    }

    private val listener =
            SharedPreferences.OnSharedPreferenceChangeListener { _, _ -> contentView.updateExchangeRate() }

    fun setCurrentPage(pageId: Int) {
        when (pageId) {
            R.id.balance -> setContentFragment(DonutFragment())
            R.id.history -> setContentFragment(HistoryFragment())
            R.id.statistics -> setContentFragment(DummyFragment())
        }
    }

    fun showEditorDialog() {
        requireFragmentManager().beginTransaction()
                .replace(R.id.container, EditorFragment())
                .addToBackStack(null)
                .commit()
    }

    @SuppressLint("PrivateResource")
    fun showAccountSettings() {
        requireFragmentManager().beginTransaction()
                .setCustomAnimations(
                        R.anim.abc_grow_fade_in_from_bottom,
                        R.anim.abc_shrink_fade_out_from_bottom,
                        R.anim.abc_popup_enter,
                        R.anim.abc_popup_exit)
                .replace(R.id.container, SettingsFragment())
                .addToBackStack(null)
                .commit()
    }
}

private class MainFragmentView : AnkoComponent<MainFragment> {

    private lateinit var header: ViewGroup

    private lateinit var exchangeRate: TextView
    lateinit var balance: TextView
    lateinit var exchangeBalance: TextView
    private lateinit var currencyView: TextView

    override fun createView(ui: AnkoContext<MainFragment>) = with(ui) {
        relativeLayout {
            lparams(matchParent, matchParent)

            linearLayout {
                id = R.id.toolbar

                rightPadding = dip(12)

                gravity = Gravity.CENTER_VERTICAL
                horizontalScrollView {
                    leftPadding = dip(12)
                    clipToPadding = false
                    isFillViewport = true
                    isHorizontalScrollBarEnabled = false
                    header = linearLayout {
                        gravity = Gravity.CENTER_VERTICAL
                        showDividers = LinearLayout.SHOW_DIVIDER_MIDDLE or LinearLayout.SHOW_DIVIDER_END
                        dividerDrawable = ContextCompat.getDrawable(ctx, R.drawable.header_divider)

                        TrackerApp.sRepository.accounts.forEachWithIndex { index, account ->
                            linearLayout {
                                layoutTransition = LayoutTransition().apply {
                                    setDuration(200)
                                    setStartDelay(LayoutTransition.CHANGE_DISAPPEARING, 0)
                                    setStartDelay(LayoutTransition.CHANGE_APPEARING, 0)
                                    setStartDelay(LayoutTransition.APPEARING, 300)
                                    disableTransitionType(LayoutTransition.CHANGE_DISAPPEARING)
                                }
                                gravity = Gravity.CENTER_VERTICAL

                                val icon = imageView(account.icon) {
                                    id = R.id.icon
                                    padding = dip(4)
                                }.lparams(dip(32), dip(32))

                                setTag(R.id.icon, icon)

                                val title = textView(account.title) {
                                    id = R.id.title
                                    singleLine = true
                                    textSize = 16.5f
                                    allCaps = true
                                    typeface = ResourcesCompat.getFont(ctx, R.font.roboto_condensed_regular)
                                    textColorResource = R.color.primary
                                }.lparams(wrapContent, wrapContent)

                                setTag(R.id.title, title)
                                setTag(R.id.account, account.id)
                                onClick {
                                    owner.setCurrentAccount(index)
                                    owner.updateDataForFragments(it?.getTag(R.id.account) as Int)
                                }
                            }.lparams(wrapContent, wrapContent)
                        }
                    }.lparams(wrapContent, matchParent)
                }.lparams(weight = 1.0f, height = matchParent)

                imageView {
                    imageResource = R.drawable.ic_account_circle_black_24dp
                    padding = dip(4)

                    onClick { owner.showAccountSettings() }
                }.lparams(dip(32), dip(32))

            }.lparams(matchParent, dip(56)) {
                alignParentTop()
            }

            verticalLayout {
                exchangeRate = textView {
                    id = R.id.title
                    typeface = ResourcesCompat.getFont(ctx, R.font.roboto_condensed_regular)
                    horizontalPadding = dip(16)
                    verticalPadding = dip(4)
                    visibility = View.GONE
                    backgroundColor = Color.parseColor("#EFEFEF")
                }.lparams(matchParent, wrapContent)

                linearLayout {
                    currencyView = textView {
                        typeface = ResourcesCompat.getFont(ctx, R.font.roboto_condensed_regular)
                        horizontalPadding = dip(16)
                        verticalPadding = dip(4)
                        textSize = 16.5f
                        text = when (ctx.defaultSharedPreferences.getInt(currentCurrency, R.id.rub)) {
                            R.id.rub -> "RUB"
                            R.id.usd -> "USD"
                            else -> throw RuntimeException("set currency text fail")
                        }
                    }
                    balance = textView {
                        id = R.id.currentRubBalance
                        typeface = ResourcesCompat.getFont(ctx, R.font.roboto_condensed_regular)
                        horizontalPadding = dip(4)
                        verticalPadding = dip(4)
                        textSize = 16.5f
                    }

                    exchangeBalance = textView {
                        id = R.id.currentUsdBalance
                        typeface = ResourcesCompat.getFont(ctx, R.font.roboto_condensed_regular)
                        horizontalPadding = dip(8)
                        verticalPadding = dip(4)
                        textSize = 16.5f
                    }
                }.lparams(wrapContent, wrapContent)

                frameLayout {

                    if (owner.isTablet) {
                        frameLayout {
                            id = R.id.donutContent
                        }.lparams(matchParent, 0)
                        frameLayout {
                            id = R.id.historyContent
                        }.lparams(matchParent, 0)
                        frameLayout {
                            id = R.id.dummyContent
                        }.lparams(matchParent, 0)
                    } else {
                        frameLayout {
                            id = R.id.content

                        }.lparams(matchParent, matchParent)
                    }

                    floatingActionButton {
                        val color = ContextCompat.getColor(ctx, R.color.primary)
                        imageResource = R.drawable.ic_add_black_24dp
                        backgroundTintList = ColorStateList.valueOf(color)

                        onClick { owner.showEditorDialog() }
                    }.lparams(wrapContent, wrapContent) {
                        gravity = Gravity.END or Gravity.BOTTOM
                        margin = dip(16)
                    }
                }.lparams(width = matchParent, weight = 1.0f)

                view {
                    backgroundResource = R.drawable.bottom_shadow
                }.lparams(matchParent, dip(2))
                if (!owner.isTablet) {
                    bottomNavigationView {
                        inflateMenu(R.menu.navigation)
                        setOnNavigationItemSelectedListener {
                            owner.setCurrentPage(it.itemId)
                            true
                        }
                        val typeface = ResourcesCompat.getFont(ctx, R.font.roboto_condensed_regular)
                        (getChildAt(0) as ViewGroup).forEachChild { child ->
                            (child as BottomNavigationItemView).apply {
                                find<TextView>(R.id.smallLabel).typeface = typeface
                                find<TextView>(R.id.largeLabel).typeface = typeface
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

        }
    }

    fun updateExchangeRate() {
        val rate = exchangeRate.context.defaultSharedPreferences.getString(usdRub, "")
        if (rate.isNotEmpty()) {
            exchangeRate.visibility = View.VISIBLE
            exchangeRate.text = "1 USD = ${BigDecimal(rate).setScale(2, RoundingMode.HALF_EVEN)} RUB"
        }
    }

    fun setCurrentAccount(id: Int) = header.forEachChildWithIndex { index, child ->
        val icon = child.getTag(R.id.icon) as ImageView
        val title = child.getTag(R.id.title) as TextView
        if (index == id) {
            icon.setColorFilter(ContextCompat.getColor(child.context, R.color.primary))
            title.visibility = View.VISIBLE
        } else {
            icon.setColorFilter(ContextCompat.getColor(child.context, R.color.secondary))
            title.visibility = View.GONE
        }
    }

}