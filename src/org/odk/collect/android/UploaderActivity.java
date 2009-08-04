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

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

/**
 * Activity to upload completed forms.
 * 
 * @author Carl Hartung (carlhartung@gmail.com)
 * 
 */
public class UploaderActivity extends Activity implements UploaderListener {

    private static final String t = "UploaderActivity";
    private final static int PROGRESS_DIALOG = 1;
    private ProgressDialog mProgressDialog;
    private UploaderTask mUploaderTask;
    private int numUploading = -1;


    /*
     * (non-Javadoc)
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ArrayList<String> toUpload = null;
        Intent i = this.getIntent();
        Bundle b = i.getBundleExtra("BUNDLE");
        if (b != null) {
            toUpload = b.getStringArrayList("UPLOAD");
        } else {
            // nothing to upload
            return;
        }

        mUploaderTask = (UploaderTask) getLastNonConfigurationInstance();
        if (mUploaderTask == null) {
            showDialog(PROGRESS_DIALOG);
            mUploaderTask = new UploaderTask();
            SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(this);
            String server =
                    p.getString("UploadServer", "http://opendatakit.appspot.com/submission");
            Log.e(t, "Uploading to server: " + server);
            mUploaderTask.setUploadServer(server);
            numUploading = toUpload.size();
            mUploaderTask.execute(toUpload.toArray(new String[toUpload.size()]));
        } else {
            Log.e("testing", "already running");
        }
    }


    /*
     * (non-Javadoc)
     * @see android.app.Activity#onRestoreInstanceState(android.os.Bundle)
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        numUploading = savedInstanceState.getInt("uploading");
    }


    /*
     * (non-Javadoc)
     * @see android.app.Activity#onSaveInstanceState(android.os.Bundle)
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("uploading", numUploading);
    }


    /*
    * (non-Javadoc)
    * @see android.app.Activity#onRetainNonConfigurationInstance()
    */
    @Override
    public Object onRetainNonConfigurationInstance() {
        return mUploaderTask;
    }


    /*
     * (non-Javadoc)
     * @see android.app.Activity#onDestroy()
     */
    @Override
    protected void onDestroy() {
        mUploaderTask.setUploaderListener(null);
        super.onDestroy();
    }


    /*
     * (non-Javadoc)
     * @see android.app.Activity#onResume()
     */
    @Override
    protected void onResume() {
        if (mUploaderTask != null) mUploaderTask.setUploaderListener(this);
        super.onResume();
    }


    /*
     * (non-Javadoc)
     * @see org.odk.collect.android.UploaderListener#uploadingComplete()
     */
    public void uploadingComplete(ArrayList<String> result) {
        // TODO: his needs to be changed. If the uploadingComplete() happens
        // when the activity is in the background
        // this won't work. don't change the orientation. fix coming soon.
        Log.e("carl", "results = " + result.size() + " and numuploading = " + numUploading);
        if (result.size() == numUploading) {
            Toast.makeText(this, "Uploads Completed Successfully!", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this,
                    numUploading - result.size() + " of " + numUploading + " uploads failed",
                    Toast.LENGTH_LONG).show();
        }

        FileDbAdapter fda = new FileDbAdapter(this);
        for (int i = 0; i < result.size(); i++) {
            String s = result.get(i);
            fda.open();
            fda.updateFile(s, "submitted");
            fda.close();
        }

        finish();
    }


    /*
     * (non-Javadoc)
     * @see org.odk.collect.android.UploaderListener#progressUpdate(int, int)
     */
    public void progressUpdate(int progress, int total) {
        mProgressDialog.setMax(total);
        mProgressDialog.setProgress(progress);
    }


    /*
     * (non-Javadoc)
     * @see android.app.Activity#onCreateDialog(int)
     */
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case PROGRESS_DIALOG:
                mProgressDialog = new ProgressDialog(this);
                DialogInterface.OnClickListener loadingButtonListener =
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                mUploaderTask.setUploaderListener(null);
                                finish();
                            }
                        };
                mProgressDialog.setMessage("Uploading data");
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mProgressDialog.setCancelable(false);
                mProgressDialog.setMax(0);
                mProgressDialog.setButton(getString(R.string.cancel), loadingButtonListener);
                return mProgressDialog;
        }
        return null;
    }

}
