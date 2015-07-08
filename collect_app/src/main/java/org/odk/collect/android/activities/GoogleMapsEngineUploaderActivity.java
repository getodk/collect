/*
 * Copyright (C) 2014 Nafundi
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

/**
 * Activity to upload completed forms to gme.
 *
 * @author Carl Hartung (chartung@nafundi.com)
 */

package org.odk.collect.android.activities;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.listeners.InstanceUploaderListener;
import org.odk.collect.android.preferences.PreferencesActivity;
import org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns;
import org.odk.collect.android.tasks.GoogleMapsEngineAbstractUploader;
import org.odk.collect.android.tasks.GoogleMapsEngineTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GooglePlayServicesAvailabilityException;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.GooglePlayServicesUtil;

public class GoogleMapsEngineUploaderActivity extends Activity implements
		InstanceUploaderListener {
	private final static String tag = "GoogleMapsEngineUploaderActivity";

	private final static int PROGRESS_DIALOG = 1;
	private final static int GOOGLE_USER_DIALOG = 3;

	private static final String ALERT_MSG = "alertmsg";
	private static final String ALERT_SHOWING = "alertshowing";

	private ProgressDialog mProgressDialog;
	private AlertDialog mAlertDialog;

	private String mAlertMsg;
	private boolean mAlertShowing;

	private Long[] mInstancesToSend;

	private GoogleMapsEngineInstanceUploaderTask mUlTask;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(tag, "onCreate: "
				+ ((savedInstanceState == null) ? "creating"
						: "re-initializing"));

		// if we start this activity, the following must be true:
		// 1) Google Maps Engine is selected in preferences
		// 2) A google user is selected

		// default initializers
		mAlertMsg = getString(R.string.please_wait);
		mAlertShowing = false;

		setTitle(getString(R.string.app_name) + " > "
				+ getString(R.string.send_data)); // get any simple saved
													// state...

		// resets alert message and showing dialog if the screen is rotated
		if (savedInstanceState != null) {
			if (savedInstanceState.containsKey(ALERT_MSG)) {
				mAlertMsg = savedInstanceState.getString(ALERT_MSG);
			}
			if (savedInstanceState.containsKey(ALERT_SHOWING)) {
				mAlertShowing = savedInstanceState.getBoolean(ALERT_SHOWING,
						false);
			}
		}

		long[] selectedInstanceIDs = null;

		Intent intent = getIntent();
		selectedInstanceIDs = intent
				.getLongArrayExtra(FormEntryActivity.KEY_INSTANCES);

		mInstancesToSend = new Long[(selectedInstanceIDs == null) ? 0
				: selectedInstanceIDs.length];
		if (selectedInstanceIDs != null) {
			for (int i = 0; i < selectedInstanceIDs.length; ++i) {
				mInstancesToSend[i] = selectedInstanceIDs[i];
			}
		}

		// at this point,
		// we don't expect this to be empty...
		if (mInstancesToSend.length == 0) {
			Log.e(tag, "onCreate: No instances to upload!");
			// drop through --
			// everything will process through OK
		} else {
			Log.i(tag, "onCreate: Beginning upload of "
					+ mInstancesToSend.length + " instances!");
		}

		runTask();
	}

	private void runTask() {
		mUlTask = (GoogleMapsEngineInstanceUploaderTask) getLastNonConfigurationInstance();
		if (mUlTask == null) {
			mUlTask = new GoogleMapsEngineInstanceUploaderTask();

			// ensure we have a google account selected
			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(this);
			String googleUsername = prefs.getString(
					PreferencesActivity.KEY_SELECTED_GOOGLE_ACCOUNT, null);
			if (googleUsername == null || googleUsername.equals("")) {
				showDialog(GOOGLE_USER_DIALOG);
				return;
			}

			// setup dialog and upload task
			showDialog(PROGRESS_DIALOG);

			mUlTask.setUserName(googleUsername);
			mUlTask.setUploaderListener(this);
			mUlTask.execute(mInstancesToSend);
		} else {
			// it's not null, so we have a task running
			// progress dialog is handled by the system
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == GoogleMapsEngineTask.PLAYSTORE_REQUEST_CODE
				&& resultCode == RESULT_OK) {
			// the user got sent to the playstore
			// it returns to this activity, but we'd rather they manually retry
			// so we finish
			finish();
		} else if (requestCode == GoogleMapsEngineTask.USER_RECOVERABLE_REQUEST_CODE
				&& resultCode == RESULT_OK) {
			// authorization granted, try again
			runTask();
		} else if (requestCode == GoogleMapsEngineTask.USER_RECOVERABLE_REQUEST_CODE
				&& resultCode == RESULT_CANCELED) {
			// the user backed out
			finish();
		} else {
			Log.e(tag, "unknown request: " + requestCode + " :: result: "
					+ resultCode);
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		Collect.getInstance().getActivityLogger().logOnStart(this);
	}

	@Override
	protected void onResume() {
		if (mUlTask != null) {
			mUlTask.setUploaderListener(this);
		}
		if (mAlertShowing) {
			createAlertDialog(mAlertMsg);
		}
		super.onResume();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(ALERT_MSG, mAlertMsg);
		outState.putBoolean(ALERT_SHOWING, mAlertShowing);
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		return mUlTask;
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (mAlertDialog != null && mAlertDialog.isShowing()) {
			mAlertDialog.dismiss();
		}
	}

	@Override
	protected void onStop() {
		Collect.getInstance().getActivityLogger().logOnStop(this);
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		if (mUlTask != null) {
			mUlTask.setUploaderListener(null);
		}
		super.onDestroy();
	}

	@Override
	public void uploadingComplete(HashMap<String, String> result) {
		try {
			dismissDialog(PROGRESS_DIALOG);
		} catch (Exception e) {
			// tried to close a dialog not open. don't care.
		}

		if (result == null) {
			// probably got an auth request, so ignore
			return;
		}
		Log.i(tag, "uploadingComplete: Processing results (" + result.size()
				+ ") from upload of " + mInstancesToSend.length + " instances!");

		StringBuilder selection = new StringBuilder();
		Set<String> keys = result.keySet();
		StringBuilder message = new StringBuilder();

		if (keys.size() == 0) {
			message.append(getString(R.string.no_forms_uploaded));
		} else {
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

			Cursor results = null;
			try {
				results = getContentResolver().query(
						InstanceColumns.CONTENT_URI, null,
						selection.toString(), selectionArgs, null);
				if (results.getCount() > 0) {
					results.moveToPosition(-1);
					while (results.moveToNext()) {
						String name = results.getString(results
								.getColumnIndex(InstanceColumns.DISPLAY_NAME));
						String id = results.getString(results
								.getColumnIndex(InstanceColumns._ID));
						message.append(name + " - " + result.get(id) + "\n\n");
					}
				} else {
					message.append(getString(R.string.no_forms_uploaded));
				}
			} finally {
				if (results != null) {
					results.close();
				}
			}
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
			Collect.getInstance().getActivityLogger()
					.logAction(this, "onCreateDialog.PROGRESS_DIALOG", "show");

			mProgressDialog = new ProgressDialog(this);
			DialogInterface.OnClickListener loadingButtonListener = new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Collect.getInstance()
							.getActivityLogger()
							.logAction(this, "onCreateDialog.PROGRESS_DIALOG",
									"cancel");
					dialog.dismiss();
					mUlTask.cancel(true);
					mUlTask.setUploaderListener(null);
					finish();
				}
			};
			mProgressDialog.setTitle(getString(R.string.uploading_data));
			mProgressDialog.setMessage(mAlertMsg);
			mProgressDialog.setIndeterminate(true);
			mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			mProgressDialog.setCancelable(false);
			mProgressDialog.setButton(getString(R.string.cancel),
					loadingButtonListener);
			return mProgressDialog;
		case GOOGLE_USER_DIALOG:
			AlertDialog.Builder gudBuilder = new AlertDialog.Builder(this);

			gudBuilder.setTitle(getString(R.string.no_google_account));
			gudBuilder.setMessage(getString(R.string.gme_set_account));
			gudBuilder.setPositiveButton(R.string.ok,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							finish();
						}
					});
			gudBuilder.setCancelable(false);
			return gudBuilder.create();
		}
		return null;
	}

	private void createAlertDialog(String message) {
		Collect.getInstance().getActivityLogger()
				.logAction(this, "createAlertDialog", "show");

		mAlertDialog = new AlertDialog.Builder(this).create();
		mAlertDialog.setTitle(getString(R.string.upload_results));
		mAlertDialog.setMessage(message);
		DialogInterface.OnClickListener quitListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int i) {
				switch (i) {
				case DialogInterface.BUTTON1: // ok
					Collect.getInstance().getActivityLogger()
							.logAction(this, "createAlertDialog", "OK");
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

	public class GoogleMapsEngineInstanceUploaderTask
			extends
			GoogleMapsEngineAbstractUploader<Long, Integer, HashMap<String, String>> {

		@Override
		protected HashMap<String, String> doInBackground(Long... values) {

			mResults = new HashMap<String, String>();

			String selection = InstanceColumns._ID + "=?";
			String[] selectionArgs = new String[(values == null) ? 0
					: values.length];
			if (values != null) {
				for (int i = 0; i < values.length; i++) {
					if (i != values.length - 1) {
						selection += " or " + InstanceColumns._ID + "=?";
					}
					selectionArgs[i] = values[i].toString();
				}
			}

			String token = null;
			try {
				token = authenticate(GoogleMapsEngineUploaderActivity.this, mGoogleUserName);
			} catch (IOException e) {
				// network or server error, the call is expected to succeed if
				// you try again later. Don't attempt to call again immediately
				// - the request is likely to fail, you'll hit quotas or
				// back-off.
				e.printStackTrace();
				mResults.put("0", oauth_fail + e.getMessage());
				return mResults;
			} catch (GooglePlayServicesAvailabilityException playEx) {
				Dialog alert = GooglePlayServicesUtil.getErrorDialog(
						playEx.getConnectionStatusCode(), GoogleMapsEngineUploaderActivity.this,
						PLAYSTORE_REQUEST_CODE);
				alert.show();
				return null;
			} catch (UserRecoverableAuthException e) {
				GoogleMapsEngineUploaderActivity.this.startActivityForResult(e.getIntent(),
						USER_RECOVERABLE_REQUEST_CODE);
				e.printStackTrace();
				return null;
			} catch (GoogleAuthException e) {
				// Failure. The call is not expected to ever succeed so it
				// should not be retried.
				e.printStackTrace();
				mResults.put("0", oauth_fail + e.getMessage());
				return mResults;
			}

			if (token == null) {
				// if token is null,
				return null;
			}

			uploadInstances(selection, selectionArgs, token);
			return mResults;
		}
	}

	@Override
	public void authRequest(Uri url, HashMap<String, String> doneSoFar) {
		// this shouldn't be in the interface...
	}

}
