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

import java.util.ArrayList;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * Responsible for displaying all the valid forms in the forms directory. Stores
 * the path to selected form for use by {@link MainMenu}.
 * 
 * @author Carl Hartung (carlhartung@gmail.com)
 */


/* TODO: It'd be great to be able to long click on a form and see a log of 
 * successful and unsuccessful submission and servers they were submitted to.
 * 
 */
public class DataUploader extends ListActivity {

    private final String t = "FormChooser";
    private ArrayList<String> mFileList;

    private static final int MENU_SET_SERVER = Menu.FIRST;
    private static final int MENU_UPLOAD = Menu.FIRST + 1;



    /*
     * (non-Javadoc)
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(t, "called onCreate");

        mFileList = FileUtils.getFilesAsArrayList(SharedConstants.ANSWERS_PATH);
        ArrayAdapter<String> fileAdapter =
                new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice,
                        mFileList);
        getListView().setItemsCanFocus(false);
        getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        setListAdapter(fileAdapter);

        PreferenceManager.setDefaultValues(this, R.xml.server_preferences, false);
    }


    /*
     * (non-Javadoc)
     * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, MENU_SET_SERVER, 0, "set server");
        menu.add(0, MENU_UPLOAD, 0, "upload");
        return true;
    }


    /*
     * (non-Javadoc)
     * @see android.app.Activity#onMenuItemSelected(int, android.view.MenuItem)
     */
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
            case MENU_UPLOAD:
                SparseBooleanArray s = this.getListView().getCheckedItemPositions();

                ArrayList<String> toUpload = new ArrayList<String>();
                for (int i = 0; i < s.size(); i++) {
                    if (s.get(s.keyAt(i)) == true) {
                        Log.e(t, "adding " + getListView().getItemAtPosition(s.keyAt(i)));
                        toUpload.add(getListView().getItemAtPosition(s.keyAt(i)).toString());
                    }
                }

                Intent i = new Intent(this, UploaderActivity.class);
                Bundle b = new Bundle();
                b.putStringArrayList("UPLOAD", toUpload);
                i.putExtra("BUNDLE", b);
                startActivity(i);
                return true;
            case MENU_SET_SERVER:
                Intent launchPreferencesIntent =
                        new Intent().setClass(this, ServerPreferences.class);
                startActivity(launchPreferencesIntent);
                return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }
}
