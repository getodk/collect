package org.odk.collect.android.widgets

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.util.TypedValue
import android.view.View
import org.javarosa.core.model.data.GeoPointData
import org.javarosa.core.model.data.IAnswerData
import org.javarosa.form.api.FormEntryPrompt
import org.odk.collect.android.databinding.GeopointQuestionBinding
import org.odk.collect.android.formentry.questions.QuestionDetails
import org.odk.collect.android.utilities.Appearances
import org.odk.collect.android.widgets.interfaces.GeoDataRequester
import org.odk.collect.android.widgets.interfaces.WidgetDataReceiver
import org.odk.collect.android.widgets.utilities.GeoWidgetUtils
import org.odk.collect.android.widgets.utilities.WaitingForDataRegistry
import org.odk.collect.geo.GeoUtils

@SuppressLint("ViewConstructor")
class GeoPointWidget(
    context: Context,
    questionDetails: QuestionDetails,
    private val waitingForDataRegistry: WaitingForDataRegistry,
    private val geoDataRequester: GeoDataRequester,
    dependencies: Dependencies
) : QuestionWidget(context, dependencies, questionDetails), WidgetDataReceiver {

    lateinit var binding: GeopointQuestionBinding

    private var answerText: String? = null

    init {
        render()
    }

    override fun onCreateWidgetView(context: Context, prompt: FormEntryPrompt, answerFontSize: Int): View {
        binding = GeopointQuestionBinding.inflate((context as Activity).layoutInflater)

        binding.geoAnswerText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontSize.toFloat())
        binding.simpleButton.setOnClickListener {
            geoDataRequester.requestGeoPoint(prompt, waitingForDataRegistry)
        }

        answerText = prompt.answerText

        val answerToDisplay = GeoWidgetUtils.getGeoPointAnswerToDisplay(context, answerText)
        binding.simpleButton.setText(getButtonText(prompt, answerToDisplay))
        if (answerToDisplay.isEmpty()) {
            binding.simpleButton.visibility = if (prompt.isReadOnly) GONE else VISIBLE
            answerText = null
        } else {
            binding.geoAnswerText.text = answerToDisplay
        }
        binding.geoAnswerText.visibility = if (binding.geoAnswerText.text.toString().isBlank()) GONE else VISIBLE

        return binding.root
    }

    override fun getAnswer(): IAnswerData? {
        val parsedGeometryPoint = GeoUtils.parseGeometryPoint(answerText)
        return if (parsedGeometryPoint == null) {
            null
        } else {
            GeoPointData(parsedGeometryPoint)
        }
    }

    override fun clearAnswer() {
        answerText = null
        binding.geoAnswerText.text = null
        binding.geoAnswerText.visibility = GONE
        binding.simpleButton.setText(getButtonText(formEntryPrompt, ""))
        widgetValueChanged()
    }

    override fun setOnLongClickListener(l: OnLongClickListener?) {
        binding.simpleButton.setOnLongClickListener(l)
        binding.geoAnswerText.setOnLongClickListener(l)
    }

    override fun cancelLongPress() {
        super.cancelLongPress()
        binding.simpleButton.cancelLongPress()
        binding.geoAnswerText.cancelLongPress()
    }

    override fun setData(answer: Any) {
        val answerToDisplay = GeoWidgetUtils.getGeoPointAnswerToDisplay(context, answer.toString())
        binding.simpleButton.setText(getButtonText(formEntryPrompt, answerToDisplay))
        if (answerToDisplay.isEmpty()) {
            answerText = null
            binding.geoAnswerText.text = ""
            binding.geoAnswerText.visibility = GONE
        } else {
            answerText = answer.toString()
            binding.geoAnswerText.text = answerToDisplay
            binding.geoAnswerText.visibility = VISIBLE
        }
        widgetValueChanged()
    }

    private fun getButtonText(prompt: FormEntryPrompt, answerToDisplay: String) = when {
        answerToDisplay.isEmpty() -> org.odk.collect.strings.R.string.get_point
        prompt.isReadOnly -> org.odk.collect.strings.R.string.view_point
        Appearances.isGeoPointMapAppearance(prompt) -> org.odk.collect.strings.R.string.view_or_change_point
        else -> org.odk.collect.strings.R.string.change_point
    }
}
