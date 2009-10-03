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

import java.util.ArrayList;

import org.odk.collect.android.R;
import org.odk.collect.android.database.FileDbAdapter;
import org.odk.collect.android.logic.GlobalConstants;
import org.odk.collect.android.preferences.ServerPreferences;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

/**
 * Responsible for displaying all the valid forms in the forms directory. Stores
 * the path to selected form for use by {@link MainMenuActivity}.
 * 
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */


// TODO long click form for submission log
public class InstanceUploaderList extends ListActivity {


    private static final int MENU_PREFERENCES = Menu.FIRST;
    private static final int MENU_UPLOAD_ALL = Menu.FIRST+1;

    private static final int INSTANCE_UPLOADER = 0;

    private SimpleCursorAdapter mInstances;
    private ArrayList<Long> mSelected = new ArrayList<Long>();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.instance_uploader_list);
        
        Button b = (Button)findViewById(R.id.upload_button);
        b.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				if (mSelected.size() > 0) {
                    // items selected
                    uploadSelectedFiles();
                    refreshData();
                } else {
                    // no items selected
                    Toast.makeText(getApplicationContext(), getString(R.string.noselect_error),
                            Toast.LENGTH_SHORT).show();
                }
			}
        	
        });
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
        Cursor c =
                fda.fetchFilesByType(FileDbAdapter.TYPE_INSTANCE, FileDbAdapter.STATUS_COMPLETE);
        startManagingCursor(c);

        String[] data = new String[] {FileDbAdapter.KEY_DISPLAY, FileDbAdapter.KEY_META};
        int[] view = new int[] {R.id.text1, R.id.text2};

        // render total instance view
        mInstances =
                new SimpleCursorAdapter(this, R.layout.two_item_multiple_choice, c, data, view);
            setListAdapter(mInstances);

        // set title
        setTitle(getString(R.string.app_name) + " > " + getString(R.string.send_data));

        // cleanup
        fda.close();
    }


    private void uploadSelectedFiles() {

        ArrayList<String> allInstances = new ArrayList<String>();

        // get all checked items
        FileDbAdapter fda = new FileDbAdapter(this);
        fda.open();

        Cursor c = null;

        for (int i = 0; i < mSelected.size(); i++) {
            c = fda.fetchFile(mSelected.get(i));
            startManagingCursor(c);
            String s = c.getString(c.getColumnIndex(FileDbAdapter.KEY_FILEPATH));
            allInstances.add(s);
        }

        // bundle intent with upload files
        Intent i = new Intent(this, InstanceUploaderActivity.class);
        i.putExtra(GlobalConstants.KEY_INSTANCES, allInstances);
        startActivityForResult(i, INSTANCE_UPLOADER);
        fda.close();
    }


    private void refreshData() {
        if (mInstances != null) {
            mInstances.getCursor().requery();
        }
        mSelected.clear();
        buildView();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, MENU_PREFERENCES, 0, getString(R.string.server_preferences)).setIcon(
                android.R.drawable.ic_menu_preferences);
        return true;
    }


    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
            case MENU_PREFERENCES:
                createPreferencesMenu();
                return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }


    private void createPreferencesMenu() {
        Intent i = new Intent(this, ServerPreferences.class);
        startActivity(i);
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
    protected void onResume() {
        refreshData();
        super.onResume();
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode == RESULT_CANCELED) {
            return;
        }
        switch (requestCode) {
            // returns with a form path, start entry
            case INSTANCE_UPLOADER:
                if (intent.getBooleanExtra(GlobalConstants.KEY_SUCCESS, false)) {
                    finish();
                }
                break;
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, intent);
    }


}
