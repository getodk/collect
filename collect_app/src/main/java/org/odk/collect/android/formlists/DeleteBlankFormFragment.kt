package org.odk.collect.android.formlists

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.odk.collect.android.R
import org.odk.collect.android.databinding.DeleteBlankFormLayoutBinding
import org.odk.collect.android.formlists.blankformlist.BlankFormListViewModel
import org.odk.collect.android.formlists.blankformlist.SelectableBlankFormListAdapter
import org.odk.collect.androidshared.ui.MultiSelectViewModel

class DeleteBlankFormFragment(private val viewModelFactory: ViewModelProvider.Factory) :
    Fragment() {

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

        binding.selectAll.setOnClickListener {
            adapter.formItems.forEach {
                multiSelectViewModel.select(it.databaseId)
            }
        }

        binding.deleteSelected.setOnClickListener {
            val selected = multiSelectViewModel.getSelected()

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
    }
}
