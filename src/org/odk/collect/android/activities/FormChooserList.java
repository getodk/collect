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
import org.odk.collect.android.listeners.DiskSyncListener;
import org.odk.collect.android.provider.FormsProviderAPI.FormsColumns;
import org.odk.collect.android.tasks.DiskSyncTask;

import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

/**
 * Responsible for displaying all the valid forms in the forms directory. Stores the path to
 * selected form for use by {@link MainMenuActivity}.
 * 
 * @author Yaw Anokwa (yanokwa@gmail.com)
 * @author Carl Hartung (carlhartung@gmail.com)
 */
public class FormChooserList extends ListActivity implements DiskSyncListener {

    private static final String t = "FormChooserList";
    DiskSyncTask mDiskSyncTask;

    private static final int PROGRESS = 1;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chooser_list_layout);
        setTitle(getString(R.string.app_name) + " > " + getString(R.string.enter_data));

        Cursor managedCursor = managedQuery(FormsColumns.CONTENT_URI, null, null, null, null);
        mDiskSyncTask = (DiskSyncTask) getLastNonConfigurationInstance();
        if (mDiskSyncTask == null) {
            mDiskSyncTask = new DiskSyncTask(managedCursor, getContentResolver());
            mDiskSyncTask.setDiskSyncListener(this);
            mDiskSyncTask.execute((Void[]) null);
            
            TextView tv = (TextView) findViewById(R.id.status_text);
            tv.setVisibility(View.VISIBLE);
        }

        if (mDiskSyncTask.getStatus() == AsyncTask.Status.RUNNING) {
            TextView tv = (TextView) findViewById(R.id.status_text);
            tv.setVisibility(View.VISIBLE);
        }

        if (mDiskSyncTask.getStatus() == AsyncTask.Status.FINISHED) {
            // TODO: set something to done
            mDiskSyncTask.setDiskSyncListener(null);
        }

        Cursor c = managedQuery(FormsColumns.CONTENT_URI, null, null, null, null);

        String[] data = new String[] {
                FormsColumns.DISPLAY_NAME, FormsColumns.DISPLAY_SUBTEXT
        };
        int[] view = new int[] {
                R.id.text1, R.id.text2
        };

        // render total instance view
        SimpleCursorAdapter instances =
            new SimpleCursorAdapter(this, R.layout.two_item, c, data, view);
        setListAdapter(instances);

    }


    @Override
    public Object onRetainNonConfigurationInstance() {
        // pass the thread on restart
        return mDiskSyncTask;
    }


    /**
     * Stores the path of selected form and finishes.
     */
    @Override
    protected void onListItemClick(ListView listView, View view, int position, long id) {
        // get uri to form
        Cursor c = (Cursor) getListAdapter().getItem(position);
        startManagingCursor(c);
        Uri formUri =
            ContentUris.withAppendedId(FormsColumns.CONTENT_URI,
                c.getLong(c.getColumnIndex(FormsColumns._ID)));

        String action = getIntent().getAction();
        if (Intent.ACTION_PICK.equals(action)) {
            // caller is waiting on a picked form
            setResult(RESULT_OK, new Intent().setData(formUri));
        } else {
            // caller wants to view/edit a form, so launch formentryactivity
            startActivity(new Intent(Intent.ACTION_EDIT, formUri));
        }

        finish();

    }


    /**
     * refreshView() in onresume because onCreate doesn't get called when activity returns from
     * background
     */
    @Override
    protected void onResume() {
        mDiskSyncTask.setDiskSyncListener(this);
        super.onResume();
    }


    @Override
    protected void onPause() {
        mDiskSyncTask.setDiskSyncListener(null);
        super.onPause();
    }


    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case PROGRESS:
                ProgressDialog p = new ProgressDialog(this);
                DialogInterface.OnClickListener loadingButtonListener =
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // dialog.dismiss();
                            // p.setFormLoaderListener(null);
                            // p.cancel(true);
                            // finish();
                        }
                    };
                p.setIcon(android.R.drawable.ic_dialog_info);
                p.setTitle(getString(R.string.loading_form));
                p.setMessage(getString(R.string.please_wait));
                p.setIndeterminate(true);
                p.setCancelable(false);
                p.setButton(getString(R.string.cancel_loading_form), loadingButtonListener);
                return p;
        }
        return null;
    }


    @Override
    public void SyncComplete() {

        // TODO: set finished

        TextView tv = (TextView) findViewById(R.id.status_text);
        tv.setVisibility(View.GONE);
    }

}
