package org.odk.collect.android.widgets.range

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import org.javarosa.core.model.data.DecimalData
import org.javarosa.core.model.data.IAnswerData
import org.javarosa.form.api.FormEntryPrompt
import org.odk.collect.android.formentry.questions.QuestionDetails
import org.odk.collect.android.widgets.QuestionWidget
import org.odk.collect.androidshared.ui.ComposeThemeProvider.Companion.setContextThemedContent

@SuppressLint("ViewConstructor")
class RangeDecimalWidget(
    context: Context,
    prompt: QuestionDetails,
    dependencies: Dependencies
) : QuestionWidget(
        context,
        dependencies,
        prompt
    ) {
    private var rangeSliderState by mutableStateOf(RangeSliderState.fromPrompt(formEntryPrompt))
    private var shouldSuppressFlingGesture = false

    init {
        render()
    }

    override fun onCreateWidgetView(context: Context, prompt: FormEntryPrompt, answerFontSize: Int): View {
        return ComposeView(context).apply {
            setContextThemedContent {
                RangeSlider(
                    rangeSliderState,
                    onValueChange = {
                        rangeSliderState = rangeSliderState.copy(sliderValue = it)
                    },
                    onValueChangeFinished = {
                        widgetValueChanged()
                    },
                    onValueChanging = {
                        shouldSuppressFlingGesture = it
                    }
                )
            }
        }
    }

    override fun getAnswer(): IAnswerData? {
        return rangeSliderState.realValue?.let {
            DecimalData(it.toDouble())
        }
    }

    override fun setOnLongClickListener(l: OnLongClickListener?) = Unit

    override fun shouldSuppressFlingGesture() = shouldSuppressFlingGesture

    override fun clearAnswer() {
        rangeSliderState = rangeSliderState.copy(sliderValue = null)
        widgetValueChanged()
    }
}
