package org.odk.collect.android.widgets.utilities

import androidx.lifecycle.ViewModelProvider
import org.javarosa.core.model.Constants
import org.javarosa.core.model.data.StringData
import org.javarosa.form.api.FormEntryPrompt
import org.odk.collect.android.utilities.FormEntryPromptUtils
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
            val result = result.getString(GeoPolyFragment.RESULT_GEOTRACE)
            if (result != null) {
                onAnswer(StringData(result))
            } else {
                dismiss()
            }
        }

        val outputMode = when (prompt.dataType) {
            Constants.DATATYPE_GEOSHAPE -> OutputMode.GEOSHAPE
            Constants.DATATYPE_GEOTRACE -> OutputMode.GEOTRACE
            else -> null
        }

        val retainMockAccuracy =
            FormEntryPromptUtils.getBindAttribute(prompt, "allow-mock-accuracy").toBoolean()

        val answer = prompt.answerValue
        val inputPolygon = if (answer != null) {
            GeoPolyUtils.parseGeometry(answer.value as String)
        } else {
            null
        }

        return GeoPolyFragment(
            outputMode,
            prompt.isReadOnly,
            retainMockAccuracy,
            inputPolygon
        )
    }
}
