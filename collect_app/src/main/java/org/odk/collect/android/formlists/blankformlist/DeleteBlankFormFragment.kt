package org.odk.collect.android.formlists.blankformlist

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuHost
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle.State
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.odk.collect.android.R
import org.odk.collect.android.databinding.DeleteBlankFormLayoutBinding
import org.odk.collect.androidshared.ui.FragmentFactoryBuilder
import org.odk.collect.androidshared.ui.MultiSelectAdapter
import org.odk.collect.androidshared.ui.MultiSelectControlsFragment
import org.odk.collect.androidshared.ui.MultiSelectViewModel

class DeleteBlankFormFragment(
    private val viewModelFactory: ViewModelProvider.Factory,
    private val menuHost: MenuHost
) : Fragment() {

    private lateinit var blankFormListViewModel: BlankFormListViewModel
    private lateinit var multiSelectViewModel: MultiSelectViewModel

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val viewModelProvider = ViewModelProvider(this, viewModelFactory)
        blankFormListViewModel = viewModelProvider[BlankFormListViewModel::class.java]
        multiSelectViewModel = viewModelProvider[MultiSelectViewModel::class.java]

        childFragmentManager.fragmentFactory = FragmentFactoryBuilder()
            .forClass(MultiSelectControlsFragment::class) {
                MultiSelectControlsFragment(
                    getString(org.odk.collect.strings.R.string.delete_file),
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
        return inflater.inflate(R.layout.delete_blank_form_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = DeleteBlankFormLayoutBinding.bind(view)
        val recyclerView = binding.list
        val adapter = MultiSelectAdapter(multiSelectViewModel) { parent ->
            BlankFormListItemViewHolder(parent).also {
                it.setTrailingView(R.layout.checkbox)
            }
        }

        recyclerView.adapter = adapter
        blankFormListViewModel.formsToDisplay.observe(viewLifecycleOwner) {
            adapter.data = it
            multiSelectViewModel.data = it.map(BlankFormListItem::databaseId).toSet()

            binding.empty.isVisible = it.isEmpty()
            binding.buttons.isVisible = it.isNotEmpty()
        }

        multiSelectViewModel.getSelected().observe(viewLifecycleOwner) {
            adapter.selected = it
        }

        val blankFormListMenuProvider =
            BlankFormListMenuProvider(requireActivity(), blankFormListViewModel)
        menuHost.addMenuProvider(blankFormListMenuProvider, viewLifecycleOwner, State.RESUMED)
    }

    private fun onDeleteSelected(selected: LongArray) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(org.odk.collect.strings.R.string.delete_file)
            .setMessage(
                getString(
                    org.odk.collect.strings.R.string.delete_confirm,
                    selected.size.toString()
                )
            )
            .setPositiveButton(getString(org.odk.collect.strings.R.string.delete_yes)) { _, _ ->
                blankFormListViewModel.deleteForms(*selected)
                multiSelectViewModel.unselectAll()
            }
            .setNegativeButton(getString(org.odk.collect.strings.R.string.delete_no), null)
            .show()
    }
}
