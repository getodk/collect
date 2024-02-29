package org.odk.collect.android.formlists.savedformlist

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.MenuHost
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.map
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.odk.collect.analytics.Analytics
import org.odk.collect.android.R
import org.odk.collect.android.analytics.AnalyticsEvents
import org.odk.collect.androidshared.ui.FragmentFactoryBuilder
import org.odk.collect.androidshared.ui.SnackbarUtils
import org.odk.collect.androidshared.ui.SnackbarUtils.SnackbarPresenterObserver
import org.odk.collect.androidshared.ui.multiselect.MultiSelectControlsFragment
import org.odk.collect.androidshared.ui.multiselect.MultiSelectItem
import org.odk.collect.androidshared.ui.multiselect.MultiSelectListFragment
import org.odk.collect.androidshared.ui.multiselect.MultiSelectViewModel
import org.odk.collect.forms.instances.Instance
import org.odk.collect.material.MaterialProgressDialogFragment
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
            .forClass(MultiSelectListFragment::class) {
                MultiSelectListFragment(getString(string.delete_file), multiSelectViewModel, ::SelectableSavedFormListItemViewHolder) {
                    it.empty.setIcon(R.drawable.ic_baseline_delete_72)
                    it.empty.setTitle(getString(string.empty_list_of_forms_to_delete_title))
                    it.empty.setSubtitle(getString(string.empty_list_of_saved_forms_to_delete_subtitle))

                    it.list.also {
                        val itemDecoration =
                            DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
                        val divider =
                            ContextCompat.getDrawable(requireContext(), R.drawable.list_item_divider)!!
                        itemDecoration.setDrawable(divider)
                        it.addItemDecoration(itemDecoration)
                    }
                }
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
            R.layout.delete_form_layout,
            container,
            false
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        menuHost?.addMenuProvider(
            SavedFormListListMenuProvider(requireContext(), savedFormListViewModel),
            viewLifecycleOwner,
            Lifecycle.State.RESUMED
        )

        MaterialProgressDialogFragment.showOn(
            viewLifecycleOwner,
            savedFormListViewModel.isDeleting,
            childFragmentManager
        ) {
            MaterialProgressDialogFragment().also {
                it.message = getString(string.form_delete_message)
            }
        }
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
                logDelete(selected.size)

                multiSelectViewModel.unselectAll()
                savedFormListViewModel.deleteForms(selected).observe(
                    viewLifecycleOwner,
                    object : SnackbarPresenterObserver<Int>(requireView()) {
                        override fun getSnackbarDetails(value: Int): SnackbarUtils.SnackbarDetails {
                            return SnackbarUtils.SnackbarDetails(
                                getString(
                                    string.file_deleted_ok,
                                    value.toString()
                                )
                            )
                        }
                    }
                )
            }
            .setNegativeButton(getString(string.delete_no), null)
            .show()
    }

    private fun logDelete(size: Int) {
        val event = when {
            size >= 100 -> AnalyticsEvents.DELETE_SAVED_FORM_HUNDREDS
            size >= 10 -> AnalyticsEvents.DELETE_SAVED_FORM_TENS
            else -> AnalyticsEvents.DELETE_SAVED_FORM_FEW
        }

        Analytics.log(event)
    }
}
