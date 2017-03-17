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

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.dao.FormsDao;
import org.odk.collect.android.listeners.FormDownloaderListener;
import org.odk.collect.android.listeners.FormListDownloaderListener;
import org.odk.collect.android.logic.FormDetails;
import org.odk.collect.android.preferences.PreferencesActivity;
import org.odk.collect.android.provider.FormsProviderAPI;
import org.odk.collect.android.provider.FormsProviderAPI.FormsColumns;
import org.odk.collect.android.tasks.DownloadFormListTask;
import org.odk.collect.android.tasks.DownloadFormsTask;
import org.odk.collect.android.utilities.AuthDialogUtility;
import org.odk.collect.android.utilities.ToastUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Responsible for displaying, adding and deleting all the valid forms in the forms directory. One
 * caveat. If the server requires authentication, a dialog will pop up asking when you request the
 * form list. If somehow you manage to wait long enough and then try to download selected forms and
 * your authorization has timed out, it won't again ask for authentication, it will just throw a
 * 401
 * and you'll have to hit 'refresh' where it will ask for credentials again. Technically a server
 * could point at other servers requiring authentication to download the forms, but the current
 * implementation in Collect doesn't allow for that. Mostly this is just because it's a pain in the
 * butt to keep track of which forms we've downloaded and where we're needing to authenticate. I
 * think we do something similar in the instanceuploader task/activity, so should change the
 * implementation eventually.
 *
 * @author Carl Hartung (carlhartung@gmail.com)
 */
public class FormDownloadList extends FormListActivity implements FormListDownloaderListener,
        FormDownloaderListener, AuthDialogUtility.AuthDialogUtilityResultListener {
    private static final String t = "RemoveFileManageList";

    private static final int PROGRESS_DIALOG = 1;
    private static final int AUTH_DIALOG = 2;
    private static final int MENU_PREFERENCES = AppListActivity.MENU_FILTER + 1;

    private static final String BUNDLE_SELECTED_COUNT = "selectedcount";
    private static final String BUNDLE_FORM_MAP = "formmap";
    private static final String DIALOG_TITLE = "dialogtitle";
    private static final String DIALOG_MSG = "dialogmsg";
    private static final String DIALOG_SHOWING = "dialogshowing";
    private static final String FORMLIST = "formlist";
    private static final String SELECTED_FORMS = "selectedForms";

    private static final String FORMNAME = "formname";
    private static final String FORMDETAIL_KEY = "formdetailkey";
    private static final String FORMID_DISPLAY = "formiddisplay";

    private static final String FORM_ID_KEY = "formid";
    private static final String FORM_VERSION_KEY = "formversion";

    private String mAlertMsg;
    private boolean mAlertShowing = false;
    private String mAlertTitle;

    private AlertDialog mAlertDialog;
    private ProgressDialog mProgressDialog;
    private Button mDownloadButton;

    private DownloadFormListTask mDownloadFormListTask;
    private DownloadFormsTask mDownloadFormsTask;
    private Button mToggleButton;

    private HashMap<String, FormDetails> mFormNamesAndURLs = new HashMap<String, FormDetails>();
    private SimpleAdapter mFormListAdapter;
    private ArrayList<HashMap<String, String>> mFormList;
    private ArrayList<HashMap<String, String>> mFilteredFormList = new ArrayList<>();
    private LinkedHashSet<String> mSelectedForms = new LinkedHashSet<>();

    private static final boolean EXIT = true;
    private static final boolean DO_NOT_EXIT = false;
    private boolean mShouldExit;
    private static final String SHOULD_EXIT = "shouldexit";


    @SuppressWarnings("unchecked")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.remote_file_manage_list);
        setTitle(getString(R.string.get_forms));
        mAlertMsg = getString(R.string.please_wait);


        mDownloadButton = (Button) findViewById(R.id.add_button);
        mDownloadButton.setEnabled(getListView().getCheckedItemCount() > 0);
        mDownloadButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // this is callled in downloadSelectedFiles():
                //    Collect.getInstance().getActivityLogger().logAction(this,
                // "downloadSelectedFiles", ...);
                downloadSelectedFiles();
            }
        });

        mToggleButton = (Button) findViewById(R.id.toggle_button);
        mToggleButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mDownloadButton.setEnabled(toggleChecked(getListView()));
                toggleButtonLabel(mToggleButton, getListView());
            }
        });

        Button mRefreshButton = (Button) findViewById(R.id.refresh_button);
        mRefreshButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Collect.getInstance().getActivityLogger().logAction(this, "refreshForms", "");

                downloadFormList();
                FormDownloadList.this.getListView().clearChoices();
                clearChoices();
            }
        });

        if (savedInstanceState != null) {
            // If the screen has rotated, the hashmap with the form ids and urls is passed here.
            if (savedInstanceState.containsKey(BUNDLE_FORM_MAP)) {
                mFormNamesAndURLs =
                        (HashMap<String, FormDetails>) savedInstanceState
                                .getSerializable(BUNDLE_FORM_MAP);
            }

            // how many items we've selected
            // Android should keep track of this, but broken on rotate...
            if (savedInstanceState.containsKey(BUNDLE_SELECTED_COUNT)) {
                mDownloadButton.setEnabled(savedInstanceState.getInt(BUNDLE_SELECTED_COUNT) >  0);
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
            if (savedInstanceState.containsKey(SHOULD_EXIT)) {
                mShouldExit = savedInstanceState.getBoolean(SHOULD_EXIT);
            }
            if (savedInstanceState.containsKey(SELECTED_FORMS)) {
                mSelectedForms = (LinkedHashSet<String>) savedInstanceState.getSerializable(SELECTED_FORMS);
            }
        }

        if (savedInstanceState != null && savedInstanceState.containsKey(FORMLIST)) {
            mFormList =
                    (ArrayList<HashMap<String, String>>) savedInstanceState.getSerializable(
                            FORMLIST);
        } else {
            mFormList = new ArrayList<HashMap<String, String>>();
        }

        mFilteredFormList.addAll(mFormList);

        if (getLastNonConfigurationInstance() instanceof DownloadFormListTask) {
            mDownloadFormListTask = (DownloadFormListTask) getLastNonConfigurationInstance();
            if (mDownloadFormListTask.getStatus() == AsyncTask.Status.FINISHED) {
                try {
                    dismissDialog(PROGRESS_DIALOG);
                } catch (IllegalArgumentException e) {
                    Log.i(t, "Attempting to close a dialog that was not previously opened");
                }
                mDownloadFormsTask = null;
            }
        } else if (getLastNonConfigurationInstance() instanceof DownloadFormsTask) {
            mDownloadFormsTask = (DownloadFormsTask) getLastNonConfigurationInstance();
            if (mDownloadFormsTask.getStatus() == AsyncTask.Status.FINISHED) {
                try {
                    dismissDialog(PROGRESS_DIALOG);
                } catch (IllegalArgumentException e) {
                    Log.i(t, "Attempting to close a dialog that was not previously opened");
                }
                mDownloadFormsTask = null;
            }
        } else if (mFormNamesAndURLs.isEmpty() && getLastNonConfigurationInstance() == null) {
            // first time, so get the formlist
            downloadFormList();
        }

        String[] data = new String[]{
                FORMNAME, FORMID_DISPLAY, FORMDETAIL_KEY
        };
        int[] view = new int[]{
                R.id.text1, R.id.text2
        };

        mFormListAdapter =
                new SimpleAdapter(this, mFilteredFormList, R.layout.two_item_multiple_choice, data, view);
        getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        getListView().setItemsCanFocus(false);
        setListAdapter(mFormListAdapter);

        mSortingOptions = new String[]{
                getString(R.string.sort_by_name_asc), getString(R.string.sort_by_name_desc)
        };
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

    private void clearChoices() {
        FormDownloadList.this.getListView().clearChoices();
        mDownloadButton.setEnabled(false);
    }


    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        toggleButtonLabel(mToggleButton, getListView());
        mDownloadButton.setEnabled(getListView().getCheckedItemCount() > 0);

        Object o = getListAdapter().getItem(position);
        @SuppressWarnings("unchecked")
        HashMap<String, String> item = (HashMap<String, String>) o;
        FormDetails detail = mFormNamesAndURLs.get(item.get(FORMDETAIL_KEY));

        if (detail != null) {
            Collect.getInstance().getActivityLogger().logAction(this, "onListItemClick",
                    detail.downloadUrl);
        } else {
            Collect.getInstance().getActivityLogger().logAction(this, "onListItemClick",
                    "<missing form detail>");
        }

        if (getListView().isItemChecked(position)) {
            mSelectedForms.add(((HashMap<String, String>) getListAdapter().getItem(position)).get(FORMDETAIL_KEY));
        } else {
            mSelectedForms.remove(((HashMap<String, String>) getListAdapter().getItem(position)).get(FORMDETAIL_KEY));
        }
    }

    /**
     * Starts the download task and shows the progress dialog.
     */
    private void downloadFormList() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = connectivityManager.getActiveNetworkInfo();

        if (ni == null || !ni.isConnected()) {
            ToastUtils.showShortToast(R.string.no_connection);
        } else {

            mFormNamesAndURLs = new HashMap<String, FormDetails>();
            if (mProgressDialog != null) {
                // This is needed because onPrepareDialog() is broken in 1.6.
                mProgressDialog.setMessage(getString(R.string.please_wait));
            }
            showDialog(PROGRESS_DIALOG);

            if (mDownloadFormListTask != null &&
                    mDownloadFormListTask.getStatus() != AsyncTask.Status.FINISHED) {
                return; // we are already doing the download!!!
            } else if (mDownloadFormListTask != null) {
                mDownloadFormListTask.setDownloaderListener(null);
                mDownloadFormListTask.cancel(true);
                mDownloadFormListTask = null;
            }

            mDownloadFormListTask = new DownloadFormListTask();
            mDownloadFormListTask.setDownloaderListener(this);
            mDownloadFormListTask.execute();

        }
    }


    @Override
    protected void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);
        toggleButtonLabel(mToggleButton, getListView());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(BUNDLE_SELECTED_COUNT, getListView().getCheckedItemCount());
        outState.putSerializable(BUNDLE_FORM_MAP, mFormNamesAndURLs);
        outState.putString(DIALOG_TITLE, mAlertTitle);
        outState.putString(DIALOG_MSG, mAlertMsg);
        outState.putBoolean(DIALOG_SHOWING, mAlertShowing);
        outState.putBoolean(SHOULD_EXIT, mShouldExit);
        outState.putSerializable(FORMLIST, mFormList);
        outState.putSerializable(SELECTED_FORMS, mSelectedForms);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Collect.getInstance().getActivityLogger().logAction(this, "onCreateOptionsMenu", "show");
        super.onCreateOptionsMenu(menu);

        menu
                .add(0, MENU_PREFERENCES, 0, R.string.general_preferences)
                .setIcon(R.drawable.ic_menu_preferences)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        return true;
    }


    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
            case MENU_PREFERENCES:
                Collect.getInstance().getActivityLogger().logAction(this, "onMenuItemSelected",
                        "MENU_PREFERENCES");
                Intent i = new Intent(this, PreferencesActivity.class);
                startActivity(i);
                return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }


    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case PROGRESS_DIALOG:
                Collect.getInstance().getActivityLogger().logAction(this,
                        "onCreateDialog.PROGRESS_DIALOG", "show");
                mProgressDialog = new ProgressDialog(this);
                DialogInterface.OnClickListener loadingButtonListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Collect.getInstance().getActivityLogger().logAction(this,
                                        "onCreateDialog.PROGRESS_DIALOG", "OK");
                                dialog.dismiss();
                                // we use the same progress dialog for both
                                // so whatever isn't null is running
                                if (mDownloadFormListTask != null) {
                                    mDownloadFormListTask.setDownloaderListener(null);
                                    mDownloadFormListTask.cancel(true);
                                    mDownloadFormListTask = null;
                                }
                                if (mDownloadFormsTask != null) {
                                    mDownloadFormsTask.setDownloaderListener(null);
                                    mDownloadFormsTask.cancel(true);
                                    mDownloadFormsTask = null;
                                }
                            }
                        };
                mProgressDialog.setTitle(getString(R.string.downloading_data));
                mProgressDialog.setMessage(mAlertMsg);
                mProgressDialog.setIcon(android.R.drawable.ic_dialog_info);
                mProgressDialog.setIndeterminate(true);
                mProgressDialog.setCancelable(false);
                mProgressDialog.setButton(getString(R.string.cancel), loadingButtonListener);
                return mProgressDialog;
            case AUTH_DIALOG:
                Collect.getInstance().getActivityLogger().logAction(this,
                        "onCreateDialog.AUTH_DIALOG", "show");

                mAlertShowing = false;

                return new AuthDialogUtility().createDialog(this, this);
        }
        return null;
    }

    @Override
    protected void setupAdapter(final String sortOrder) {
        getListView().clearChoices();
        Collections.sort(mFilteredFormList, new Comparator<HashMap<String, String>>() {
            @Override
            public int compare(HashMap<String, String> lhs, HashMap<String, String> rhs) {
                if (sortOrder.equals(FormsProviderAPI.FormsColumns.DISPLAY_NAME + " ASC")) {
                    return lhs.get(FORMNAME).compareToIgnoreCase(rhs.get(FORMNAME));
                } else {
                    return rhs.get(FORMNAME).compareToIgnoreCase(lhs.get(FORMNAME));
                }
            }
        });

        mFormListAdapter.notifyDataSetChanged();
        checkPreviouslyCheckedItems();
    }

    @Override
    protected void filter(CharSequence charSequence) {
        mFilteredFormList.clear();
        for (HashMap<String, String> form : mFormList) {
            if (form.get(FORMNAME).toLowerCase().contains(charSequence.toString().toLowerCase())) {
                mFilteredFormList.add(form);
            }
        }
        mFormListAdapter.notifyDataSetChanged();
        checkPreviouslyCheckedItems();
    }

    @Override
    protected void checkPreviouslyCheckedItems() {
        getListView().clearChoices();
        for (int i = 0; i < getListView().getCount(); i++) {
            HashMap<String, String> item =
                    (HashMap<String, String>) getListAdapter().getItem(i);
            if (mSelectedForms.contains(item.get(FORMDETAIL_KEY))) {
                getListView().setItemChecked(i, true);
            }
        }
    }

    /**
     * starts the task to download the selected forms, also shows progress dialog
     */
    @SuppressWarnings("unchecked")
    private void downloadSelectedFiles() {
        int totalCount = 0;
        ArrayList<FormDetails> filesToDownload = new ArrayList<FormDetails>();

        SparseBooleanArray sba = getListView().getCheckedItemPositions();
        for (int i = 0; i < getListView().getCount(); i++) {
            if (sba.get(i, false)) {
                HashMap<String, String> item =
                        (HashMap<String, String>) getListAdapter().getItem(i);
                filesToDownload.add(mFormNamesAndURLs.get(item.get(FORMDETAIL_KEY)));
            }
        }
        totalCount = filesToDownload.size();

        Collect.getInstance().getActivityLogger().logAction(this, "downloadSelectedFiles",
                Integer.toString(totalCount));

        if (totalCount > 0) {
            // show dialog box
            showDialog(PROGRESS_DIALOG);

            mDownloadFormsTask = new DownloadFormsTask();
            mDownloadFormsTask.setDownloaderListener(this);
            mDownloadFormsTask.execute(filesToDownload);
        } else {
            ToastUtils.showShortToast(R.string.noselect_error);
        }
    }


    @Override
    public Object onRetainNonConfigurationInstance() {
        if (mDownloadFormsTask != null) {
            return mDownloadFormsTask;
        } else {
            return mDownloadFormListTask;
        }
    }


    @Override
    protected void onDestroy() {
        if (mDownloadFormListTask != null) {
            mDownloadFormListTask.setDownloaderListener(null);
        }
        if (mDownloadFormsTask != null) {
            mDownloadFormsTask.setDownloaderListener(null);
        }
        super.onDestroy();
    }


    @Override
    protected void onResume() {
        if (mDownloadFormListTask != null) {
            mDownloadFormListTask.setDownloaderListener(this);
        }
        if (mDownloadFormsTask != null) {
            mDownloadFormsTask.setDownloaderListener(this);
        }
        if (mAlertShowing) {
            createAlertDialog(mAlertTitle, mAlertMsg, mShouldExit);
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

    /**
     * Determines if a local form on the device is superseded by a given version (of the same form
     * presumably available
     * on the server).
     *
     * @param formId        the form to be checked. A form with this ID may or may not reside on the
     *                      local device.
     * @param latestVersion the version against which the local form (if any) is tested.
     * @return true if a form with id <code>formId</code> exists on the local device and its version
     * is less than
     * <code>latestVersion</code>.
     */
    public static boolean isLocalFormSuperseded(String formId, String latestVersion) {

        if (formId == null) {
            Log.e(t, "isLocalFormSuperseded: server is not OpenRosa-compliant. <formID> is null!");
            return true;
        }

        Cursor formCursor = null;
        try {
            formCursor = new FormsDao().getFormsCursorForFormId(formId);
            if (formCursor.getCount() == 0) {
                // form does not already exist locally
                return true;
            }
            formCursor.moveToFirst();
            int idxJrVersion = formCursor.getColumnIndex(FormsColumns.JR_VERSION);
            if (formCursor.isNull(idxJrVersion)) {
                // any non-null version on server is newer
                return (latestVersion != null);
            }
            String jr_version = formCursor.getString(idxJrVersion);
            // apparently, the isNull() predicate above is not respected on all Android OSes???
            if (jr_version == null && latestVersion == null) {
                return false;
            }
            if (jr_version == null) {
                return true;
            }
            if (latestVersion == null) {
                return false;
            }
            // if what we have is less, then the server is newer
            return (jr_version.compareTo(latestVersion) < 0);
        } finally {
            if (formCursor != null) {
                formCursor.close();
            }
        }
    }

    /**
     * Causes any local forms that have been updated on the server to become checked in the list.
     * This is a prompt and a
     * convenience to users to download the latest version of those forms from the server.
     */
    private void selectSupersededForms() {

        ListView ls = getListView();
        for (int idx = 0; idx < mFormList.size(); idx++) {
            HashMap<String, String> item = mFormList.get(idx);
            if (isLocalFormSuperseded(item.get(FORM_ID_KEY), item.get(FORM_VERSION_KEY))) {
                ls.setItemChecked(idx, true);
                mSelectedForms.add(item.get(FORMDETAIL_KEY));
            }
        }
    }

    /**
     * Called when the form list has finished downloading. results will either contain a set of
     * <formname, formdetails> tuples, or one tuple of DL.ERROR.MSG and the associated message.
     */
    public void formListDownloadingComplete(HashMap<String, FormDetails> result) {
        dismissDialog(PROGRESS_DIALOG);
        mDownloadFormListTask.setDownloaderListener(null);
        mDownloadFormListTask = null;

        if (result == null) {
            Log.e(t, "Formlist Downloading returned null.  That shouldn't happen");
            // Just displayes "error occured" to the user, but this should never happen.
            createAlertDialog(getString(R.string.load_remote_form_error),
                    getString(R.string.error_occured), EXIT);
            return;
        }

        if (result.containsKey(DownloadFormListTask.DL_AUTH_REQUIRED)) {
            // need authorization
            showDialog(AUTH_DIALOG);
        } else if (result.containsKey(DownloadFormListTask.DL_ERROR_MSG)) {
            // Download failed
            String dialogMessage =
                    getString(R.string.list_failed_with_error,
                            result.get(DownloadFormListTask.DL_ERROR_MSG).errorStr);
            String dialogTitle = getString(R.string.load_remote_form_error);
            createAlertDialog(dialogTitle, dialogMessage, DO_NOT_EXIT);
        } else {
            // Everything worked. Clear the list and add the results.
            mFormNamesAndURLs = result;

            mFormList.clear();

            ArrayList<String> ids = new ArrayList<String>(mFormNamesAndURLs.keySet());
            for (int i = 0; i < result.size(); i++) {
                String formDetailsKey = ids.get(i);
                FormDetails details = mFormNamesAndURLs.get(formDetailsKey);
                HashMap<String, String> item = new HashMap<String, String>();
                item.put(FORMNAME, details.formName);
                item.put(FORMID_DISPLAY,
                        ((details.formVersion == null) ? "" : (getString(R.string.version) + " "
                                + details.formVersion + " ")) +
                                "ID: " + details.formID);
                item.put(FORMDETAIL_KEY, formDetailsKey);
                item.put(FORM_ID_KEY, details.formID);
                item.put(FORM_VERSION_KEY, details.formVersion);

                // Insert the new form in alphabetical order.
                if (mFormList.size() == 0) {
                    mFormList.add(item);
                } else {
                    int j;
                    for (j = 0; j < mFormList.size(); j++) {
                        HashMap<String, String> compareMe = mFormList.get(j);
                        String name = compareMe.get(FORMNAME);
                        if (name.compareTo(mFormNamesAndURLs.get(ids.get(i)).formName) > 0) {
                            break;
                        }
                    }
                    mFormList.add(j, item);
                }
            }
            selectSupersededForms();
            mFilteredFormList.addAll(mFormList);
            mFormListAdapter.notifyDataSetChanged();
            mDownloadButton.setEnabled(getListView().getCheckedItemCount() > 0);
            toggleButtonLabel(mToggleButton, getListView());
        }
    }


    /**
     * Creates an alert dialog with the given tite and message. If shouldExit is set to true, the
     * activity will exit when the user clicks "ok".
     */
    private void createAlertDialog(String title, String message, final boolean shouldExit) {
        Collect.getInstance().getActivityLogger().logAction(this, "createAlertDialog", "show");
        mAlertDialog = new AlertDialog.Builder(this).create();
        mAlertDialog.setTitle(title);
        mAlertDialog.setMessage(message);
        DialogInterface.OnClickListener quitListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                switch (i) {
                    case DialogInterface.BUTTON_POSITIVE: // ok
                        Collect.getInstance().getActivityLogger().logAction(this,
                                "createAlertDialog", "OK");
                        // just close the dialog
                        mAlertShowing = false;
                        // successful download, so quit
                        if (shouldExit) {
                            finish();
                        }
                        break;
                }
            }
        };
        mAlertDialog.setCancelable(false);
        mAlertDialog.setButton(getString(R.string.ok), quitListener);
        mAlertDialog.setIcon(android.R.drawable.ic_dialog_info);
        mAlertMsg = message;
        mAlertTitle = title;
        mAlertShowing = true;
        mShouldExit = shouldExit;
        mAlertDialog.show();
    }


    @Override
    public void progressUpdate(String currentFile, int progress, int total) {
        mAlertMsg = getString(R.string.fetching_file, currentFile, String.valueOf(progress), String.valueOf(total));
        mProgressDialog.setMessage(mAlertMsg);
    }


    @Override
    public void formsDownloadingComplete(HashMap<FormDetails, String> result) {
        if (mDownloadFormsTask != null) {
            mDownloadFormsTask.setDownloaderListener(null);
        }

        if (mProgressDialog.isShowing()) {
            // should always be true here
            mProgressDialog.dismiss();
        }

        Set<FormDetails> keys = result.keySet();
        StringBuilder b = new StringBuilder();
        for (FormDetails k : keys) {
            b.append(k.formName +
                    " (" +
                    ((k.formVersion != null) ?
                            (this.getString(R.string.version) + ": " + k.formVersion + " ")
                            : "") +
                    "ID: " + k.formID + ") - " +
                    result.get(k));
            b.append("\n\n");
        }

        createAlertDialog(getString(R.string.download_forms_result), b.toString().trim(), EXIT);
    }

    @Override
    public void updatedCredentials() {
        downloadFormList();
    }

    @Override
    public void cancelledUpdatingCredentials() {
        finish();
    }
}