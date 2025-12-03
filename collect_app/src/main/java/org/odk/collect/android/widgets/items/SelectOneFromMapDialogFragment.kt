package org.odk.collect.android.widgets.items

import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentDialog
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import org.javarosa.core.model.FormIndex
import org.javarosa.core.model.SelectChoice
import org.javarosa.core.model.data.IAnswerData
import org.javarosa.core.model.data.SelectOneData
import org.javarosa.form.api.FormEntryPrompt
import org.odk.collect.android.R
import org.odk.collect.android.databinding.WidgetAnswerDialogLayoutBinding
import org.odk.collect.android.formentry.FormEntryViewModel
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.utilities.Appearances
import org.odk.collect.android.widgets.utilities.GeoWidgetUtils
import org.odk.collect.androidshared.livedata.MutableNonNullLiveData
import org.odk.collect.androidshared.livedata.NonNullLiveData
import org.odk.collect.androidshared.ui.FragmentFactoryBuilder
import org.odk.collect.async.Scheduler
import org.odk.collect.entities.javarosa.parse.EntitySchema
import org.odk.collect.geo.geopoly.GeoPolyUtils.parseGeometry
import org.odk.collect.geo.selection.IconifiedText
import org.odk.collect.geo.selection.MappableSelectItem
import org.odk.collect.geo.selection.SelectionMapData
import org.odk.collect.geo.selection.SelectionMapFragment
import org.odk.collect.geo.selection.SelectionMapFragment.Companion.REQUEST_SELECT_ITEM
import org.odk.collect.material.MaterialFullScreenDialogFragment
import javax.inject.Inject
import kotlin.reflect.KClass

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

    override fun onCreateFragment(prompt: FormEntryPrompt): SelectionMapFragment {
        childFragmentManager.setFragmentResultListener(REQUEST_SELECT_ITEM, this) { _, result ->
            val selectedIndex = result.getLong(SelectionMapFragment.RESULT_SELECTED_ITEM).toInt()
            val selectedChoice = prompt.selectChoices[selectedIndex]
            onAnswer(SelectOneData(selectedChoice.selection()))
        }

        val selectedIndex = requireArguments().getSerializable(ARG_SELECTED_INDEX) as Int?
        return SelectionMapFragment(
            SelectChoicesMapData(this.resources, scheduler, prompt, selectedIndex),
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

abstract class WidgetAnswerDialogFragment<T : Fragment>(
    private val type: KClass<T>,
    private val viewModelFactory: ViewModelProvider.Factory
) : MaterialFullScreenDialogFragment() {

    private val formEntryViewModel: FormEntryViewModel by activityViewModels { viewModelFactory }
    private val prompt: FormEntryPrompt by lazy {
        formEntryViewModel.getQuestionPrompt(requireArguments().getSerializable(ARG_FORM_INDEX) as FormIndex)
    }

    abstract fun onCreateFragment(prompt: FormEntryPrompt): T

    override fun onAttach(context: Context) {
        super.onAttach(context)

        childFragmentManager.fragmentFactory = FragmentFactoryBuilder()
            .forClass(type) { onCreateFragment(prompt) }
            .build()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = WidgetAnswerDialogLayoutBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        childFragmentManager.commit {
            add(R.id.answer_fragment, type.java, null)
        }
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

    fun onAnswer(answer: IAnswerData, dismiss: Boolean = true) {
        formEntryViewModel.answerQuestion(prompt.index, answer)

        if (dismiss) {
            dismiss()
        }
    }

    companion object {
        const val ARG_FORM_INDEX = "form_index"
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
            val geometry = selectChoice.getChild(GEOMETRY)

            if (geometry != null) {
                try {
                    val points = parseGeometry(geometry)
                    if (points.isNotEmpty()) {
                        val withinBounds = points.all {
                            GeoWidgetUtils.isWithinMapBounds(it)
                        }

                        if (withinBounds) {
                            val properties = selectChoice.additionalChildren.filterNot {
                                FILTERED_PROPERTIES.contains(it.first)
                            }.map {
                                IconifiedText(null, "${it.first}: ${it.second}")
                            }

                            if (points.size == 1) {
                                val markerColor =
                                    getPropertyValue(selectChoice, MARKER_COLOR)
                                val markerSymbol =
                                    getPropertyValue(selectChoice, MARKER_SYMBOL)

                                list + MappableSelectItem.MappableSelectPoint(
                                    index.toLong(),
                                    prompt.getSelectChoiceText(selectChoice),
                                    properties,
                                    selectChoice.index == selectedIndex,
                                    point = points[0],
                                    smallIcon = if (markerSymbol.isNullOrBlank()) org.odk.collect.icons.R.drawable.ic_map_marker_with_hole_small else org.odk.collect.icons.R.drawable.ic_map_marker_small,
                                    largeIcon = if (markerSymbol.isNullOrBlank()) org.odk.collect.icons.R.drawable.ic_map_marker_with_hole_big else org.odk.collect.icons.R.drawable.ic_map_marker_big,
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
                                    strokeWidth = getPropertyValue(selectChoice, STROKE_WIDTH),
                                    strokeColor = getPropertyValue(selectChoice, STROKE)
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
                                    strokeWidth = getPropertyValue(selectChoice, STROKE_WIDTH),
                                    strokeColor = getPropertyValue(selectChoice, STROKE),
                                    fillColor = getPropertyValue(selectChoice, FILL)
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

    private fun getPropertyValue(selectChoice: SelectChoice, propertyName: String): String? {
        return selectChoice.additionalChildren.firstOrNull { it.first == propertyName }?.second
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

    companion object PropertyNames {
        const val GEOMETRY = "geometry"
        const val MARKER_COLOR = "marker-color"
        const val MARKER_SYMBOL = "marker-symbol"
        const val STROKE = "stroke"
        const val STROKE_WIDTH = "stroke-width"
        const val FILL = "fill"

        private val FILTERED_PROPERTIES = arrayOf(
            GEOMETRY,
            MARKER_COLOR,
            MARKER_SYMBOL,
            STROKE,
            STROKE_WIDTH,
            FILL,
            EntitySchema.VERSION,
            EntitySchema.TRUNK_VERSION,
            EntitySchema.BRANCH_ID
        )
    }
}
