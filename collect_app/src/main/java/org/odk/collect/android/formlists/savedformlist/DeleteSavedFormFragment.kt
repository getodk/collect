package org.odk.collect.android.formlists.savedformlist

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.view.MenuHost
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.map
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.odk.collect.android.R
import org.odk.collect.android.databinding.DeleteBlankFormLayoutBinding
import org.odk.collect.androidshared.ui.FragmentFactoryBuilder
import org.odk.collect.androidshared.ui.multiselect.MultiSelectAdapter
import org.odk.collect.androidshared.ui.multiselect.MultiSelectControlsFragment
import org.odk.collect.androidshared.ui.multiselect.MultiSelectItem
import org.odk.collect.androidshared.ui.multiselect.MultiSelectViewModel
import org.odk.collect.forms.instances.Instance
import org.odk.collect.strings.R.string

class DeleteSavedFormFragment(
    private val viewModelFactory: ViewModelProvider.Factory,
    private val menuHost: MenuHost? = null
) : Fragment() {

    private val savedFormListViewModel: SavedFormListViewModel by viewModels { viewModelFactory }
    private val multiSelectViewModel: MultiSelectViewModel<Instance> by viewModels {
        MultiSelectViewModel.Factory(
            savedFormListViewModel.formsToDisplay.map {
                it.map { instance -> MultiSelectItem(instance.dbId, instance) }
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
        return inflater.inflate(
            R.layout.delete_blank_form_layout,
            container,
            false
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = DeleteBlankFormLayoutBinding.bind(view)
        val recyclerView = binding.list
        val adapter = MultiSelectAdapter(multiSelectViewModel) { parent ->
            InstanceItemViewHolder(parent.context)
        }

        recyclerView.adapter = adapter

        multiSelectViewModel.getData().observe(viewLifecycleOwner) {
            adapter.data = it
        }

        multiSelectViewModel.getSelected().observe(viewLifecycleOwner) {
            adapter.selected = it
        }

        menuHost?.addMenuProvider(
            SavedFormListListMenuProvider(requireContext()),
            viewLifecycleOwner,
            Lifecycle.State.RESUMED
        )
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
                savedFormListViewModel.deleteForms(selected)
                multiSelectViewModel.unselectAll()
            }
            .setNegativeButton(getString(string.delete_no), null)
            .show()
    }
}

private class InstanceItemView(context: Context) : FrameLayout(context) {

    val textView = TextView(context).also {
        it.id = R.id.form_title
        addView(it)
    }
    val checkBox = CheckBox(context).also {
        it.id = R.id.checkbox
        addView(it)
    }
}

private class InstanceItemViewHolder(context: Context) :
    MultiSelectAdapter.ViewHolder<Instance>(InstanceItemView(context)) {

    val view = itemView as InstanceItemView

    override fun setItem(item: Instance) {
        view.textView.text = item.displayName
    }

    override fun getCheckbox(): CheckBox {
        return view.checkBox
    }
}
