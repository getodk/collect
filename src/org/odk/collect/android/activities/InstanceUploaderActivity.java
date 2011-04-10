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

import java.util.ArrayList;

import org.odk.collect.android.R;
import org.odk.collect.android.listeners.InstanceUploaderListener;
import org.odk.collect.android.preferences.ServerPreferences;
import org.odk.collect.android.provider.SubmissionsStorage;
import org.odk.collect.android.tasks.InstanceUploaderTask;
import org.odk.collect.android.utilities.PasswordPromptDialogBuilder;
import org.odk.collect.android.utilities.WebUtils;
import org.odk.collect.android.utilities.PasswordPromptDialogBuilder.OnOkListener;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;

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

    private static final class UploadArgs {
    	String url;
    	ArrayList<String> instances;
    }

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

        // get the task if we've changed orientations. If it's null it's a new upload.
        mInstanceUploaderTask = (InstanceUploaderTask) getLastNonConfigurationInstance();
        if (mInstanceUploaderTask == null) {
            SharedPreferences settings =
                PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            String url =
                settings.getString(ServerPreferences.KEY_SERVER, getString(R.string.default_server))
                        + "/submission";
            
            UploadArgs args = new UploadArgs();
            args.instances = instances;
            args.url = url;
            boolean deferForPassword = false;
            String username =
            	settings.getString(ServerPreferences.KEY_USERNAME, null);
            if (username != null && username.length() != 0 ) {
            	Uri u = Uri.parse(url);
            	if ( !WebUtils.hasCredentials(username, u.getHost()) ) {
            		PasswordPromptDialogBuilder b = 
            			new PasswordPromptDialogBuilder(this, 
            											username, 
            											u.getHost(),
            											new OnOkListener() {

															@Override
															public void onOk(
																	Object okListenerContext) {
																UploadArgs args = (UploadArgs) okListenerContext;
																InstanceUploaderActivity.this.executeUpload(args);
															}
            				
            			},
            			args);
            		deferForPassword = true;
            		b.show();
            	}
            }
            if ( !deferForPassword ) {
            	executeUpload(args);
            }
        }
    }

    private void executeUpload(UploadArgs args) {
        // setup dialog and upload task
        showDialog(PROGRESS_DIALOG);
        mInstanceUploaderTask = new InstanceUploaderTask();
        
        mInstanceUploaderTask.setUploadServer(args.url);
        totalCount = args.instances.size();

        // convert array list to an array
        String[] sa = args.instances.toArray(new String[totalCount]);
        mInstanceUploaderTask.execute(sa);
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

        // for each path, update the status
        for (int i = 0; i < resultSize; i++) {
        	ContentValues values = new ContentValues();
        	values.put(SubmissionsStorage.KEY_STATUS, SubmissionsStorage.STATUS_SUBMITTED);
        	try {
        		getContentResolver().update(
        			SubmissionsStorage.CONTENT_URI_INFO_DATASET, 
        			values, 
        			SubmissionsStorage.KEY_INSTANCE_FILE_PATH + "= ?", 
        			new String[] { result.get(i) });
        	} catch ( Exception e ) {
        		e.printStackTrace();
        	}
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
