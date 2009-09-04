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

package org.odk.collect.android.activities;

import org.odk.collect.android.R;
import org.odk.collect.android.R.layout;
import org.odk.collect.android.R.string;
import org.odk.collect.android.db.FileDbAdapter;
import org.odk.collect.android.logic.GlobalConstants;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SimpleCursorAdapter;

import java.util.ArrayList;

/**
 * Responsible for displaying all the valid forms in the forms directory. Stores
 * the path to selected form for use by {@link MainMenu}.
 * 
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */


// TODO long click form for submission log
// TODO support individual submits
public class InstanceUploader extends ListActivity {

    private static final int MENU_UPLOAD_ALL = Menu.FIRST;

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

        String[] data = new String[] {FileDbAdapter.KEY_DISPLAY, FileDbAdapter.KEY_META};
        int[] view = new int[] {android.R.id.text1, android.R.id.text2};

        // render total instance view
        mInstances =
                new SimpleCursorAdapter(this, android.R.layout.simple_list_item_2, c, data, view);
        setListAdapter(mInstances);
        if (c.getCount() > 0) {
            setListAdapter(mInstances);
        } else {
            setContentView(R.layout.no_items);
        }

        // set title
        setTitle(getString(R.string.app_name) + " > " + getString(R.string.send_data));

        // cleanup
        fda.close();
    }


    private void uploadAllData() {

        // paths to upload
        ArrayList<String> allInstances = new ArrayList<String>();

        // get all checked items
        Cursor c = null;

        for (int i = 0; i < mInstances.getCount(); i++) {
            c = (Cursor) getListAdapter().getItem(i);
            String s = c.getString(c.getColumnIndex(FileDbAdapter.KEY_FILEPATH));
            allInstances.add(s);
        }

        if (c != null) {
            c.close();
        }

        // bundle intent with upload files
        Intent i = new Intent(this, InstanceUploaderActivity.class);
        i.putExtra(GlobalConstants.KEY_INSTANCES, allInstances);
        startActivity(i);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, MENU_UPLOAD_ALL, 0, R.string.send_data).setIcon(
                android.R.drawable.ic_menu_upload);
        return true;
    }


    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
            case MENU_UPLOAD_ALL:
                uploadAllData();
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
