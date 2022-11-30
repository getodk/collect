package org.odk.collect.android.widgets.items

import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentDialog
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.FragmentResultListener
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.javarosa.core.model.FormIndex
import org.javarosa.core.model.SelectChoice
import org.javarosa.core.model.data.SelectOneData
import org.javarosa.core.model.instance.geojson.GeojsonFeature
import org.javarosa.form.api.FormEntryPrompt
import org.odk.collect.android.R
import org.odk.collect.android.databinding.SelectOneFromMapDialogLayoutBinding
import org.odk.collect.android.formentry.FormEntryViewModel
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.utilities.Appearances
import org.odk.collect.androidshared.livedata.MutableNonNullLiveData
import org.odk.collect.androidshared.livedata.NonNullLiveData
import org.odk.collect.androidshared.ui.FragmentFactoryBuilder
import org.odk.collect.async.Scheduler
import org.odk.collect.geo.selection.MappableSelectItem
import org.odk.collect.geo.selection.SelectionMapData
import org.odk.collect.geo.selection.SelectionMapFragment
import org.odk.collect.geo.selection.SelectionMapFragment.Companion.REQUEST_SELECT_ITEM
import org.odk.collect.material.MaterialFullScreenDialogFragment
import javax.inject.Inject
import kotlin.math.absoluteValue

class SelectOneFromMapDialogFragment : MaterialFullScreenDialogFragment(), FragmentResultListener {

    @Inject
    lateinit var scheduler: Scheduler

    @Inject
    lateinit var formEntryViewModelFactory: FormEntryViewModel.Factory
    private val formEntryViewModel: FormEntryViewModel by activityViewModels { formEntryViewModelFactory }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        DaggerUtils.getComponent(context).inject(this)

        val formIndex = requireArguments().getSerializable(ARG_FORM_INDEX) as FormIndex
        val selectedIndex = requireArguments().getSerializable(ARG_SELECTED_INDEX) as Int?
        val prompt = formEntryViewModel.getQuestionPrompt(formIndex)
        val selectionMapData = SelectChoicesMapData(resources, scheduler, prompt, selectedIndex)

        childFragmentManager.fragmentFactory = FragmentFactoryBuilder()
            .forClass(SelectionMapFragment::class.java) {
                SelectionMapFragment(
                    selectionMapData,
                    skipSummary = Appearances.hasAppearance(prompt, Appearances.QUICK),
                    zoomToFitItems = false,
                    showNewItemButton = false,
                    onBackPressedDispatcher = { (requireDialog() as ComponentDialog).onBackPressedDispatcher }
                )
            }
            .build()

        childFragmentManager.setFragmentResultListener(REQUEST_SELECT_ITEM, this, this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val binding = SelectOneFromMapDialogLayoutBinding.inflate(inflater)
        return binding.root
    }

    override fun getToolbar(): Toolbar? {
        return null
    }

    override fun onBackPressed() {
        dismiss()
    }

    override fun onCloseClicked() {
        // No toolbar so not relevant
    }

    override fun onFragmentResult(requestKey: String, result: Bundle) {
        val selectedIndex = result.getLong(SelectionMapFragment.RESULT_SELECTED_ITEM).toInt()
        val formIndex = requireArguments().getSerializable(ARG_FORM_INDEX) as FormIndex
        val prompt = formEntryViewModel.getQuestionPrompt(formIndex)
        val selectedChoice = prompt.selectChoices[selectedIndex]
        formEntryViewModel.answerQuestion(formIndex, SelectOneData(selectedChoice.selection()))
        dismiss()
    }

    companion object {
        const val ARG_FORM_INDEX = "form_index"
        const val ARG_SELECTED_INDEX = "selected_index"
    }
}

internal class SelectChoicesMapData(
    private val resources: Resources,
    scheduler: Scheduler,
    prompt: FormEntryPrompt,
    private val selectedIndex: Int?,
) : SelectionMapData {

    private val mapTitle = MutableLiveData(prompt.longText)
    private val itemCount = MutableNonNullLiveData(0)
    private val items = MutableLiveData<List<MappableSelectItem>?>(null)
    private val isLoading = MutableNonNullLiveData(true)

    init {
        isLoading.value = true

        scheduler.immediate(
            background = {
                loadItemsFromChoices(prompt.selectChoices, prompt)
            },
            foreground = {
                itemCount.value = prompt.selectChoices.size
                items.value = it
                isLoading.value = false
            }
        )
    }

    private fun loadItemsFromChoices(
        selectChoices: MutableList<SelectChoice>,
        prompt: FormEntryPrompt,
    ): List<MappableSelectItem> {
        return selectChoices.foldIndexed(emptyList()) { index, list, selectChoice ->
            val geometry = selectChoice.getChild("geometry")

            if (geometry != null) {
                val latitude: Double
                val longitude: Double

                try {
                    latitude = geometry.split(" ")[0].toDouble()
                    longitude = geometry.split(" ")[1].toDouble()

                    if (latitude.absoluteValue <= 90 && longitude.absoluteValue <= 180) {
                        val properties = selectChoice.additionalChildren.filter {
                            it.first != GeojsonFeature.GEOMETRY_CHILD_NAME
                        }.map {
                            MappableSelectItem.IconifiedText(null, "${it.first}: ${it.second}")
                        }

                        val markerColor = selectChoice.additionalChildren.firstOrNull { it.first == "marker-color" }?.second
                        val markerSymbol = selectChoice.additionalChildren.firstOrNull { it.first == "marker-symbol" }?.second

                        list + MappableSelectItem.WithAction(
                            index.toLong(),
                            latitude,
                            longitude,
                            if (markerSymbol == null) R.drawable.ic_map_marker_with_hole_small else R.drawable.ic_map_marker_small,
                            if (markerSymbol == null) R.drawable.ic_map_marker_with_hole_big else R.drawable.ic_map_marker_big,
                            prompt.getSelectChoiceText(selectChoice),
                            properties,
                            MappableSelectItem.IconifiedText(
                                R.drawable.ic_save,
                                resources.getString(R.string.select_item)
                            ),
                            selectChoice.index == selectedIndex,
                            markerColor,
                            markerSymbol
                        )
                    } else {
                        list
                    }
                } catch (_: NumberFormatException) {
                    list
                }
            } else {
                list
            }
        }
    }

    override fun isLoading(): NonNullLiveData<Boolean> {
        return isLoading
    }

    override fun getMapTitle(): LiveData<String?> {
        return mapTitle
    }

    override fun getItemType(): String {
        return resources.getString(R.string.choices)
    }

    override fun getItemCount(): NonNullLiveData<Int> {
        return itemCount
    }

    override fun getMappableItems(): LiveData<List<MappableSelectItem>?> {
        return items
    }
}
