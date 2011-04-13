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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.odk.collect.android.R;
import org.odk.collect.android.listeners.FormDownloaderListener;
import org.odk.collect.android.preferences.ServerPreferences;
import org.odk.collect.android.tasks.DownloadFormsTask;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.android.utilities.PasswordPromptDialogBuilder;
import org.odk.collect.android.utilities.WebUtils;
import org.odk.collect.android.utilities.PasswordPromptDialogBuilder.OnOkListener;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

/**
 * Responsible for displaying, adding and deleting all the valid forms in the forms directory.
 * 
 * @author Carl Hartung (carlhartung@gmail.com)
 */
public class FormDownloadList extends ListActivity implements FormDownloaderListener {
    private static final String t = "RemoveFileManageList";

    private static final int PROGRESS_DIALOG = 1;
    private static final int MENU_PREFERENCES = Menu.FIRST;

    private static final String BUNDLE_TOGGLED_KEY = "toggled";
    private static final String BUNDLE_SELECTED_COUNT = "selectedcount";
    private static final String BUNDLE_FORM_LIST = "formlist";
    private static final String DIALOG_TITLE = "dialogtitle";
    private static final String DIALOG_MSG = "dialogmsg";
    private static final String DIALOG_SHOWING = "dialogshowing";

    public static final String LIST_URL = "listurl";

    private String mAlertMsg;
    private boolean mAlertShowing = false;
    private boolean mSuccess = false;
    private String mAlertTitle;

    private AlertDialog mAlertDialog;
    private ProgressDialog mProgressDialog;
    private Button mActionButton;

    private DownloadFormsTask mDownloadFormsTask;
    private Button mToggleButton;
    private Button mRefreshButton;

    private HashMap<String, FormDetails> mFormNamesAndDetails;
    private ArrayAdapter<String> mFileAdapter;

    private boolean mToggled = false;
    private int mSelectedCount = 0;

    private int totalCount;


    @SuppressWarnings("unchecked")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.remote_file_manage_list);
        setTitle(getString(R.string.app_name) + " > " + getString(R.string.get_forms));
        mAlertMsg = getString(R.string.please_wait);
        
        // need white background before load
        getListView().setBackgroundColor(Color.WHITE);

        mActionButton = (Button) findViewById(R.id.add_button);
        mActionButton.setEnabled(false);
        mActionButton.setOnClickListener(new OnClickListener() {
            @Override
			public void onClick(View v) {
                downloadSelectedFiles();
                mToggled = false;
            }
        });

        mToggleButton = (Button) findViewById(R.id.toggle_button);
        mToggleButton.setOnClickListener(new OnClickListener() {
            @Override
			public void onClick(View v) {
                // toggle selections of items to all or none
                ListView ls = getListView();
                mToggled = !mToggled;

                for (int pos = 0; pos < ls.getCount(); pos++)
                    ls.setItemChecked(pos, mToggled);

                mActionButton.setEnabled(!(selectedItemCount() == 0));
            }
        });

        mRefreshButton = (Button) findViewById(R.id.refresh_button);
        mRefreshButton.setOnClickListener(new OnClickListener() {
            @Override
			public void onClick(View v) {
                mToggled = false;
                downloadFormList();
            }
        });

        if (savedInstanceState != null) {
            // If the screen has rotated, the hashmap with the form names and urls is passed here.
            if (savedInstanceState.containsKey(BUNDLE_FORM_LIST)) {
                mFormNamesAndDetails =
                    (HashMap<String, FormDetails>) savedInstanceState.getSerializable(BUNDLE_FORM_LIST);
            }
            // indicating whether or not select-all is on or off.
            if (savedInstanceState.containsKey(BUNDLE_TOGGLED_KEY)) {
                mToggled = savedInstanceState.getBoolean(BUNDLE_TOGGLED_KEY);
            }

            // how many items we've selected
            if (savedInstanceState.containsKey(BUNDLE_SELECTED_COUNT)) {
                mSelectedCount = savedInstanceState.getInt(BUNDLE_SELECTED_COUNT);
                mActionButton.setEnabled(!(mSelectedCount == 0));

            }

            // to restore alert dialog.
            if (savedInstanceState.containsKey(DIALOG_TITLE)) {
                mAlertTitle = savedInstanceState.getString(DIALOG_TITLE);
            }
            if (savedInstanceState.containsKey(DIALOG_MSG)) {
                mAlertMsg = savedInstanceState.getString(DIALOG_MSG);
            }
            if (savedInstanceState.containsKey(DIALOG_SHOWING)) {
                mAlertShowing = savedInstanceState.getBoolean(DIALOG_SHOWING);
            }
        }

        if (mAlertShowing) {
            createAlertDialog(mAlertTitle, mAlertMsg);
        }

        mDownloadFormsTask = (DownloadFormsTask) getLastNonConfigurationInstance();
        if (mDownloadFormsTask == null) {
            downloadFormList();
        } else if (mDownloadFormsTask.getStatus() == AsyncTask.Status.FINISHED) {
            try {
                dismissDialog(PROGRESS_DIALOG);
            } catch (IllegalArgumentException e) {
                Log.w(t, "Attempting to close a dialog that was not previously opened");
            }
            buildView();
        }
    }


    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        mActionButton.setEnabled(!(selectedItemCount() == 0));
    }


    @SuppressWarnings("unchecked")
	private void downloadFormList() {
        mFormNamesAndDetails = new HashMap<String, FormDetails>();
        if (mProgressDialog != null) {
            // This is needed because onPrepareDialog() is broken in 1.6.
            mProgressDialog.setMessage(getString(R.string.please_wait));
        }

        FileUtils.createFolder(FileUtils.CACHE_PATH);

        SharedPreferences settings =
            PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String url =
            settings.getString(ServerPreferences.KEY_SERVER, getString(R.string.default_server))
                    + "/formList";

        HashMap<String, FormDetails> arg = new HashMap<String, FormDetails>();
        arg.put(LIST_URL, new FormDetails(url));

        boolean deferForPassword = false;
        final String userEmail =
            	settings.getString(ServerPreferences.KEY_USER_EMAIL, null);
        if (userEmail != null && userEmail.length() != 0 ) {
        	final Uri u = Uri.parse(url);
        	if ( !WebUtils.hasCredentials(userEmail, u.getHost()) ) {
        		PasswordPromptDialogBuilder b = 
        			new PasswordPromptDialogBuilder(this, 
        											userEmail, 
        											u.getHost(),
        											new OnOkListener() {

														@Override
														public void onOk(
																Object okListenerContext) {
															FormDownloadList.this.executeDownload((HashMap<String, FormDetails>) okListenerContext);
														}
        				
        			}, arg);
        		deferForPassword = true;
        		b.show();
        	}
        }
        if ( !deferForPassword ) {
        	executeDownload(arg);
        }
    }

    @SuppressWarnings("unchecked")
	private void executeDownload(HashMap<String, FormDetails> arg) {
        showDialog(PROGRESS_DIALOG);
        mDownloadFormsTask = new DownloadFormsTask();

        mDownloadFormsTask.setDownloaderListener(this);

        mDownloadFormsTask.execute(arg);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(BUNDLE_TOGGLED_KEY, mToggled);
        outState.putInt(BUNDLE_SELECTED_COUNT, selectedItemCount());
        outState.putSerializable(BUNDLE_FORM_LIST, mFormNamesAndDetails);
        outState.putString(DIALOG_TITLE, mAlertTitle);
        outState.putString(DIALOG_MSG, mAlertMsg);
        outState.putBoolean(DIALOG_SHOWING, mAlertShowing);
    }


    private int selectedItemCount() {
        int count = 0;
        SparseBooleanArray sba = getListView().getCheckedItemPositions();
        for (int i = 0; i < getListView().getCount(); i++) {
            if (sba.get(i, false)) {
                count++;
            }
        }
        return count;
    }


    private void buildView() {
        ArrayList<String> formNames = new ArrayList<String>(mFormNamesAndDetails.keySet());

        mFileAdapter =
            new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice,
                    formNames);
        setListAdapter(mFileAdapter);
        getListView().setItemsCanFocus(false);
        getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, MENU_PREFERENCES, 0, getString(R.string.server_preferences)).setIcon(
            android.R.drawable.ic_menu_preferences);
        return true;
    }


    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
            case MENU_PREFERENCES:
                createPreferencesMenu();
                return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }


    private void createPreferencesMenu() {
        Intent i = new Intent(this, ServerPreferences.class);
        startActivity(i);
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
                            mDownloadFormsTask.setDownloaderListener(null);
                        }
                    };
                mProgressDialog.setTitle(getString(R.string.downloading_data));
                mProgressDialog.setMessage(mAlertMsg);
                mProgressDialog.setIcon(android.R.drawable.ic_dialog_info);
                mProgressDialog.setIndeterminate(true);
                mProgressDialog.setCancelable(false);
                mProgressDialog.setButton(getString(R.string.cancel), loadingButtonListener);
                return mProgressDialog;
        }
        return null;
    }


    private static class DownloadArgs {
        HashMap<String, FormDetails> filesToDownload;
        Set<String> hosts;
        String userEmail;
    }
    
    /**
     * Adds the selected form
     */
    private void downloadSelectedFiles() {
        HashMap<String, FormDetails> filesToDownload = new HashMap<String, FormDetails>();
        totalCount = 0;

        SparseBooleanArray sba = getListView().getCheckedItemPositions();
        for (int i = 0; i < getListView().getCount(); i++) {
            if (sba.get(i, false)) {
                String form = (String) getListAdapter().getItem(i);
                filesToDownload.put(form, mFormNamesAndDetails.get(form));
            }
        }
        totalCount = filesToDownload.size();

        if (totalCount > 0) {

            SharedPreferences settings =
                PreferenceManager.getDefaultSharedPreferences(getBaseContext());

            FileUtils.createFolder(FileUtils.FORMS_PATH);
            boolean deferForPassword = false;
            String userEmail =
            	settings.getString(ServerPreferences.KEY_USER_EMAIL, null);
            if (userEmail != null && userEmail.length() != 0 ) {
            	Set<String> hosts = new HashSet<String>();
            	for ( FormDetails f : filesToDownload.values() ) {
            		if ( f.downloadUrl != null ) {
            			Uri u = Uri.parse(f.downloadUrl);
                    	if ( !WebUtils.hasCredentials(userEmail, u.getHost()) ) {
                    		hosts.add(u.getHost());
                    	}
            		}
            		if ( f.manifestUrl != null ) {
            			Uri u = Uri.parse(f.manifestUrl);
                    	if ( !WebUtils.hasCredentials(userEmail, u.getHost()) ) {
                    		hosts.add(u.getHost());
                    	}
            		}
            	}

            	if ( !hosts.isEmpty() ) {
            		DownloadArgs args = new DownloadArgs();
            		args.filesToDownload = filesToDownload;
            		args.hosts = hosts;
            		args.userEmail = userEmail;
            		deferForPassword = true;
            		launchPasswordDialog(args);
            	}
            }

            if ( !deferForPassword ) {
            	executeMultiDownload(filesToDownload);
            }

        } else {
            Toast.makeText(getApplicationContext(), R.string.noselect_error, Toast.LENGTH_SHORT)
                    .show();
        }
    }

    private void launchPasswordDialog( DownloadArgs args ) {
    	if ( args.hosts.isEmpty() ) {
    		executeMultiDownload(args.filesToDownload);
    		return;
    	}
    	
		String h = args.hosts.iterator().next();
		args.hosts.remove(h);
    	PasswordPromptDialogBuilder b = 
    			new PasswordPromptDialogBuilder(
    					this, 
						args.userEmail, 
						h,
						new PasswordPromptDialogBuilder.OnOkListener() {

							@Override
							public void onOk(
									Object okListenerContext) {
								DownloadArgs args = (DownloadArgs) okListenerContext;
			            		FormDownloadList.this.launchPasswordDialog(args);
							}
    			}, args);
    	b.show();
    }
    
    @SuppressWarnings("unchecked")
	private void executeMultiDownload(HashMap<String, FormDetails> filesToDownload) {
        // show dialog box
        showDialog(PROGRESS_DIALOG);
        mDownloadFormsTask = new DownloadFormsTask();
        mDownloadFormsTask.setDownloaderListener(this);

        mDownloadFormsTask.execute(filesToDownload);
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return mDownloadFormsTask;
    }


    @Override
    protected void onDestroy() {
        if (mDownloadFormsTask != null) {
            mDownloadFormsTask.setDownloaderListener(null);
        }
        super.onDestroy();
    }


    @Override
    protected void onResume() {
        if (mDownloadFormsTask != null) {
            mDownloadFormsTask.setDownloaderListener(this);
        }
        super.onResume();
    }


    @Override
    protected void onPause() {
        if (mAlertDialog != null && mAlertDialog.isShowing()) {
            mAlertDialog.dismiss();
        }
        super.onPause();
    }


    @Override
	public void formDownloadingComplete(HashMap<String, FormDetails> result) {
        dismissDialog(PROGRESS_DIALOG);
        String dialogMessage = null;
        String dialogTitle = null;

        if (result != null) {
            if (result.containsKey(DownloadFormsTask.DL_FORMS)) {
                // We tried to download one or more forms
                if (!result.containsKey(DownloadFormsTask.DL_ERROR_MSG)) {
                    // Download of forms succeeded
                    dialogTitle = getString(R.string.download_complete);
                    dialogMessage = getString(R.string.download_all_successful);

                    result.remove(DownloadFormsTask.DL_FORMS);
                    if (result.size() > 0) {
                        // after we remove DL_FORMS, if we have anything left in the
                        // hashmap it's the renamed files in <old, new>
                        Set<String> keys = result.keySet();
                        Iterator<String> i = keys.iterator();
                        while (i.hasNext()) {
                            String form = i.next();
                            dialogMessage +=
                                " " + getString(R.string.form_renamed, form, result.get(form).stringValue);
                        }
                    }
                    mSuccess = true;
                } else {
                    // Download of at least one form had an error
                    String formName = result.get(DownloadFormsTask.DL_FORM).stringValue;
                    String errorMsg = result.get(DownloadFormsTask.DL_ERROR_MSG).stringValue;

                    dialogMessage =
                        getString(R.string.download_failed_with_error, formName, errorMsg);
                    dialogTitle = getString(R.string.error_downloading);
                    mSuccess = false;
                }
                createAlertDialog(dialogTitle, dialogMessage);
            } else {
                // We tried to download a formlist
                if (!result.containsKey(DownloadFormsTask.DL_ERROR_MSG)) {
                    // Download succeeded
                    mFormNamesAndDetails = result;
                    mSuccess = true;
                } else {
                    // Download failed
                    dialogMessage =
                        getString(R.string.list_failed_with_error, 
                        	result.get(DownloadFormsTask.DL_ERROR_MSG).stringValue);
                    dialogTitle = getString(R.string.load_remote_form_error);
                    createAlertDialog(dialogTitle, dialogMessage);

                    mSuccess = false;
                }
            }
        } else {
            Log.e(t, "result was null when downloading");
        }
        buildView();
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
                        mAlertShowing = false;
                        // successful download, so quit
                        if (mSuccess) {
                            finish();
                        }

                        break;
                }
            }
        };
        mAlertDialog.setCancelable(false);
        mAlertDialog.setButton(getString(R.string.ok), quitListener);
        if (mSuccess) {
            mAlertDialog.setIcon(android.R.drawable.ic_dialog_info);
        } else {
            mAlertDialog.setIcon(android.R.drawable.ic_dialog_alert);
        }
        mAlertShowing = true;
        mAlertMsg = message;
        mAlertTitle = title;
        mAlertDialog.show();
    }


    @Override
	public void progressUpdate(String currentFile, int progress, int total) {
        mAlertMsg = getString(R.string.fetching_file, currentFile, progress, total);
        mProgressDialog.setMessage(mAlertMsg);
    }

}

// TODO: make dialog persist through screen rotations.
