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

import java.util.ArrayList;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

/**
 * Responsible for displaying all the valid forms in the forms directory. Stores
 * the path to selected form for use by {@link MainMenu}.
 * 
 * @author Carl Hartung (carlhartung@gmail.com)
 */
public class DataUploader extends ListActivity {

    private final String t = "FormChooser";
    private ArrayList<String> mFileList;

    private static final int MENU_UPLOAD = Menu.FIRST;


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

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, MENU_UPLOAD, 0, "upload");
        return true;
    }


    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
            case MENU_UPLOAD:
                SparseBooleanArray s = this.getListView().getCheckedItemPositions();

                Log.e("carl", "got items = " + s.size());
                ArrayList<String> toUpload = new ArrayList<String>();
                for (int i = 0; i < s.size(); i++) {
                    if (s.get(s.keyAt(i)) == true) {
                        Log.e("testing", "adding " + getListView().getItemAtPosition(s.keyAt(i)));
                        toUpload.add(getListView().getItemAtPosition(s.keyAt(i)).toString());
                    }
                }
                Toast.makeText(this, "uploading", Toast.LENGTH_LONG).show();
                
                Intent i = new Intent(this, UploaderActivity.class);
                Bundle b = new Bundle();
                b.putStringArrayList("UPLOAD", toUpload);
                i.putExtra("BUNDLE", b);
                startActivity(i);
                return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }

}
