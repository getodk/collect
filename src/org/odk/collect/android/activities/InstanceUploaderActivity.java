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
import org.odk.collect.android.listeners.InstanceUploaderListener;
import org.odk.collect.android.preferences.PreferencesActivity;
import org.odk.collect.android.provider.InstanceProviderAPI;
import org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns;
import org.odk.collect.android.tasks.InstanceUploaderTask;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Activity to upload completed forms.
 * 
 * @author Carl Hartung (carlhartung@gmail.com)
 */
public class InstanceUploaderActivity extends Activity implements InstanceUploaderListener {

    private final static int PROGRESS_DIALOG = 1;
    private final static String KEY_TOTALCOUNT = "totalcount";
    private ProgressDialog mProgressDialog;

    private InstanceUploaderTask mInstanceUploaderTask;
    private int totalCount = -1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(getString(R.string.app_name) + " > " + getString(R.string.send_data));

        // get instances to upload
        Intent i = getIntent();
        ArrayList<String> instances = i.getStringArrayListExtra(FormEntryActivity.KEY_INSTANCES);
        if (instances == null) {
            // nothing to upload
            return;
        }

        for (int j = 0; j < instances.size(); j++) {
            Log.e("Carl", "instance: " + instances.get(j));
        }
        
        // get the task if we've changed orientations. If it's null it's a new upload.
        mInstanceUploaderTask = (InstanceUploaderTask) getLastNonConfigurationInstance();
        if (mInstanceUploaderTask == null) {
            // setup dialog and upload task
            showDialog(PROGRESS_DIALOG);
            mInstanceUploaderTask = new InstanceUploaderTask();

            SharedPreferences settings =
                PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            String url =
                settings.getString(PreferencesActivity.KEY_SERVER_URL, getString(R.string.default_server_url))
                        + "/submission";
            mInstanceUploaderTask.setUploadServer(url);
            totalCount = instances.size();

            // convert array list to an array
            String[] sa = instances.toArray(new String[totalCount]);
            mInstanceUploaderTask.execute(sa);
        }
    }


    // TODO: if uploadingComplete() when activity backgrounded, won't work.
    // just check task status in onResume
    @Override
    public void uploadingComplete(ArrayList<String> result) {
        int resultSize = result.size();
        boolean success = false;
        if (resultSize == totalCount) {
            Toast.makeText(this, getString(R.string.upload_all_successful, totalCount),
                Toast.LENGTH_SHORT).show();

            success = true;
        } else {
            String s = totalCount - resultSize + " of " + totalCount;
            Toast.makeText(this, getString(R.string.upload_some_failed, s), Toast.LENGTH_LONG)
                    .show();
        }

        Intent in = new Intent();
        in.putExtra(FormEntryActivity.KEY_SUCCESS, success);
        setResult(RESULT_OK, in);

        for (int i = 0; i < resultSize; i++) {
            ContentValues values = new ContentValues();
            values.put(InstanceColumns.STATUS, InstanceProviderAPI.STATUS_SUBMITTED);
            String where = InstanceColumns.INSTANCE_FILE_PATH + " =?";
            String [] selectionArgs = {result.get(i)};
            getContentResolver().update(InstanceColumns.CONTENT_URI, values, where, selectionArgs);
      }
        finish();
    }


    @Override
    public void progressUpdate(int progress, int total) {
        mProgressDialog.setMessage(getString(R.string.sending_items, progress, total));
    }


    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case PROGRESS_DIALOG:
                mProgressDialog = new ProgressDialog(this);
                DialogInterface.OnClickListener loadingButtonListener =
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            mInstanceUploaderTask.setUploaderListener(null);
                            finish();
                        }
                    };
                mProgressDialog.setTitle(getString(R.string.uploading_data));
                mProgressDialog.setMessage(getString(R.string.please_wait));
                mProgressDialog.setIndeterminate(true);
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                mProgressDialog.setCancelable(false);
                mProgressDialog.setButton(getString(R.string.cancel), loadingButtonListener);
                return mProgressDialog;
        }
        return null;
    }


    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        totalCount = savedInstanceState.getInt(KEY_TOTALCOUNT);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_TOTALCOUNT, totalCount);
    }


    @Override
    public Object onRetainNonConfigurationInstance() {
        return mInstanceUploaderTask;
    }


    @Override
    protected void onDestroy() {
        mInstanceUploaderTask.setUploaderListener(null);
        super.onDestroy();
    }


    @Override
    protected void onResume() {
        if (mInstanceUploaderTask != null) {
            mInstanceUploaderTask.setUploaderListener(this);
        }
        super.onResume();
    }

}
