/*
 * Copyright 2019 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.odk.collect.android.ui.formdownload;

import android.os.Bundle;

import org.odk.collect.android.R;
import org.odk.collect.android.http.HttpCredentialsInterface;
import org.odk.collect.android.logic.FormDetails;
import org.odk.collect.android.ui.base.BaseViewModel;
import org.odk.collect.android.utilities.ApplicationConstants;
import org.odk.collect.android.utilities.NetworkUtils;
import org.odk.collect.android.utilities.ToastUtils;
import org.odk.collect.android.utilities.WebCredentialsUtils;
import org.odk.collect.android.utilities.providers.BaseResourceProvider;
import org.odk.collect.android.utilities.rx.SchedulerProvider;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;
import timber.log.Timber;

import static org.odk.collect.android.ui.formdownload.FormDownloadActivity.getDownloadResultMessage;
import static org.odk.collect.android.utilities.DownloadFormListUtils.DL_AUTH_REQUIRED;

public class FormDownloadViewModel extends BaseViewModel<FormDownloadNavigator> {

    private HashMap<String, FormDetails> formNamesAndURLs = new HashMap<>();

    private final ArrayList<HashMap<String, String>> formList = new ArrayList<>();

    private final LinkedHashSet<String> selectedForms = new LinkedHashSet<>();

    private final BehaviorSubject<AlertDialogUiModel> alertDialogSubject;
    private final BehaviorSubject<Boolean> progressDialogSubject;
    private final BehaviorSubject<String> progressDialogMessageSubject;
    private final BehaviorSubject<Boolean> cancelDialogSubject;
    private final BehaviorSubject<AuthorizationModel> authDialogSubject;
    private final BehaviorSubject<HashMap<String, FormDetails>> formListDownloadSubject;

    private boolean alertDialogVisible;
    private boolean loadingCanceled;

    // Variables used when the activity is called from an external app
    private boolean isDownloadOnlyMode;
    private String[] formIdsToDownload;
    private String url;
    private String username;
    private String password;
    private final HashMap<String, Boolean> formResults = new HashMap<>();

    private final NetworkUtils networkUtils;
    private final BaseResourceProvider resourceProvider;
    private final FormDownloadRepository downloadRepository;
    private final WebCredentialsUtils webCredentialsUtils;

    private Disposable formListDownloadDisposable;
    private Disposable formDownloadDisposable;
    private Disposable downloadProgressDisposable;

    public FormDownloadViewModel(
            SchedulerProvider schedulerProvider,
            NetworkUtils networkUtils,
            BaseResourceProvider resourceProvider,
            FormDownloadRepository downloadRepository,
            WebCredentialsUtils webCredentialsUtils) {
        super(schedulerProvider);

        this.networkUtils = networkUtils;
        this.resourceProvider = resourceProvider;
        this.downloadRepository = downloadRepository;
        this.webCredentialsUtils = webCredentialsUtils;

        alertDialogSubject = BehaviorSubject.create();
        progressDialogSubject = BehaviorSubject.create();
        progressDialogMessageSubject = BehaviorSubject.create();
        cancelDialogSubject = BehaviorSubject.create();
        formListDownloadSubject = BehaviorSubject.create();
        authDialogSubject = BehaviorSubject.create();
    }

    public void restoreState(Bundle bundle) {
        if (bundle != null && bundle.containsKey(ApplicationConstants.BundleKeys.FORM_IDS)) {
            isDownloadOnlyMode = true;
            formIdsToDownload = bundle.getStringArray(ApplicationConstants.BundleKeys.FORM_IDS);

            if (formIdsToDownload == null) {
                getNavigator().setReturnResult(false, "Form Ids is null", null);
                getNavigator().goBack();
            }

            if (bundle.containsKey(ApplicationConstants.BundleKeys.URL)) {
                url = bundle.getString(ApplicationConstants.BundleKeys.URL);
            }

            if (bundle.containsKey(ApplicationConstants.BundleKeys.USERNAME)) {
                username = bundle.getString(ApplicationConstants.BundleKeys.USERNAME);
            }

            if (bundle.containsKey(ApplicationConstants.BundleKeys.PASSWORD)) {
                password = bundle.getString(ApplicationConstants.BundleKeys.PASSWORD);
            }
        }
    }

    public HashMap<String, FormDetails> getFormNamesAndURLs() {
        return formNamesAndURLs;
    }

    public void setFormNamesAndURLs(HashMap<String, FormDetails> formNamesAndURLs) {
        this.formNamesAndURLs = formNamesAndURLs;
    }

    public void clearFormNamesAndURLs() {
        formNamesAndURLs.clear();
    }

    public BehaviorSubject<String> getProgressDialogMessage() {
        return progressDialogMessageSubject;
    }

    public void setProgressDialogMessage(String progressDialogMessage) {
        progressDialogMessageSubject.onNext(progressDialogMessage);
    }

    public ArrayList<HashMap<String, String>> getFormList() {
        return formList;
    }

    public void clearFormList() {
        formList.clear();
    }

    public void addForm(HashMap<String, String> item) {
        formList.add(item);
    }

    public void addForm(int index, HashMap<String, String> item) {
        formList.add(index, item);
    }

    public LinkedHashSet<String> getSelectedForms() {
        return selectedForms;
    }

    public void addSelectedForm(String form) {
        selectedForms.add(form);
    }

    public void removeSelectedForm(String form) {
        selectedForms.remove(form);
    }

    public void clearSelectedForms() {
        selectedForms.clear();
    }

    public void setDownloadOnlyMode(boolean isDownloadOnlyMode) {
        this.isDownloadOnlyMode = isDownloadOnlyMode;
    }

    public boolean isDownloadOnlyMode() {
        return isDownloadOnlyMode;
    }

    public HashMap<String, Boolean> getFormResults() {
        return formResults;
    }

    public void putFormResult(String formId, boolean result) {
        formResults.put(formId, result);
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String[] getFormIdsToDownload() {
        return Arrays.copyOf(formIdsToDownload, formIdsToDownload.length);
    }

    public void setCancelDialogShowing(boolean cancelDialogShowing) {
        cancelDialogSubject.onNext(cancelDialogShowing);
    }

    public Observable<Boolean> getCancelDialog() {
        return cancelDialogSubject;
    }

    public boolean wasLoadingCanceled() {
        return loadingCanceled;
    }

    public void setLoadingCanceled(boolean loadingCanceled) {
        this.loadingCanceled = loadingCanceled;
    }

    public void setProgressDialogShowing(boolean progressDialogShowing) {
        progressDialogSubject.onNext(progressDialogShowing);
    }

    public Observable<Boolean> getProgressDialog() {
        return progressDialogSubject;
    }

    public Observable<AlertDialogUiModel> getAlertDialog() {
        return alertDialogSubject
                .filter(__ -> alertDialogVisible);
    }

    public void setAlertDialog(String title, String message, boolean shouldExit) {
        alertDialogVisible = true;
        alertDialogSubject.onNext(new AlertDialogUiModel(title, message, shouldExit));
    }

    public void removeAlertDialog() {
        alertDialogVisible = false;
    }

    public Observable<AuthorizationModel> getAuthDialogSubject() {
        return authDialogSubject
                .doOnNext(__ -> removeAlertDialog());
    }

    public Disposable getFormListDownloadDisposable() {
        return formListDownloadDisposable;
    }

    public void startDownloadingFormList() {
        if (!networkUtils.isNetworkAvailable()) {
            ToastUtils.showShortToast(R.string.no_connection);

            if (isDownloadOnlyMode) {
                getNavigator().setReturnResult(false, resourceProvider.getString(R.string.no_connection), formResults);
                getNavigator().goBack();
            }
        } else {
            clearFormNamesAndURLs();
            setProgressDialogShowing(true);

            // cancel pending tasks
            cancelFormListDownloadTask();

            formListDownloadDisposable = downloadRepository.downloadFormList(url, username, password)
                    .subscribeOn(getSchedulerProvider().computation())
                    .observeOn(getSchedulerProvider().io())
                    .subscribe(result -> {
                        if (result.containsKey(DL_AUTH_REQUIRED)) {
                            authDialogSubject.onNext(new AuthorizationModel(url, username, password));
                        } else {
                            formListDownloadSubject.onNext(result);
                        }
                    }, Timber::e);

            getCompositeDisposable().add(formListDownloadDisposable);
        }
    }

    public void cancelFormListDownloadTask() {
        loadingCanceled = true;

        if (downloadRepository.isLoading() && formListDownloadDisposable != null) {
            formListDownloadDisposable.dispose();
            formListDownloadDisposable = null;

            setProgressDialogShowing(false);

            // Only explicitly exit if DownloadFormListTask is running since
            // DownloadFormTask has a callback when cancelled and has code to handle
            // cancellation when in download mode only
            if (isDownloadOnlyMode) {
                getNavigator().setReturnResult(false, "User cancelled the operation", formResults);
                getNavigator().goBack();
            }
        }
    }

    public Observable<HashMap<String, FormDetails>> getFormDownloadList() {
        return formListDownloadSubject
                .doOnNext(__ -> setProgressDialogShowing(false));
    }

    public Disposable getFormDownloadDisposable() {
        return formDownloadDisposable;
    }

    public void startDownloadingForms(List<FormDetails> filesToDownload) {

        if (url != null) {
            if (username != null && password != null) {
                webCredentialsUtils.saveCredentials(url, username, password);
            } else {
                webCredentialsUtils.clearCredentials(url);
            }
        }

        if (filesToDownload.isEmpty()) {
            ToastUtils.showShortToast(R.string.noselect_error);
        } else {
            setProgressDialogShowing(true);

            formDownloadDisposable = downloadRepository.downloadForms(filesToDownload)
                    .subscribeOn(getSchedulerProvider().computation())
                    .observeOn(getSchedulerProvider().io())
                    .doOnSubscribe(disposable -> {
                        downloadProgressDisposable = downloadRepository
                                .getFormDownloadProgress()
                                .subscribe(this::setProgressDialogMessage, Timber::e);

                        getCompositeDisposable().add(downloadProgressDisposable);
                    })
                    .doOnDispose(() -> {
                        downloadProgressDisposable.dispose();
                        webCredentialsUtils.clearCredentials(url);

                        setCancelDialogShowing(false);

                        if (isDownloadOnlyMode && getNavigator() != null) {
                            getNavigator().setReturnResult(false, "Download cancelled", null);
                            getNavigator().goBack();
                        }
                    })
                    .subscribe(result -> {
                        webCredentialsUtils.clearCredentials(url);

                        setProgressDialogShowing(false);

                        setAlertDialog(resourceProvider.getString(R.string.download_forms_result), getDownloadResultMessage(result), true);

                        // Set result to true for forms which were downloaded
                        if (isDownloadOnlyMode()) {
                            for (FormDetails formDetails : result.keySet()) {
                                String successKey = result.get(formDetails);
                                if (resourceProvider.getString(R.string.success).equals(successKey)) {
                                    if (getFormResults().containsKey(formDetails.getFormID())) {
                                        putFormResult(formDetails.getFormID(), true);
                                    }
                                }
                            }

                            getNavigator().setReturnResult(true, null, getFormResults());
                        }
                    }, Timber::e);

            getCompositeDisposable().add(formDownloadDisposable);
        }
    }

    public void cancelFormDownloadTask() {
        loadingCanceled = true;
        setCancelDialogShowing(true);
        setProgressDialogShowing(false);

        if (downloadRepository.isLoading() && formDownloadDisposable != null) {
            formDownloadDisposable.dispose();
            formDownloadDisposable = null;
        }
    }

    public void updateCredentials() {
        if (url != null) {
            HttpCredentialsInterface httpCredentials = webCredentialsUtils.getCredentials(URI.create(url));

            if (httpCredentials != null) {
                username = httpCredentials.getUsername();
                password = httpCredentials.getPassword();
            }
        }
    }
}
