package org.odk.collect.android.widgets.range

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import org.javarosa.core.model.data.IAnswerData
import org.javarosa.core.model.data.IntegerData
import org.javarosa.form.api.FormEntryPrompt
import org.odk.collect.android.formentry.questions.QuestionDetails
import org.odk.collect.android.widgets.QuestionWidget
import org.odk.collect.androidshared.ui.ComposeThemeProvider.Companion.setContextThemedContent
import org.odk.collect.androidshared.ui.ToastUtils

@SuppressLint("ViewConstructor")
class RangeIntegerWidget(
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
                    onValueChanging = { shouldSuppressFlingGesture = it },
                    onValueChangeFinished = {
                        rangeSliderState = it
                        widgetValueChanged()
                    },
                    onRangeInvalid = {
                        ToastUtils.showLongToast(org.odk.collect.strings.R.string.invalid_range_widget)
                    }
                )
            }
        }
    }

    override fun getAnswer(): IAnswerData? {
        return rangeSliderState.realValue?.let {
            IntegerData(it.toInt())
        }
    }

    override fun setOnLongClickListener(l: OnLongClickListener?) = Unit

    override fun shouldSuppressFlingGesture() = shouldSuppressFlingGesture

    override fun clearAnswer() {
        rangeSliderState = rangeSliderState.copy(sliderValue = null)
        widgetValueChanged()
    }
}
