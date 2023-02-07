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

class DeleteBlankFormFragment(private val viewModelFactory: ViewModelProvider.Factory) :
    Fragment() {

    private lateinit var viewModel: BlankFormListViewModel

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val viewModelProvider = ViewModelProvider(this, viewModelFactory)
        viewModel = viewModelProvider[BlankFormListViewModel::class.java]
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
        val adapter = SelectableBlankFormListAdapter()
        recyclerView.adapter = adapter

        viewModel.formsToDisplay.observe(viewLifecycleOwner) {
            if (it != null) {
                adapter.setData(it)
            }
        }

        binding.deleteButton.setOnClickListener {
            val alertDialog = MaterialAlertDialogBuilder(requireContext()).create()
            alertDialog.setMessage(getString(R.string.delete_confirm, "1"))
            alertDialog.setButton(
                DialogInterface.BUTTON_POSITIVE,
                getString(R.string.delete_yes)
            ) { _, _ ->
                viewModel.deleteAllForms()
            }

            alertDialog.show()
        }
    }
}
