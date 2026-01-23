package org.odk.collect.android.widgets.range

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.widget.TextView
import com.google.android.material.slider.Slider
import org.javarosa.core.model.data.IAnswerData
import org.javarosa.core.model.data.IntegerData
import org.javarosa.form.api.FormEntryPrompt
import org.odk.collect.android.formentry.questions.QuestionDetails
import org.odk.collect.android.views.TrackingTouchSlider
import org.odk.collect.android.widgets.QuestionWidget
import org.odk.collect.android.widgets.utilities.RangeWidgetUtils
import java.math.BigDecimal
import java.util.Locale

@SuppressLint("ViewConstructor")
class RangeIntegerWidget(
    context: Context,
    prompt: QuestionDetails,
    dependencies: Dependencies
) : QuestionWidget(
        context,
        dependencies,
        prompt
    ), Slider.OnChangeListener {
    lateinit var slider: TrackingTouchSlider
    lateinit var currentValue: TextView

    init {
        render()
    }

    override fun onCreateWidgetView(
        context: Context,
        prompt: FormEntryPrompt,
        answerFontSize: Int
    ): View {
        val layoutElements = RangeWidgetUtils.setUpLayoutElements(context, prompt)
        slider = layoutElements.slider
        currentValue = layoutElements.currentValue

        setUpActualValueLabel(RangeWidgetUtils.setUpSlider(prompt, slider, true))

        if (slider.isEnabled) {
            slider.setListener(this)
        }
        return layoutElements.answerView
    }

    override fun getAnswer(): IAnswerData? {
        val stringAnswer = currentValue.getText().toString()
        return if (stringAnswer.isEmpty()) null else IntegerData(stringAnswer.toInt())
    }

    override fun setOnLongClickListener(l: OnLongClickListener?) = Unit

    override fun shouldSuppressFlingGesture() = slider.isTrackingTouch

    override fun clearAnswer() {
        setUpActualValueLabel(null)
        widgetValueChanged()
    }

    @SuppressLint("RestrictedApi")
    override fun onValueChange(slider: Slider, value: Float, fromUser: Boolean) {
        if (fromUser) {
            val actualValue = RangeWidgetUtils.getActualValue(formEntryPrompt, value)
            setUpActualValueLabel(actualValue)
            widgetValueChanged()
        }
    }

    private fun setUpActualValueLabel(actualValue: BigDecimal?) {
        if (actualValue != null) {
            currentValue.text = String.format(Locale.getDefault(), "%d", actualValue.toInt())
        } else {
            currentValue.text = ""
            slider.reset()
        }
    }
}
