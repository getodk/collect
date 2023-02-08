package org.odk.collect.android.formlists

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.odk.collect.android.R
import org.odk.collect.android.databinding.DeleteBlankFormLayoutBinding
import org.odk.collect.android.formlists.blankformlist.BlankFormListMenuDelegate
import org.odk.collect.android.formlists.blankformlist.BlankFormListViewModel
import org.odk.collect.android.formlists.blankformlist.SelectableBlankFormListAdapter
import org.odk.collect.androidshared.ui.MultiSelectViewModel

class DeleteBlankFormFragment(private val viewModelFactory: ViewModelProvider.Factory) :
    Fragment(), MenuProvider {

    private lateinit var blankFormListViewModel: BlankFormListViewModel
    private lateinit var multiSelectViewModel: MultiSelectViewModel
    private lateinit var menuDelegate: BlankFormListMenuDelegate

    private var selected = emptySet<Long>()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val viewModelProvider = ViewModelProvider(this, viewModelFactory)
        blankFormListViewModel = viewModelProvider[BlankFormListViewModel::class.java]
        multiSelectViewModel = viewModelProvider[MultiSelectViewModel::class.java]

        menuDelegate = BlankFormListMenuDelegate(requireActivity(), blankFormListViewModel)
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
        val adapter = SelectableBlankFormListAdapter { databaseId, checked ->
            if (checked) {
                multiSelectViewModel.select(databaseId)
            } else {
                multiSelectViewModel.unselect(databaseId)
            }
        }

        recyclerView.adapter = adapter
        blankFormListViewModel.formsToDisplay.observe(viewLifecycleOwner) {
            if (it != null) {
                adapter.formItems = it
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

            selected = it
        }

        binding.selectAll.setOnClickListener {
            if (selected.isEmpty()) {
                adapter.formItems.forEach {
                    multiSelectViewModel.select(it.databaseId)
                }
            } else {
                multiSelectViewModel.unselectAll()
            }
        }

        binding.deleteSelected.setOnClickListener {
            val alertDialog = MaterialAlertDialogBuilder(requireContext()).create()
            alertDialog.setMessage(getString(R.string.delete_confirm, selected.size.toString()))
            alertDialog.setButton(
                DialogInterface.BUTTON_POSITIVE,
                getString(R.string.delete_yes)
            ) { _, _ ->
                blankFormListViewModel.deleteForms(*selected.toLongArray())
            }

            alertDialog.show()
        }

        requireActivity().addMenuProvider(this, viewLifecycleOwner)
    }

    override fun onCreateMenu(menu: Menu, inflater: MenuInflater) {
        menuDelegate.onCreateOptionsMenu(inflater, menu)
    }

    override fun onPrepareMenu(menu: Menu) {
        menuDelegate.onPrepareOptionsMenu(menu)
    }

    override fun onMenuItemSelected(item: MenuItem): Boolean {
        return menuDelegate.onOptionsItemSelected(item)
    }
}
