/*
 * Copyright (C) 2017 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.odk.collect.android.fragments

import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.database.Cursor
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.odk.collect.android.R
import org.odk.collect.android.adapters.InstanceListCursorAdapter
import org.odk.collect.android.dao.CursorLoaderFactory
import org.odk.collect.android.database.instances.DatabaseInstanceColumns
import org.odk.collect.android.fragments.viewmodels.SavedFormListFragmentViewModel
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.instancemanagement.InstanceDeleter
import org.odk.collect.android.projects.CurrentProjectProvider
import org.odk.collect.android.views.DayNightProgressDialog
import org.odk.collect.androidshared.ui.ToastUtils.showLongToast
import org.odk.collect.androidshared.ui.ToastUtils.showShortToast
import org.odk.collect.async.Scheduler
import timber.log.Timber
import javax.inject.Inject

/**
 * Responsible for displaying and deleting all the saved form instances
 * directory.
 *
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */
class SavedFormListFragment : InstanceListFragment(), View.OnClickListener {

    private var alertDialog: AlertDialog? = null
    private var progressDialog: ProgressDialog? = null

    private lateinit var viewModel: SavedFormListFragmentViewModel

    @Inject
    lateinit var scheduler: Scheduler

    @Inject
    lateinit var instanceDeleter: InstanceDeleter

    @Inject
    lateinit var currentProjectProvider: CurrentProjectProvider

    override fun onAttach(context: Context) {
        super.onAttach(context)
        DaggerUtils.getComponent(context).inject(this)
        viewModel = ViewModelProvider(
            this,
            SavedFormListFragmentViewModel.Factory(scheduler, instanceDeleter)
        ).get(SavedFormListFragmentViewModel::class.java)
    }

    override fun onViewCreated(rootView: View, savedInstanceState: Bundle?) {
        deleteButton.setOnClickListener(this)
        toggleButton.setOnClickListener(this)
        setupAdapter()
        super.onViewCreated(rootView, savedInstanceState)
    }

    override fun onPause() {
        if (alertDialog != null && alertDialog!!.isShowing) {
            alertDialog!!.dismiss()
        }
        super.onPause()
    }

    private fun setupAdapter() {
        val data = arrayOf(DatabaseInstanceColumns.DISPLAY_NAME)
        val view = intArrayOf(R.id.form_title)
        listAdapter = InstanceListCursorAdapter(
            activity,
            R.layout.form_chooser_list_item_multiple_choice, null, data, view, false
        )
        setListAdapter(listAdapter)
        checkPreviouslyCheckedItems()
    }

    override fun getSortingOrderKey(): String {
        return DATA_MANAGER_LIST_SORTING_ORDER
    }

    override fun getCursorLoader(): CursorLoader {
        return CursorLoaderFactory(currentProjectProvider).createSavedInstancesCursorLoader(
            filterText, sortingOrder
        )
    }

    /**
     * Create the instance delete dialog
     */
    private fun createDeleteInstancesDialog() {
        alertDialog = MaterialAlertDialogBuilder(requireContext()).create()
        alertDialog!!.setTitle(getString(R.string.delete_file))
        alertDialog!!.setMessage(getString(R.string.delete_confirm, checkedCount.toString()))
        val dialogYesNoListener =
            DialogInterface.OnClickListener { dialog: DialogInterface?, i: Int ->
                if (i == DialogInterface.BUTTON_POSITIVE) { // delete
                    deleteSelectedInstances()
                    if (listView.count == checkedCount) {
                        toggleButton.isEnabled = false
                    }
                }
            }
        alertDialog!!.setCancelable(false)
        alertDialog!!.setButton(
            DialogInterface.BUTTON_POSITIVE, getString(R.string.delete_yes),
            dialogYesNoListener
        )
        alertDialog!!.setButton(
            DialogInterface.BUTTON_NEGATIVE, getString(R.string.delete_no),
            dialogYesNoListener
        )
        alertDialog!!.show()
    }

    /**
     * Deletes the selected files. Content provider handles removing the files
     * from the filesystem.
     */
    private fun deleteSelectedInstances() {
        progressDialog = DayNightProgressDialog(context)
        progressDialog!!.setMessage(resources.getString(R.string.form_delete_message))
        progressDialog!!.isIndeterminate = true
        progressDialog!!.setCancelable(false)
        progressDialog!!.show()

        viewModel.deleteInstances(listOf(*checkedIdObjects)).observe(viewLifecycleOwner) {
            if (it.complete) {
                deleteComplete(it.progress, it.total)
            } else {
                progressUpdate(it.progress, it.total)
            }
        }
    }

    private fun progressUpdate(progress: Int, total: Int) {
        val message = String.format(
            resources.getString(R.string.deleting_form_dialog_update_message),
            progress,
            total
        )
        progressDialog!!.setMessage(message)
    }

    private fun deleteComplete(deletedInstances: Int, toDeleteCount: Int) {
        Timber.i("Delete instances complete")
        if (deletedInstances == toDeleteCount) {
            // all deletes were successful
            showShortToast(
                requireContext(),
                getString(R.string.file_deleted_ok, deletedInstances.toString())
            )
        } else {
            // had some failures
            Timber.e("Failed to delete %d instances", toDeleteCount - deletedInstances)
            showLongToast(
                requireContext(),
                getString(
                    R.string.file_deleted_error,
                    (toDeleteCount - deletedInstances).toString(),
                    toDeleteCount.toString()
                )
            )
        }
        listView.clearChoices() // doesn't unset the checkboxes
        for (i in 0 until listView.count) {
            listView.setItemChecked(i, false)
        }
        deleteButton.isEnabled = false
        updateAdapter()
        progressDialog!!.dismiss()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.delete_button -> {
                val checkedItemCount = checkedCount
                if (checkedItemCount > 0) {
                    createDeleteInstancesDialog()
                } else {
                    showShortToast(requireContext(), R.string.noselect_error)
                }
            }
            R.id.toggle_button -> {
                val lv = listView
                val allChecked = toggleChecked(lv)
                if (allChecked) {
                    var i = 0
                    while (i < lv.count) {
                        selectedInstances.add(lv.getItemIdAtPosition(i))
                        i++
                    }
                } else {
                    selectedInstances.clear()
                }
                toggleButtonLabel(toggleButton, listView)
                deleteButton.isEnabled = allChecked
            }
        }
    }

    override fun onLoadFinished(loader: Loader<Cursor>, cursor: Cursor) {
        super.onLoadFinished(loader, cursor)
        hideProgressBarAndAllow()
    }

    companion object {
        private const val DATA_MANAGER_LIST_SORTING_ORDER = "dataManagerListSortingOrder"
    }
}
