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

package org.google.android.odk;

import android.app.ListActivity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Select a file from a file listing and return it in an intent.
 * 
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */
public abstract class FileChooser extends ListActivity {

    private final String t = "File Chooser";

    protected ArrayList<String> mFileList;
    protected File mRoot;
    private boolean mRadio;


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        setTheme(SharedConstants.APPLICATION_THEME);

        super.onCreate(savedInstanceState);
        Log.i(t, "called onCreate");

        setContentView(R.layout.filelister);


    }


    public void initialize(String title, String path, Boolean radio) {
        setTitle(getString(R.string.app_name) + " > " + title);
        mRadio = radio;
        mFileList = new ArrayList<String>();
        if (getDirectory(path)) {
            getFiles(mRoot);
            displayFiles();
        }

    }


    public void refreshRoot() {
        getFiles(mRoot);
        displayFiles();
    }


    private boolean getDirectory(String path) {

        TextView tv = (TextView) findViewById(R.id.filelister_message);

        // check to see if there's an sd card.
        String cardstatus = Environment.getExternalStorageState();
        if (cardstatus.equals(Environment.MEDIA_REMOVED)
                || cardstatus.equals(Environment.MEDIA_UNMOUNTABLE)
                || cardstatus.equals(Environment.MEDIA_UNMOUNTED)
                || cardstatus.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
            tv.setText(getString(R.string.sdcard_error));
            return false;
        }

        // if storage directory does not exist, create it.
        boolean made = true;
        mRoot = new File(path);
        if (!mRoot.exists()) {
            made = mRoot.mkdirs();
        }

        if (!made) {
            tv.setText(getString(R.string.directory_error, path));
            return false;
        } else {
            return true;
        }
    }



    private void getFiles(File f) {
        if (f.isDirectory()) {
            File[] childs = f.listFiles();
            for (File child : childs) {
                getFiles(child);
            }
            return;
        }
        String filename = f.getName();
        if (filename.matches(SharedConstants.VALID_FILENAME)) {
            mFileList.add(filename);
        }
    }



    /**
     * Opens the directory, puts valid files in array adapter for display
     */
    private void displayFiles() {

        ArrayAdapter<String> fileAdapter;
        Collections.sort(mFileList, String.CASE_INSENSITIVE_ORDER);

        if (mRadio) {
            getListView().setItemsCanFocus(false);
            getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            fileAdapter =
                    new ArrayAdapter<String>(this, android.R.layout.simple_list_item_single_choice,
                            mFileList);
        } else {
            fileAdapter =
                    new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mFileList);
        }

        setListAdapter(fileAdapter);

    }


}
