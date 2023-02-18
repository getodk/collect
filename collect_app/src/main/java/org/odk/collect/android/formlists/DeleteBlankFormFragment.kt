package org.odk.collect.android.formlists

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuHost
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle.State
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.odk.collect.android.R
import org.odk.collect.android.databinding.DeleteBlankFormLayoutBinding
import org.odk.collect.android.formlists.blankformlist.BlankFormListMenuProvider
import org.odk.collect.android.formlists.blankformlist.BlankFormListViewModel
import org.odk.collect.android.formlists.blankformlist.SelectableBlankFormListAdapter
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

                if (it.isEmpty()) {
                    binding.selectAll.visibility = View.GONE
                    binding.deleteSelected.visibility = View.GONE
                } else {
                    binding.selectAll.visibility = View.VISIBLE
                    binding.deleteSelected.visibility = View.VISIBLE
                }
            }
        }

        multiSelectViewModel.getSelected().observe(viewLifecycleOwner) {
            if (it.isEmpty()) {
                binding.deleteSelected.isEnabled = false
                binding.selectAll.setText(R.string.select_all)
            } else {
                binding.deleteSelected.isEnabled = true
                binding.selectAll.setText(R.string.clear_all)
            }

            adapter.selected = it
        }

        binding.selectAll.setOnClickListener {
            if (adapter.selected.isEmpty()) {
                adapter.formItems.forEach {
                    multiSelectViewModel.select(it.databaseId)
                }
            } else {
                multiSelectViewModel.unselectAll()
            }
        }

        binding.deleteSelected.setOnClickListener {
            val alertDialog = MaterialAlertDialogBuilder(requireContext()).create()
            alertDialog.setMessage(getString(R.string.delete_confirm, adapter.selected.size.toString()))
            alertDialog.setButton(
                DialogInterface.BUTTON_POSITIVE,
                getString(R.string.delete_yes)
            ) { _, _ ->
                blankFormListViewModel.deleteForms(*adapter.selected.toLongArray())
            }

            alertDialog.show()
        }

        val blankFormListMenuProvider =
            BlankFormListMenuProvider(requireActivity(), blankFormListViewModel)
        menuHost.addMenuProvider(blankFormListMenuProvider, viewLifecycleOwner, State.RESUMED)
    }
}
