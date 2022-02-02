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

package org.odk.collect.android.activities.viewmodels;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import org.odk.collect.android.formmanagement.ServerFormDetails;
import org.odk.collect.android.formmanagement.ServerFormsDetailsFetcher;
import org.odk.collect.android.utilities.WebCredentialsUtils;
import org.odk.collect.async.Scheduler;
import org.odk.collect.forms.FormSourceException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;

public class FormDownloadListViewModel extends ViewModel {

    private HashMap<String, ServerFormDetails> formDetailsByFormId = new HashMap<>();

    /**
     * List of forms from the formList response. The map acts like a DisplayableForm object with
     * values for each component that shows up in the form list UI. See
     * FormDownloadListActivity.formListDownloadingComplete for keys.
     */
    private final ArrayList<HashMap<String, String>> formList = new ArrayList<>();

    private final LinkedHashSet<String> selectedFormIds = new LinkedHashSet<>();

    private String alertTitle;
    private String alertDialogMsg;

    private boolean alertShowing;
    private boolean cancelDialogShowing;
    private boolean shouldExit;
    private boolean loadingCanceled;

    // Variables used when the activity is called from an external app
    private boolean isDownloadOnlyMode;
    private String[] formIdsToDownload;
    private String url;
    private String username;
    private String password;
    private WebCredentialsUtils webCredentialsUtils;
    private final HashMap<String, Boolean> formResults = new HashMap<>();
    private final Scheduler scheduler;
    private final ServerFormsDetailsFetcher serverFormsDetailsFetcher;

    private final MutableLiveData<Pair<HashMap<String, ServerFormDetails>, FormSourceException>> downloadMutableLiveData = new
            MutableLiveData<>(null);
    public final LiveData<Pair<HashMap<String, ServerFormDetails>, FormSourceException>> downloadFormListLiveData = downloadMutableLiveData;

    public FormDownloadListViewModel(Scheduler scheduler, ServerFormsDetailsFetcher serverFormsDetailsFetcher) {
        this.scheduler = scheduler;
        this.serverFormsDetailsFetcher = serverFormsDetailsFetcher;
    }

    public HashMap<String, ServerFormDetails> getFormDetailsByFormId() {
        return formDetailsByFormId;
    }

    public void setFormDetailsByFormId(HashMap<String, ServerFormDetails> formDetailsByFormId) {
        this.formDetailsByFormId = formDetailsByFormId;
    }

    public void clearFormDetailsByFormId() {
        formDetailsByFormId.clear();
    }

    public String getAlertTitle() {
        return alertTitle;
    }

    public void setAlertTitle(String alertTitle) {
        this.alertTitle = alertTitle;
    }

    public String getAlertDialogMsg() {
        return alertDialogMsg;
    }

    public void setAlertDialogMsg(String alertDialogMsg) {
        this.alertDialogMsg = alertDialogMsg;
    }

    public boolean isAlertShowing() {
        return alertShowing;
    }

    public void setAlertShowing(boolean alertShowing) {
        this.alertShowing = alertShowing;
    }

    public boolean shouldExit() {
        return shouldExit;
    }

    public void setShouldExit(boolean shouldExit) {
        this.shouldExit = shouldExit;
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

    public LinkedHashSet<String> getSelectedFormIds() {
        return selectedFormIds;
    }

    public void addSelectedFormId(String selectedFormId) {
        selectedFormIds.add(selectedFormId);
    }

    public void removeSelectedFormId(String selectedFormId) {
        selectedFormIds.remove(selectedFormId);
    }

    public void clearSelectedFormIds() {
        selectedFormIds.clear();
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

    public boolean isCancelDialogShowing() {
        return cancelDialogShowing;
    }

    public void setCancelDialogShowing(boolean cancelDialogShowing) {
        this.cancelDialogShowing = cancelDialogShowing;
    }

    public boolean wasLoadingCanceled() {
        return loadingCanceled;
    }

    public void setLoadingCanceled(boolean loadingCanceled) {
        this.loadingCanceled = loadingCanceled;
    }

    public void downloadFormList() {
        scheduler.immediate(
                () -> {
                    if (webCredentialsUtils != null) {
                        setTemporaryCredentials();
                    }

                    List<ServerFormDetails> formList = null;
                    FormSourceException exception = null;

                    try {
                        formList = serverFormsDetailsFetcher.fetchFormDetails();
                    } catch (FormSourceException e) {
                        exception = e;
                    } finally {
                        if (webCredentialsUtils != null) {
                            clearTemporaryCredentials();
                        }
                    }

                    return new Pair<>(formList, exception);
                },
                result -> {
                    if (result.first != null) {
                        HashMap<String, ServerFormDetails> detailsHashMap = new HashMap<>();
                        for (ServerFormDetails details : result.first) {
                            detailsHashMap.put(details.getFormId(), details);
                        }

                        downloadMutableLiveData.postValue(new Pair<>(detailsHashMap, result.second));
                    } else {
                        downloadMutableLiveData.postValue(new Pair<>(null, result.second));
                    }
                }
        );
    }

    public void setAlternateCredentials(WebCredentialsUtils webCredentialsUtils) {
        this.webCredentialsUtils = webCredentialsUtils;
        serverFormsDetailsFetcher.updateCredentials(webCredentialsUtils);

        if (url != null && !url.isEmpty()) {
            serverFormsDetailsFetcher.updateUrl(url);
        }
    }

    private void setTemporaryCredentials() {
        if (url != null) {
            String host = Uri.parse(url).getHost();

            if (host != null) {
                if (username != null && password != null) {
                    webCredentialsUtils.saveCredentials(url, username, password);
                } else {
                    webCredentialsUtils.clearCredentials(url);
                }
            }
        }
    }

    private void clearTemporaryCredentials() {
        if (url != null) {
            String host = Uri.parse(url).getHost();

            if (host != null) {
                webCredentialsUtils.clearCredentials(url);
            }
        }
    }

    public static class Factory implements ViewModelProvider.Factory {

        private final Scheduler scheduler;
        private final ServerFormsDetailsFetcher serverFormsDetailsFetcher;

        public Factory(Scheduler scheduler, ServerFormsDetailsFetcher serverFormsDetailsFetcher) {
            this.scheduler = scheduler;
            this.serverFormsDetailsFetcher = serverFormsDetailsFetcher;
        }

        @SuppressWarnings("unchecked")
        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new FormDownloadListViewModel(scheduler, serverFormsDetailsFetcher);
        }
    }
}
