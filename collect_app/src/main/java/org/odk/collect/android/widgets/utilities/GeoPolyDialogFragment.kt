package org.odk.collect.android.widgets.utilities

import androidx.activity.ComponentDialog
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.map
import org.javarosa.core.model.Constants
import org.javarosa.core.model.data.GeoShapeData
import org.javarosa.core.model.data.GeoTraceData
import org.javarosa.form.api.FormEntryPrompt
import org.odk.collect.android.javarosawrapper.FailedValidationResult
import org.odk.collect.android.utilities.FormEntryPromptUtils
import org.odk.collect.android.widgets.utilities.AdditionalAttributes.INCREMENTAL
import org.odk.collect.android.widgets.utilities.BindAttributes.ALLOW_MOCK_ACCURACY
import org.odk.collect.geo.GeoUtils.toMapPoint
import org.odk.collect.geo.geopoly.GeoPolyFragment
import org.odk.collect.geo.geopoly.GeoPolyFragment.OutputMode

class GeoPolyDialogFragment(viewModelFactory: ViewModelProvider.Factory) :
    WidgetAnswerDialogFragment<GeoPolyFragment>(
        GeoPolyFragment::class,
        viewModelFactory
    ) {

    override fun onCreateFragment(prompt: FormEntryPrompt): GeoPolyFragment {
        val outputMode = when (prompt.dataType) {
            Constants.DATATYPE_GEOSHAPE -> OutputMode.GEOSHAPE
            Constants.DATATYPE_GEOTRACE -> OutputMode.GEOTRACE
            else -> throw IllegalArgumentException()
        }

        childFragmentManager.setFragmentResultListener(
            GeoPolyFragment.REQUEST_GEOPOLY,
            this
        ) { _, result ->
            val geopolyChange = result.getString(GeoPolyFragment.RESULT_GEOPOLY_CHANGE)
            val geopoly = result.getString(GeoPolyFragment.RESULT_GEOPOLY)

            if (geopolyChange != null) {
                val incremental = FormEntryPromptUtils.getAdditionalAttribute(prompt, INCREMENTAL)
                if (incremental == "true") {
                    onValidate(geopolyChange, outputMode)
                }
            } else if (geopoly != null) {
                onAnswer(geopoly, outputMode)
            } else {
                dismiss()
            }
        }

        val retainMockAccuracy =
            FormEntryPromptUtils.getBindAttribute(prompt, ALLOW_MOCK_ACCURACY).toBoolean()

        val inputPolygon = when (val answer = prompt.answerValue) {
            is GeoTraceData -> answer.points.map { it.toMapPoint() }
            is GeoShapeData -> answer.points.map { it.toMapPoint() }
            null -> emptyList()
            else -> throw IllegalArgumentException()
        }

        return GeoPolyFragment(
            { (requireDialog() as ComponentDialog).onBackPressedDispatcher },
            outputMode,
            prompt.isReadOnly,
            retainMockAccuracy,
            inputPolygon,
            validationResult.map {
                val validationResult = it.value
                if (validationResult is FailedValidationResult) {
                    validationResult.customErrorMessage ?: getString(validationResult.defaultErrorMessage)
                } else {
                    null
                }
            }
        )
    }

    private fun onValidate(geoString: String, outputMode: OutputMode) {
        val answer = when (outputMode) {
            OutputMode.GEOTRACE -> GeoTraceData().also { it.value = geoString }
            OutputMode.GEOSHAPE -> GeoShapeData().also { it.value = geoString }
        }

        onValidate(answer)
    }

    private fun onAnswer(geoString: String, outputMode: OutputMode) {
        val answer = when (outputMode) {
            OutputMode.GEOTRACE -> GeoTraceData().also { it.value = geoString }
            OutputMode.GEOSHAPE -> GeoShapeData().also { it.value = geoString }
        }

        onAnswer(answer)
    }
}
