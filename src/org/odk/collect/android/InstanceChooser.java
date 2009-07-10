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

import java.io.File;
import java.util.ArrayList;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

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
    

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Log.i(t, "called onCreate");

        // start file lister with app name, path to search, display style
        // super.initialize(getString(R.string.edit_data),
        // SharedConstants.ANSWERS_PATH, 0);

        mFileList = FileUtils.getFilesAsArrayList(SharedConstants.ANSWERS_PATH);
        ArrayAdapter<String> fileAdapter =
                new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mFileList);
        setListAdapter(fileAdapter);


    }



    /**
     * Stores the path of clicked file in the intent and exits.
     */
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {

        File f = new File(SharedConstants.ANSWERS_PATH + "/" + mFileList.get(position));

        Intent i = new Intent();
        i.putExtra(SharedConstants.FILEPATH_KEY, f.getAbsolutePath());
        setResult(RESULT_OK, i);

        finish();
    }

}
