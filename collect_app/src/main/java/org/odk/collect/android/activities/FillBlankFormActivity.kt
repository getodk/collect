/*
 * Copyright (C) 2009 University of Washington
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
package org.odk.collect.android.activities

import android.content.Intent
import android.database.Cursor
import android.os.AsyncTask
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import androidx.activity.viewModels
import androidx.loader.app.LoaderManager
import androidx.loader.content.Loader
import org.odk.collect.android.R
import org.odk.collect.android.adapters.FormListAdapter
import org.odk.collect.android.dao.CursorLoaderFactory
import org.odk.collect.android.database.forms.DatabaseFormColumns
import org.odk.collect.android.external.FormsContract
import org.odk.collect.android.formmanagement.BlankFormListMenuDelegate
import org.odk.collect.android.formmanagement.BlankFormsListViewModel
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.listeners.DiskSyncListener
import org.odk.collect.android.network.NetworkStateProvider
import org.odk.collect.android.preferences.dialogs.ServerAuthDialogFragment
import org.odk.collect.android.projects.CurrentProjectProvider
import org.odk.collect.android.tasks.FormSyncTask
import org.odk.collect.android.utilities.ApplicationConstants
import org.odk.collect.android.utilities.InstancesRepositoryProvider
import org.odk.collect.android.views.ObviousProgressBar
import org.odk.collect.androidshared.ui.DialogFragmentUtils.dismissDialog
import org.odk.collect.androidshared.ui.DialogFragmentUtils.showIfNotShowing
import org.odk.collect.androidshared.ui.multiclicksafe.MultiClickGuard.allowClick
import org.odk.collect.permissions.PermissionListener
import org.odk.collect.settings.keys.ProjectKeys
import timber.log.Timber
import javax.inject.Inject

/**
 * Responsible for displaying all the valid forms in the forms directory. Stores the path to
 * selected form for use by [MainMenuActivity].
 *
 * @author Yaw Anokwa (yanokwa@gmail.com)
 * @author Carl Hartung (carlhartung@gmail.com)
 */
class FillBlankFormActivity :
    FormListActivity(),
    DiskSyncListener,
    OnItemClickListener,
    LoaderManager.LoaderCallbacks<Cursor> {

    @Inject
    lateinit var networkStateProvider: NetworkStateProvider

    @Inject
    lateinit var blankFormsListViewModelFactory: BlankFormsListViewModel.Factory

    @Inject
    lateinit var currentProjectProvider: CurrentProjectProvider

    @Inject
    lateinit var instancesRepositoryProvider: InstancesRepositoryProvider

    lateinit var menuDelegate: BlankFormListMenuDelegate

    private var formSyncTask: FormSyncTask? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DaggerUtils.getComponent(this).inject(this)

        setContentView(R.layout.form_chooser_list)
        title = getString(R.string.enter_data)

        val blankFormsListViewModel: BlankFormsListViewModel by viewModels {
            blankFormsListViewModelFactory
        }

        blankFormsListViewModel.isSyncing.observe(this) { syncing ->
            val progressBar = findViewById<ObviousProgressBar>(R.id.progressBar)
            if (syncing) {
                progressBar.show()
            } else {
                progressBar.hide(View.GONE)
            }
        }

        blankFormsListViewModel.isAuthenticationRequired.observe(this) { authenticationRequired ->
            if (authenticationRequired) {
                showIfNotShowing(ServerAuthDialogFragment::class.java, supportFragmentManager)
            } else {
                dismissDialog(ServerAuthDialogFragment::class.java, supportFragmentManager)
            }
        }

        menuDelegate =
            BlankFormListMenuDelegate(this, blankFormsListViewModel, networkStateProvider)

        init()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val result = super.onCreateOptionsMenu(menu)
        menuDelegate.onCreateOptionsMenu(menuInflater, menu)
        return result
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val result = super.onPrepareOptionsMenu(menu)
        menuDelegate.onPrepareOptionsMenu(menu)
        return result
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (!allowClick(javaClass.name)) {
            return true
        }
        return if (super.onOptionsItemSelected(item)) {
            true
        } else {
            menuDelegate.onOptionsItemSelected(item)
        }
    }

    private fun init() {
        setupAdapter()

        // DiskSyncTask checks the disk for any forms not already in the content provider
        // that is, put here by dragging and dropping onto the SDCard
        formSyncTask = lastCustomNonConfigurationInstance as FormSyncTask?
        if (formSyncTask == null) {
            Timber.i("Starting new disk sync task")
            formSyncTask = FormSyncTask().also {
                it.setDiskSyncListener(this)
                it.execute()
            }
        }

        sortingOptions = intArrayOf(
            R.string.sort_by_name_asc, R.string.sort_by_name_desc,
            R.string.sort_by_date_asc, R.string.sort_by_date_desc
        )
        setupAdapter()
        supportLoaderManager.initLoader(LOADER_ID, null, this)
    }

    override fun onRetainCustomNonConfigurationInstance(): Any? {
        // pass the thread on restart
        return formSyncTask
    }

    /**
     * Stores the path of selected form and finishes.
     */
    override fun onItemClick(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
        if (allowClick(javaClass.name)) {
            // get uri to form
            val idFormsTable = listView.adapter.getItemId(position)
            val formUri = FormsContract.getUri(
                currentProjectProvider.getCurrentProject().uuid,
                idFormsTable
            )

            if (Intent.ACTION_PICK == intent.action) {
                // caller is waiting on a picked form
                setResult(RESULT_OK, Intent().setData(formUri))
            } else {
                // caller wants to view/edit a form, so launch formentryactivity
                val intent = Intent(this, FormEntryActivity::class.java).also {
                    it.action = Intent.ACTION_EDIT
                    it.data = formUri
                    it.putExtra(
                        ApplicationConstants.BundleKeys.FORM_MODE,
                        ApplicationConstants.FormModes.EDIT_SAVED
                    )
                }

                startActivity(intent)
            }
            finish()
        }
    }

    private fun onMapButtonClick(id: Long) {
        permissionsProvider.requestLocationPermissions(
            this,
            object : PermissionListener {
                override fun granted() {
                    startActivity(
                        Intent(
                            this@FillBlankFormActivity,
                            FormMapActivity::class.java
                        ).also {
                            it.putExtra(FormMapActivity.EXTRA_FORM_ID, id)
                        }
                    )
                }

                override fun denied() {}
            }
        )
    }

    override fun onResume() {
        super.onResume()

        formSyncTask?.let {
            it.setDiskSyncListener(this)
            if (it.status == AsyncTask.Status.FINISHED) {
                syncComplete(it.statusMessage)
            }
        }
    }

    override fun onPause() {
        formSyncTask?.setDiskSyncListener(null)
        super.onPause()
    }

    /**
     * Called by DiskSyncTask when the task is finished
     */
    override fun syncComplete(result: String) {
        Timber.i("Disk scan complete")
        hideProgressBarAndAllow()
        showSnackbar(result)
    }

    private fun setupAdapter() {
        val columnNames = arrayOf(
            DatabaseFormColumns.DISPLAY_NAME,
            DatabaseFormColumns.JR_VERSION,
            DatabaseFormColumns.DATE,
            DatabaseFormColumns.GEOMETRY_XPATH
        )
        val viewIds = intArrayOf(
            R.id.form_title,
            R.id.form_subtitle,
            R.id.form_subtitle2,
            R.id.map_view
        )
        listAdapter = FormListAdapter(
            listView,
            DatabaseFormColumns.JR_VERSION,
            this,
            R.layout.form_chooser_list_item,
            { parent: AdapterView<*>?, view: View?, position: Int, id: Long ->
                onMapButtonClick(
                    id
                )
            },
            columnNames,
            viewIds
        )
        listView.adapter = listAdapter
    }

    override fun getSortingOrderKey(): String {
        return FORM_CHOOSER_LIST_SORTING_ORDER
    }

    override fun updateAdapter() {
        supportLoaderManager.restartLoader(LOADER_ID, null, this)
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        showProgressBar()
        return CursorLoaderFactory(currentProjectProvider).getFormsCursorLoader(
            filterText,
            sortingOrder,
            hideOldFormVersions()
        )
    }

    override fun onLoadFinished(loader: Loader<Cursor>, cursor: Cursor) {
        hideProgressBarIfAllowed()
        listAdapter.swapCursor(cursor)
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        listAdapter.swapCursor(null)
    }

    private fun hideOldFormVersions(): Boolean {
        return settingsProvider.getUnprotectedSettings()
            .getBoolean(ProjectKeys.KEY_HIDE_OLD_FORM_VERSIONS)
    }

    companion object {
        private const val FORM_CHOOSER_LIST_SORTING_ORDER = "formChooserListSortingOrder"
    }
}
