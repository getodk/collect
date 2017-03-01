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

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import org.odk.collect.android.R;
import org.odk.collect.android.adapters.SearchableAdapter;
import org.odk.collect.android.adapters.ViewSentListAdapter;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.dao.InstancesDao;
import org.odk.collect.android.provider.InstanceProviderAPI;
import org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns;
import org.odk.collect.android.utilities.ApplicationConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * Responsible for displaying all the valid instances in the instance directory.
 *
 * @author Yaw Anokwa (yanokwa@gmail.com)
 * @author Carl Hartung (carlhartung@gmail.com)
 */
public class InstanceChooserList extends ListActivity {

    private static final String SEARCH_BOX_VISIBILITY = "searchBoxVisibility";
    private static final int MENU_FILTER = Menu.FIRST;
    private static final boolean EXIT = true;
    private static final boolean DO_NOT_EXIT = false;
    private AlertDialog mAlertDialog;

    private boolean mEditSavedMode;

    private LinearLayout mSearchBoxLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // must be at the beginning of any activity that can be called from an external intent
        try {
            Collect.createODKDirs();
        } catch (RuntimeException e) {
            createErrorDialog(e.getMessage(), EXIT);
            return;
        }

        setContentView(R.layout.chooser_list_layout);
        TextView tv = (TextView) findViewById(R.id.status_text);
        tv.setVisibility(View.GONE);

        mSearchBoxLayout = (LinearLayout) findViewById(R.id.searchBoxLayout);

        Cursor cursor;
        InstancesDao instancesDao = new InstancesDao();
        if (getIntent().getStringExtra(ApplicationConstants.BundleKeys.FORM_MODE).equalsIgnoreCase(ApplicationConstants.FormModes.EDIT_SAVED)) {
            setTitle(getString(R.string.review_data));
            cursor = instancesDao.getUnsentInstancesCursor();
        } else {
            setTitle(getString(R.string.view_sent_forms));
            cursor = instancesDao.getSentInstancesCursor();
        }

        String[] data = new String[]{
                InstanceColumns.DISPLAY_NAME, InstanceColumns.DISPLAY_SUBTEXT, InstanceColumns.DELETED_DATE
        };
        int[] view = new int[]{
                R.id.text1, R.id.text2, R.id.text4
        };

        // render total instance view
        SimpleCursorAdapter instances;
        if (getIntent().getStringExtra(ApplicationConstants.BundleKeys.FORM_MODE).equalsIgnoreCase(ApplicationConstants.FormModes.EDIT_SAVED)) {
            setupAdapter();
            setupSearchBox();
            mEditSavedMode = true;
        } else {
            ((TextView) findViewById(android.R.id.empty)).setText(R.string.no_items_display_sent_forms);
            instances = new ViewSentListAdapter(this, R.layout.two_item, cursor, data, view);
            setListAdapter(instances);
            mEditSavedMode = false;
        }
    }

    private void setupAdapter() {
        Cursor c = new InstancesDao().getUnsentInstancesCursor();

        List<SearchableAdapter.ListElement> listElements = new ArrayList<>();
        while (c.moveToNext()) {
            long id = c.getLong(c.getColumnIndex(InstanceColumns._ID));
            String name = c.getString(c.getColumnIndex(InstanceColumns.DISPLAY_NAME));
            String subtext = c.getString(c.getColumnIndex(InstanceColumns.DISPLAY_SUBTEXT));

            listElements.add(new SearchableAdapter.ListElement(id, name, subtext));
        }
        SearchableAdapter searchableAdapter = new SearchableAdapter(this, listElements);
        setListAdapter(searchableAdapter);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
    }


    /**
     * Stores the path of selected instance in the parent class and finishes.
     */
    @Override
    protected void onListItemClick(ListView listView, View view, int position, long id) {
        Cursor c;
        Uri instanceUri;
        if (mEditSavedMode) {
            SearchableAdapter.ListElement listElement = (SearchableAdapter.ListElement)
                    getListAdapter().getItem(position);
            instanceUri = ContentUris.withAppendedId(InstanceColumns.CONTENT_URI, listElement.getId());

            c = managedQuery(instanceUri, null, null, null, null);
            c.moveToFirst();
        } else {
            c = (Cursor) getListAdapter().getItem(position);
            startManagingCursor(c);
            instanceUri = ContentUris.withAppendedId(InstanceColumns.CONTENT_URI, c.getLong(c.getColumnIndex(InstanceColumns._ID)));
        }

        Collect.getInstance().getActivityLogger().logAction(this, "onListItemClick",
                instanceUri.toString());

        if (view.findViewById(R.id.visible_off).getVisibility() != View.VISIBLE) {
            String action = getIntent().getAction();
            if (Intent.ACTION_PICK.equals(action)) {
                // caller is waiting on a picked form
                setResult(RESULT_OK, new Intent().setData(instanceUri));
            } else {
                // the form can be edited if it is incomplete or if, when it was
                // marked as complete, it was determined that it could be edited
                // later.
                String status = c.getString(c.getColumnIndex(InstanceColumns.STATUS));
                String strCanEditWhenComplete =
                        c.getString(c.getColumnIndex(InstanceColumns.CAN_EDIT_WHEN_COMPLETE));

                boolean canEdit = status.equals(InstanceProviderAPI.STATUS_INCOMPLETE)
                        || Boolean.parseBoolean(strCanEditWhenComplete);
                if (!canEdit) {
                    createErrorDialog(getString(R.string.cannot_edit_completed_form),
                            DO_NOT_EXIT);
                    return;
                }
                // caller wants to view/edit a form, so launch formentryactivity
                Intent parentIntent = this.getIntent();
                Intent intent = new Intent(Intent.ACTION_EDIT, instanceUri);
                if (parentIntent.getStringExtra(ApplicationConstants.BundleKeys.FORM_MODE).equalsIgnoreCase(ApplicationConstants.FormModes.EDIT_SAVED)) {
                    intent.putExtra(ApplicationConstants.BundleKeys.FORM_MODE, ApplicationConstants.FormModes.EDIT_SAVED);
                } else {
                    intent.putExtra(ApplicationConstants.BundleKeys.FORM_MODE, ApplicationConstants.FormModes.VIEW_SENT);
                }
                startActivity(intent);
            }
            finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Collect.getInstance().getActivityLogger().logOnStart(this);
    }

    @Override
    protected void onStop() {
        Collect.getInstance().getActivityLogger().logOnStop(this);
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Collect.getInstance().getActivityLogger().logInstanceAction(this, "onCreateOptionsMenu", "show");
        super.onCreateOptionsMenu(menu);

        menu
                .add(0, MENU_FILTER, 0, R.string.general_preferences)
                .setIcon(R.drawable.ic_search)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(MENU_FILTER).setVisible(mEditSavedMode);
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SEARCH_BOX_VISIBILITY, mSearchBoxLayout.getVisibility() == View.VISIBLE);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            mSearchBoxLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_FILTER:
                if (mSearchBoxLayout.getVisibility() == View.GONE) {
                    mSearchBoxLayout.setVisibility(View.VISIBLE);
                } else {
                    mSearchBoxLayout.setVisibility(View.GONE);
                }
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setupSearchBox() {
        EditText inputSearch = (EditText) findViewById(R.id.inputSearch);
        inputSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                performFiltering(s);
            }
        });
    }

    private void performFiltering(CharSequence filter) {
        ((SearchableAdapter) getListAdapter()).getFilter().filter(filter);
    }

    private void createErrorDialog(String errorMsg, final boolean shouldExit) {
        Collect.getInstance().getActivityLogger().logAction(this, "createErrorDialog", "show");

        mAlertDialog = new AlertDialog.Builder(this).create();
        mAlertDialog.setIcon(android.R.drawable.ic_dialog_info);
        mAlertDialog.setMessage(errorMsg);
        DialogInterface.OnClickListener errorListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                switch (i) {
                    case DialogInterface.BUTTON_POSITIVE:
                        Collect.getInstance().getActivityLogger().logAction(this,
                                "createErrorDialog",
                                shouldExit ? "exitApplication" : "OK");
                        if (shouldExit) {
                            finish();
                        }
                        break;
                }
            }
        };
        mAlertDialog.setCancelable(false);
        mAlertDialog.setButton(getString(R.string.ok), errorListener);
        mAlertDialog.show();
    }


}
