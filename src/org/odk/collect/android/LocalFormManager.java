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

package org.odk.collect.android;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Responsible for displaying and deleting all the valid forms in the forms
 * directory.
 * 
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */
public class LocalFormManager extends ListActivity {

    // add or delete form
    private static final int MENU_DELETE = Menu.FIRST;

    private int mDeletePosition;
    private AlertDialog mAlertDialog;
    private ArrayList<String> mFiles = new ArrayList<String>();
    private ArrayList<String> mFilenames = new ArrayList<String>();
    private ArrayAdapter<String> mFileAdapter;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // buildView takes place in resume
    }



    private void buildView() {

        mFilenames.clear();

        // check directories for files
        mFiles = FileUtils.getFilesAsArrayList(SharedConstants.FORMS_PATH);
        Collections.sort(mFiles, NaturalOrderComparator.NUMERICAL_ORDER);

        // parse list for filenames
        for (int i = 0; i < mFiles.size(); i++) {
            String file = mFiles.get(i);
            mFilenames.add(file.substring(file.lastIndexOf("/") + 1));
        }

        // set adapter
        mFileAdapter =
                new ArrayAdapter<String>(this, android.R.layout.simple_list_item_single_choice,
                        mFilenames);

        // view options
        getListView().setItemsCanFocus(false);
        getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        setListAdapter(mFileAdapter);

    }


    /**
     * Notify the file adapter of data changes and clear radio buttons.
     */
    private void refreshData() {
        mFileAdapter.notifyDataSetChanged();
        getListView().clearChoices();
        FormManagerTabs.setTabHeader(getString(R.string.local_forms_tab, mFiles.size()), "tab1");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, MENU_DELETE, 0, getString(R.string.delete_form)).setIcon(
                android.R.drawable.ic_menu_delete);
        return true;
    }


    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
            case MENU_DELETE:
                if (getListView().getCheckedItemPosition() != -1) {
                    createDeleteDialog();
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.noselect_error),
                            Toast.LENGTH_SHORT).show();
                }
                return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }


    /**
     * Á Create the form delete dialog
     */
    private void createDeleteDialog() {
        mAlertDialog = new AlertDialog.Builder(this).create();

        // match the selected item in mFilenames with the form in mFiles
        mDeletePosition = getListView().getCheckedItemPosition();
        mAlertDialog
                .setMessage(getString(R.string.delete_confirm, mFilenames.get(mDeletePosition)));
        DialogInterface.OnClickListener dialogYesNoListener =
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int i) {
                        switch (i) {
                            case DialogInterface.BUTTON1: // yes, delete and
                                // refresh
                                deleteSelectedForm();
                                refreshData();
                                break;
                            case DialogInterface.BUTTON2: // no, do nothing
                                break;
                        }
                    }
                };
        mAlertDialog.setCancelable(false);
        mAlertDialog.setButton(getString(R.string.yes), dialogYesNoListener);
        mAlertDialog.setButton2(getString(R.string.no), dialogYesNoListener);
        mAlertDialog.show();
    }


    /**
     * Deletes the selected form
     */
    private void deleteSelectedForm() {

        // delete from mFiles because it has full path
        String filename = mFilenames.get(mDeletePosition);
        String filepath = mFiles.get(mDeletePosition);

        boolean deleted = FileUtils.deleteFile(filepath);
        if (deleted) {
            Toast.makeText(getApplicationContext(), getString(R.string.form_deleted_ok, filename),
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.form_deleted_error, filename), Toast.LENGTH_SHORT).show();
        }

        // remove item both arrays
        mFilenames.remove(mDeletePosition);
        mFiles.remove(mDeletePosition);

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
        buildView();
        refreshData();

        super.onResume();
    }

}
