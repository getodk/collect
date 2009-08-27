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
 * WARRANTIES OR CONDITIONS OF ANY KIND, either expAress or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.collect.android;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Responsible for displaying all the valid forms in the forms directory. Stores
 * the path to selected form for use by {@link MainMenu}.
 * 
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */


// TODO long click form for submission log
// TODO show item KEY_META information in view
public class InstanceSubmitter extends ListActivity {

    private static final int MENU_UPLOAD = Menu.FIRST;
    private SimpleCursorAdapter mInstances;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // buildView takes place in resume
    }


    /**
     * Retrieves instance information from {@link FileDbAdapter}, composes and
     * displays each row.
     */
    private void buildView() {


        // get all mInstances that match the status.
        FileDbAdapter fda = new FileDbAdapter(this);
        fda.open();
        Cursor c = fda.fetchFiles(FileDbAdapter.TYPE_INSTANCE, FileDbAdapter.STATUS_COMPLETED);
        startManagingCursor(c);

        // create data and views for cursor adapter
        String[] data = new String[] {FileDbAdapter.KEY_DISPLAY};
        int[] view = new int[] {android.R.id.text1};

        // render total instance view
        mInstances =
                new SimpleCursorAdapter(this, android.R.layout.simple_list_item_multiple_choice, c,
                        data, view);
        setListAdapter(mInstances);

        getListView().setItemsCanFocus(false);
        getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        // set title
        setTitle(getString(R.string.app_name) + " > " + getString(R.string.send_data));

        // cleanup
        fda.close();
       // c.close();
    }


    private void uploadSelectedData() {

        // paths to upload
        ArrayList<String> selectedInstances = new ArrayList<String>();

        // get all checked items
        Cursor c = null;
        SparseBooleanArray checkedItems = getListView().getCheckedItemPositions();
        for (int i = 0; i < checkedItems.size(); i++) {
            if (checkedItems.get(checkedItems.keyAt(i)) == true) {
                // put path of item into array
                c = (Cursor) getListAdapter().getItem(checkedItems.keyAt(i));
                c.moveToFirst();
                String s = c.getString(c.getColumnIndex(FileDbAdapter.KEY_FILEPATH));
                selectedInstances.add(s);
            }
        }
        if (c != null) {
            c.close();
        }

        // bundle intent with upload files
        Intent i = new Intent(this, InstanceUploaderActivity.class);
        i.putExtra(SharedConstants.KEY_INSTANCES, selectedInstances);
        startActivity(i);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, MENU_UPLOAD, 0, R.string.send_selected_data).setIcon(
                android.R.drawable.ic_menu_upload);
        return true;
    }


    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
            case MENU_UPLOAD:
                if (getListView().getCheckedItemPositions().size() > 0) {
                    uploadSelectedData();
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.noselect_error),
                            Toast.LENGTH_SHORT).show();
                }
                return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }


    @Override
    protected void onResume() {
        super.onResume();
        buildView();
    }


}
