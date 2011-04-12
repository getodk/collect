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
import org.odk.collect.android.provider.SubmissionsStorage;
import org.odk.collect.android.utilities.FileUtils;

import android.app.ListActivity;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

/**
 * Responsible for displaying all the valid instances in the instance directory.
 * 
 * @author Yaw Anokwa (yanokwa@gmail.com)
 * @author Carl Hartung (carlhartung@gmail.com)
 */
public class InstanceChooserList extends ListActivity {
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chooser_list_layout);
        setTitle(getString(R.string.app_name) + " > " + getString(R.string.review_data));
        refreshView();
    }


    /**
     * Stores the path of selected instance in the parent class and finishes.
     */
    @Override
    protected void onListItemClick(ListView listView, View view, int position, long id) {
        // get full path to the instance
        Cursor c = (Cursor) getListAdapter().getItem(position);
        String instanceDirPath = c.getString(c.getColumnIndex(SubmissionsStorage.KEY_INSTANCE_DIRECTORY_PATH));

        String formpath = null;
        Cursor fp = null;
        Cursor fi = null;
        try {
        	fp = getContentResolver().query(
        			ContentUris.withAppendedId( SubmissionsStorage.CONTENT_URI_FORMS_INFO_URI_DATASET,
        										c.getLong(c.getColumnIndex(SubmissionsStorage.KEY_ID))),
        		new String[] { SubmissionsStorage.KEY_ID, SubmissionsStorage.KEY_URI_FORMS_INFO },
        		null, null, null );
        
        	String uri = null;
        	if ( fp.moveToNext() ) {
        		uri = fp.getString(fp.getColumnIndex(SubmissionsStorage.KEY_URI_FORMS_INFO));
        	}
        	if ( uri != null ) {
        		Uri u = Uri.parse(uri);
        		fi = getContentResolver().query(u,
        				new String[] { FormsStorage.KEY_ID, FormsStorage.KEY_FORM_FILE_PATH },
        				null, null, null );
        		if ( fi.moveToNext() ) {
        			formpath = fi.getString(fi.getColumnIndex(FormsStorage.KEY_FORM_FILE_PATH));
        		}
        	}
        } finally {
        	if ( fp != null ) {
        		fp.close();
        	}
        	if ( fi != null ) {
        		fi.close();
        	}
        }

        if ( formpath != null ) {
            // create intent for return and store path
            Intent i = new Intent();
            i.putExtra(FormEntryActivity.KEY_INSTANCEPATH, FileUtils.getInstanceFilePath(instanceDirPath));
            i.putExtra(FormEntryActivity.KEY_FORMPATH, formpath);

            // return the result to the parent class
            // getParent().setResult(RESULT_OK, i);
            setResult(RESULT_OK, i);
        } else {
        	// form defn not available...
        	// TODO: communicate back the error?
        }
        
        // don't close cursor or tab host delays closing
        finish();
    }


    /**
     * Retrieves instance information from {@link SubmissionsStorage}, composes and displays each row.
     */
    private void refreshView() {
    	
    	String[] projection = new String[] {
    			SubmissionsStorage.KEY_ID, 
    			SubmissionsStorage.KEY_DISPLAY_NAME,
    			SubmissionsStorage.KEY_DISPLAY_SUBTEXT,
    			SubmissionsStorage.KEY_INSTANCE_DIRECTORY_PATH
    	};

    	// create data and views for cursor adapter
        String[] data = new String[] {
        		SubmissionsStorage.KEY_DISPLAY_NAME,
        		SubmissionsStorage.KEY_DISPLAY_SUBTEXT
        };
        int[] view = new int[] {
                android.R.id.text1, android.R.id.text2
        };
        String sortOrder = SubmissionsStorage.KEY_DISPLAY_NAME + " ASC";
        
        Cursor c = getContentResolver().query(SubmissionsStorage.CONTENT_URI_INFO_DATASET,
        		projection, null, null, sortOrder);
        startManagingCursor(c);
        
        // render total instance view
        SimpleCursorAdapter instances =
            new SimpleCursorAdapter(this, android.R.layout.simple_list_item_2, c, data, view);
        setListAdapter(instances);
    }

}
