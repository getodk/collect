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

package org.odk.collect.android.preferences;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.listeners.InstanceUploaderListener;
import org.odk.collect.android.tasks.GoogleMapsEngineTask;
import org.odk.collect.android.views.DynamicListPreference;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.GooglePlayServicesAvailabilityException;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


/**
 * Handles GME specific preferences.
 * 
 * @author Carl Hartung (chartung@nafundi.com)
 */
public class GMEPreferencesActivity extends PreferenceActivity implements
		InstanceUploaderListener {

	private DynamicListPreference mGMEProjectIDPreference;

	private static String GME_ERROR = "gme_error";

	private boolean partnerListDialogShowing;

	private final static int PROGRESS_DIALOG = 1;
	private final static int GOOGLE_USER_DIALOG = 3;

	private static final String ALERT_MSG = "alertmsg";
	private static final String ALERT_SHOWING = "alertshowing";

	private ProgressDialog mProgressDialog;
	private AlertDialog mAlertDialog;

	private String mAlertMsg;
	private boolean mAlertShowing;

	private GetProjectIDTask mUlTask;

	private ListPreference mSelectedGoogleAccountPreference;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.gme_preferences);

		// default initializers
		mAlertMsg = getString(R.string.please_wait);
		mAlertShowing = false;

		boolean adminMode = getIntent().getBooleanExtra(PreferencesActivity.INTENT_KEY_ADMIN_MODE, false);

		SharedPreferences adminPreferences = getSharedPreferences(
				AdminPreferencesActivity.ADMIN_PREFERENCES, 0);

		// determine if the partners list should be showing or not
		if (savedInstanceState != null
				&& savedInstanceState.containsKey("partner_dialog")) {
			partnerListDialogShowing = savedInstanceState
					.getBoolean("partner_dialog");
		} else {
			partnerListDialogShowing = false;
			if (savedInstanceState != null
					&& savedInstanceState.containsKey(ALERT_MSG)) {
				mAlertMsg = savedInstanceState.getString(ALERT_MSG);
			}
			if (savedInstanceState != null
					&& savedInstanceState.containsKey(ALERT_SHOWING)) {
				mAlertShowing = savedInstanceState.getBoolean(ALERT_SHOWING,
						false);
			}
		}

		mGMEProjectIDPreference = (DynamicListPreference) findPreference(PreferencesActivity.KEY_GME_PROJECT_ID);
		mGMEProjectIDPreference.setShowDialog(partnerListDialogShowing);
		mGMEProjectIDPreference
				.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
					@Override
					public boolean onPreferenceChange(Preference preference,
							Object newValue) {
						mGMEProjectIDPreference.setValue((String) newValue);
						mGMEProjectIDPreference
								.setSummary(mGMEProjectIDPreference.getEntry());
						mGMEProjectIDPreference.setShowDialog(false);
						return false;
					}
				});
		mSelectedGoogleAccountPreference = (ListPreference) findPreference(PreferencesActivity.KEY_SELECTED_GOOGLE_ACCOUNT);
		PreferenceCategory gmePreferences = (PreferenceCategory) findPreference(getString(R.string.gme_preferences));

		mGMEProjectIDPreference
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference preference) {
						// mUlTask.setUserName(googleUsername);

						// ensure we have a google account selected
						SharedPreferences prefs = PreferenceManager
								.getDefaultSharedPreferences(GMEPreferencesActivity.this);
						String googleUsername = prefs
								.getString(
										PreferencesActivity.KEY_SELECTED_GOOGLE_ACCOUNT,
										null);
						if (googleUsername == null || googleUsername.equals("")) {
							showDialog(GOOGLE_USER_DIALOG);
							return true;
						}
						
						//TODO: CHECK FOR NETWORK CONNECTIVITY.

						// setup dialog and upload task
						showDialog(PROGRESS_DIALOG);

						
						
						mUlTask = new GetProjectIDTask();
						mUlTask.setUserName(googleUsername);
						mUlTask.setUploaderListener(GMEPreferencesActivity.this);
						mUlTask.execute();
						return true;
					}
				});

		SharedPreferences appSharedPrefs = PreferenceManager
				.getDefaultSharedPreferences(this.getApplicationContext());
		// Editor prefsEditor = appSharedPrefs.edit();
		Gson gson = new Gson();
		String json = appSharedPrefs.getString(
				PreferencesActivity.KEY_GME_ID_HASHMAP, "");
		HashMap<String, String> idhashmap = gson.fromJson(json, HashMap.class);

		if (idhashmap != null) {
			String[] entries = new String[idhashmap.size()];
			String[] values = new String[idhashmap.size()];

			Iterator<String> iterator = idhashmap.keySet().iterator();
			int it = 0;
			while (iterator.hasNext()) {
				String key = iterator.next();
				entries[it] = key;
				values[it] = idhashmap.get(key);
				it++;
			}

			mGMEProjectIDPreference.setEntries(entries);
			mGMEProjectIDPreference.setEntryValues(values);
			mGMEProjectIDPreference.setSummary(mGMEProjectIDPreference
					.getEntry());
		}

		// get list of google accounts
		final Account[] accounts = AccountManager.get(getApplicationContext())
				.getAccountsByType("com.google");
		ArrayList<String> accountEntries = new ArrayList<String>();
		ArrayList<String> accountValues = new ArrayList<String>();

		for (int i = 0; i < accounts.length; i++) {
			accountEntries.add(accounts[i].name);
			accountValues.add(accounts[i].name);
		}
		accountEntries.add(getString(R.string.no_account));
		accountValues.add("");

		mSelectedGoogleAccountPreference.setEntries(accountEntries
				.toArray(new String[accountEntries.size()]));
		mSelectedGoogleAccountPreference.setEntryValues(accountValues
				.toArray(new String[accountValues.size()]));
		mSelectedGoogleAccountPreference
				.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
					@Override
					public boolean onPreferenceChange(Preference preference,
							Object newValue) {
						int index = ((ListPreference) preference)
								.findIndexOfValue(newValue.toString());
						String value = (String) ((ListPreference) preference)
								.getEntryValues()[index];
						((ListPreference) preference).setSummary(value);
						return true;
					}
				});
		mSelectedGoogleAccountPreference
				.setSummary(mSelectedGoogleAccountPreference.getValue());

		boolean googleAccountAvailable = adminPreferences.getBoolean(
				AdminPreferencesActivity.KEY_CHANGE_GOOGLE_ACCOUNT, true);
		if (!(googleAccountAvailable || adminMode)) {
			gmePreferences.removePreference(mSelectedGoogleAccountPreference);
		}

	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean("partner_dialog",
				mGMEProjectIDPreference.shouldShow());
		outState.putString(ALERT_MSG, mAlertMsg);
		outState.putBoolean(ALERT_SHOWING, mAlertShowing);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mAlertShowing) {
			createAlertDialog(mAlertMsg);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (mAlertDialog != null && mAlertDialog.isShowing()) {
			mAlertDialog.dismiss();
		}
	}

	private class GetProjectIDTask extends
			GoogleMapsEngineTask<Void, Void, HashMap<String, String>> {

		private InstanceUploaderListener mStateListener;
		private String mGoogleUserName;

		@Override
		protected HashMap<String, String> doInBackground(Void... values) {
			HashMap<String, String> projectList = new HashMap<String, String>();

			String token = null;
			try {
				token = authenticate(GMEPreferencesActivity.this,
						mGoogleUserName);
			} catch (IOException e) {
				// network or server error, the call is expected to succeed if
				// you try again later. Don't attempt to call again immediately
				// - the request is likely to fail, you'll hit quotas or
				// back-off.
				e.printStackTrace();
				return null;
			} catch (GooglePlayServicesAvailabilityException playEx) {
				Dialog alert = GooglePlayServicesUtil.getErrorDialog(
						playEx.getConnectionStatusCode(),
						GMEPreferencesActivity.this, PLAYSTORE_REQUEST_CODE);
				alert.show();
				return null;
			} catch (UserRecoverableAuthException e) {
				GMEPreferencesActivity.this.startActivityForResult(
						e.getIntent(), USER_RECOVERABLE_REQUEST_CODE);
				e.printStackTrace();
				return null;
			} catch (GoogleAuthException e) {
				// Failure. The call is not expected to ever succeed so it
				// should not be retried.
				e.printStackTrace();
				return null;
			}

			if (token == null) {
				// if token is null,
				return null;
			}

			HttpURLConnection conn = null;
			int status = -1;
			try {
				URL url = new URL(
						"https://www.googleapis.com/mapsengine/v1/projects/");

				conn = (HttpURLConnection) url.openConnection();
				conn.setRequestMethod("GET");
				conn.addRequestProperty("Authorization", "OAuth " + token);

				conn.connect();
				// try {
				if (conn.getResponseCode() / 100 == 2) {
					BufferedReader br = new BufferedReader(
							new InputStreamReader(conn.getInputStream()));
					GsonBuilder builder = new GsonBuilder();
					Gson gson = builder.create();

					ProjectsListResponse projects = gson.fromJson(br,
							ProjectsListResponse.class);
					for (int i = 0; i < projects.projects.length; i++) {
						Project p = projects.projects[i];
						projectList.put(p.name, p.id);
					}
				} else {
					String errorMessage = getErrorMesssage(conn
							.getErrorStream());
					if (status == 400) {
					} else if (status == 403 || status == 401) {
						GoogleAuthUtil.invalidateToken(
								GMEPreferencesActivity.this, token);
					}
					projectList.put(GME_ERROR, errorMessage);
					return projectList;
				}
			} catch (Exception e) {
				e.printStackTrace();
				GoogleAuthUtil.invalidateToken(GMEPreferencesActivity.this,
						token);
				String errorMessage = getErrorMesssage(conn.getErrorStream());
				projectList.put(GME_ERROR, errorMessage);
				return projectList;
			} finally {
				if (conn != null) {
					conn.disconnect();
				}
			}
			return projectList;
		}

		public void setUserName(String username) {
			mGoogleUserName = username;
		}

		@Override
		protected void onPostExecute(HashMap<String, String> results) {
			synchronized (this) {
				if (mStateListener != null) {
					mStateListener.uploadingComplete(results);
				}
			}
		}

		public void setUploaderListener(InstanceUploaderListener sl) {
			synchronized (this) {
				mStateListener = sl;
			}
		}

	}

	@Override
	public void uploadingComplete(HashMap<String, String> result) {
		try {
			dismissDialog(PROGRESS_DIALOG);
		} catch (Exception e) {
			// tried to close a dialog not open. don't care.
		}

		if (result == null) {
			// If result is null, then we needed to authorize the user
			return;
		} else {
			if (result.containsKey(GME_ERROR)) {
				// something went wrong, show the user
				String error = result.get(GME_ERROR);
				createAlertDialog("GME Error:" + error);
				return;
			} else {
				// everything is fine
				String[] entries = new String[result.size()];
				String[] values = new String[result.size()];

				// creates an ordered map
				Map<String, Object> copy = new TreeMap<String, Object>(result);
				Iterator<String> iterator = copy.keySet().iterator();
				int i = 0;
				while (iterator.hasNext()) {
					String key = iterator.next();
					entries[i] = key;
					values[i] = result.get(key);
					i++;
				}

				mGMEProjectIDPreference.setEntries(entries);
				mGMEProjectIDPreference.setEntryValues(values);

				SharedPreferences appSharedPrefs = PreferenceManager
						.getDefaultSharedPreferences(this
								.getApplicationContext());
				Editor prefsEditor = appSharedPrefs.edit();
				Gson gson = new Gson();
				String json = gson.toJson(result);
				prefsEditor.putString(PreferencesActivity.KEY_GME_ID_HASHMAP,
						json);
				prefsEditor.commit();

				mGMEProjectIDPreference.setShowDialog(true);
				mGMEProjectIDPreference.show();
			}
		}

	}

	@Override
	public void progressUpdate(int progress, int total) {
	}

	@Override
	public void authRequest(Uri url, HashMap<String, String> doneSoFar) {
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
			mProgressDialog.setTitle(R.string.get_project_IDs);
			mProgressDialog.setMessage(mAlertMsg);
			mProgressDialog.setIndeterminate(true);
			mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			mProgressDialog.setCancelable(false);
			mProgressDialog.setButton(getString(R.string.cancel),
					loadingButtonListener);
			return mProgressDialog;
		case GOOGLE_USER_DIALOG:
			AlertDialog.Builder gudBuilder = new AlertDialog.Builder(this);

			gudBuilder.setTitle(R.string.no_google_account);
			gudBuilder.setMessage(R.string.select_maps_account);
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

	// JSON
	public static class Project {
		public String id;
		public String name;
	}

	public static class ProjectsListResponse {
		public Project[] projects;
	}

}
