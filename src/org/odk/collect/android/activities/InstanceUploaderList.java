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
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.preferences.PreferencesActivity;
import org.odk.collect.android.provider.InstanceProviderAPI;
import org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns;
import org.odk.collect.android.receivers.NetworkReceiver;
import org.odk.collect.android.utilities.CompatibilityUtils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

/**
 * Responsible for displaying all the valid forms in the forms directory. Stores
 * the path to selected form for use by {@link MainMenuActivity}.
 *
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */

public class InstanceUploaderList extends ListActivity implements
		OnLongClickListener {

	private static final String BUNDLE_SELECTED_ITEMS_KEY = "selected_items";
	private static final String BUNDLE_TOGGLED_KEY = "toggled";

	private static final int MENU_PREFERENCES = Menu.FIRST;
	private static final int MENU_SHOW_UNSENT = Menu.FIRST + 1;
	private static final int INSTANCE_UPLOADER = 0;
	
	private static final int GOOGLE_USER_DIALOG = 1;

	private Button mUploadButton;
	private Button mToggleButton;

	private boolean mShowUnsent = true;
	private SimpleCursorAdapter mInstances;
	private ArrayList<Long> mSelected = new ArrayList<Long>();
	private boolean mRestored = false;
	private boolean mToggled = false;

	public Cursor getUnsentCursor() {
		// get all complete or failed submission instances
		String selection = InstanceColumns.STATUS + "=? or "
				+ InstanceColumns.STATUS + "=?";
		String selectionArgs[] = { InstanceProviderAPI.STATUS_COMPLETE,
				InstanceProviderAPI.STATUS_SUBMISSION_FAILED };
		String sortOrder = InstanceColumns.DISPLAY_NAME + " ASC";
		Cursor c = managedQuery(InstanceColumns.CONTENT_URI, null, selection,
				selectionArgs, sortOrder);
		return c;
	}

	public Cursor getAllCursor() {
		// get all complete or failed submission instances
		String selection = InstanceColumns.STATUS + "=? or "
				+ InstanceColumns.STATUS + "=? or " + InstanceColumns.STATUS
				+ "=?";
		String selectionArgs[] = { InstanceProviderAPI.STATUS_COMPLETE,
				InstanceProviderAPI.STATUS_SUBMISSION_FAILED,
				InstanceProviderAPI.STATUS_SUBMITTED };
		String sortOrder = InstanceColumns.DISPLAY_NAME + " ASC";
		Cursor c = managedQuery(InstanceColumns.CONTENT_URI, null, selection,
				selectionArgs, sortOrder);
		return c;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.instance_uploader_list);

		// set up long click listener

		mUploadButton = (Button) findViewById(R.id.upload_button);
		mUploadButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo ni = connectivityManager.getActiveNetworkInfo();

				if (NetworkReceiver.running == true) {
					Toast.makeText(
							InstanceUploaderList.this,
							"Background send running, please try again shortly",
							Toast.LENGTH_SHORT).show();
				} else if (ni == null || !ni.isConnected()) {
					Collect.getInstance().getActivityLogger()
							.logAction(this, "uploadButton", "noConnection");

					Toast.makeText(InstanceUploaderList.this,
							R.string.no_connection, Toast.LENGTH_SHORT).show();
				} else {
					Collect.getInstance()
							.getActivityLogger()
							.logAction(this, "uploadButton",
									Integer.toString(mSelected.size()));

					if (mSelected.size() > 0) {
						// items selected
						uploadSelectedFiles();
						mToggled = false;
						mSelected.clear();
						InstanceUploaderList.this.getListView().clearChoices();
						mUploadButton.setEnabled(false);
					} else {
						// no items selected
						Toast.makeText(getApplicationContext(),
								getString(R.string.noselect_error),
								Toast.LENGTH_SHORT).show();
					}
				}
			}
		});

		mToggleButton = (Button) findViewById(R.id.toggle_button);
		mToggleButton.setLongClickable(true);
		mToggleButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// toggle selections of items to all or none
				ListView ls = getListView();
				mToggled = !mToggled;

				Collect.getInstance()
						.getActivityLogger()
						.logAction(this, "toggleButton",
								Boolean.toString(mToggled));
				// remove all items from selected list
				mSelected.clear();
				for (int pos = 0; pos < ls.getCount(); pos++) {
					ls.setItemChecked(pos, mToggled);
					// add all items if mToggled sets to select all
					if (mToggled)
						mSelected.add(ls.getItemIdAtPosition(pos));
				}
				mUploadButton.setEnabled(!(mSelected.size() == 0));

			}
		});
		mToggleButton.setOnLongClickListener(this);

		Cursor c = mShowUnsent ? getUnsentCursor() : getAllCursor();

		String[] data = new String[] { InstanceColumns.DISPLAY_NAME,
				InstanceColumns.DISPLAY_SUBTEXT };
		int[] view = new int[] { R.id.text1, R.id.text2 };

		// render total instance view
		mInstances = new SimpleCursorAdapter(this,
				R.layout.two_item_multiple_choice, c, data, view);

		setListAdapter(mInstances);
		getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		getListView().setItemsCanFocus(false);
		mUploadButton.setEnabled(!(mSelected.size() == 0));

		// set title
		setTitle(getString(R.string.app_name) + " > "
				+ getString(R.string.send_data));

		// if current activity is being reinitialized due to changing
		// orientation restore all check
		// marks for ones selected
		if (mRestored) {
			ListView ls = getListView();
			for (long id : mSelected) {
				for (int pos = 0; pos < ls.getCount(); pos++) {
					if (id == ls.getItemIdAtPosition(pos)) {
						ls.setItemChecked(pos, true);
						break;
					}
				}

			}
			mRestored = false;
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

	private void uploadSelectedFiles() {
        // send list of _IDs.
        long[] instanceIDs = new long[mSelected.size()];
        for (int i = 0; i < mSelected.size(); i++) {
            instanceIDs[i] = mSelected.get(i);
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String server = prefs.getString(PreferencesActivity.KEY_PROTOCOL, null);
        if (server.equalsIgnoreCase(getString(R.string.protocol_google_maps_engine))) {
            // if it's maps engine, start the maps-engine uploader
            // first make sure we have a google account selected

            String googleUsername = prefs.getString(
                    PreferencesActivity.KEY_SELECTED_GOOGLE_ACCOUNT, null);
            if (googleUsername == null || googleUsername.equals("")) {
                showDialog(GOOGLE_USER_DIALOG);
                return;
            }
            Intent i = new Intent(this, GoogleMapsEngineUploaderActivity.class);
            i.putExtra(FormEntryActivity.KEY_INSTANCES, instanceIDs);
            startActivityForResult(i, INSTANCE_UPLOADER);
        } else {
            // otherwise, do the normal agregate/other thing.
            Intent i = new Intent(this, InstanceUploaderActivity.class);
            i.putExtra(FormEntryActivity.KEY_INSTANCES, instanceIDs);
            startActivityForResult(i, INSTANCE_UPLOADER);
        }
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
				menu.add(0, MENU_SHOW_UNSENT, 1, R.string.change_view)
						.setIcon(R.drawable.ic_menu_manage),
				MenuItem.SHOW_AS_ACTION_NEVER);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case MENU_PREFERENCES:
			Collect.getInstance().getActivityLogger()
					.logAction(this, "onMenuItemSelected", "MENU_PREFERENCES");
			createPreferencesMenu();
			return true;
		case MENU_SHOW_UNSENT:
			Collect.getInstance().getActivityLogger()
					.logAction(this, "onMenuItemSelected", "MENU_SHOW_UNSENT");
			showSentAndUnsentChoices();
			return true;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	private void createPreferencesMenu() {
		Intent i = new Intent(this, PreferencesActivity.class);
		startActivity(i);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		// get row id from db
		Cursor c = (Cursor) getListAdapter().getItem(position);
		long k = c.getLong(c.getColumnIndex(InstanceColumns._ID));

		Collect.getInstance().getActivityLogger()
				.logAction(this, "onListItemClick", Long.toString(k));

		// add/remove from selected list
		if (mSelected.contains(k))
			mSelected.remove(k);
		else
			mSelected.add(k);

		mUploadButton.setEnabled(!(mSelected.size() == 0));

	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		long[] selectedArray = savedInstanceState
				.getLongArray(BUNDLE_SELECTED_ITEMS_KEY);
		for (int i = 0; i < selectedArray.length; i++)
			mSelected.add(selectedArray[i]);
		mToggled = savedInstanceState.getBoolean(BUNDLE_TOGGLED_KEY);
		mRestored = true;
		mUploadButton.setEnabled(selectedArray.length > 0);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		long[] selectedArray = new long[mSelected.size()];
		for (int i = 0; i < mSelected.size(); i++)
			selectedArray[i] = mSelected.get(i);
		outState.putLongArray(BUNDLE_SELECTED_ITEMS_KEY, selectedArray);
		outState.putBoolean(BUNDLE_TOGGLED_KEY, mToggled);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		if (resultCode == RESULT_CANCELED) {
			return;
		}
		switch (requestCode) {
		// returns with a form path, start entry
		case INSTANCE_UPLOADER:
			if (intent.getBooleanExtra(FormEntryActivity.KEY_SUCCESS, false)) {
				mSelected.clear();
				getListView().clearChoices();
				if (mInstances.isEmpty()) {
					finish();
				}
			}
			break;
		default:
			break;
		}
		super.onActivityResult(requestCode, resultCode, intent);
	}

	private void showUnsent() {
		mShowUnsent = true;
		Cursor c = mShowUnsent ? getUnsentCursor() : getAllCursor();
		Cursor old = mInstances.getCursor();
		try {
			mInstances.changeCursor(c);
		} finally {
			if (old != null) {
				old.close();
				this.stopManagingCursor(old);
			}
		}
		getListView().invalidate();
	}

	private void showAll() {
		mShowUnsent = false;
		Cursor c = mShowUnsent ? getUnsentCursor() : getAllCursor();
		Cursor old = mInstances.getCursor();
		try {
			mInstances.changeCursor(c);
		} finally {
			if (old != null) {
				old.close();
				this.stopManagingCursor(old);
			}
		}
		getListView().invalidate();
	}

	@Override
	public boolean onLongClick(View v) {
		Collect.getInstance()
				.getActivityLogger()
				.logAction(this, "toggleButton.longClick",
						Boolean.toString(mToggled));
		return showSentAndUnsentChoices();
	}

	private boolean showSentAndUnsentChoices() {
		/**
		 * Create a dialog with options to save and exit, save, or quit without
		 * saving
		 */
		String[] items = { getString(R.string.show_unsent_forms),
				getString(R.string.show_sent_and_unsent_forms) };

		Collect.getInstance().getActivityLogger()
				.logAction(this, "changeView", "show");

		AlertDialog alertDialog = new AlertDialog.Builder(this)
				.setIcon(android.R.drawable.ic_dialog_info)
				.setTitle(getString(R.string.change_view))
				.setNeutralButton(getString(R.string.cancel),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								Collect.getInstance()
										.getActivityLogger()
										.logAction(this, "changeView", "cancel");
								dialog.cancel();
							}
						})
				.setItems(items, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {

						case 0: // show unsent
							Collect.getInstance()
									.getActivityLogger()
									.logAction(this, "changeView", "showUnsent");
							InstanceUploaderList.this.showUnsent();
							break;

						case 1: // show all
							Collect.getInstance().getActivityLogger()
									.logAction(this, "changeView", "showAll");
							InstanceUploaderList.this.showAll();
							break;

						case 2:// do nothing
							break;
						}
					}
				}).create();
		alertDialog.show();
		return true;
	}
	
	@Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case GOOGLE_USER_DIALOG:
                AlertDialog.Builder gudBuilder = new AlertDialog.Builder(this);

                gudBuilder.setTitle(R.string.no_google_account);
                gudBuilder
                        .setMessage("You have selected Google Maps Engine as your server, please select a corresponding Google Account in the General Settings before continuing");
                gudBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                gudBuilder.setCancelable(false);
                return gudBuilder.create();
        }
        return null;
    }

}
