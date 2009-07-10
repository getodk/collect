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
import android.os.Bundle;
import android.util.Log;

/**
 * Activity to upload completed forms.
 * 
 * @author Carl Hartung (carlhartung@gmail.com)
 *
 */
public class UploaderActivity extends Activity implements UploaderListener {

    private final static int PROGRESS_DIALOG = 1;
    private ProgressDialog mProgressDialog;
    private UploaderTask mUploaderTask;


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
            //for (int j = 0; j < toUpload.size(); j++) {
            //    Log.e("testing", "got " + toUpload.get(j));
            //}
        } else {
            // nothing to upload
            return;
        }

        mUploaderTask = (UploaderTask) getLastNonConfigurationInstance();
        if (mUploaderTask == null) {
            showDialog(PROGRESS_DIALOG);
            mUploaderTask = new UploaderTask();
            mUploaderTask.execute(toUpload.toArray(new String[toUpload.size()]));
        } else {
            Log.e("testing", "alreaedy running");
        }
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
    public void uploadingComplete() {
        //do some intelligent toast here.
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
                mProgressDialog.setMessage(getString(R.string.loading_form));
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mProgressDialog.setCancelable(false);
                mProgressDialog.setMax(0);
                mProgressDialog.setButton(getString(R.string.cancel), loadingButtonListener);
                return mProgressDialog;
        }
        return null;
    }



}
