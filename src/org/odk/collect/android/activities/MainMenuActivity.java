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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Map.Entry;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.preferences.AdminPreferencesActivity;
import org.odk.collect.android.preferences.PreferencesActivity;
import org.odk.collect.android.provider.InstanceProviderAPI;
import org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns;
import org.odk.collect.android.utilities.CompatibilityUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Responsible for displaying buttons to launch the major activities. Launches
 * some activities based on returns of others.
 *
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */
public class MainMenuActivity extends Activity {
	private static final String t = "MainMenuActivity";

	private static final int PASSWORD_DIALOG = 1;

	// menu options
	private static final int MENU_PREFERENCES = Menu.FIRST;
	private static final int MENU_ADMIN = Menu.FIRST + 1;

	// buttons
	private Button mEnterDataButton;
	private Button mManageFilesButton;
	private Button mSendDataButton;
	private Button mReviewDataButton;
	private Button mGetFormsButton;

	private View mReviewSpacer;
	private View mGetFormsSpacer;

	private AlertDialog mAlertDialog;
	private SharedPreferences mAdminPreferences;

	private int mCompletedCount;
	private int mSavedCount;

	private Cursor mFinalizedCursor;
	private Cursor mSavedCursor;

	private IncomingHandler mHandler = new IncomingHandler(this);
	private MyContentObserver mContentObserver = new MyContentObserver();

	private static boolean EXIT = true;

	// private static boolean DO_NOT_EXIT = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// must be at the beginning of any activity that can be called from an
		// external intent
		Log.i(t, "Starting up, creating directories");
		try {
			Collect.createODKDirs();
		} catch (RuntimeException e) {
			createErrorDialog(e.getMessage(), EXIT);
			return;
		}

		setContentView(R.layout.main_menu);

		{
			// dynamically construct the "ODK Collect vA.B" string
			TextView mainMenuMessageLabel = (TextView) findViewById(R.id.main_menu_header);
			mainMenuMessageLabel.setText(Collect.getInstance()
					.getVersionedAppName());
		}

		setTitle(getString(R.string.app_name) + " > "
				+ getString(R.string.main_menu));

		File f = new File(Collect.ODK_ROOT + "/collect.settings");
		if (f.exists()) {
			boolean success = loadSharedPreferencesFromFile(f);
			if (success) {
				Toast.makeText(this,
						"Settings successfully loaded from file",
						Toast.LENGTH_LONG).show();
				f.delete();
			} else {
				Toast.makeText(
						this,
						"Sorry, settings file is corrupt and should be deleted or replaced",
						Toast.LENGTH_LONG).show();
			}
		}

		mReviewSpacer = findViewById(R.id.review_spacer);
		mGetFormsSpacer = findViewById(R.id.get_forms_spacer);

		mAdminPreferences = this.getSharedPreferences(
				AdminPreferencesActivity.ADMIN_PREFERENCES, 0);

		// enter data button. expects a result.
		mEnterDataButton = (Button) findViewById(R.id.enter_data);
		mEnterDataButton.setText(getString(R.string.enter_data_button));
		mEnterDataButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Collect.getInstance().getActivityLogger()
						.logAction(this, "fillBlankForm", "click");
				Intent i = new Intent(getApplicationContext(),
						FormChooserList.class);
				startActivity(i);
			}
		});

		// review data button. expects a result.
		mReviewDataButton = (Button) findViewById(R.id.review_data);
		mReviewDataButton.setText(getString(R.string.review_data_button));
		mReviewDataButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Collect.getInstance().getActivityLogger()
						.logAction(this, "editSavedForm", "click");
				Intent i = new Intent(getApplicationContext(),
						InstanceChooserList.class);
				startActivity(i);
			}
		});

		// send data button. expects a result.
		mSendDataButton = (Button) findViewById(R.id.send_data);
		mSendDataButton.setText(getString(R.string.send_data_button));
		mSendDataButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Collect.getInstance().getActivityLogger()
						.logAction(this, "uploadForms", "click");
				Intent i = new Intent(getApplicationContext(),
						InstanceUploaderList.class);
				startActivity(i);
			}
		});

		// manage forms button. no result expected.
		mGetFormsButton = (Button) findViewById(R.id.get_forms);
		mGetFormsButton.setText(getString(R.string.get_forms));
		mGetFormsButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Collect.getInstance().getActivityLogger()
						.logAction(this, "downloadBlankForms", "click");
				Intent i = new Intent(getApplicationContext(),
						FormDownloadList.class);
				startActivity(i);

			}
		});

		// manage forms button. no result expected.
		mManageFilesButton = (Button) findViewById(R.id.manage_forms);
		mManageFilesButton.setText(getString(R.string.manage_files));
		mManageFilesButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Collect.getInstance().getActivityLogger()
						.logAction(this, "deleteSavedForms", "click");
				Intent i = new Intent(getApplicationContext(),
						FileManagerTabs.class);
				startActivity(i);
			}
		});

		// count for finalized instances
		String selection = InstanceColumns.STATUS + "=? or "
				+ InstanceColumns.STATUS + "=?";
		String selectionArgs[] = { InstanceProviderAPI.STATUS_COMPLETE,
				InstanceProviderAPI.STATUS_SUBMISSION_FAILED };

        try {
            mFinalizedCursor = managedQuery(InstanceColumns.CONTENT_URI, null,
                    selection, selectionArgs, null);
        } catch (Exception e) {
            createErrorDialog(e.getMessage(), EXIT);
            return;
        }

        startManagingCursor(mFinalizedCursor);
		mCompletedCount = mFinalizedCursor.getCount();
		mFinalizedCursor.registerContentObserver(mContentObserver);


		// count for incomplete instances
		String selectionSaved = InstanceColumns.STATUS + "=?";
		String selectionArgsSaved[] = { InstanceProviderAPI.STATUS_INCOMPLETE };

        try {
            mSavedCursor = managedQuery(InstanceColumns.CONTENT_URI, null,
                    selectionSaved, selectionArgsSaved, null);
        } catch (Exception e) {
            createErrorDialog(e.getMessage(), EXIT);
            return;
        }

        startManagingCursor(mSavedCursor);
		mSavedCount = mSavedCursor.getCount();
		// don't need to set a content observer because it can't change in the
		// background

		updateButtons();
	}

	@Override
	protected void onResume() {
		super.onResume();
		SharedPreferences sharedPreferences = this.getSharedPreferences(
				AdminPreferencesActivity.ADMIN_PREFERENCES, 0);

		boolean edit = sharedPreferences.getBoolean(
				AdminPreferencesActivity.KEY_EDIT_SAVED, true);
		if (!edit) {
			mReviewDataButton.setVisibility(View.GONE);
			mReviewSpacer.setVisibility(View.GONE);
		} else {
			mReviewDataButton.setVisibility(View.VISIBLE);
			mReviewSpacer.setVisibility(View.VISIBLE);
		}

		boolean send = sharedPreferences.getBoolean(
				AdminPreferencesActivity.KEY_SEND_FINALIZED, true);
		if (!send) {
			mSendDataButton.setVisibility(View.GONE);
		} else {
			mSendDataButton.setVisibility(View.VISIBLE);
		}

		boolean get_blank = sharedPreferences.getBoolean(
				AdminPreferencesActivity.KEY_GET_BLANK, true);
		if (!get_blank) {
			mGetFormsButton.setVisibility(View.GONE);
			mGetFormsSpacer.setVisibility(View.GONE);
		} else {
			mGetFormsButton.setVisibility(View.VISIBLE);
			mGetFormsSpacer.setVisibility(View.VISIBLE);
		}

		boolean delete_saved = sharedPreferences.getBoolean(
				AdminPreferencesActivity.KEY_DELETE_SAVED, true);
		if (!delete_saved) {
			mManageFilesButton.setVisibility(View.GONE);
		} else {
			mManageFilesButton.setVisibility(View.VISIBLE);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (mAlertDialog != null && mAlertDialog.isShowing()) {
			mAlertDialog.dismiss();
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		Collect.getInstance().getActivityLogger().logOnStart(this);
	}

	@Override
	protected void onStop() {
		Collect.getInstance().getActivityLogger().logOnStop(this);
		super.onStop();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Collect.getInstance().getActivityLogger()
				.logAction(this, "onCreateOptionsMenu", "show");
		super.onCreateOptionsMenu(menu);

		CompatibilityUtils.setShowAsAction(
    		menu.add(0, MENU_PREFERENCES, 0, R.string.general_preferences)
				.setIcon(R.drawable.ic_menu_preferences),
			MenuItem.SHOW_AS_ACTION_NEVER);
		CompatibilityUtils.setShowAsAction(
    		menu.add(0, MENU_ADMIN, 0, R.string.admin_preferences)
				.setIcon(R.drawable.ic_menu_login),
			MenuItem.SHOW_AS_ACTION_NEVER);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_PREFERENCES:
			Collect.getInstance()
					.getActivityLogger()
					.logAction(this, "onOptionsItemSelected",
							"MENU_PREFERENCES");
			Intent ig = new Intent(this, PreferencesActivity.class);
			startActivity(ig);
			return true;
		case MENU_ADMIN:
			Collect.getInstance().getActivityLogger()
					.logAction(this, "onOptionsItemSelected", "MENU_ADMIN");
			String pw = mAdminPreferences.getString(
					AdminPreferencesActivity.KEY_ADMIN_PW, "");
			if ("".equalsIgnoreCase(pw)) {
				Intent i = new Intent(getApplicationContext(),
						AdminPreferencesActivity.class);
				startActivity(i);
			} else {
				showDialog(PASSWORD_DIALOG);
				Collect.getInstance().getActivityLogger()
						.logAction(this, "createAdminPasswordDialog", "show");
			}
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void createErrorDialog(String errorMsg, final boolean shouldExit) {
		Collect.getInstance().getActivityLogger()
				.logAction(this, "createErrorDialog", "show");
		mAlertDialog = new AlertDialog.Builder(this).create();
		mAlertDialog.setIcon(android.R.drawable.ic_dialog_info);
		mAlertDialog.setMessage(errorMsg);
		DialogInterface.OnClickListener errorListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int i) {
				switch (i) {
				case DialogInterface.BUTTON_POSITIVE:
					Collect.getInstance()
							.getActivityLogger()
							.logAction(this, "createErrorDialog",
									shouldExit ? "exitApplication" : "OK");
					if (shouldExit) {
						finish();
					}
					break;
				}
			}
		};
		mAlertDialog.setCancelable(false);
		mAlertDialog.setButton(getString(R.string.ok), errorListener);
		mAlertDialog.show();
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case PASSWORD_DIALOG:

			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			final AlertDialog passwordDialog = builder.create();

			passwordDialog.setTitle(getString(R.string.enter_admin_password));
			final EditText input = new EditText(this);
			input.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
			input.setTransformationMethod(PasswordTransformationMethod
					.getInstance());
			passwordDialog.setView(input, 20, 10, 20, 10);

			passwordDialog.setButton(AlertDialog.BUTTON_POSITIVE,
					getString(R.string.ok),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							String value = input.getText().toString();
							String pw = mAdminPreferences.getString(
									AdminPreferencesActivity.KEY_ADMIN_PW, "");
							if (pw.compareTo(value) == 0) {
								Intent i = new Intent(getApplicationContext(),
										AdminPreferencesActivity.class);
								startActivity(i);
								input.setText("");
								passwordDialog.dismiss();
							} else {
								Toast.makeText(
										MainMenuActivity.this,
										getString(R.string.admin_password_incorrect),
										Toast.LENGTH_SHORT).show();
								Collect.getInstance()
										.getActivityLogger()
										.logAction(this, "adminPasswordDialog",
												"PASSWORD_INCORRECT");
							}
						}
					});

			passwordDialog.setButton(AlertDialog.BUTTON_NEGATIVE,
					getString(R.string.cancel),
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							Collect.getInstance()
									.getActivityLogger()
									.logAction(this, "adminPasswordDialog",
											"cancel");
							input.setText("");
							return;
						}
					});

			passwordDialog.getWindow().setSoftInputMode(
					WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
			return passwordDialog;

		}
		return null;
	}

	private void updateButtons() {
		mFinalizedCursor.requery();
		mCompletedCount = mFinalizedCursor.getCount();
		if (mCompletedCount > 0) {
			mSendDataButton.setText(getString(R.string.send_data_button,
					mCompletedCount));
		} else {
			mSendDataButton.setText(getString(R.string.send_data));
		}

		mSavedCursor.requery();
		mSavedCount = mSavedCursor.getCount();
		if (mSavedCount > 0) {
			mReviewDataButton.setText(getString(R.string.review_data_button,
					mSavedCount));
		} else {
			mReviewDataButton.setText(getString(R.string.review_data));
		}

	}

	/**
	 * notifies us that something changed
	 *
	 */
	private class MyContentObserver extends ContentObserver {

		public MyContentObserver() {
			super(null);
		}

		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
			mHandler.sendEmptyMessage(0);
		}
	}

	/*
	 * Used to prevent memory leaks
	 */
	static class IncomingHandler extends Handler {
		private final WeakReference<MainMenuActivity> mTarget;

		IncomingHandler(MainMenuActivity target) {
			mTarget = new WeakReference<MainMenuActivity>(target);
		}

		@Override
		public void handleMessage(Message msg) {
			MainMenuActivity target = mTarget.get();
			if (target != null) {
				target.updateButtons();
			}
		}
	}

	private boolean loadSharedPreferencesFromFile(File src) {
		// this should probably be in a thread if it ever gets big
		boolean res = false;
		ObjectInputStream input = null;
		try {
			input = new ObjectInputStream(new FileInputStream(src));
			Editor prefEdit = PreferenceManager.getDefaultSharedPreferences(
					this).edit();
			prefEdit.clear();
			// first object is preferences
			Map<String, ?> entries = (Map<String, ?>) input.readObject();
			for (Entry<String, ?> entry : entries.entrySet()) {
				Object v = entry.getValue();
				String key = entry.getKey();

				if (v instanceof Boolean)
					prefEdit.putBoolean(key, ((Boolean) v).booleanValue());
				else if (v instanceof Float)
					prefEdit.putFloat(key, ((Float) v).floatValue());
				else if (v instanceof Integer)
					prefEdit.putInt(key, ((Integer) v).intValue());
				else if (v instanceof Long)
					prefEdit.putLong(key, ((Long) v).longValue());
				else if (v instanceof String)
					prefEdit.putString(key, ((String) v));
			}
			prefEdit.commit();

			// second object is admin options
			Editor adminEdit = getSharedPreferences(AdminPreferencesActivity.ADMIN_PREFERENCES, 0).edit();
			adminEdit.clear();
			// first object is preferences
			Map<String, ?> adminEntries = (Map<String, ?>) input.readObject();
			for (Entry<String, ?> entry : adminEntries.entrySet()) {
				Object v = entry.getValue();
				String key = entry.getKey();

				if (v instanceof Boolean)
					adminEdit.putBoolean(key, ((Boolean) v).booleanValue());
				else if (v instanceof Float)
					adminEdit.putFloat(key, ((Float) v).floatValue());
				else if (v instanceof Integer)
					adminEdit.putInt(key, ((Integer) v).intValue());
				else if (v instanceof Long)
					adminEdit.putLong(key, ((Long) v).longValue());
				else if (v instanceof String)
					adminEdit.putString(key, ((String) v));
			}
			adminEdit.commit();

			res = true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {
				if (input != null) {
					input.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return res;
	}

}
