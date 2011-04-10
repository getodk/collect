/*
 * Copyright (C) 2009 University of Washington
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.collect.android.activities;

import org.odk.collect.android.R;
import org.odk.collect.android.provider.FormsStorage;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

/**
 * Responsible for displaying all the valid forms in the forms directory. Stores the path to
 * selected form for use by {@link MainMenuActivity}.
 * 
 * @author Yaw Anokwa (yanokwa@gmail.com)
 * @author Carl Hartung (carlhartung@gmail.com)
 */
public class FormChooserList extends ListActivity {
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chooser_list_layout);
        setTitle(getString(R.string.app_name) + " > " + getString(R.string.enter_data));
        refreshView();
    }

	/**
     * Get form list from database and insert into view.
     */
    private void refreshView() {
        String[] projection = new String[] {
        		FormsStorage.KEY_ID,
        		FormsStorage.KEY_FORM_FILE_PATH,
                FormsStorage.KEY_DISPLAY_NAME, 
                FormsStorage.KEY_DISPLAY_SUBTEXT
        };
        
        String[] data = new String[] {
                FormsStorage.KEY_DISPLAY_NAME, 
                FormsStorage.KEY_DISPLAY_SUBTEXT
        };
        int[] view = new int[] {
                android.R.id.text1, android.R.id.text2
        };
        String sortOrder = FormsStorage.KEY_DISPLAY_NAME + " ASC";
        
        Cursor c = getContentResolver().query(FormsStorage.CONTENT_URI_INFO_DATASET,
        								projection,	null, null, sortOrder);
        startManagingCursor(c);

        // render total instance view
        SimpleCursorAdapter instances =
            new SimpleCursorAdapter(this, android.R.layout.simple_list_item_2, c, data, view);
        setListAdapter(instances);
    }

    /**
     * Stores the path of selected form and finishes.
     */
    @Override
    protected void onListItemClick(ListView listView, View view, int position, long id) {
        // get full path to the form
        Cursor c = (Cursor) getListAdapter().getItem(position);
        String formPath = c.getString(c.getColumnIndex(FormsStorage.KEY_FORM_FILE_PATH));
        
        // create intent for return and store path
        Intent i = new Intent();
        i.putExtra(FormEntryActivity.KEY_FORMPATH, formPath);
        setResult(RESULT_OK, i);

        finish();
    }

}
