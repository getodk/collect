/*
 * Copyright (C) 2009 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.collect.android.activities;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import org.odk.collect.android.R;
import org.odk.collect.android.database.FileDbAdapter;

import java.util.ArrayList;

/**
 * Responsible for displaying and deleting all the valid forms in the forms
 * directory.
 * 
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */
public class LocalFileManagerList extends ListActivity {

    // delete an item
    private static final int MENU_DELETE = Menu.FIRST;

    private AlertDialog mAlertDialog;

    private SimpleCursorAdapter mInstances;
    private ArrayList<Long> mSelected = new ArrayList<Long>();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // buildView takes place in resume
    }


    private void buildView() {

        // get all mInstances that match the status.
        FileDbAdapter fda = new FileDbAdapter(this);
        fda.open();
        fda.addOrphanForms();
        Cursor c = fda.fetchAllFiles();
        startManagingCursor(c);

        String[] data = new String[] {FileDbAdapter.KEY_DISPLAY, FileDbAdapter.KEY_META};
        int[] view = new int[] {R.id.text1, R.id.text2};

        // render total instance view
        mInstances =
                new SimpleCursorAdapter(this, R.layout.two_item_multiple_choice, c, data, view);
        if (c.getCount() > 0) {
            setListAdapter(mInstances);
        } else {
            setContentView(R.layout.list_view_empty);
        }

        // cleanup
        fda.close();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, MENU_DELETE, 0, getString(R.string.delete_file)).setIcon(
                android.R.drawable.ic_menu_delete);
        return true;
    }


    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
            case MENU_DELETE:
                if (mSelected.size() > 0) {
                    // items selected
                    createDeleteDialog();
                } else {
                    // no items selected
                    Toast.makeText(getApplicationContext(), getString(R.string.noselect_error),
                            Toast.LENGTH_SHORT).show();
                }
                return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }


    /**
     * Create the file delete dialog
     */
    private void createDeleteDialog() {
        mAlertDialog = new AlertDialog.Builder(this).create();

        mAlertDialog.setMessage(getString(R.string.delete_confirm, mSelected.size()));
        DialogInterface.OnClickListener dialogYesNoListener =
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int i) {
                        switch (i) {
                            case DialogInterface.BUTTON1: // delete and
                                deleteSelectedFiles();
                                refreshData();
                                break;
                            case DialogInterface.BUTTON2: // do nothing
                                break;
                        }
                    }


                };
        mAlertDialog.setCancelable(false);
        mAlertDialog.setButton(getString(R.string.yes), dialogYesNoListener);
        mAlertDialog.setButton2(getString(R.string.no), dialogYesNoListener);
        mAlertDialog.show();
    }


    private void refreshData() {
        if (mInstances != null) {
            mInstances.getCursor().requery();
        }
        mSelected.clear();
        buildView();
    }


    /**
     * Deletes the selected files.First from the database then from the file
     * system
     */
    private void deleteSelectedFiles() {

        FileDbAdapter fda = new FileDbAdapter(this);
        fda.open();

        // delete removes the file from the database first
        int deleted = 0;
        for (int i = 0; i < mSelected.size(); i++) {
            if (fda.deleteFile(mSelected.get(i))) {
                deleted++;
            }
        }
        
        // remove the actual files and close db
        fda.removeOrphanForms();
        fda.removeOrphanInstances();
        fda.close();

        if (deleted > 0) {
            // all deletes were successful
            Toast.makeText(getApplicationContext(), getString(R.string.file_deleted_ok, deleted),
                    Toast.LENGTH_SHORT).show();
            finish();
        } else {
            // had some failures
            Toast.makeText(
                    getApplicationContext(),
                    getString(R.string.file_deleted_error, mSelected.size() - deleted + " of "
                            + mSelected.size()), Toast.LENGTH_LONG).show();
        }

    }


    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        // get row id from db
        Cursor c = (Cursor) getListAdapter().getItem(position);
        long k = c.getLong(c.getColumnIndex(FileDbAdapter.KEY_ID));

        // toggle checkbox and add/remove from selected list
        CheckBox cb = (CheckBox) v.findViewById(R.id.checkbox);
        if (cb.isChecked()) {
            mSelected.remove(k);
            cb.setChecked(false);
        } else {
            mSelected.add(k);
            cb.setChecked(true);
        }

    }


    @Override
    protected void onPause() {
        if (mAlertDialog != null && mAlertDialog.isShowing()) {
            mAlertDialog.dismiss();
        }
        super.onPause();
    }


    @Override
    protected void onResume() {
        // update the list (for returning from the remote manager)
        refreshData();
        super.onResume();
    }

}
