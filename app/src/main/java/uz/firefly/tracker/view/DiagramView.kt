package uz.firefly.tracker.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.ViewManager
import org.jetbrains.anko.AnkoViewDslMarker
import org.jetbrains.anko.custom.ankoView

class DiagramView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    val paint = Paint().also {
        it.isAntiAlias = true
        it.strokeWidth = 6.0f
    }

    var rect = RectF(0.0f, 0.0f, 0.0f, 0.0f)

    val circlePaint = Paint().also {
        it.isAntiAlias = true
        it.color = Color.WHITE
    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        // TODO !!!

        canvas?.let {

            rect.right = width.toFloat()
            rect.bottom = height.toFloat()


            paint.color = Color.BLUE
            canvas.drawArc(rect, 0.0f, 45.0f, true, paint)

            paint.color = Color.RED
            canvas.drawArc(rect, 47.0f, 81.0f, true, paint)

            paint.color = Color.GRAY
            canvas.drawArc(rect, 132.0f, 226.0f, true, paint)

            canvas.drawCircle(width / 2.0f, height / 2.0f, width / 2.0f - 6.0f, circlePaint)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec)
    }

}

inline fun ViewManager.diagramView(): DiagramView = diagramView() {}
inline fun ViewManager.diagramView(init: (@AnkoViewDslMarker DiagramView).() -> Unit): DiagramView {
    return ankoView({ DiagramView(it) }, theme = 0) { init() }
}