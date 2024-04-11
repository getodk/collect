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
import androidx.lifecycle.ViewModelProvider
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
import org.odk.collect.android.widgets.utilities.GeoWidgetUtils
import org.odk.collect.androidshared.livedata.MutableNonNullLiveData
import org.odk.collect.androidshared.livedata.NonNullLiveData
import org.odk.collect.androidshared.ui.FragmentFactoryBuilder
import org.odk.collect.async.Scheduler
import org.odk.collect.geo.selection.IconifiedText
import org.odk.collect.geo.selection.MappableSelectItem
import org.odk.collect.geo.selection.SelectionMapData
import org.odk.collect.geo.selection.SelectionMapFragment
import org.odk.collect.geo.selection.SelectionMapFragment.Companion.REQUEST_SELECT_ITEM
import org.odk.collect.material.MaterialFullScreenDialogFragment
import javax.inject.Inject

class SelectOneFromMapDialogFragment(private val viewModelFactory: ViewModelProvider.Factory) :
    MaterialFullScreenDialogFragment(), FragmentResultListener {

    @Inject
    lateinit var scheduler: Scheduler

    private val formEntryViewModel: FormEntryViewModel by activityViewModels { viewModelFactory }

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
        savedInstanceState: Bundle?
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
    private val selectedIndex: Int?
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
        prompt: FormEntryPrompt
    ): List<MappableSelectItem> {
        return selectChoices.foldIndexed(emptyList()) { index, list, selectChoice ->
            val geometry = selectChoice.getChild("geometry")

            if (geometry != null) {
                try {
                    val points = GeoWidgetUtils.parseGeometry(geometry)
                    if (points.isNotEmpty()) {
                        val withinBounds = points.all {
                            GeoWidgetUtils.isWithinMapBounds(it)
                        }

                        if (withinBounds) {
                            val properties = selectChoice.additionalChildren.filter {
                                it.first != GeojsonFeature.GEOMETRY_CHILD_NAME
                            }.map {
                                IconifiedText(null, "${it.first}: ${it.second}")
                            }

                            if (points.size == 1) {
                                val markerColor =
                                    selectChoice.additionalChildren.firstOrNull { it.first == "marker-color" }?.second
                                val markerSymbol =
                                    selectChoice.additionalChildren.firstOrNull { it.first == "marker-symbol" }?.second

                                list + MappableSelectItem.MappableSelectPoint(
                                    index.toLong(),
                                    prompt.getSelectChoiceText(selectChoice),
                                    properties,
                                    selectChoice.index == selectedIndex,
                                    point = points[0],
                                    smallIcon = if (markerSymbol == null) org.odk.collect.icons.R.drawable.ic_map_marker_with_hole_small else org.odk.collect.icons.R.drawable.ic_map_marker_small,
                                    largeIcon = if (markerSymbol == null) org.odk.collect.icons.R.drawable.ic_map_marker_with_hole_big else org.odk.collect.icons.R.drawable.ic_map_marker_big,
                                    color = markerColor,
                                    symbol = markerSymbol,
                                    action = IconifiedText(
                                        org.odk.collect.icons.R.drawable.ic_save,
                                        resources.getString(org.odk.collect.strings.R.string.select_item)
                                    )
                                )
                            } else if (points.first() != points.last()) {
                                list + MappableSelectItem.MappableSelectLine(
                                    index.toLong(),
                                    prompt.getSelectChoiceText(selectChoice),
                                    properties,
                                    selectChoice.index == selectedIndex,
                                    points = points,
                                    action = IconifiedText(
                                        org.odk.collect.icons.R.drawable.ic_save,
                                        resources.getString(org.odk.collect.strings.R.string.select_item)
                                    ),
                                    strokeWidth = selectChoice.additionalChildren.firstOrNull { it.first == "stroke-width" }?.second,
                                    strokeColor = selectChoice.additionalChildren.firstOrNull { it.first == "stroke" }?.second
                                )
                            } else {
                                list + MappableSelectItem.MappableSelectPolygon(
                                    index.toLong(),
                                    prompt.getSelectChoiceText(selectChoice),
                                    properties,
                                    selectChoice.index == selectedIndex,
                                    points = points,
                                    action = IconifiedText(
                                        org.odk.collect.icons.R.drawable.ic_save,
                                        resources.getString(org.odk.collect.strings.R.string.select_item)
                                    ),
                                    strokeWidth = selectChoice.additionalChildren.firstOrNull { it.first == "stroke-width" }?.second,
                                    strokeColor = selectChoice.additionalChildren.firstOrNull { it.first == "stroke" }?.second,
                                    fillColor = selectChoice.additionalChildren.firstOrNull { it.first == "fill" }?.second
                                )
                            }
                        } else {
                            list
                        }
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
        return resources.getString(org.odk.collect.strings.R.string.choices)
    }

    override fun getItemCount(): NonNullLiveData<Int> {
        return itemCount
    }

    override fun getMappableItems(): LiveData<List<MappableSelectItem>?> {
        return items
    }
}
