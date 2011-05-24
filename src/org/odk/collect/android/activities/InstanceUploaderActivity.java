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
import org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns;
import org.odk.collect.android.tasks.InstanceUploaderTask;
import org.odk.collect.android.utilities.WebUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * Activity to upload completed forms.
 * 
 * @author Carl Hartung (carlhartung@gmail.com)
 */
public class InstanceUploaderActivity extends Activity implements InstanceUploaderListener {

    private final static int PROGRESS_DIALOG = 1;
    private final static int AUTH_DIALOG = 2;

    private ProgressDialog mProgressDialog;
    private AlertDialog mAlertDialog;

    private InstanceUploaderTask mInstanceUploaderTask;

    // maintain a list of what we've yet to send, in case we're interrupted by auth requests
    private ArrayList<String> toSend;
    
    // maintain a list of what we've sent, in case we're interrupted by auth requests
    private HashMap<String, String> completed;  
    
    private final static String AUTH_URI = "auth";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        completed = new HashMap<String, String>();

        setTitle(getString(R.string.app_name) + " > " + getString(R.string.send_data));

        // get instances to upload
        Intent intent = getIntent();
        long[] selectedInstanceIDs = intent.getLongArrayExtra(FormEntryActivity.KEY_INSTANCES);
        if (selectedInstanceIDs.length == 0) {
            // nothing to upload
            //TODO:  toast and quit?
            return;
        }


        // get the task if we've changed orientations. If it's null it's a new upload.
        mInstanceUploaderTask = (InstanceUploaderTask) getLastNonConfigurationInstance();
        if (mInstanceUploaderTask == null) {
            // setup dialog and upload task
            showDialog(PROGRESS_DIALOG);
            mInstanceUploaderTask = new InstanceUploaderTask();

            String selection = InstanceColumns._ID + "=?";
            String[] selectionArgs = new String[selectedInstanceIDs.length];
            for (int i = 0; i < selectedInstanceIDs.length; i++) {
                selectionArgs[i] = new Long(selectedInstanceIDs[i]).toString();
                if (i != selectedInstanceIDs.length - 1) {
                    selection += " or " + InstanceColumns._ID + "=?";
                }
            }

            String[] instances = new String[selectedInstanceIDs.length];
            Cursor c =
                managedQuery(InstanceColumns.CONTENT_URI, null, selection, selectionArgs, null);
            if (c.getCount() > 0) {
                c.moveToPosition(-1);
                while (c.moveToNext()) {
                    instances[c.getPosition()] =
                        c.getString(c.getColumnIndex(InstanceColumns.INSTANCE_FILE_PATH));
                }
                mInstanceUploaderTask.execute(instances);
            }
            toSend = new ArrayList<String>(Arrays.asList(instances));

        }
    }


    @Override
    public void uploadingComplete(HashMap<String, String> result) {
        try {
            dismissDialog(PROGRESS_DIALOG);
        } catch (Exception e) {
            // tried to close a dialog not open.  don't care.
        }

        StringBuilder b = new StringBuilder();
        Set<String> keys = result.keySet();
        Iterator<String> it = keys.iterator();
        while (it.hasNext()) {
            String instance = it.next();
            b.append(instance + " :: ");
            b.append(result.get(instance) + " \n");
        }

        String msg = b.toString();

        createAlertDialog("title", msg);
    }


    @Override
    public void progressUpdate(int progress, int total) {
        mProgressDialog.setMessage(getString(R.string.sending_items, progress, total));
    }


    @Override
    protected Dialog onCreateDialog(int id, final Bundle bundle) {
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
            case AUTH_DIALOG:
                AlertDialog.Builder b = new AlertDialog.Builder(this);
                b.setTitle("Server Requires Authentication");
                b.setMessage("Please enter usernamd and password");

                LayoutInflater factory = LayoutInflater.from(this);
                final View dialogView = factory.inflate(R.layout.server_auth_dialog, null);

                b.setView(dialogView);
                b.setPositiveButton("ok", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditText username = (EditText) dialogView.findViewById(R.id.username_edit);
                        EditText password = (EditText) dialogView.findViewById(R.id.password_edit);

                        URI u = URI.create(bundle.getString(AUTH_URI));
                        WebUtils.addCredentials(username.getText().toString(), password.getText()
                                .toString(), u.getHost());

                        mInstanceUploaderTask = new InstanceUploaderTask();

                        String[] toSendArray = new String[toSend.size()];
                        toSend.toArray(toSendArray);
                        mInstanceUploaderTask.execute(toSendArray);
                        showDialog(PROGRESS_DIALOG);
                    }
                });
                b.setNegativeButton("cancel", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TOOD: cancel

                    }
                });

                b.setCancelable(false);
                // mAlertShowing = true;
                return b.create();
        }
        return null;
    }


    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
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


    @Override
    public void authRequest(URI url, HashMap<String, String> doneSoFar) {

        if (mProgressDialog.isShowing()) {
            // should always be showing here
            mProgressDialog.dismiss();
        }

        // add our list of completed uploads to "completed"
        // and remove them from our toSend list.
        if (doneSoFar != null) {
            Set<String> uploadedInstances = doneSoFar.keySet();
            Iterator<String> itr = uploadedInstances.iterator();
            while (itr.hasNext()) {
                String removeMe = itr.next();
                boolean test = toSend.remove(removeMe);
                //TODO:  I think this doesn't work with strings.
            }
            completed.putAll(doneSoFar);
        }

        Bundle b = new Bundle();
        b.putString(AUTH_URI, url.toString());
        showDialog(AUTH_DIALOG, b);
    }


    private void createAlertDialog(String title, String message) {
        mAlertDialog = new AlertDialog.Builder(this).create();
        mAlertDialog.setTitle(title);
        mAlertDialog.setMessage(message);
        DialogInterface.OnClickListener quitListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                switch (i) {
                    case DialogInterface.BUTTON1: // ok
                        // just close the dialog
                        // mAlertShowing = false;
                        // // successful download, so quit
                        // if (mSuccess) {
                        // finish();
                        // }

                        break;
                }
            }
        };
        mAlertDialog.setCancelable(false);
        mAlertDialog.setButton(getString(R.string.ok), quitListener);
        // if (mSuccess) {
        // mAlertDialog.setIcon(android.R.drawable.ic_dialog_info);
        // } else {
        // mAlertDialog.setIcon(android.R.drawable.ic_dialog_alert);
        // }
        // mAlertShowing = true;
        // mAlertMsg = message;
        // mAlertTitle = title;
        mAlertDialog.show();
    }

}
