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
import org.odk.collect.android.logic.FormDetails;
import org.odk.collect.android.ui.base.BaseViewModel;
import org.odk.collect.android.utilities.ApplicationConstants;
import org.odk.collect.android.utilities.NetworkUtils;
import org.odk.collect.android.utilities.ToastUtils;
import org.odk.collect.android.utilities.providers.BaseResourceProvider;
import org.odk.collect.android.utilities.rx.SchedulerProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;
import timber.log.Timber;

public class FormDownloadViewModel extends BaseViewModel<FormDownloadNavigator> {

    private HashMap<String, FormDetails> formNamesAndURLs = new HashMap<>();

    private final ArrayList<HashMap<String, String>> formList = new ArrayList<>();

    private final LinkedHashSet<String> selectedForms = new LinkedHashSet<>();

    private final BehaviorSubject<AlertDialogUiModel> alertDialogSubject;
    private final BehaviorSubject<Boolean> progressDialogSubject;
    private final BehaviorSubject<String> progressDialogMessageSubject;
    private final BehaviorSubject<Boolean> cancelDialogSubject;
    private final BehaviorSubject<HashMap<String, FormDetails>> formDownloadSubject;

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
    private Disposable downloadDisposable;

    public FormDownloadViewModel(SchedulerProvider schedulerProvider, NetworkUtils networkUtils, BaseResourceProvider resourceProvider, FormDownloadRepository downloadRepository) {
        super(schedulerProvider);

        this.networkUtils = networkUtils;
        this.resourceProvider = resourceProvider;
        this.downloadRepository = downloadRepository;

        alertDialogSubject = BehaviorSubject.create();
        progressDialogSubject = BehaviorSubject.create();
        progressDialogMessageSubject = BehaviorSubject.create();
        cancelDialogSubject = BehaviorSubject.create();
        formDownloadSubject = BehaviorSubject.create();
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

    public boolean isDownloadOnlyMode() {
        return isDownloadOnlyMode;
    }

    public void setDownloadOnlyMode(boolean downloadOnlyMode) {
        isDownloadOnlyMode = downloadOnlyMode;
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

    public void setFormIdsToDownload(String[] formIdsToDownload) {
        this.formIdsToDownload = formIdsToDownload;
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

    public void startDownloadingForms() {
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
            if (downloadRepository.isLoading()) {
                downloadDisposable.dispose();
            }

            downloadDisposable = downloadRepository.downloadForms(url, username, password)
                    .subscribeOn(getSchedulerProvider().computation())
                    .observeOn(getSchedulerProvider().io())
                    .subscribe(formDownloadSubject::onNext, Timber::e);

            getCompositeDisposable().add(downloadDisposable);
        }
    }

    public Observable<HashMap<String, FormDetails>> getFormDownloadList() {
        return formDownloadSubject;
    }
}
