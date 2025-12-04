package org.odk.collect.android.widgets.utilities

import androidx.lifecycle.ViewModelProvider
import org.javarosa.core.model.Constants
import org.javarosa.form.api.FormEntryPrompt
import org.odk.collect.geo.geopoly.GeoPolyFragment
import org.odk.collect.geo.geopoly.GeoPolyFragment.OutputMode

class GeoPolyDialogFragment(viewModelFactory: ViewModelProvider.Factory) :
    WidgetAnswerDialogFragment<GeoPolyFragment>(
        GeoPolyFragment::class,
        viewModelFactory
    ) {

    override fun onCreateFragment(prompt: FormEntryPrompt): GeoPolyFragment {
        val outputMode = when (prompt.controlType) {
            Constants.DATATYPE_GEOSHAPE -> OutputMode.GEOSHAPE
            Constants.DATATYPE_GEOTRACE -> OutputMode.GEOTRACE
            else -> null
        }

        return GeoPolyFragment(
            outputMode,
            prompt.isReadOnly,
            false,
            null
        )
    }
}
