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
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
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
import java.util.Locale;
import java.util.Set;

import timber.log.Timber;

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
        FormDownloaderListener, AuthDialogUtility.AuthDialogUtilityResultListener, AdapterView.OnItemClickListener {
    private static final String FORM_DOWNLOAD_LIST_SORTING_ORDER = "formDownloadListSortingOrder";

    private static final int PROGRESS_DIALOG = 1;
    private static final int AUTH_DIALOG = 2;
    private static final int MENU_PREFERENCES = Menu.FIRST;

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

    private String alertMsg;
    private boolean alertShowing = false;
    private String alertTitle;

    private AlertDialog alertDialog;
    private ProgressDialog progressDialog;
    private Button downloadButton;

    private DownloadFormListTask downloadFormListTask;
    private DownloadFormsTask downloadFormsTask;
    private Button toggleButton;

    private HashMap<String, FormDetails> formNamesAndURLs = new HashMap<String, FormDetails>();
    private SimpleAdapter formListAdapter;
    private ArrayList<HashMap<String, String>> formList;
    private ArrayList<HashMap<String, String>> filteredFormList = new ArrayList<>();
    private LinkedHashSet<String> selectedForms = new LinkedHashSet<>();

    private static final boolean EXIT = true;
    private static final boolean DO_NOT_EXIT = false;
    private boolean shouldExit;
    private static final String SHOULD_EXIT = "shouldexit";


    @SuppressWarnings("unchecked")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.remote_file_manage_list);
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.get_forms));

        alertMsg = getString(R.string.please_wait);

        downloadButton = (Button) findViewById(R.id.add_button);
        downloadButton.setEnabled(listView.getCheckedItemCount() > 0);
        downloadButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // this is called in downloadSelectedFiles():
                //    Collect.getInstance().getActivityLogger().logAction(this,
                // "downloadSelectedFiles", ...);
                downloadSelectedFiles();
            }
        });

        toggleButton = (Button) findViewById(R.id.toggle_button);
        toggleButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadButton.setEnabled(toggleChecked(listView));
                toggleButtonLabel(toggleButton, listView);
                selectedForms.clear();
                if (listView.getCheckedItemCount() == listView.getCount()) {
                    for (HashMap<String, String> map : formList) {
                        selectedForms.add(map.get(FORMDETAIL_KEY));
                    }
                }
            }
        });

        Button refreshButton = (Button) findViewById(R.id.refresh_button);
        refreshButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Collect.getInstance().getActivityLogger().logAction(this, "refreshForms", "");

                downloadFormList();
                filteredFormList.clear();
                clearChoices();
            }
        });

        if (savedInstanceState != null) {
            // If the screen has rotated, the hashmap with the form ids and urls is passed here.
            if (savedInstanceState.containsKey(BUNDLE_FORM_MAP)) {
                formNamesAndURLs =
                        (HashMap<String, FormDetails>) savedInstanceState
                                .getSerializable(BUNDLE_FORM_MAP);
            }

            // how many items we've selected
            // Android should keep track of this, but broken on rotate...
            if (savedInstanceState.containsKey(BUNDLE_SELECTED_COUNT)) {
                downloadButton.setEnabled(savedInstanceState.getInt(BUNDLE_SELECTED_COUNT) > 0);
            }

            // to restore alert dialog.
            if (savedInstanceState.containsKey(DIALOG_TITLE)) {
                alertTitle = savedInstanceState.getString(DIALOG_TITLE);
            }
            if (savedInstanceState.containsKey(DIALOG_MSG)) {
                alertMsg = savedInstanceState.getString(DIALOG_MSG);
            }
            if (savedInstanceState.containsKey(DIALOG_SHOWING)) {
                alertShowing = savedInstanceState.getBoolean(DIALOG_SHOWING);
            }
            if (savedInstanceState.containsKey(SHOULD_EXIT)) {
                shouldExit = savedInstanceState.getBoolean(SHOULD_EXIT);
            }
            if (savedInstanceState.containsKey(SELECTED_FORMS)) {
                selectedForms = (LinkedHashSet<String>) savedInstanceState.getSerializable(SELECTED_FORMS);
            }
        }

        if (savedInstanceState != null && savedInstanceState.containsKey(FORMLIST)) {
            formList =
                    (ArrayList<HashMap<String, String>>) savedInstanceState.getSerializable(
                            FORMLIST);
        } else {
            formList = new ArrayList<HashMap<String, String>>();
        }

        filteredFormList.addAll(formList);

        if (getLastNonConfigurationInstance() instanceof DownloadFormListTask) {
            downloadFormListTask = (DownloadFormListTask) getLastNonConfigurationInstance();
            if (downloadFormListTask.getStatus() == AsyncTask.Status.FINISHED) {
                try {
                    dismissDialog(PROGRESS_DIALOG);
                } catch (IllegalArgumentException e) {
                    Timber.i("Attempting to close a dialog that was not previously opened");
                }
                downloadFormsTask = null;
            }
        } else if (getLastNonConfigurationInstance() instanceof DownloadFormsTask) {
            downloadFormsTask = (DownloadFormsTask) getLastNonConfigurationInstance();
            if (downloadFormsTask.getStatus() == AsyncTask.Status.FINISHED) {
                try {
                    dismissDialog(PROGRESS_DIALOG);
                } catch (IllegalArgumentException e) {
                    Timber.i("Attempting to close a dialog that was not previously opened");
                }
                downloadFormsTask = null;
            }
        } else if (formNamesAndURLs.isEmpty() && getLastNonConfigurationInstance() == null) {
            // first time, so get the formlist
            downloadFormList();
        }

        String[] data = new String[]{
                FORMNAME, FORMID_DISPLAY, FORMDETAIL_KEY
        };
        int[] view = new int[]{
                R.id.text1, R.id.text2
        };

        formListAdapter =
                new SimpleAdapter(this, filteredFormList, R.layout.two_item_multiple_choice, data, view);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        listView.setItemsCanFocus(false);
        listView.setAdapter(formListAdapter);

        sortingOptions = new String[]{
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
        FormDownloadList.this.listView.clearChoices();
        downloadButton.setEnabled(false);
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        toggleButtonLabel(toggleButton, listView);
        downloadButton.setEnabled(listView.getCheckedItemCount() > 0);

        Object o = listView.getAdapter().getItem(position);
        @SuppressWarnings("unchecked")
        HashMap<String, String> item = (HashMap<String, String>) o;
        FormDetails detail = formNamesAndURLs.get(item.get(FORMDETAIL_KEY));

        if (detail != null) {
            Collect.getInstance().getActivityLogger().logAction(this, "onListItemClick",
                    detail.downloadUrl);
        } else {
            Collect.getInstance().getActivityLogger().logAction(this, "onListItemClick",
                    "<missing form detail>");
        }

        if (listView.isItemChecked(position)) {
            selectedForms.add(((HashMap<String, String>) listView.getAdapter().getItem(position)).get(FORMDETAIL_KEY));
        } else {
            selectedForms.remove(((HashMap<String, String>) listView.getAdapter().getItem(position)).get(FORMDETAIL_KEY));
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

            formNamesAndURLs = new HashMap<String, FormDetails>();
            if (progressDialog != null) {
                // This is needed because onPrepareDialog() is broken in 1.6.
                progressDialog.setMessage(getString(R.string.please_wait));
            }
            showDialog(PROGRESS_DIALOG);

            if (downloadFormListTask != null
                    && downloadFormListTask.getStatus() != AsyncTask.Status.FINISHED) {
                return; // we are already doing the download!!!
            } else if (downloadFormListTask != null) {
                downloadFormListTask.setDownloaderListener(null);
                downloadFormListTask.cancel(true);
                downloadFormListTask = null;
            }

            downloadFormListTask = new DownloadFormListTask();
            downloadFormListTask.setDownloaderListener(this);
            downloadFormListTask.execute();

        }
    }


    @Override
    protected void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);
        toggleButtonLabel(toggleButton, listView);
        updateAdapter();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(BUNDLE_SELECTED_COUNT, listView.getCheckedItemCount());
        outState.putSerializable(BUNDLE_FORM_MAP, formNamesAndURLs);
        outState.putString(DIALOG_TITLE, alertTitle);
        outState.putString(DIALOG_MSG, alertMsg);
        outState.putBoolean(DIALOG_SHOWING, alertShowing);
        outState.putBoolean(SHOULD_EXIT, shouldExit);
        outState.putSerializable(FORMLIST, formList);
        outState.putSerializable(SELECTED_FORMS, selectedForms);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Collect.getInstance().getActivityLogger().logAction(this, "onCreateOptionsMenu", "show");
        super.onCreateOptionsMenu(menu);

        menu
                .add(0, MENU_PREFERENCES, 0, R.string.general_preferences)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_PREFERENCES:
                Collect.getInstance().getActivityLogger().logAction(this, "onMenuItemSelected",
                        "MENU_PREFERENCES");
                Intent i = new Intent(this, PreferencesActivity.class);
                startActivity(i);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case PROGRESS_DIALOG:
                Collect.getInstance().getActivityLogger().logAction(this,
                        "onCreateDialog.PROGRESS_DIALOG", "show");
                progressDialog = new ProgressDialog(this);
                DialogInterface.OnClickListener loadingButtonListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Collect.getInstance().getActivityLogger().logAction(this,
                                        "onCreateDialog.PROGRESS_DIALOG", "OK");
                                dialog.dismiss();
                                // we use the same progress dialog for both
                                // so whatever isn't null is running
                                if (downloadFormListTask != null) {
                                    downloadFormListTask.setDownloaderListener(null);
                                    downloadFormListTask.cancel(true);
                                    downloadFormListTask = null;
                                }
                                if (downloadFormsTask != null) {
                                    downloadFormsTask.setDownloaderListener(null);
                                    downloadFormsTask.cancel(true);
                                    downloadFormsTask = null;
                                }
                            }
                        };
                progressDialog.setTitle(getString(R.string.downloading_data));
                progressDialog.setMessage(alertMsg);
                progressDialog.setIcon(android.R.drawable.ic_dialog_info);
                progressDialog.setIndeterminate(true);
                progressDialog.setCancelable(false);
                progressDialog.setButton(getString(R.string.cancel), loadingButtonListener);
                return progressDialog;
            case AUTH_DIALOG:
                Collect.getInstance().getActivityLogger().logAction(this,
                        "onCreateDialog.AUTH_DIALOG", "show");

                alertShowing = false;

                return new AuthDialogUtility().createDialog(this, this);
        }
        return null;
    }

    @Override
    protected String getSortingOrderKey() {
        return FORM_DOWNLOAD_LIST_SORTING_ORDER;
    }

    @Override
    protected void updateAdapter() {
        CharSequence charSequence = getFilterText();
        filteredFormList.clear();
        if (charSequence.length() > 0) {
            for (HashMap<String, String> form : formList) {
                if (form.get(FORMNAME).toLowerCase(Locale.US).contains(charSequence.toString().toLowerCase(Locale.US))) {
                    filteredFormList.add(form);
                }
            }
        } else {
            filteredFormList.addAll(formList);
        }
        sortList();
        formListAdapter.notifyDataSetChanged();

        checkPreviouslyCheckedItems();
    }

    @Override
    protected void checkPreviouslyCheckedItems() {
        listView.clearChoices();
        for (int i = 0; i < listView.getCount(); i++) {
            HashMap<String, String> item =
                    (HashMap<String, String>) listView.getAdapter().getItem(i);
            if (selectedForms.contains(item.get(FORMDETAIL_KEY))) {
                listView.setItemChecked(i, true);
            }
        }
    }

    private void sortList() {
        Collections.sort(filteredFormList, new Comparator<HashMap<String, String>>() {
            @Override
            public int compare(HashMap<String, String> lhs, HashMap<String, String> rhs) {
                if (getSortingOrder().equals(FormsProviderAPI.FormsColumns.DISPLAY_NAME + " ASC")) {
                    return lhs.get(FORMNAME).compareToIgnoreCase(rhs.get(FORMNAME));
                } else {
                    return rhs.get(FORMNAME).compareToIgnoreCase(lhs.get(FORMNAME));
                }
            }
        });
    }

    /**
     * starts the task to download the selected forms, also shows progress dialog
     */
    @SuppressWarnings("unchecked")
    private void downloadSelectedFiles() {
        int totalCount = 0;
        ArrayList<FormDetails> filesToDownload = new ArrayList<FormDetails>();

        SparseBooleanArray sba = listView.getCheckedItemPositions();
        for (int i = 0; i < listView.getCount(); i++) {
            if (sba.get(i, false)) {
                HashMap<String, String> item =
                        (HashMap<String, String>) listView.getAdapter().getItem(i);
                filesToDownload.add(formNamesAndURLs.get(item.get(FORMDETAIL_KEY)));
            }
        }
        totalCount = filesToDownload.size();

        Collect.getInstance().getActivityLogger().logAction(this, "downloadSelectedFiles",
                Integer.toString(totalCount));

        if (totalCount > 0) {
            // show dialog box
            showDialog(PROGRESS_DIALOG);

            downloadFormsTask = new DownloadFormsTask();
            downloadFormsTask.setDownloaderListener(this);
            downloadFormsTask.execute(filesToDownload);
        } else {
            ToastUtils.showShortToast(R.string.noselect_error);
        }
    }


    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        if (downloadFormsTask != null) {
            return downloadFormsTask;
        } else {
            return downloadFormListTask;
        }
    }


    @Override
    protected void onDestroy() {
        if (downloadFormListTask != null) {
            downloadFormListTask.setDownloaderListener(null);
        }
        if (downloadFormsTask != null) {
            downloadFormsTask.setDownloaderListener(null);
        }
        super.onDestroy();
    }


    @Override
    protected void onResume() {
        if (downloadFormListTask != null) {
            downloadFormListTask.setDownloaderListener(this);
        }
        if (downloadFormsTask != null) {
            downloadFormsTask.setDownloaderListener(this);
        }
        if (alertShowing) {
            createAlertDialog(alertTitle, alertMsg, shouldExit);
        }
        super.onResume();
    }


    @Override
    protected void onPause() {
        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
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
            Timber.e("isLocalFormSuperseded: server is not OpenRosa-compliant. <formID> is null!");
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
            String jrVersion = formCursor.getString(idxJrVersion);
            // apparently, the isNull() predicate above is not respected on all Android OSes???
            if (jrVersion == null && latestVersion == null) {
                return false;
            }
            if (jrVersion == null) {
                return true;
            }
            if (latestVersion == null) {
                return false;
            }
            // if what we have is less, then the server is newer
            return (jrVersion.compareTo(latestVersion) < 0);
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

        ListView ls = listView;
        for (int idx = 0; idx < filteredFormList.size(); idx++) {
            HashMap<String, String> item = filteredFormList.get(idx);
            if (isLocalFormSuperseded(item.get(FORM_ID_KEY), item.get(FORM_VERSION_KEY))) {
                ls.setItemChecked(idx, true);
                selectedForms.add(item.get(FORMDETAIL_KEY));
            }
        }
    }

    /*
     * Called when the form list has finished downloading. results will either contain a set of
     * <formname, formdetails> tuples, or one tuple of DL.ERROR.MSG and the associated message.
     */
    public void formListDownloadingComplete(HashMap<String, FormDetails> result) {
        dismissDialog(PROGRESS_DIALOG);
        downloadFormListTask.setDownloaderListener(null);
        downloadFormListTask = null;

        if (result == null) {
            Timber.e("Formlist Downloading returned null.  That shouldn't happen");
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
            formNamesAndURLs = result;

            formList.clear();

            ArrayList<String> ids = new ArrayList<String>(formNamesAndURLs.keySet());
            for (int i = 0; i < result.size(); i++) {
                String formDetailsKey = ids.get(i);
                FormDetails details = formNamesAndURLs.get(formDetailsKey);
                HashMap<String, String> item = new HashMap<String, String>();
                item.put(FORMNAME, details.formName);
                item.put(FORMID_DISPLAY,
                        ((details.formVersion == null) ? "" : (getString(R.string.version) + " "
                                + details.formVersion + " ")) + "ID: " + details.formID);
                item.put(FORMDETAIL_KEY, formDetailsKey);
                item.put(FORM_ID_KEY, details.formID);
                item.put(FORM_VERSION_KEY, details.formVersion);

                // Insert the new form in alphabetical order.
                if (formList.size() == 0) {
                    formList.add(item);
                } else {
                    int j;
                    for (j = 0; j < formList.size(); j++) {
                        HashMap<String, String> compareMe = formList.get(j);
                        String name = compareMe.get(FORMNAME);
                        if (name.compareTo(formNamesAndURLs.get(ids.get(i)).formName) > 0) {
                            break;
                        }
                    }
                    formList.add(j, item);
                }
            }
            filteredFormList.addAll(formList);
            updateAdapter();
            selectSupersededForms();
            formListAdapter.notifyDataSetChanged();
            downloadButton.setEnabled(listView.getCheckedItemCount() > 0);
            toggleButtonLabel(toggleButton, listView);
        }
    }


    /**
     * Creates an alert dialog with the given tite and message. If shouldExit is set to true, the
     * activity will exit when the user clicks "ok".
     */
    private void createAlertDialog(String title, String message, final boolean shouldExit) {
        Collect.getInstance().getActivityLogger().logAction(this, "createAlertDialog", "show");
        alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        DialogInterface.OnClickListener quitListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                switch (i) {
                    case DialogInterface.BUTTON_POSITIVE: // ok
                        Collect.getInstance().getActivityLogger().logAction(this,
                                "createAlertDialog", "OK");
                        // just close the dialog
                        alertShowing = false;
                        // successful download, so quit
                        if (shouldExit) {
                            finish();
                        }
                        break;
                }
            }
        };
        alertDialog.setCancelable(false);
        alertDialog.setButton(getString(R.string.ok), quitListener);
        alertDialog.setIcon(android.R.drawable.ic_dialog_info);
        alertMsg = message;
        alertTitle = title;
        alertShowing = true;
        this.shouldExit = shouldExit;
        alertDialog.show();
    }


    @Override
    public void progressUpdate(String currentFile, int progress, int total) {
        alertMsg = getString(R.string.fetching_file, currentFile, String.valueOf(progress), String.valueOf(total));
        progressDialog.setMessage(alertMsg);
    }


    @Override
    public void formsDownloadingComplete(HashMap<FormDetails, String> result) {
        if (downloadFormsTask != null) {
            downloadFormsTask.setDownloaderListener(null);
        }

        if (progressDialog.isShowing()) {
            // should always be true here
            progressDialog.dismiss();
        }

        Set<FormDetails> keys = result.keySet();
        StringBuilder b = new StringBuilder();
        for (FormDetails k : keys) {
            b.append(k.formName + " ("
                    + ((k.formVersion != null)
                    ? (this.getString(R.string.version) + ": " + k.formVersion + " ")
                    : "") + "ID: " + k.formID + ") - " + result.get(k));
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