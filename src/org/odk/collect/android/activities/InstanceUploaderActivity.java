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
import org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns;
import org.odk.collect.android.tasks.InstanceUploaderTask;
import org.odk.collect.android.utilities.WebUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * Activity to upload completed forms.
 * 
 * @author Carl Hartung (carlhartung@gmail.com)
 */
public class InstanceUploaderActivity extends Activity implements InstanceUploaderListener {
    private final static String t = "InstanceUploaderActivity";
    private final static int PROGRESS_DIALOG = 1;
    private final static int AUTH_DIALOG = 2;

    private ProgressDialog mProgressDialog;
    private AlertDialog mAlertDialog;

    private String mAlertMsg;
    private String ALERT_MSG = "alertmsg";
    private String ALERT_SHOWING = "alertshowing";
    private static final String TO_SEND = "tosend";
    private boolean mAlertShowing;

    private InstanceUploaderTask mInstanceUploaderTask;

    // maintain a list of what we've yet to send, in case we're interrupted by auth requests
    private ArrayList<Long> mInstancesToSend;

    // maintain a list of what we've sent, in case we're interrupted by auth requests
    private HashMap<String, String> mUploadedInstances;
    private String mUrl;

    private final static String AUTH_URI = "auth";


    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAlertMsg = getString(R.string.please_wait);

        mUploadedInstances = new HashMap<String, String>();

        setTitle(getString(R.string.app_name) + " > " + getString(R.string.send_data));

        // get instances to upload
        Intent intent = getIntent();
        long[] selectedInstanceIDs = intent.getLongArrayExtra(FormEntryActivity.KEY_INSTANCES);
        if (selectedInstanceIDs.length == 0) {
            // If we get nothing, toast and quit
            Toast.makeText(this, R.string.noselect_error, Toast.LENGTH_LONG);
            finish();
            return;
        }

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(ALERT_MSG)) {
                mAlertMsg = savedInstanceState.getString(ALERT_MSG);
            }
            if (savedInstanceState.containsKey(ALERT_SHOWING)) {
                mAlertShowing = savedInstanceState.getBoolean(ALERT_SHOWING, false);
            }
        }

        if (savedInstanceState != null && !savedInstanceState.containsKey(TO_SEND)) {
            mInstancesToSend = (ArrayList<Long>) savedInstanceState.getSerializable(TO_SEND);
        } else {
            mInstancesToSend = new ArrayList<Long>();
            for (int i = 0; i < selectedInstanceIDs.length; i++) {
                mInstancesToSend.add(new Long(selectedInstanceIDs[i]));
            }
        }

        // get the task if we've changed orientations. If it's null it's a new upload.
        mInstanceUploaderTask = (InstanceUploaderTask) getLastNonConfigurationInstance();
        if (mInstanceUploaderTask == null) {
            // setup dialog and upload task
            showDialog(PROGRESS_DIALOG);
            SharedPreferences settings =
                PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            mInstanceUploaderTask = new InstanceUploaderTask();

            // register this activity with the new uploader task
            mInstanceUploaderTask.setUploaderListener(InstanceUploaderActivity.this);

            Long[] toSendArray = new Long[mInstancesToSend.size()];
            mInstancesToSend.toArray(toSendArray);
            String auth = settings.getString(PreferencesActivity.KEY_AUTH, "");
            mInstanceUploaderTask.setAuth(auth);
            mInstanceUploaderTask.execute(toSendArray);
        }
    }


    @Override
    public void uploadingComplete(HashMap<String, String> result) {
        try {
            dismissDialog(PROGRESS_DIALOG);
        } catch (Exception e) {
            // tried to close a dialog not open. don't care.
        }

        StringBuilder selection = new StringBuilder();
        Set<String> keys = result.keySet();
        Iterator<String> it = keys.iterator();

        String[] selectionArgs = new String[keys.size()];
        int i = 0;
        while (it.hasNext()) {
            String id = it.next();
            selection.append(InstanceColumns._ID + "=?");
            selectionArgs[i++] = id;
            if (i != keys.size()) {
                selection.append(" or ");
            }
        }

        Cursor results =
            managedQuery(InstanceColumns.CONTENT_URI, null, selection.toString(), selectionArgs,
                null);
        StringBuilder message = new StringBuilder();
        if (results.getCount() > 0) {
            results.moveToPosition(-1);
            while (results.moveToNext()) {
                String name =
                    results.getString(results.getColumnIndex(InstanceColumns.DISPLAY_NAME));
                String id = results.getString(results.getColumnIndex(InstanceColumns._ID));
                message.append(name + " - " + result.get(id) + "\n\n");
            }
        } else {
            message.append(getString(R.string.no_forms_uploaded));
        }

        createAlertDialog(message.toString().trim());
    }


    @Override
    public void progressUpdate(int progress, int total) {
        mAlertMsg = getString(R.string.sending_items, progress, total);
        mProgressDialog.setMessage(mAlertMsg);
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
                            mInstanceUploaderTask.cancel(true);
                            mInstanceUploaderTask.setUploaderListener(null);
                            finish();
                        }
                    };
                mProgressDialog.setTitle(getString(R.string.uploading_data));
                mProgressDialog.setMessage(mAlertMsg);
                mProgressDialog.setIndeterminate(true);
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                mProgressDialog.setCancelable(false);
                mProgressDialog.setButton(getString(R.string.cancel), loadingButtonListener);
                return mProgressDialog;
            case AUTH_DIALOG:
                AlertDialog.Builder b = new AlertDialog.Builder(this);

                LayoutInflater factory = LayoutInflater.from(this);
                final View dialogView = factory.inflate(R.layout.server_auth_dialog, null);

                // Get the server, username, and password from the settings
                SharedPreferences settings =
                    PreferenceManager.getDefaultSharedPreferences(getBaseContext());

                String server = mUrl;
                if (server == null) {
                    // if the bundle is null, we're looking for a formlist
                    server =
                        settings.getString(PreferencesActivity.KEY_SERVER_URL,
                            getString(R.string.default_server_url))
                                + settings.getString(PreferencesActivity.KEY_FORMLIST_URL,
                                    "/formList");
                }

                final String url = server;

                Log.i(t, "Trying connecting to: " + url);

                EditText username = (EditText) dialogView.findViewById(R.id.username_edit);
                String storedUsername = settings.getString(PreferencesActivity.KEY_USERNAME, null);
                username.setText(storedUsername);

                EditText password = (EditText) dialogView.findViewById(R.id.password_edit);
                String storedPassword = settings.getString(PreferencesActivity.KEY_PASSWORD, null);
                password.setText(storedPassword);

                b.setTitle(getString(R.string.server_requires_auth));
                b.setMessage(getString(R.string.server_auth_credentials, url));
                b.setView(dialogView);
                b.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditText username = (EditText) dialogView.findViewById(R.id.username_edit);
                        EditText password = (EditText) dialogView.findViewById(R.id.password_edit);

                        URI u = URI.create(mUrl);
                        WebUtils.addCredentials(username.getText().toString(), password.getText()
                                .toString(), u.getHost());

                        mInstanceUploaderTask = new InstanceUploaderTask();

                        // register this activity with the new uploader task
                        mInstanceUploaderTask.setUploaderListener(InstanceUploaderActivity.this);

                        Long[] toSendArray = new Long[mInstancesToSend.size()];
                        mInstancesToSend.toArray(toSendArray);
                        mInstanceUploaderTask.execute(toSendArray);
                        showDialog(PROGRESS_DIALOG);
                    }
                });
                b.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });

                b.setCancelable(false);
                return b.create();
        }
        return null;
    }


    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mUrl = savedInstanceState.getString(AUTH_URI);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(ALERT_MSG, mAlertMsg);
        outState.putBoolean(ALERT_SHOWING, mAlertShowing);
        outState.putSerializable(TO_SEND, mInstancesToSend);
        outState.putString(AUTH_URI, mUrl);
    }


    @Override
    public Object onRetainNonConfigurationInstance() {
        return mInstanceUploaderTask;
    }


    @Override
    protected void onDestroy() {
        if (mInstanceUploaderTask != null) {
            mInstanceUploaderTask.setUploaderListener(null);
        }
        super.onDestroy();
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (mAlertDialog != null && mAlertDialog.isShowing()) {
            mAlertDialog.dismiss();
        }
    }


    @Override
    protected void onResume() {
        if (mInstanceUploaderTask != null) {
            mInstanceUploaderTask.setUploaderListener(this);
        }
        if (mAlertShowing) {
            createAlertDialog(mAlertMsg);
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
                Long removeMe = new Long(itr.next());
                boolean removed = mInstancesToSend.remove(removeMe);
                if (removed) {
                    Log.i(t, removeMe
                            + " was already sent, removing from queue before restarting task");
                }
            }
            mUploadedInstances.putAll(doneSoFar);
        }

        // Bundle b = new Bundle();
        // b.putString(AUTH_URI, url.toString());
        // showDialog(AUTH_DIALOG, b);
        mUrl = url.toString();
        showDialog(AUTH_DIALOG);
    }


    private void createAlertDialog(String message) {
        mAlertDialog = new AlertDialog.Builder(this).create();
        mAlertDialog.setTitle(getString(R.string.upload_results));
        mAlertDialog.setMessage(message);
        DialogInterface.OnClickListener quitListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                switch (i) {
                    case DialogInterface.BUTTON1: // ok
                        // always exit this activity since it has no interface
                        mAlertShowing = false;
                        finish();
                        break;
                }
            }
        };
        mAlertDialog.setCancelable(false);
        mAlertDialog.setButton(getString(R.string.ok), quitListener);
        mAlertDialog.setIcon(android.R.drawable.ic_dialog_info);
        mAlertShowing = true;
        mAlertMsg = message;
        mAlertDialog.show();
    }

}
