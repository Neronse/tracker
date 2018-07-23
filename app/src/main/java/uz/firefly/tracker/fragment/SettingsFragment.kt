package uz.firefly.tracker.fragment

import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import org.jetbrains.anko.*
import org.jetbrains.anko.appcompat.v7.switchCompat
import org.jetbrains.anko.appcompat.v7.themedToolbar
import org.jetbrains.anko.support.v4.act
import org.jetbrains.anko.support.v4.ctx
import uz.firefly.tracker.R

class SettingsFragment : BaseFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
            SettingsFragmentView().createView(AnkoContext.create(ctx, this))
}

internal class SettingsFragmentView : AnkoComponent<SettingsFragment> {
    override fun createView(ui: AnkoContext<SettingsFragment>) = with(ui) {
        verticalLayout {
            themedToolbar(R.style.ToolbarTheme) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    elevation = 0.0f
                    stateListAnimator = null
                    outlineProvider = null
                }
                backgroundColorResource = R.color.white
                textView(R.string.settings) {
                    textColorResource = R.color.gray800
                    textSize = 20.0f
                }.lparams {
                    gravity = Gravity.CENTER
                }
                setNavigationIcon(R.drawable.ic_arrow_back_24dp)
                setNavigationOnClickListener { owner.act.onBackPressed() }
            }.lparams(matchParent, wrapContent)

            view {
                backgroundResource = R.drawable.dropshadow
            }.lparams(matchParent, dip(4))

            frameLayout {
                switchCompat {
                    text = "Я ненавижу котиков"
                    isChecked = true
                    setOnCheckedChangeListener { _, checked -> if (!checked) postDelayed({ isChecked = true }, 500) }
                }.lparams(gravity = Gravity.CENTER)
            }.lparams(matchParent, weight = 1.0f)

        }
    }

}