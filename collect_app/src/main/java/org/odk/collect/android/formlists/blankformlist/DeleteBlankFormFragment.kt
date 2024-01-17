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
import org.odk.collect.androidshared.ui.MultiSelectViewModel
import org.odk.collect.androidshared.ui.setupControls

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
        val adapter = SelectableBlankFormListAdapter { databaseId ->
            multiSelectViewModel.toggle(databaseId)
        }

        recyclerView.adapter = adapter
        blankFormListViewModel.formsToDisplay.observe(viewLifecycleOwner) {
            adapter.formItems = it

            binding.empty.isVisible = it.isEmpty()
            binding.buttons.isVisible = it.isNotEmpty()

            multiSelectViewModel.data = it.map(BlankFormListItem::databaseId).toSet()
        }

        multiSelectViewModel.getSelected().observe(viewLifecycleOwner) {
            adapter.selected = it
        }

        setupControls(
            binding.buttons,
            getString(org.odk.collect.strings.R.string.delete_file),
            multiSelectViewModel,
            viewLifecycleOwner
        ) {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(org.odk.collect.strings.R.string.delete_file)
                .setMessage(
                    getString(
                        org.odk.collect.strings.R.string.delete_confirm,
                        it.size.toString()
                    )
                )
                .setPositiveButton(getString(org.odk.collect.strings.R.string.delete_yes)) { _, _ ->
                    blankFormListViewModel.deleteForms(*it.toLongArray())
                    multiSelectViewModel.unselectAll()
                }
                .setNegativeButton(getString(org.odk.collect.strings.R.string.delete_no), null)
                .show()
        }

        val blankFormListMenuProvider =
            BlankFormListMenuProvider(requireActivity(), blankFormListViewModel)
        menuHost.addMenuProvider(blankFormListMenuProvider, viewLifecycleOwner, State.RESUMED)
    }
}
