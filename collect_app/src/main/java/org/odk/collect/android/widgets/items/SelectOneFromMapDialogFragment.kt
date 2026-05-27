package org.odk.collect.android.widgets.items

import android.content.Context
import androidx.activity.ComponentDialog
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.viewModelFactory
import org.javarosa.core.model.data.SelectOneData
import org.javarosa.form.api.FormEntryPrompt
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.utilities.Appearances
import org.odk.collect.android.widgets.interfaces.SelectChoiceLoader
import org.odk.collect.android.widgets.utilities.WidgetAnswerDialogFragment
import org.odk.collect.async.Scheduler
import org.odk.collect.geo.selection.SelectionMapFragment
import org.odk.collect.geo.selection.SelectionMapFragment.Companion.REQUEST_SELECT_ITEM
import javax.inject.Inject
import kotlin.getValue

class SelectOneFromMapDialogFragment(viewModelFactory: ViewModelProvider.Factory) :
    WidgetAnswerDialogFragment<SelectionMapFragment>(
        SelectionMapFragment::class,
        viewModelFactory
    ) {

    @Inject
    lateinit var scheduler: Scheduler

    override fun onAttach(context: Context) {
        super.onAttach(context)
        DaggerUtils.getComponent(context).inject(this)
    }

    override fun onCreateFragment(
        prompt: FormEntryPrompt,
        selectChoiceLoader: SelectChoiceLoader
    ): SelectionMapFragment {
        childFragmentManager.setFragmentResultListener(REQUEST_SELECT_ITEM, this) { _, result ->
            val selectedIndex = result.getLong(SelectionMapFragment.RESULT_SELECTED_ITEM).toInt()
            val selectedChoice = prompt.selectChoices[selectedIndex]
            onAnswer(SelectOneData(selectedChoice.selection()))
        }

        val selectedIndex = requireArguments().getSerializable(ARG_SELECTED_INDEX) as Int?
        val selectOnFromMapData by viewModels<SelectOneFromMapData> {
            viewModelFactory {
                addInitializer(SelectOneFromMapData::class) {
                    SelectOneFromMapData(
                        this@SelectOneFromMapDialogFragment.resources,
                        scheduler,
                        prompt,
                        selectChoiceLoader,
                        selectedIndex
                    )
                }
            }
        }

        return SelectionMapFragment(
            selectOnFromMapData,
            skipSummary = Appearances.hasAppearance(prompt, Appearances.QUICK),
            zoomToFitItems = false,
            showNewItemButton = false,
            onBackPressedDispatcher = { (requireDialog() as ComponentDialog).onBackPressedDispatcher }
        )
    }

    companion object {
        const val ARG_SELECTED_INDEX = "selected_index"
    }
}

