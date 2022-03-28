package org.odk.collect.android.widgets.items

import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.FragmentResultListener
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.javarosa.core.model.FormIndex
import org.javarosa.core.model.data.SelectOneData
import org.javarosa.form.api.FormEntryPrompt
import org.odk.collect.android.R
import org.odk.collect.android.databinding.SelectOneFromMapDialogLayoutBinding
import org.odk.collect.android.formentry.FormEntryViewModel
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.utilities.Appearances
import org.odk.collect.androidshared.livedata.MutableNonNullLiveData
import org.odk.collect.androidshared.livedata.NonNullLiveData
import org.odk.collect.androidshared.ui.FragmentFactoryBuilder
import org.odk.collect.geo.selection.MappableSelectItem
import org.odk.collect.geo.selection.SelectionMapData
import org.odk.collect.geo.selection.SelectionMapFragment
import org.odk.collect.geo.selection.SelectionMapFragment.Companion.REQUEST_SELECT_ITEM
import org.odk.collect.material.MaterialFullScreenDialogFragment
import javax.inject.Inject

class SelectOneFromMapDialogFragment : MaterialFullScreenDialogFragment(), FragmentResultListener {

    @Inject
    lateinit var formEntryViewModelFactory: FormEntryViewModel.Factory
    private val formEntryViewModel: FormEntryViewModel by activityViewModels { formEntryViewModelFactory }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        DaggerUtils.getComponent(context).inject(this)

        childFragmentManager.fragmentFactory = FragmentFactoryBuilder()
            .forClass(SelectionMapFragment::class.java) {
                val formIndex = requireArguments().getSerializable(ARG_FORM_INDEX) as FormIndex
                val prompt = formEntryViewModel.getQuestionPrompt(formIndex)
                SelectionMapFragment(
                    SelectChoicesMapData(resources, prompt),
                    skipSummary = Appearances.hasAppearance(prompt, "quick"),
                    showNewItemButton = false
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
    }
}

internal class SelectChoicesMapData(private val resources: Resources, prompt: FormEntryPrompt) :
    SelectionMapData {

    private val mapTitle = MutableLiveData(prompt.longText)
    private val itemCount = MutableLiveData<Int>()
    private val items = MutableNonNullLiveData(emptyList<MappableSelectItem>())

    init {
        val selectChoices = prompt.selectChoices
        itemCount.value = selectChoices.size
        items.value = selectChoices.foldIndexed(emptyList()) { index, list, selectChoice ->
            val geometry = selectChoice.getChild("geometry")

            if (geometry != null) {
                val latitude = geometry.split(" ")[0].toDouble()
                val longitude = geometry.split(" ")[1].toDouble()

                list + MappableSelectItem.WithAction(
                    index.toLong(),
                    latitude,
                    longitude,
                    R.drawable.ic_map_marker_24dp,
                    R.drawable.ic_map_marker_48dp,
                    prompt.getSelectChoiceText(selectChoice),
                    emptyList(),
                    MappableSelectItem.IconifiedText(
                        R.drawable.ic_save,
                        resources.getString(R.string.select_item)
                    )
                )
            } else {
                list
            }
        }
    }

    override fun getMapTitle(): LiveData<String> {
        return mapTitle
    }

    override fun getItemType(): String {
        return resources.getString(R.string.choices)
    }

    override fun getItemCount(): LiveData<Int> {
        return itemCount
    }

    override fun getMappableItems(): NonNullLiveData<List<MappableSelectItem>> {
        return items
    }
}
