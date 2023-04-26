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

class DeleteBlankFormFragment(
    private val viewModelFactory: ViewModelProvider.Factory,
    private val menuHost: MenuHost
) : Fragment() {

    private lateinit var blankFormListViewModel: BlankFormListViewModel
    private lateinit var multiSelectViewModel: MultiSelectViewModel

    private var allSelected = false

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
            if (it != null) {
                adapter.formItems = it

                binding.empty.isVisible = it.isEmpty()
                binding.buttons.isVisible = it.isNotEmpty()
            }

            updateAllSelected(binding, adapter)
        }

        multiSelectViewModel.getSelected().observe(viewLifecycleOwner) {
            binding.deleteSelected.isEnabled = it.isNotEmpty()
            adapter.selected = it

            updateAllSelected(binding, adapter)
        }

        binding.selectAll.setOnClickListener {
            if (allSelected) {
                multiSelectViewModel.unselectAll()
            } else {
                adapter.formItems.forEach {
                    multiSelectViewModel.select(it.databaseId)
                }
            }
        }

        binding.deleteSelected.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setMessage(getString(R.string.delete_confirm, adapter.selected.size.toString()))
                .setPositiveButton(getString(R.string.delete_yes)) { _, _ ->
                    blankFormListViewModel.deleteForms(*adapter.selected.toLongArray())
                    multiSelectViewModel.unselectAll()
                }
                .setNegativeButton(getString(R.string.delete_no), null)
                .show()
        }

        val blankFormListMenuProvider =
            BlankFormListMenuProvider(requireActivity(), blankFormListViewModel)
        menuHost.addMenuProvider(blankFormListMenuProvider, viewLifecycleOwner, State.RESUMED)
    }

    fun updateAllSelected(binding: DeleteBlankFormLayoutBinding, adapter: SelectableBlankFormListAdapter) {
        allSelected = adapter.formItems.isNotEmpty() && adapter.selected.size == adapter.formItems.size

        if (allSelected) {
            binding.selectAll.setText(R.string.clear_all)
        } else {
            binding.selectAll.setText(R.string.select_all)
        }
    }
}
