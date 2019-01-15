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

import android.arch.lifecycle.ViewModel;

import org.odk.collect.android.logic.FormDetails;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;

public class FormDownloadListViewModel extends ViewModel {
    private HashMap<String, FormDetails> formNamesAndURLs = new HashMap<>();
    private final HashMap<String, Boolean> formResult = new HashMap<>();

    private final ArrayList<HashMap<String, String>> formList = new ArrayList<>();
    private final ArrayList<String> formsFound = new ArrayList<>();

    private final LinkedHashSet<String> selectedForms = new LinkedHashSet<>();

    private String[] formIdsToDownload;

    private String alertTitle;
    private String alertMsg;
    private String url;
    private String username;
    private String password;

    private boolean alertShowing;
    private boolean shouldExit;
    // Variables for the external app intent call
    private boolean isDownloadOnlyMode;

    public HashMap<String, FormDetails> getFormNamesAndURLs() {
        return formNamesAndURLs;
    }

    public void setFormNamesAndURLs(HashMap<String, FormDetails> formNamesAndURLs) {
        this.formNamesAndURLs = formNamesAndURLs;
    }

    public void clearFormNamesAndURLs() {
        formNamesAndURLs.clear();
    }

    public String getAlertTitle() {
        return alertTitle;
    }

    public void setAlertTitle(String alertTitle) {
        this.alertTitle = alertTitle;
    }

    public String getAlertMsg() {
        return alertMsg;
    }

    public void setAlertMsg(String alertMsg) {
        this.alertMsg = alertMsg;
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

    public void addFormList(HashMap<String, String> item) {
        formList.add(item);
    }

    public void addFormList(int index, HashMap<String, String> item) {
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

    public HashMap<String, Boolean> getFormResult() {
        return formResult;
    }

    public void putFormResult(String form, boolean reasult) {
        formResult.put(form, reasult);
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

    public void addFormsFound(String formId) {
        formsFound.add(formId);
    }
}
