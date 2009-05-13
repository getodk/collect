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
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Responsible for displaying all the valid forms in the forms directory. Stores
 * the path to selected form for use by {@link MainMenu}.
 * 
 * @author Carl Hartung
 */
public class FormChooser extends ListActivity {

    private List<String> mFormNames;
    private File mFormsDirectory;
    private String mFormPath;
    private String mFormsPath;
    //private final String t = "FormChooser";


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.formchooser);
        setTitle(getString(R.string.app_name) + " > " + getString(R.string.choose_form));

        mFormNames = new ArrayList<String>();
        mFormPath = SharedConstants.FORMPATH_KEY;
        mFormsPath = SharedConstants.FORMS_PATH;

        TextView tv = (TextView) findViewById(R.id.formchooser_message);

        // Make sure there is an SD cared present.
        String cardstatus = Environment.getExternalStorageState();
        if (cardstatus.equals(Environment.MEDIA_REMOVED)
                || cardstatus.equals(Environment.MEDIA_UNMOUNTABLE)
                || cardstatus.equals(Environment.MEDIA_UNMOUNTED)) {
            tv.setText(getString(R.string.sdcard_error));
            return;
        }

        // Check if our storage directory exists and create it if not.
        boolean made = true;
        mFormsDirectory = new File(mFormsPath);
        if (!mFormsDirectory.exists()) {
            made = mFormsDirectory.mkdirs();
        }
        if (!made) {
            tv.setText(getString(R.string.directory_error, mFormsPath));
            return;
        }

        displayForms();
    }


    /**
     * Displays the valid forms on the screen.
     */
    private void displayForms() {
        mFormNames.clear();
        File[] files = mFormsDirectory.listFiles();

        for (File f : files) {
            String fileName =
                    f.getAbsolutePath().substring(mFormsDirectory.getAbsolutePath().length() + 1);
            if (fileName.matches(SharedConstants.VALID_FORMNAME)) {
                mFormNames.add(fileName);
            }
        }

        Collections.sort(mFormNames, String.CASE_INSENSITIVE_ORDER);

        ArrayAdapter<String> directoryList =
                new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mFormNames);
        setListAdapter(directoryList);
    }


    /**
     * Stores the path of clicked file in the intent and exits.
     */
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        File f = new File(mFormsDirectory.getAbsolutePath() + "/" + mFormNames.get(position));

        Bundle b = new Bundle();
        b.putString(mFormPath, f.getAbsolutePath());

        Intent i = new Intent();
        i.putExtras(b);
        setResult(RESULT_OK, i);
        finish();
    }
}
