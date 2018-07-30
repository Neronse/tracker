package uz.firefly.tracker.fragment

import android.os.Build
import android.os.Bundle
import android.support.v4.content.res.ResourcesCompat
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import org.jetbrains.anko.*
import org.jetbrains.anko.appcompat.v7.switchCompat
import org.jetbrains.anko.appcompat.v7.themedToolbar
import org.jetbrains.anko.support.v4.act
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.support.v4.ctx
import uz.firefly.tracker.BuildConfig
import uz.firefly.tracker.R

class SettingsFragment : BaseFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
            SettingsFragmentView().createView(AnkoContext.create(ctx, this))

    fun showAboutDialog() {
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

                    textView(R.string.app_name) {
                        textSize = 20.0f
                        textColorResource = R.color.secondary
                        gravity = Gravity.CENTER_HORIZONTAL
                        typeface = ResourcesCompat.getFont(ctx, R.font.roboto_condensed_regular)
                    }.lparams(wrapContent, wrapContent)

                    textView(message) {
                        textSize = 18.0f
                        textColorResource = R.color.secondary
                        gravity = Gravity.CENTER_HORIZONTAL
                        typeface = ResourcesCompat.getFont(ctx, R.font.roboto_condensed_regular)
                    }.lparams(wrapContent, wrapContent)
                }

            }

        }.show()
    }

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
                textView(R.string.app_name) {
                    textColorResource = R.color.secondary
                    textSize = 20.0f
                    typeface = ResourcesCompat.getFont(ctx, R.font.roboto_condensed_regular)
                }.lparams {
                    gravity = Gravity.CENTER
                }
                setNavigationIcon(R.drawable.ic_arrow_back_24dp)
                setNavigationOnClickListener { owner.act.onBackPressed() }
                inflateMenu(R.menu.settings)
                setOnMenuItemClickListener {
                    when {
                        it.itemId == R.id.about -> {
                            owner.showAboutDialog()
                            true
                        }
                        else -> false
                    }
                }
            }.lparams(matchParent, wrapContent)

            view {
                backgroundResource = R.drawable.dropshadow
            }.lparams(matchParent, dip(2))

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