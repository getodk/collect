package org.odk.collect.android.formentry.questions

import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TableLayout
import android.widget.TextView
import androidx.annotation.IdRes
import com.google.android.material.button.MaterialButton
import org.odk.collect.android.R
import org.odk.collect.android.utilities.ThemeUtils
import org.odk.collect.android.widgets.QuestionWidget
import org.odk.collect.android.widgets.interfaces.ButtonClickListener
import org.odk.collect.androidshared.ui.multiclicksafe.MultiClickGuard.allowClick

object WidgetViewUtils {
    @JvmStatic
    fun createAnswerTextView(context: Context, text: CharSequence?, answerFontSize: Int): TextView {
        return TextView(context).apply {
            id = R.id.answer_text
            setTextColor(ThemeUtils(context).colorOnSurface)
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontSize.toFloat())
            setPadding(20, 20, 20, 20)
            setText(text)
        }
    }

    @JvmStatic
    fun createAnswerImageView(context: Context): ImageView {
        return ImageView(context).apply {
            id = View.generateViewId()
            tag = "ImageView"
            setPadding(10, 10, 10, 10)
            adjustViewBounds = true
        }
    }

    @JvmStatic
    @JvmOverloads
    fun createSimpleButton(context: Context, readOnly: Boolean, text: String?, listener: ButtonClickListener, addMargin: Boolean, @IdRes withId: Int = R.id.simple_button): Button {
        val button = LayoutInflater
            .from(context)
            .inflate(R.layout.widget_answer_button, null, false) as MaterialButton
        if (readOnly) {
            button.visibility = View.GONE
        } else {
            button.id = withId
            button.text = text
            button.contentDescription = text
            if (addMargin) {
                val params = TableLayout.LayoutParams()
                val marginStandard = context.resources.getDimension(org.odk.collect.androidshared.R.dimen.margin_standard).toInt()
                params.setMargins(0, marginStandard, 0, 0)
                button.layoutParams = params
            }
            button.setOnClickListener { v: View? ->
                if (allowClick(QuestionWidget::class.java.name)) {
                    listener.onButtonClick(withId)
                }
            }
        }
        return button
    }
}
