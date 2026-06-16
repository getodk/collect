package org.odk.collect.android.widgets.geo

import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.viewModelFactory
import org.javarosa.core.model.data.GeoPointData
import org.javarosa.form.api.FormEntryPrompt
import org.odk.collect.android.utilities.Appearances
import org.odk.collect.android.utilities.FormEntryPromptUtils
import org.odk.collect.android.widgets.interfaces.SelectChoiceLoader
import org.odk.collect.android.widgets.utilities.BindAttributes
import org.odk.collect.android.widgets.utilities.WidgetAnswerDialogFragment
import org.odk.collect.async.Scheduler
import org.odk.collect.geo.GeoUtils.parseGeometryPoint
import org.odk.collect.geo.GeoUtils.toMapPoint
import org.odk.collect.geo.geopoint.GeoPointMapFragment

class GeoPointMapDialogFragment(
    viewModelFactory: ViewModelProvider.Factory,
    private val scheduler: Scheduler
) :
    WidgetAnswerDialogFragment<GeoPointMapFragment>(
        GeoPointMapFragment::class,
        viewModelFactory
    ) {

    override fun onCreateFragment(
        prompt: FormEntryPrompt,
        selectChoiceLoader: SelectChoiceLoader
    ): GeoPointMapFragment {
        childFragmentManager.setFragmentResultListener(
            GeoPointMapFragment.REQUEST_GEOPOINT,
            this
        ) { _, result ->
            val geoPoint = result.getString(GeoPointMapFragment.RESULT_GEOPOINT)

            if (geoPoint != null) {
                onAnswer(geoPoint)
            } else {
                dismiss()
            }
        }

        val retainMockAccuracy =
            FormEntryPromptUtils.getBindAttribute(prompt, BindAttributes.ALLOW_MOCK_ACCURACY)
                .toBoolean()

        val inputPoint = when (val answer = prompt.answerValue) {
            is GeoPointData -> answer.toMapPoint()
            null -> null
            else -> throw IllegalArgumentException()
        }

        val referenceGeometryMappableData by viewModels<ReferenceGeometryMappableData> {
            viewModelFactory {
                addInitializer(ReferenceGeometryMappableData::class) {
                    ReferenceGeometryMappableData(scheduler, prompt, selectChoiceLoader)
                }
            }
        }

        val draggable = Appearances.hasAppearance(prompt, Appearances.PLACEMENT_MAP)
        return GeoPointMapFragment(
            inputPoint,
            draggable,
            prompt.isReadOnly,
            retainMockAccuracy,
            referenceGeometryMappableData
        )
    }

    private fun onAnswer(geoString: String) {
        val answer = if (geoString.isBlank()) {
            null
        } else {
            GeoPointData(parseGeometryPoint(geoString))
        }

        onAnswer(answer)
    }
}