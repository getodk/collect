package org.odk.collect.android.formlists.blankformlist

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.core.content.ContextCompat
import androidx.core.view.MenuHost
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle.State
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.map
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.odk.collect.android.R
import org.odk.collect.androidshared.databinding.MultiSelectListBinding
import org.odk.collect.androidshared.ui.FragmentFactoryBuilder
import org.odk.collect.androidshared.ui.multiselect.MultiSelectAdapter
import org.odk.collect.androidshared.ui.multiselect.MultiSelectControlsFragment
import org.odk.collect.androidshared.ui.multiselect.MultiSelectItem
import org.odk.collect.androidshared.ui.multiselect.MultiSelectListView
import org.odk.collect.androidshared.ui.multiselect.MultiSelectViewModel
import org.odk.collect.strings.R.string

class DeleteBlankFormFragment(
    private val viewModelFactory: ViewModelProvider.Factory,
    private val menuHost: MenuHost
) : Fragment() {

    private val blankFormListViewModel: BlankFormListViewModel by viewModels { viewModelFactory }
    private val multiSelectViewModel: MultiSelectViewModel<BlankFormListItem> by viewModels {
        MultiSelectViewModel.Factory(
            blankFormListViewModel.formsToDisplay.map {
                it.map { blankForm -> MultiSelectItem(blankForm.databaseId, blankForm) }
            }
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        childFragmentManager.fragmentFactory = FragmentFactoryBuilder()
            .forClass(MultiSelectControlsFragment::class) {
                MultiSelectControlsFragment(
                    getString(string.delete_file),
                    multiSelectViewModel
                )
            }
            .build()

        childFragmentManager.setFragmentResultListener(
            MultiSelectControlsFragment.REQUEST_ACTION,
            this
        ) { _, result ->
            val selected = result.getLongArray(MultiSelectControlsFragment.RESULT_SELECTED)!!
            onDeleteSelected(selected)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.delete_form_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = MultiSelectListBinding.bind(view)

        binding.empty.setIcon(R.drawable.ic_baseline_delete_72)
        binding.empty.setTitle(getString(string.empty_list_of_forms_to_delete_title))
        binding.empty.setSubtitle(getString(string.empty_list_of_blank_forms_to_delete_subtitle))

        binding.list.also {
            val itemDecoration =
                DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
            val divider =
                ContextCompat.getDrawable(requireContext(), R.drawable.list_item_divider)!!
            itemDecoration.setDrawable(divider)
            it.addItemDecoration(itemDecoration)
        }

        MultiSelectListView.setup(requireContext(), viewLifecycleOwner, binding, multiSelectViewModel) {
            SelectableBlankFormListItemViewHolder(it)
        }

        val blankFormListMenuProvider =
            BlankFormListMenuProvider(requireActivity(), blankFormListViewModel)
        menuHost.addMenuProvider(blankFormListMenuProvider, viewLifecycleOwner, State.RESUMED)
    }

    private fun onDeleteSelected(selected: LongArray) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(string.delete_file)
            .setMessage(
                getString(
                    string.delete_confirm,
                    selected.size.toString()
                )
            )
            .setPositiveButton(getString(string.delete_yes)) { _, _ ->
                blankFormListViewModel.deleteForms(*selected)
                multiSelectViewModel.unselectAll()
            }
            .setNegativeButton(getString(string.delete_no), null)
            .show()
    }
}

private class SelectableBlankFormListItemViewHolder(parent: ViewGroup) :
    MultiSelectAdapter.ViewHolder<BlankFormListItem>(
        BlankFormListItemView(parent.context).also {
            it.setTrailingView(R.layout.checkbox)
        }
    ) {

    init {
        itemView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun setItem(item: BlankFormListItem) {
        (itemView as BlankFormListItemView).blankFormListItem = item
    }

    override fun getCheckbox(): CheckBox {
        return itemView.findViewById(R.id.checkbox)
    }
}
