package org.odk.collect.android.widgets

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.util.TypedValue
import android.view.View
import org.javarosa.core.model.data.IAnswerData
import org.javarosa.form.api.FormEntryPrompt
import org.odk.collect.android.databinding.GeotraceQuestionBinding
import org.odk.collect.android.formentry.questions.QuestionDetails
import org.odk.collect.android.widgets.interfaces.GeoDataRequester
import org.odk.collect.android.widgets.utilities.GeoWidgetUtils

@SuppressLint("ViewConstructor")
class GeoTraceWidget(
    context: Context,
    questionDetails: QuestionDetails,
    private val geoDataRequester: GeoDataRequester,
    dependencies: Dependencies
) : QuestionWidget(context, dependencies, questionDetails) {

    lateinit var binding: GeotraceQuestionBinding

    init {
        render()
    }

    override fun onCreateWidgetView(context: Context, prompt: FormEntryPrompt, answerFontSize: Int): View {
        binding = GeotraceQuestionBinding.inflate((context as Activity).layoutInflater)

        binding.geoAnswerText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontSize.toFloat())

        binding.simpleButton.setOnClickListener {
            geoDataRequester.requestGeoPoly(prompt)
        }

        val stringAnswer = GeoWidgetUtils.getGeoPolyAnswerToDisplay(prompt.answerText)
        binding.geoAnswerText.text = stringAnswer
        binding.geoAnswerText.visibility = if (binding.geoAnswerText.text.toString().isBlank()) GONE else VISIBLE

        val dataAvailable = !stringAnswer.isNullOrEmpty()

        if (formEntryPrompt.isReadOnly) {
            if (dataAvailable) {
                binding.simpleButton.setText(org.odk.collect.strings.R.string.view_line)
            } else {
                binding.simpleButton.visibility = GONE
            }
        } else {
            if (dataAvailable) {
                binding.simpleButton.setText(org.odk.collect.strings.R.string.view_or_change_line)
            } else {
                binding.simpleButton.setText(org.odk.collect.strings.R.string.get_line)
            }
        }

        return binding.root
    }

    override fun getAnswer(): IAnswerData? = formEntryPrompt.answerValue

    override fun clearAnswer() = Unit

    override fun setOnLongClickListener(l: OnLongClickListener?) {
        binding.simpleButton.setOnLongClickListener(l)
        binding.geoAnswerText.setOnLongClickListener(l)
    }

    override fun cancelLongPress() {
        super.cancelLongPress()
        binding.simpleButton.cancelLongPress()
        binding.geoAnswerText.cancelLongPress()
    }
}
