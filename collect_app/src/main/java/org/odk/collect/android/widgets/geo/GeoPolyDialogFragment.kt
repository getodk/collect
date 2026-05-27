package org.odk.collect.android.widgets.geo

import androidx.activity.ComponentDialog
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.map
import androidx.lifecycle.viewmodel.viewModelFactory
import org.javarosa.core.model.Constants
import org.javarosa.core.model.data.GeoShapeData
import org.javarosa.core.model.data.GeoTraceData
import org.javarosa.core.model.data.IAnswerData
import org.javarosa.form.api.FormEntryPrompt
import org.odk.collect.android.javarosawrapper.FailedValidationResult
import org.odk.collect.android.utilities.FormEntryPromptUtils
import org.odk.collect.android.widgets.interfaces.SelectChoiceLoader
import org.odk.collect.android.widgets.utilities.AdditionalAttributes
import org.odk.collect.android.widgets.utilities.BindAttributes
import org.odk.collect.android.widgets.utilities.WidgetAnswerDialogFragment
import org.odk.collect.androidshared.ui.DisplayString
import org.odk.collect.async.Scheduler
import org.odk.collect.geo.GeoUtils.toMapPoint
import org.odk.collect.geo.geopoly.GeoPolyFragment

class GeoPolyDialogFragment(
    viewModelFactory: ViewModelProvider.Factory,
    private val scheduler: Scheduler
) :
    WidgetAnswerDialogFragment<GeoPolyFragment>(
        GeoPolyFragment::class,
        viewModelFactory
    ) {

    override fun onCreateFragment(
        prompt: FormEntryPrompt,
        selectChoiceLoader: SelectChoiceLoader
    ): GeoPolyFragment {
        val outputMode = when (prompt.dataType) {
            Constants.DATATYPE_GEOSHAPE -> GeoPolyFragment.OutputMode.GEOSHAPE
            Constants.DATATYPE_GEOTRACE -> GeoPolyFragment.OutputMode.GEOTRACE
            else -> throw IllegalArgumentException()
        }

        childFragmentManager.setFragmentResultListener(
            GeoPolyFragment.REQUEST_GEOPOLY,
            this
        ) { _, result ->
            val geopolyChange = result.getString(GeoPolyFragment.RESULT_GEOPOLY_CHANGE)
            val geopoly = result.getString(GeoPolyFragment.RESULT_GEOPOLY)

            if (geopolyChange != null) {
                val incremental = FormEntryPromptUtils.getAdditionalAttribute(
                    prompt,
                    AdditionalAttributes.INCREMENTAL
                )
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
            FormEntryPromptUtils.getBindAttribute(prompt, BindAttributes.ALLOW_MOCK_ACCURACY)
                .toBoolean()

        val inputPolygon = when (val answer = prompt.answerValue) {
            is GeoTraceData -> answer.points.map { it.toMapPoint() }
            is GeoShapeData -> answer.points.map { it.toMapPoint() }
            null -> emptyList()
            else -> throw IllegalArgumentException()
        }

        val referenceGeometryMappableDate by viewModels<ReferenceGeometryMappableDate> {
            viewModelFactory {
                addInitializer(ReferenceGeometryMappableDate::class) {
                    ReferenceGeometryMappableDate(scheduler, prompt, selectChoiceLoader)
                }
            }
        }

        return GeoPolyFragment(
            { (requireDialog() as ComponentDialog).onBackPressedDispatcher },
            outputMode,
            prompt.isReadOnly,
            retainMockAccuracy,
            inputPolygon,
            constraintValidationResult.map {
                if (it is FailedValidationResult) {
                    if (it.customErrorMessage != null) {
                        DisplayString.Raw(it.customErrorMessage)
                    } else {
                        DisplayString.Resource(it.defaultErrorMessage)
                    }
                } else {
                    null
                }
            },
            mappableData = referenceGeometryMappableDate
        )
    }

    private fun onValidate(geoString: String, outputMode: GeoPolyFragment.OutputMode) {
        val answer = getAnswerData(geoString, outputMode)
        onValidate(answer)
    }

    private fun onAnswer(geoString: String, outputMode: GeoPolyFragment.OutputMode) {
        val answer = getAnswerData(geoString, outputMode)
        onAnswer(answer)
    }

    private fun getAnswerData(
        geoString: String,
        outputMode: GeoPolyFragment.OutputMode
    ): IAnswerData? {
        return if (geoString.isBlank()) {
            null
        } else {
            when (outputMode) {
                GeoPolyFragment.OutputMode.GEOTRACE -> GeoTraceData().also { it.value = geoString }
                GeoPolyFragment.OutputMode.GEOSHAPE -> GeoShapeData().also { it.value = geoString }
            }
        }
    }
}