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
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import org.odk.collect.android.R;
import org.odk.collect.android.adapters.ViewSentListAdapter;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.dao.InstancesDao;
import org.odk.collect.android.provider.InstanceProviderAPI;
import org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns;
import org.odk.collect.android.utilities.ApplicationConstants;

/**
 * Responsible for displaying all the valid instances in the instance directory.
 *
 * @author Yaw Anokwa (yanokwa@gmail.com)
 * @author Carl Hartung (carlhartung@gmail.com)
 */
public class InstanceChooserList extends ListActivity {

    private static final int MENU_SORT = Menu.FIRST;
    private static final boolean EXIT = true;
    private static final boolean DO_NOT_EXIT = false;
    private AlertDialog mAlertDialog;

    private ListView mDrawerList;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    private boolean mEdidSavedMode;

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

        if (getIntent().getStringExtra(ApplicationConstants.BundleKeys.FORM_MODE).equalsIgnoreCase(ApplicationConstants.FormModes.EDIT_SAVED)) {
            setupDrawer();
            setupDrawerItems();
            setupAdapter(InstanceColumns.STATUS + " DESC, " + InstanceColumns.DISPLAY_NAME + " ASC");
            mEdidSavedMode = true;
        } else {
            ((TextView) findViewById(android.R.id.empty)).setText(R.string.no_items_display_sent_forms);
            ViewSentListAdapter instances = new ViewSentListAdapter(this, R.layout.two_item, cursor, data, view);
            setListAdapter(instances);
        }
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
        Cursor c = (Cursor) getListAdapter().getItem(position);
        startManagingCursor(c);
        Uri instanceUri =
                ContentUris.withAppendedId(InstanceColumns.CONTENT_URI,
                        c.getLong(c.getColumnIndex(InstanceColumns._ID)));

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

    private void setupDrawerItems() {
        String[] sortingOptions = {
                getString(R.string.sort_by_name_asc), getString(R.string.sort_by_name_desc),
                getString(R.string.sort_by_date_asc), getString(R.string.sort_by_date_desc),
                getString(R.string.sort_by_status_asc), getString(R.string.sort_by_status_desc)
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, sortingOptions) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView textView = (TextView) super.getView(position, convertView, parent);
                textView.setPadding(50, 0, 0, 0);
                return textView;
            }
        };

        mDrawerList.setAdapter(adapter);
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch(position) {
                    case 0:
                        sortByNameAsc();
                        break;
                    case 1:
                        sortByNameDesc();
                        break;
                    case 2:
                        sortByDateDesc();
                        break;
                    case 3:
                        sortByDateAsc();
                        break;
                    case 4:
                        sortByStatusAsc();
                        break;
                    case 5:
                        sortByStatusDesc();
                        break;
                }
                mDrawerLayout.closeDrawer(Gravity.RIGHT);
            }
        });
    }

    private void setupDrawer() {
        mDrawerList = (ListView) findViewById(R.id.sortingMenu);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.sorting_menu_open, R.string.sorting_menu_close) {
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
                mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            }

            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu();
                mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            }
        };

        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (mDrawerToggle != null) {
            mDrawerToggle.syncState();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mDrawerToggle != null) {
            mDrawerToggle.onConfigurationChanged(newConfig);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Collect.getInstance().getActivityLogger().logInstanceAction(this, "onCreateOptionsMenu", "show");
        super.onCreateOptionsMenu(menu);

        menu
                .add(0, MENU_SORT, 0, R.string.sort_the_list)
                .setIcon(R.drawable.ic_sort)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(MENU_SORT).setVisible(mEdidSavedMode);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_SORT:
                if (mDrawerLayout.isDrawerOpen(Gravity.RIGHT)) {
                    mDrawerLayout.closeDrawer(Gravity.RIGHT);
                } else {
                    mDrawerLayout.openDrawer(Gravity.RIGHT);
                }
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void sortByNameAsc() {
        setupAdapter(InstanceColumns.DISPLAY_NAME + " ASC, " + InstanceColumns.STATUS + " DESC");
    }

    private void sortByNameDesc() {
        setupAdapter(InstanceColumns.DISPLAY_NAME + " DESC, " + InstanceColumns.STATUS + " DESC");
    }

    private void sortByDateAsc() {
        setupAdapter(InstanceColumns.LAST_STATUS_CHANGE_DATE + " ASC");
    }

    private void sortByDateDesc() {
        setupAdapter(InstanceColumns.LAST_STATUS_CHANGE_DATE + " DESC");
    }

    private void sortByStatusAsc() {
        setupAdapter(InstanceColumns.STATUS + " ASC, " + InstanceColumns.DISPLAY_NAME + " ASC");
    }

    private void sortByStatusDesc() {
        setupAdapter(InstanceColumns.STATUS + " DESC, " + InstanceColumns.DISPLAY_NAME + " ASC");
    }

    private void setupAdapter(String sortOrder) {
        String selection = InstanceColumns.STATUS + " != ?";
        String[] selectionArgs = {InstanceProviderAPI.STATUS_SUBMITTED};
        Cursor c = new InstancesDao().getInstancesCursor(selection, selectionArgs, sortOrder);
        ListAdapter adapter = new SimpleCursorAdapter(this, R.layout.two_item,
                c, new String[] {InstanceColumns.DISPLAY_NAME, InstanceColumns.DISPLAY_SUBTEXT},
                new int[] {R.id.text1, R.id.text2});
        setListAdapter(adapter);
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
