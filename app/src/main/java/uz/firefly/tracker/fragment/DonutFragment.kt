package uz.firefly.tracker.fragment

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.jetbrains.anko.*
import org.jetbrains.anko.support.v4.ctx
import uz.firefly.tracker.view.diagramView

class DonutFragment : BaseFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return DonutFragmentView().createView(AnkoContext.create(ctx, this))
    }

}


private class DonutFragmentView : AnkoComponent<DonutFragment> {

    override fun createView(ui: AnkoContext<DonutFragment>) = with(ui) {
        frameLayout {
            padding = dip(64)
            diagramView { }.lparams(matchParent, matchParent) {
                gravity = Gravity.CENTER
            }
        }
    }
}