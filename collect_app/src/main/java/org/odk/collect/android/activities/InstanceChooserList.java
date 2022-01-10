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

package org.odk.collect.android.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.odk.collect.android.R;
import org.odk.collect.android.adapters.InstanceListCursorAdapter;
import org.odk.collect.android.dao.CursorLoaderFactory;
import org.odk.collect.android.database.instances.DatabaseInstanceColumns;
import org.odk.collect.android.external.InstancesContract;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.android.projects.CurrentProjectProvider;
import org.odk.collect.android.utilities.ApplicationConstants;
import org.odk.collect.android.utilities.MultiClickGuard;
import org.odk.collect.forms.instances.Instance;

import javax.inject.Inject;

/**
 * Responsible for displaying all the valid instances in the instance directory.
 *
 * @author Yaw Anokwa (yanokwa@gmail.com)
 * @author Carl Hartung (carlhartung@gmail.com)
 */
public class InstanceChooserList extends InstanceListActivity implements AdapterView.OnItemClickListener, LoaderManager.LoaderCallbacks<Cursor> {
    private static final String INSTANCE_LIST_ACTIVITY_SORTING_ORDER = "instanceListActivitySortingOrder";
    private static final String VIEW_SENT_FORM_SORTING_ORDER = "ViewSentFormSortingOrder";

    private static final boolean DO_NOT_EXIT = false;

    private boolean editMode;

    @Inject
    CurrentProjectProvider currentProjectProvider;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.form_chooser_list);
        DaggerUtils.getComponent(this).inject(this);

        String formMode = getIntent().getStringExtra(ApplicationConstants.BundleKeys.FORM_MODE);
        if (formMode == null || ApplicationConstants.FormModes.EDIT_SAVED.equalsIgnoreCase(formMode)) {

            setTitle(getString(R.string.review_data));
            editMode = true;
            sortingOptions = new int[]{
                    R.string.sort_by_name_asc, R.string.sort_by_name_desc,
                    R.string.sort_by_date_asc, R.string.sort_by_date_desc,
                    R.string.sort_by_status_asc, R.string.sort_by_status_desc
            };
        } else {
            setTitle(getString(R.string.view_sent_forms));

            sortingOptions = new int[]{
                    R.string.sort_by_name_asc, R.string.sort_by_name_desc,
                    R.string.sort_by_date_asc, R.string.sort_by_date_desc
            };
            ((TextView) findViewById(android.R.id.empty)).setText(R.string.no_items_display_sent_forms);
        }

        init();
    }

    private void init() {
        setupAdapter();
        getSupportLoaderManager().initLoader(LOADER_ID, null, this);
    }

    /**
     * Stores the path of selected instance in the parent class and finishes.
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (MultiClickGuard.allowClick(getClass().getName())) {
            if (view.isEnabled()) {
                Cursor c = (Cursor) listView.getAdapter().getItem(position);
                long instanceId = c.getLong(c.getColumnIndex(DatabaseInstanceColumns._ID));
                Uri instanceUri = InstancesContract.getUri(currentProjectProvider.getCurrentProject().getUuid(), instanceId);

                String action = getIntent().getAction();
                if (Intent.ACTION_PICK.equals(action)) {
                    // caller is waiting on a picked form
                    setResult(RESULT_OK, new Intent().setData(instanceUri));
                } else {
                    // the form can be edited if it is incomplete or if, when it was
                    // marked as complete, it was determined that it could be edited
                    // later.
                    String status = c.getString(c.getColumnIndex(DatabaseInstanceColumns.STATUS));
                    String strCanEditWhenComplete =
                            c.getString(c.getColumnIndex(DatabaseInstanceColumns.CAN_EDIT_WHEN_COMPLETE));

                    boolean canEdit = status.equals(Instance.STATUS_INCOMPLETE)
                            || Boolean.parseBoolean(strCanEditWhenComplete);
                    if (!canEdit) {
                        createErrorDialog(getString(R.string.cannot_edit_completed_form),
                                DO_NOT_EXIT);
                        return;
                    }
                    // caller wants to view/edit a form, so launch formentryactivity
                    Intent parentIntent = this.getIntent();
                    Intent intent = new Intent(this, FormEntryActivity.class);
                    intent.setAction(Intent.ACTION_EDIT);
                    intent.setData(instanceUri);
                    String formMode = parentIntent.getStringExtra(ApplicationConstants.BundleKeys.FORM_MODE);
                    if (formMode == null || ApplicationConstants.FormModes.EDIT_SAVED.equalsIgnoreCase(formMode)) {
                        intent.putExtra(ApplicationConstants.BundleKeys.FORM_MODE, ApplicationConstants.FormModes.EDIT_SAVED);
                    } else {
                        intent.putExtra(ApplicationConstants.BundleKeys.FORM_MODE, ApplicationConstants.FormModes.VIEW_SENT);
                    }
                    startActivity(intent);
                }
                finish();
            } else {
                TextView disabledCause = view.findViewById(R.id.form_subtitle2);
                Toast.makeText(this, disabledCause.getText(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setupAdapter() {
        String[] data = {DatabaseInstanceColumns.DISPLAY_NAME, DatabaseInstanceColumns.DELETED_DATE};
        int[] view = {R.id.form_title, R.id.form_subtitle2};

        boolean shouldCheckDisabled = !editMode;
        listAdapter = new InstanceListCursorAdapter(
                this, R.layout.form_chooser_list_item, null, data, view, shouldCheckDisabled);
        listView.setAdapter(listAdapter);
    }

    @Override
    protected String getSortingOrderKey() {
        return editMode ? INSTANCE_LIST_ACTIVITY_SORTING_ORDER : VIEW_SENT_FORM_SORTING_ORDER;
    }

    @Override
    protected void updateAdapter() {
        getSupportLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        showProgressBar();
        if (editMode) {
            return new CursorLoaderFactory(currentProjectProvider).createEditableInstancesCursorLoader(getFilterText(), getSortingOrder());
        } else {
            return new CursorLoaderFactory(currentProjectProvider).createSentInstancesCursorLoader(getFilterText(), getSortingOrder());
        }
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        hideProgressBarAndAllow();
        listAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(@NonNull Loader loader) {
        listAdapter.swapCursor(null);
    }

    private void createErrorDialog(String errorMsg, final boolean shouldExit) {
        AlertDialog alertDialog = new MaterialAlertDialogBuilder(this).create();
        alertDialog.setIcon(android.R.drawable.ic_dialog_info);
        alertDialog.setMessage(errorMsg);
        DialogInterface.OnClickListener errorListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                switch (i) {
                    case DialogInterface.BUTTON_POSITIVE:
                        if (shouldExit) {
                            finish();
                        }
                        break;
                }
            }
        };
        alertDialog.setCancelable(false);
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.ok), errorListener);
        alertDialog.show();
    }
}
