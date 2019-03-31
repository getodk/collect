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

package org.odk.collect.android.ui.formdownload;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import org.odk.collect.android.R;
import org.odk.collect.android.activities.FormListActivity;
import org.odk.collect.android.adapters.FormDownloadListAdapter;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.dao.FormsDao;
import org.odk.collect.android.http.HttpCredentialsInterface;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.android.injection.ViewModelProviderFactory;
import org.odk.collect.android.listeners.DownloadFormsTaskListener;
import org.odk.collect.android.listeners.FormListDownloaderListener;
import org.odk.collect.android.listeners.PermissionListener;
import org.odk.collect.android.logic.FormDetails;
import org.odk.collect.android.tasks.DownloadFormListTask;
import org.odk.collect.android.tasks.DownloadFormsTask;
import org.odk.collect.android.utilities.ApplicationConstants;
import org.odk.collect.android.utilities.AuthDialogUtility;
import org.odk.collect.android.utilities.DialogUtils;
import org.odk.collect.android.utilities.PermissionUtils;
import org.odk.collect.android.utilities.ToastUtils;
import org.odk.collect.android.utilities.WebCredentialsUtils;
import org.odk.collect.android.utilities.rx.SchedulerProvider;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Set;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import timber.log.Timber;

import static org.odk.collect.android.utilities.DownloadFormListUtils.DL_AUTH_REQUIRED;
import static org.odk.collect.android.utilities.DownloadFormListUtils.DL_ERROR_MSG;

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
public class FormDownloadActivity extends FormListActivity implements FormListDownloaderListener,
        DownloadFormsTaskListener, AuthDialogUtility.AuthDialogUtilityResultListener,
        AdapterView.OnItemClickListener, FormDownloadNavigator {

    private static final String FORM_DOWNLOAD_LIST_SORTING_ORDER = "formDownloadListSortingOrder";

    public static final String DISPLAY_ONLY_UPDATED_FORMS = "displayOnlyUpdatedForms";
    private static final String BUNDLE_SELECTED_COUNT = "selectedcount";

    public static final String FORMNAME = "formname";
    private static final String FORMDETAIL_KEY = "formdetailkey";
    public static final String FORMID_DISPLAY = "formiddisplay";

    public static final String FORM_ID_KEY = "formid";
    private static final String FORM_VERSION_KEY = "formversion";

    private AlertDialog alertDialog;
    private ProgressDialog progressDialog;
    private ProgressDialog cancelDialog;
    private Button downloadButton;

    private DownloadFormListTask downloadFormListTask;
    private DownloadFormsTask downloadFormsTask;
    private Button toggleButton;

    private final ArrayList<HashMap<String, String>> filteredFormList = new ArrayList<>();
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private static final boolean EXIT = true;
    private static final boolean DO_NOT_EXIT = false;

    private boolean displayOnlyUpdatedForms;

    private FormDownloadViewModel viewModel;

    @Inject
    WebCredentialsUtils webCredentialsUtils;

    @Inject
    PermissionUtils permissionUtils;

    @Inject
    ViewModelProviderFactory factory;

    @Inject
    SchedulerProvider schedulerProvider;

    @SuppressWarnings("unchecked")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DaggerUtils.getComponent(this).inject(this);

        setContentView(R.layout.form_download_list);
        setTitle(getString(R.string.get_forms));

        viewModel = ViewModelProviders.of(this, factory).get(FormDownloadViewModel.class);
        viewModel.setNavigator(this);

        // This activity is accessed directly externally
        permissionUtils.requestStoragePermissions(this, new PermissionListener() {
            @Override
            public void granted() {
                // must be at the beginning of any activity that can be called from an external intent
                try {
                    Collect.createODKDirs();
                } catch (RuntimeException e) {
                    DialogUtils.showDialog(DialogUtils.createErrorDialog(FormDownloadActivity.this, e.getMessage(), EXIT), FormDownloadActivity.this);
                    return;
                }

                init(savedInstanceState);
            }

            @Override
            public void denied() {
                // The activity has to finish because ODK Collect cannot function without these permissions.
                finish();
            }
        });
    }

    private void init(Bundle savedInstanceState) {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            if (bundle.containsKey(DISPLAY_ONLY_UPDATED_FORMS)) {
                displayOnlyUpdatedForms = (boolean) bundle.get(DISPLAY_ONLY_UPDATED_FORMS);
            }
        }

        viewModel.restoreState(bundle);

        downloadButton = findViewById(R.id.add_button);
        downloadButton.setEnabled(listView.getCheckedItemCount() > 0);
        downloadButton.setOnClickListener(v -> downloadSelectedFiles());

        toggleButton = findViewById(R.id.toggle_button);
        toggleButton.setEnabled(false);
        toggleButton.setOnClickListener(v -> {
            downloadButton.setEnabled(toggleChecked(listView));
            toggleButtonLabel(toggleButton, listView);
            viewModel.clearSelectedForms();
            if (listView.getCheckedItemCount() == listView.getCount()) {
                for (HashMap<String, String> map : viewModel.getFormList()) {
                    viewModel.addSelectedForm(map.get(FORMDETAIL_KEY));
                }
            }
        });

        Button refreshButton = findViewById(R.id.refresh_button);
        refreshButton.setOnClickListener(v -> {
            viewModel.setLoadingCanceled(false);
            viewModel.clearFormList();
            updateAdapter();
            clearChoices();
            downloadFormList();
        });

        if (savedInstanceState != null) {
            // how many items we've selected
            // Android should keep track of this, but broken on rotate...
            if (savedInstanceState.containsKey(BUNDLE_SELECTED_COUNT)) {
                downloadButton.setEnabled(savedInstanceState.getInt(BUNDLE_SELECTED_COUNT) > 0);
            }
        }

        filteredFormList.addAll(viewModel.getFormList());

        if (getLastCustomNonConfigurationInstance() instanceof DownloadFormListTask) {
            downloadFormListTask = (DownloadFormListTask) getLastCustomNonConfigurationInstance();
            if (downloadFormListTask.getStatus() == AsyncTask.Status.FINISHED) {
                try {
                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                    viewModel.setProgressDialogShowing(false);
                } catch (IllegalArgumentException e) {
                    Timber.i("Attempting to close a dialog that was not previously opened");
                }
                downloadFormsTask = null;
            }
        } else if (getLastCustomNonConfigurationInstance() instanceof DownloadFormsTask) {
            downloadFormsTask = (DownloadFormsTask) getLastCustomNonConfigurationInstance();
            if (downloadFormsTask.getStatus() == AsyncTask.Status.FINISHED) {
                try {
                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                    viewModel.setProgressDialogShowing(false);
                } catch (IllegalArgumentException e) {
                    Timber.i("Attempting to close a dialog that was not previously opened");
                }
                downloadFormsTask = null;
            }
        } else if (viewModel.getFormNamesAndURLs().isEmpty()
                && getLastCustomNonConfigurationInstance() == null
                && !viewModel.wasLoadingCanceled()) {
            // first time, so get the formlist
            downloadFormList();
        }

        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        listView.setItemsCanFocus(false);

        sortingOptions = new String[]{
                getString(R.string.sort_by_name_asc), getString(R.string.sort_by_name_desc)
        };
    }

    private void clearChoices() {
        listView.clearChoices();
        downloadButton.setEnabled(false);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        toggleButtonLabel(toggleButton, listView);
        downloadButton.setEnabled(listView.getCheckedItemCount() > 0);

        if (listView.isItemChecked(position)) {
            viewModel.addSelectedForm(((HashMap<String, String>) listView.getAdapter().getItem(position)).get(FORMDETAIL_KEY));
        } else {
            viewModel.removeSelectedForm(((HashMap<String, String>) listView.getAdapter().getItem(position)).get(FORMDETAIL_KEY));
        }
    }

    /**
     * Starts the download task and shows the progress dialog.
     */
    private void downloadFormList() {
        viewModel.startDownloadingForms();
    }

    private void bindViewModel() {
        // add subscriptions here

        compositeDisposable.add(viewModel.getAlertDialog()
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(model -> createAlertDialog(model.getTitle(), model.getMessage(), model.shouldExit()), Timber::e));

        compositeDisposable.add(viewModel.getProgressDialog()
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(shouldDisplay -> {
                    if (shouldDisplay && (progressDialog == null || !progressDialog.isShowing())) {
                        createProgressDialog();
                    } else if (!shouldDisplay && progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                }, Timber::e));

        compositeDisposable.add(viewModel.getProgressDialogMessage()
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .filter(message -> progressDialog != null)
                .subscribe(message -> progressDialog.setMessage(message), Timber::e));

        compositeDisposable.add(viewModel.getCancelDialog()
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .filter(shouldDisplay -> shouldDisplay)
                .subscribe(shouldDisplay -> createCancelDialog(), Timber::e));

        compositeDisposable.add(viewModel.getFormDownloadList()
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(this::formListDownloadingComplete, Timber::e));
    }

    private void unbindViewModel() {
        compositeDisposable.clear();
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
            for (HashMap<String, String> form : viewModel.getFormList()) {
                if (form.get(FORMNAME).toLowerCase(Locale.US).contains(charSequence.toString().toLowerCase(Locale.US))) {
                    filteredFormList.add(form);
                }
            }
        } else {
            filteredFormList.addAll(viewModel.getFormList());
        }
        sortList();
        if (listView.getAdapter() == null) {
            listView.setAdapter(new FormDownloadListAdapter(this, filteredFormList, viewModel.getFormNamesAndURLs()));
        } else {
            FormDownloadListAdapter formDownloadListAdapter = (FormDownloadListAdapter) listView.getAdapter();
            formDownloadListAdapter.setFromIdsToDetails(viewModel.getFormNamesAndURLs());
            formDownloadListAdapter.notifyDataSetChanged();
        }
        toggleButton.setEnabled(!filteredFormList.isEmpty());
        checkPreviouslyCheckedItems();
        toggleButtonLabel(toggleButton, listView);
    }

    @Override
    protected void checkPreviouslyCheckedItems() {
        listView.clearChoices();
        for (int i = 0; i < listView.getCount(); i++) {
            HashMap<String, String> item =
                    (HashMap<String, String>) listView.getAdapter().getItem(i);
            if (viewModel.getSelectedForms().contains(item.get(FORMDETAIL_KEY))) {
                listView.setItemChecked(i, true);
            }
        }
    }

    private void sortList() {
        Collections.sort(filteredFormList, (lhs, rhs) -> {
            if (getSortingOrder().equals(SORT_BY_NAME_ASC)) {
                return lhs.get(FORMNAME).compareToIgnoreCase(rhs.get(FORMNAME));
            } else {
                return rhs.get(FORMNAME).compareToIgnoreCase(lhs.get(FORMNAME));
            }
        });
    }

    /**
     * starts the task to download the selected forms, also shows progress dialog
     */
    private void downloadSelectedFiles() {
        ArrayList<FormDetails> filesToDownload = new ArrayList<>();

        SparseBooleanArray sba = listView.getCheckedItemPositions();
        for (int i = 0; i < listView.getCount(); i++) {
            if (sba.get(i, false)) {
                HashMap<String, String> item =
                        (HashMap<String, String>) listView.getAdapter().getItem(i);
                filesToDownload.add(viewModel.getFormNamesAndURLs().get(item.get(FORMDETAIL_KEY)));
            }
        }

        startFormsDownload(filesToDownload);
    }

    @SuppressWarnings("unchecked")
    private void startFormsDownload(@NonNull ArrayList<FormDetails> filesToDownload) {
        int totalCount = filesToDownload.size();
        if (totalCount > 0) {
            // show dialog box
            viewModel.setProgressDialogShowing(true);

            downloadFormsTask = new DownloadFormsTask();
            downloadFormsTask.setDownloaderListener(this);

            if (viewModel.getUrl() != null) {
                if (viewModel.getUsername() != null && viewModel.getPassword() != null) {
                    webCredentialsUtils.saveCredentials(viewModel.getUrl(), viewModel.getUsername(), viewModel.getPassword());
                } else {
                    webCredentialsUtils.clearCredentials(viewModel.getUrl());
                }
            }

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
        bindViewModel();

        if (downloadFormListTask != null) {
            downloadFormListTask.setDownloaderListener(this);
        }
        if (downloadFormsTask != null) {
            downloadFormsTask.setDownloaderListener(this);
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        unbindViewModel();

        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
        super.onPause();
    }

    public boolean isLocalFormSuperseded(String formId) {
        if (formId == null) {
            Timber.e("isLocalFormSuperseded: server is not OpenRosa-compliant. <formID> is null!");
            return true;
        }

        try (Cursor formCursor = new FormsDao().getFormsCursorForFormId(formId)) {
            return formCursor != null && formCursor.getCount() == 0 // form does not already exist locally
                    || viewModel.getFormNamesAndURLs().get(formId).isNewerFormVersionAvailable() // or a newer version of this form is available
                    || viewModel.getFormNamesAndURLs().get(formId).areNewerMediaFilesAvailable(); // or newer versions of media files are available
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
            if (isLocalFormSuperseded(item.get(FORM_ID_KEY))) {
                ls.setItemChecked(idx, true);
                viewModel.addSelectedForm(item.get(FORMDETAIL_KEY));
            }
        }
    }

    /*
     * Called when the form list has finished downloading. results will either contain a set of
     * <formname, formdetails> tuples, or one tuple of DL.ERROR.MSG and the associated message.
     */
    public void formListDownloadingComplete(HashMap<String, FormDetails> result) {
        downloadFormListTask.setDownloaderListener(null);
        downloadFormListTask = null;

        if (result == null) {
            Timber.e("Formlist Downloading returned null.  That shouldn't happen");
            // Just displayes "error occured" to the user, but this should never happen.
            if (viewModel.isDownloadOnlyMode()) {
                setReturnResult(false, "Formlist Downloading returned null.  That shouldn't happen", null);
            }

            viewModel.setAlertDialog(getString(R.string.load_remote_form_error), getString(R.string.error_occured), EXIT);
            return;
        }

        if (result.containsKey(DL_AUTH_REQUIRED)) {
            // need authorization
            createAuthDialog();
        } else if (result.containsKey(DL_ERROR_MSG)) {
            // Download failed
            String dialogMessage =
                    getString(R.string.list_failed_with_error,
                            result.get(DL_ERROR_MSG).getErrorStr());
            String dialogTitle = getString(R.string.load_remote_form_error);

            if (viewModel.isDownloadOnlyMode()) {
                setReturnResult(false, getString(R.string.load_remote_form_error), viewModel.getFormResults());
            }

            viewModel.setAlertDialog(dialogTitle, dialogMessage, DO_NOT_EXIT);
        } else {
            // Everything worked. Clear the list and add the results.
            viewModel.setFormNamesAndURLs(result);

            viewModel.clearFormList();

            ArrayList<String> ids = new ArrayList<String>(viewModel.getFormNamesAndURLs().keySet());
            for (int i = 0; i < result.size(); i++) {
                String formDetailsKey = ids.get(i);
                FormDetails details = viewModel.getFormNamesAndURLs().get(formDetailsKey);

                if (!displayOnlyUpdatedForms || (details.isNewerFormVersionAvailable() || details.areNewerMediaFilesAvailable())) {
                    HashMap<String, String> item = new HashMap<>();
                    item.put(FORMNAME, details.getFormName());
                    item.put(FORMID_DISPLAY,
                            ((details.getFormVersion() == null) ? "" : (getString(R.string.version) + " "
                                    + details.getFormVersion() + " ")) + "ID: " + details.getFormID());
                    item.put(FORMDETAIL_KEY, formDetailsKey);
                    item.put(FORM_ID_KEY, details.getFormID());
                    item.put(FORM_VERSION_KEY, details.getFormVersion());

                    // Insert the new form in alphabetical order.
                    if (viewModel.getFormList().isEmpty()) {
                        viewModel.addForm(item);
                    } else {
                        int j;
                        for (j = 0; j < viewModel.getFormList().size(); j++) {
                            HashMap<String, String> compareMe = viewModel.getFormList().get(j);
                            String name = compareMe.get(FORMNAME);
                            if (name.compareTo(viewModel.getFormNamesAndURLs().get(ids.get(i)).getFormName()) > 0) {
                                break;
                            }
                        }
                        viewModel.addForm(j, item);
                    }
                }
            }
            filteredFormList.addAll(viewModel.getFormList());
            updateAdapter();
            selectSupersededForms();
            downloadButton.setEnabled(listView.getCheckedItemCount() > 0);
            toggleButton.setEnabled(listView.getCount() > 0);
            toggleButtonLabel(toggleButton, listView);

            if (viewModel.isDownloadOnlyMode()) {
                //1. First check if all form IDS could be found on the server - Register forms that could not be found

                for (String formId: viewModel.getFormIdsToDownload()) {
                    viewModel.putFormResult(formId, false);
                }

                ArrayList<FormDetails> filesToDownload  = new ArrayList<>();

                for (FormDetails formDetails: viewModel.getFormNamesAndURLs().values()) {
                    String formId = formDetails.getFormID();

                    if (viewModel.getFormResults().containsKey(formId)) {
                        filesToDownload.add(formDetails);
                    }
                }

                //2. Select forms and start downloading
                if (!filesToDownload.isEmpty()) {
                    startFormsDownload(filesToDownload);
                } else {
                    // None of the forms was found
                    setReturnResult(false, "Forms not found on server", viewModel.getFormResults());
                    finish();
                }

            }
        }
    }

    /**
     * Creates an alert dialog with the given tite and message. If shouldExit is set to true, the
     * activity will exit when the user clicks "ok".
     */
    private void createAlertDialog(String title, String message, final boolean shouldExit) {
        alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        DialogInterface.OnClickListener quitListener = (dialog, i) -> {
            switch (i) {
                case DialogInterface.BUTTON_POSITIVE: // ok
                    // just close the dialog
                    viewModel.removeAlertDialog();
                    // successful download, so quit
                    // Also quit if in download_mode only(called by another app/activity just to download)
                    if (shouldExit || viewModel.isDownloadOnlyMode()) {
                        finish();
                    }
                    break;
            }
        };
        alertDialog.setCancelable(false);
        alertDialog.setButton(getString(R.string.ok), quitListener);
        alertDialog.setIcon(android.R.drawable.ic_dialog_info);
        DialogUtils.showDialog(alertDialog, this);
    }

    private void createProgressDialog() {
        progressDialog = new ProgressDialog(this);
        DialogInterface.OnClickListener loadingButtonListener =
                (dialog, which) -> {
                    // we use the same progress dialog for both
                    // so whatever isn't null is running
                    dialog.dismiss();
                    if (downloadFormListTask != null) {
                        downloadFormListTask.setDownloaderListener(null);
                        downloadFormListTask.cancel(true);
                        downloadFormListTask = null;

                        // Only explicitly exit if DownloadFormListTask is running since
                        // DownloadFormTask has a callback when cancelled and has code to handle
                        // cancellation when in download mode only
                        if (viewModel.isDownloadOnlyMode()) {
                            setReturnResult(false, "User cancelled the operation", viewModel.getFormResults());
                            finish();
                        }
                    }

                    if (downloadFormsTask != null) {
                        viewModel.setCancelDialogShowing(true);
                        downloadFormsTask.cancel(true);
                    }
                    viewModel.setLoadingCanceled(true);
                    viewModel.setProgressDialogShowing(false);
                };
        progressDialog.setTitle(getString(R.string.downloading_data));
        progressDialog.setMessage(getString(R.string.please_wait));
        progressDialog.setIcon(android.R.drawable.ic_dialog_info);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setButton(getString(R.string.cancel), loadingButtonListener);
        DialogUtils.showDialog(progressDialog, this);
    }

    private void createAuthDialog() {
        viewModel.removeAlertDialog();

        AuthDialogUtility authDialogUtility = new AuthDialogUtility();
        if (viewModel.getUrl() != null && viewModel.getUsername() != null && viewModel.getPassword() != null) {
            authDialogUtility.setCustomUsername(viewModel.getUsername());
            authDialogUtility.setCustomPassword(viewModel.getPassword());
        }
        DialogUtils.showDialog(authDialogUtility.createDialog(this, this, viewModel.getUrl()), this);
    }

    private void createCancelDialog() {
        cancelDialog = new ProgressDialog(this);
        cancelDialog.setTitle(getString(R.string.canceling));
        cancelDialog.setMessage(getString(R.string.please_wait));
        cancelDialog.setIcon(android.R.drawable.ic_dialog_info);
        cancelDialog.setIndeterminate(true);
        cancelDialog.setCancelable(false);
        DialogUtils.showDialog(cancelDialog, this);
    }

    @Override
    public void progressUpdate(String currentFile, int progress, int total) {
        String message = getString(R.string.fetching_file, currentFile, String.valueOf(progress), String.valueOf(total));
        viewModel.setProgressDialogMessage(message);
    }

    @Override
    public void formsDownloadingComplete(HashMap<FormDetails, String> result) {
        if (downloadFormsTask != null) {
            downloadFormsTask.setDownloaderListener(null);
        }

        cleanUpWebCredentials();

        if (progressDialog.isShowing()) {
            // should always be true here
            progressDialog.dismiss();
            viewModel.setProgressDialogShowing(false);
        }

        viewModel.setAlertDialog(getString(R.string.download_forms_result), getDownloadResultMessage(result), EXIT);

        // Set result to true for forms which were downloaded
        if (viewModel.isDownloadOnlyMode()) {
            for (FormDetails formDetails: result.keySet()) {
                String successKey = result.get(formDetails);
                if (Collect.getInstance().getString(R.string.success).equals(successKey)) {
                    if (viewModel.getFormResults().containsKey(formDetails.getFormID())) {
                        viewModel.putFormResult(formDetails.getFormID(), true);
                    }
                }
            }

            setReturnResult(true, null, viewModel.getFormResults());
        }
    }

    public static String getDownloadResultMessage(HashMap<FormDetails, String> result) {
        Set<FormDetails> keys = result.keySet();
        StringBuilder b = new StringBuilder();
        for (FormDetails k : keys) {
            b.append(k.getFormName() + " ("
                    + ((k.getFormVersion() != null)
                    ? (Collect.getInstance().getString(R.string.version) + ": " + k.getFormVersion() + " ")
                    : "") + "ID: " + k.getFormID() + ") - " + result.get(k));
            b.append("\n\n");
        }

        return b.toString().trim();
    }

    @Override
    public void formsDownloadingCancelled() {
        if (downloadFormsTask != null) {
            downloadFormsTask.setDownloaderListener(null);
            downloadFormsTask = null;
        }

        cleanUpWebCredentials();

        if (cancelDialog != null && cancelDialog.isShowing()) {
            cancelDialog.dismiss();
            viewModel.setCancelDialogShowing(false);
        }

        if (viewModel.isDownloadOnlyMode()) {
            setReturnResult(false, "Download cancelled", null);
            finish();
        }
    }

    @Override
    public void updatedCredentials() {
        // If the user updated the custom credentials using the dialog, let us update our
        // variables holding the custom credentials
        if (viewModel.getUrl() != null) {
            HttpCredentialsInterface httpCredentials = webCredentialsUtils.getCredentials(URI.create(viewModel.getUrl()));

            if (httpCredentials != null) {
                viewModel.setUsername(httpCredentials.getUsername());
                viewModel.setPassword(httpCredentials.getPassword());
            }
        }

        downloadFormList();
    }

    @Override
    public void cancelledUpdatingCredentials() {
        finish();
    }

    @Override
    public void setReturnResult(boolean successful, @Nullable String message, @Nullable HashMap<String, Boolean> resultFormIds) {
        Intent intent = new Intent();
        intent.putExtra(ApplicationConstants.BundleKeys.SUCCESS_KEY, successful);
        if (message != null) {
            intent.putExtra(ApplicationConstants.BundleKeys.MESSAGE, message);
        }
        if (resultFormIds != null) {
            intent.putExtra(ApplicationConstants.BundleKeys.FORM_IDS, resultFormIds);
        }

        setResult(RESULT_OK, intent);
    }

    @Override
    public void goBack() {
        finish();
    }

    private void cleanUpWebCredentials() {
        if (viewModel.getUrl() != null) {
            String host = Uri.parse(viewModel.getUrl())
                    .getHost();

            if (host != null) {
                webCredentialsUtils.clearCredentials(viewModel.getUrl());
            }
        }
    }
}
