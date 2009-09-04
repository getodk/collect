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

package org.odk.collect.android.activities;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import org.odk.collect.android.R;
import org.odk.collect.android.db.FileDbAdapter;
import org.odk.collect.android.logic.GlobalConstants;
import org.odk.collect.android.utils.FileUtils;
import org.odk.collect.android.utils.NaturalOrderComparator;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * Responsible for displaying all the valid forms in the forms directory. Stores
 * the path to selected form for use by {@link MainMenu}.
 * 
 * @author Yaw Anokwa (yanokwa@gmail.com)
 * @author Carl Hartung (carlhartung@gmail.com)
 */
public class FormChooser extends ListActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildView();
    }


    @Override
    protected void onResume() {
        super.onResume();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
    }


    /**
     * Get form list from database and insert into view.
     */
    private void buildView() {

        updateFormDirectory();

        setTitle(getString(R.string.app_name) + " > " + getString(R.string.enter_data));

        // get all forms that match the status.
        FileDbAdapter fda = new FileDbAdapter(this);
        fda.open();
        Cursor c = fda.fetchFiles(FileDbAdapter.TYPE_FORM, null);
        startManagingCursor(c);

        // create data and views for cursor adapter
        String[] data = new String[] {FileDbAdapter.KEY_DISPLAY, FileDbAdapter.KEY_META};
        int[] view = new int[] {android.R.id.text1, android.R.id.text2};

        // render total instance view
        SimpleCursorAdapter instances =
                new SimpleCursorAdapter(this, android.R.layout.simple_list_item_2, c, data, view);
        setListAdapter(instances);
        if (c.getCount() > 0) {
            setListAdapter(instances);
        } else {
            setContentView(R.layout.no_items);
        }

        // cleanup
        fda.close();
    }


    /**
     * Stores new forms in the database and removes old processed formdefs.
     */
    private void updateFormDirectory() {

        // full path to the files
        ArrayList<String> storedForms = FileUtils.getFilesAsArrayList(GlobalConstants.FORMS_PATH);
        ArrayList<String> cachedForms = FileUtils.getFilesAsArrayList(GlobalConstants.CACHE_PATH);

        // the hashes of the forms in the db
        HashMap<String, String> availableForms = new HashMap<String, String>();

        FileDbAdapter fda = new FileDbAdapter(this);
        fda.open();

        // find all forms in database and grab all their hashes and filenames
        Cursor c = null;
        c = fda.fetchFiles(FileDbAdapter.TYPE_FORM, null);
        if (c != null) {
            if (c.moveToFirst()) {
                int i = c.getColumnIndex(FileDbAdapter.KEY_HASH);
                int j = c.getColumnIndex(FileDbAdapter.KEY_FILEPATH);
                do {
                    availableForms.put(c.getString(i), c.getString(j));
                } while (c.moveToNext());
            }
        }
        // clean up cursor
        c.close();


        // sort, then loop through forms on sdcard. add and remove as necessary.
        if (storedForms != null) {
            Collections.sort(storedForms, NaturalOrderComparator.NUMERICAL_ORDER);
            for (String formPath : storedForms) {
                String hash = FileUtils.getMd5Hash(new File(formPath));
                // if hash is not in db, add the form.
                if (!availableForms.containsKey((hash))) {
                    fda.createFile(formPath, FileDbAdapter.TYPE_FORM,
                            FileDbAdapter.STATUS_AVAILABLE);
                } else if (availableForms.containsKey((hash))) {
                    // if duplicate form found on sd card, remove it.
                    if (!formPath.equals(availableForms.get(hash))) {
                        (new File(formPath)).delete();
                    }
                }
            }
        }


        // clean up adapter
        fda.close();

        // remove orphaned form defs
        if (cachedForms != null) {
            for (String cachePath : cachedForms) {
                String hash =
                        cachePath.substring(cachePath.lastIndexOf("/") + 1, cachePath
                                .lastIndexOf("."));
                if (!availableForms.containsKey(hash)) {
                    (new File(cachePath)).delete();
                }
            }
        }


    }


    /**
     * Stores the path of selected form and finishes.
     */
    @Override
    protected void onListItemClick(ListView listView, View view, int position, long id) {

        // get full path to the form
        Cursor c = (Cursor) getListAdapter().getItem(position);
        String formPath = c.getString(c.getColumnIndex(FileDbAdapter.KEY_FILEPATH));

        // create intent for return and store path
        Intent i = new Intent();
        i.putExtra(GlobalConstants.KEY_FORMPATH, formPath);
        setResult(RESULT_OK, i);

        // close cursor and finish
        c.close();
        finish();
    }

}
