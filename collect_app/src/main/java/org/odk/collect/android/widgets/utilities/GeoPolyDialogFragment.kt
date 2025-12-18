package org.odk.collect.android.widgets.utilities

import androidx.activity.ComponentDialog
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.map
import org.javarosa.core.model.Constants
import org.javarosa.core.model.data.StringData
import org.javarosa.form.api.FormEntryPrompt
import org.odk.collect.android.utilities.FormEntryPromptUtils
import org.odk.collect.android.widgets.utilities.AdditionalAttributes.INCREMENTAL
import org.odk.collect.android.widgets.utilities.BindAttributes.ALLOW_MOCK_ACCURACY
import org.odk.collect.geo.geopoly.GeoPolyFragment
import org.odk.collect.geo.geopoly.GeoPolyFragment.OutputMode
import org.odk.collect.geo.geopoly.GeoPolyUtils

class GeoPolyDialogFragment(viewModelFactory: ViewModelProvider.Factory) :
    WidgetAnswerDialogFragment<GeoPolyFragment>(
        GeoPolyFragment::class,
        viewModelFactory
    ) {

    override fun onCreateFragment(prompt: FormEntryPrompt): GeoPolyFragment {
        childFragmentManager.setFragmentResultListener(
            GeoPolyFragment.REQUEST_GEOPOLY,
            this
        ) { _, result ->
            val geopolyChange = result.getString(GeoPolyFragment.RESULT_GEOPOLY_CHANGE)
            val geopoly = result.getString(GeoPolyFragment.RESULT_GEOPOLY)
            val incremental = FormEntryPromptUtils.getAdditionalAttribute(prompt, INCREMENTAL)

            if (geopolyChange != null) {
                if (incremental == "true") {
                    onAnswer(StringData(geopolyChange), dismiss = false, validate = true)
                }
            } else if (geopoly != null) {
                onAnswer(StringData(geopoly))
            } else {
                dismiss()
            }
        }

        val outputMode = when (prompt.dataType) {
            Constants.DATATYPE_GEOSHAPE -> OutputMode.GEOSHAPE
            else -> OutputMode.GEOTRACE
        }

        val retainMockAccuracy =
            FormEntryPromptUtils.getBindAttribute(prompt, ALLOW_MOCK_ACCURACY).toBoolean()

        val answer = prompt.answerValue
        val inputPolygon = GeoPolyUtils.parseGeometry(answer?.value as String?)

        return GeoPolyFragment(
            { (requireDialog() as ComponentDialog).onBackPressedDispatcher },
            outputMode,
            prompt.isReadOnly,
            retainMockAccuracy,
            inputPolygon,
            currentIndex.map {
                val validationResult = it.second
                if (validationResult != null) {
                    validationResult.customErrorMessage
                        ?: getString(validationResult.defaultErrorMessage)
                } else {
                    null
                }
            }
        )
    }
}
