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
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import org.odk.collect.android.R;
import org.odk.collect.android.dao.InstancesDao;
import org.odk.collect.android.preferences.PreferencesActivity;
import org.odk.collect.android.preferences.PreferenceKeys;
import org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns;
import org.odk.collect.android.receivers.NetworkReceiver;
import org.odk.collect.android.utilities.ListViewUtils;

/**
 * Responsible for displaying all the valid forms in the forms directory. Stores
 * the path to selected form for use by {@link MainMenuActivity}.
 *
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */

public class InstanceUploaderList extends AppListActivity implements OnLongClickListener {
    private static final String t = "InstanceUploaderList";
    private static final int MENU_PREFERENCES = Menu.FIRST;
    private static final int MENU_SHOW_UNSENT = Menu.FIRST + 1;

    private static final int INSTANCE_UPLOADER = 0;
    private static final int GOOGLE_USER_DIALOG = 1;

    private Button mUploadButton;

    private SimpleCursorAdapter mCursorAdapter;

    private InstancesDao mInstanceDao;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(t, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.instance_uploader_list);

        mInstanceDao = new InstancesDao();

        mUploadButton = (Button) findViewById(R.id.upload_button);
        mUploadButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(
                        Context.CONNECTIVITY_SERVICE);
                NetworkInfo ni = connectivityManager.getActiveNetworkInfo();

                if (NetworkReceiver.running) {
                    Toast.makeText(
                            InstanceUploaderList.this,
                            R.string.send_in_progress,
                            Toast.LENGTH_SHORT).show();
                } else if (ni == null || !ni.isConnected()) {
                    logger.logAction(this, "uploadButton", "noConnection");

                    Toast.makeText(InstanceUploaderList.this,
                            R.string.no_connection, Toast.LENGTH_SHORT).show();
                } else {
                    int checkedItemCount = getCheckedCount();
                    logger.logAction(this, "uploadButton", Integer.toString(checkedItemCount));

                    if (checkedItemCount > 0) {
                        // items selected
                        uploadSelectedFiles();
                        InstanceUploaderList.this.getListView().clearChoices();
                        mUploadButton.setEnabled(false);
                    } else {
                        // no items selected
                        Toast.makeText(getApplicationContext(),
                                getString(R.string.noselect_error),
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        final Button toggleSelsButton = (Button) findViewById(R.id.toggle_button);
        toggleSelsButton.setLongClickable(true);
        toggleSelsButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ListView lv = getListView();
                boolean allChecked = ListViewUtils.toggleChecked(lv);
                ListViewUtils.toggleButtonLabel(toggleSelsButton, lv);
                mUploadButton.setEnabled(allChecked);
            }
        });
        toggleSelsButton.setOnLongClickListener(this);

        {
            Cursor cursor = mInstanceDao.getFinalizedInstancesCursor();
            // ToDo: Look at VCS history and examine the always true ? : for the above line
            String[] data = new String[]
                    {InstanceColumns.DISPLAY_NAME, InstanceColumns.DISPLAY_SUBTEXT};
            int[] view = new int[]{R.id.text1, R.id.text2};

            // render total instance view
            mCursorAdapter = new SimpleCursorAdapter(this,
                    R.layout.two_item_multiple_choice, cursor, data, view);
        }

        setListAdapter(mCursorAdapter);
        getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        getListView().setItemsCanFocus(false);
        mUploadButton.setEnabled(false);

        // set title
        setTitle(getString(R.string.send_data));
    }

    @Override
    protected void onStart() {
        super.onStart();
        logger.logOnStart(this);
    }

    @Override
    protected void onStop() {
        logger.logOnStop(this);
        super.onStop();
    }

    private void uploadSelectedFiles() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String server = prefs.getString(PreferenceKeys.KEY_PROTOCOL, null);
        long[] instanceIds = getListView().getCheckedItemIds();
        if (server.equalsIgnoreCase(getString(R.string.protocol_google_sheets))) {
            // if it's Sheets, start the Sheets uploader
            // first make sure we have a google account selected

            String googleUsername = prefs.getString(
                    PreferenceKeys.KEY_SELECTED_GOOGLE_ACCOUNT, null);
            if (googleUsername == null || googleUsername.equals("")) {
                showDialog(GOOGLE_USER_DIALOG);
                return;
            }
            Intent i = new Intent(this, GoogleSheetsUploaderActivity.class);
            i.putExtra(FormEntryActivity.KEY_INSTANCES, instanceIds);
            startActivityForResult(i, INSTANCE_UPLOADER);
        } else {
            // otherwise, do the normal aggregate/other thing.
            Intent i = new Intent(this, InstanceUploaderActivity.class);
            i.putExtra(FormEntryActivity.KEY_INSTANCES, instanceIds);
            startActivityForResult(i, INSTANCE_UPLOADER);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        logger.logAction(this, "onCreateOptionsMenu", "show");
        super.onCreateOptionsMenu(menu);

        menu
                .add(0, MENU_PREFERENCES, 0, R.string.general_preferences)
                .setIcon(R.drawable.ic_menu_preferences)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

        menu
                .add(0, MENU_SHOW_UNSENT, 1, R.string.change_view)
                .setIcon(R.drawable.ic_menu_manage)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
            case MENU_PREFERENCES:
                logger.logAction(this, "onMenuItemSelected", "MENU_PREFERENCES");
                createPreferencesMenu();
                return true;
            case MENU_SHOW_UNSENT:
                logger.logAction(this, "onMenuItemSelected", "MENU_SHOW_UNSENT");
                showSentAndUnsentChoices();
                return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }

    private void createPreferencesMenu() {
        Intent i = new Intent(this, PreferencesActivity.class);
        startActivity(i);
    }

    @Override
    protected void onListItemClick(ListView listView, View view, int position, long rowId) {
        super.onListItemClick(listView, view, position, rowId);

        logger.logAction(this, "onListItemClick", Long.toString(rowId));

        mUploadButton.setEnabled(areCheckedItems());
        Button toggleSelectionsButton = (Button) findViewById(R.id.toggle_button);
        ListViewUtils.toggleButtonLabel(toggleSelectionsButton, getListView());
    }

    @Override
    protected void onRestoreInstanceState(Bundle bundle) {
        Log.d(t, "onRestoreInstanceState");
        super.onRestoreInstanceState(bundle);
        mUploadButton.setEnabled(areCheckedItems());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode == RESULT_CANCELED) {
            return;
        }
        switch (requestCode) {
            // returns with a form path, start entry
            case INSTANCE_UPLOADER:
                if (intent.getBooleanExtra(FormEntryActivity.KEY_SUCCESS, false)) {
                    getListView().clearChoices();
                    if (mCursorAdapter.isEmpty()) {
                        finish();
                    }
                }
                break;
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, intent);
    }

    private void showUnsent() {
        Cursor c = mInstanceDao.getFinalizedInstancesCursor();
        Cursor old = mCursorAdapter.getCursor();
        try {
            mCursorAdapter.changeCursor(c);
        } finally {
            if (old != null) {
                old.close();
                this.stopManagingCursor(old);
            }
        }
        getListView().invalidate();
    }

    private void showAll() {
        Cursor c = mInstanceDao.getAllCompletedUndeletedInstancesCursor();
        Cursor old = mCursorAdapter.getCursor();
        try {
            mCursorAdapter.changeCursor(c);
        } finally {
            if (old != null) {
                old.close();
                this.stopManagingCursor(old);
            }
        }
        getListView().invalidate();
    }

    @Override
    public boolean onLongClick(View v) {
        logger.logAction(this, "toggleButton.longClick", "");
        return showSentAndUnsentChoices();
    }

    private boolean showSentAndUnsentChoices() {
        /**
         * Create a dialog with options to save and exit, save, or quit without
         * saving
         */
        String[] items = {getString(R.string.show_unsent_forms),
                getString(R.string.show_sent_and_unsent_forms)};

        logger.logAction(this, "changeView", "show");

        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setTitle(getString(R.string.change_view))
                .setNeutralButton(getString(R.string.cancel),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                logger.logAction(this, "changeView", "cancel");
                                dialog.cancel();
                            }
                        })
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {

                            case 0: // show unsent
                                logger.logAction(this, "changeView", "showUnsent");
                                InstanceUploaderList.this.showUnsent();
                                break;

                            case 1: // show all
                                logger.logAction(this, "changeView", "showAll");
                                InstanceUploaderList.this.showAll();
                                break;

                            case 2:// do nothing
                                break;
                        }
                    }
                }).create();
        alertDialog.show();
        return true;
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case GOOGLE_USER_DIALOG:
                AlertDialog.Builder gudBuilder = new AlertDialog.Builder(this);

                gudBuilder.setTitle(R.string.no_google_account);
                gudBuilder
                        .setMessage(R.string.sheets_google_account_needed);
                gudBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                gudBuilder.setCancelable(false);
                return gudBuilder.create();
        }
        return null;
    }
}
