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

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Responsible for displaying all the valid forms in the forms directory. Stores
 * the path to selected form for use by {@link MainMenu}.
 * 
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */
public class InstanceChooser extends ListActivity {
    private final String t = "Instance Chooser";
    private ArrayList<String> mFileList;
   // private static final int MENU_SET_SERVER = Menu.FIRST;


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Log.i(t, "called onCreate");

        setTheme(SharedConstants.APPLICATION_THEME);
        setTitle(getString(R.string.app_name) + " > " + getString(R.string.edit_data));
        setContentView(R.layout.filelister);

        mFileList = FileUtils.getFilesAsArrayListRecursive(SharedConstants.ANSWERS_PATH);
        Collections.sort(mFileList);

        refresh();

      //  PreferenceManager.setDefaultValues(this, R.xml.saved_preferences, false);
    }


    /**
     * Stores the path of clicked file in the intent and exits.
     */
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Cursor c = (Cursor) this.getListAdapter().getItem(position);
        String name = c.getString(c.getColumnIndex(FileDbAdapter.KEY_FILENAME));
        Log.e("carl", "doing " + name);
        File f = new File(SharedConstants.ANSWERS_PATH + "/" + name + "/" + name + ".xml");

        Intent i = new Intent();
        i.putExtra(SharedConstants.FILEPATH_KEY, f.getAbsolutePath());
        setResult(RESULT_OK, i);

        finish();
    }


    /*
     * (non-Javadoc)
     * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
     */
    /* @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, MENU_SET_SERVER, 0, "Display Settings");
        return true;
    } */


    /*
     * (non-Javadoc)
     * @see android.app.Activity#onMenuItemSelected(int, android.view.MenuItem)
     */
    /*
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
            case MENU_SET_SERVER:
                Intent launchPreferencesIntent =
                        new Intent().setClass(this, SavedPreferences.class);
                startActivity(launchPreferencesIntent);
                return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }
*/

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onResume()
     */
    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }


    private void refresh() {
       // SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(this);
        Intent i = getIntent();
        String status = i.getStringExtra("status");

        FileDbAdapter fda = new FileDbAdapter(this);
        fda.open();
        Cursor c = fda.fetchFiles(status);
        startManagingCursor(c);

        String[] from = new String[] {FileDbAdapter.KEY_FILENAME};
        int[] to = new int[] {android.R.id.text1};

        // Now create an array adapter and set it to display using our row
        SimpleCursorAdapter notes =
                new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, c, from, to);
        setListAdapter(notes);
        fda.close();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        Log.i("yaw", "oar instancechooser");


    }

}
