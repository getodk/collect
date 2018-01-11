/*
 * Copyright 2017 Shobhit
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

package org.odk.collect.android.preferences;

import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.text.InputFilter;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListPopupWindow;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.utilities.gdrive.GoogleAccountsManager;
import org.odk.collect.android.preferences.filters.ControlCharacterFilter;
import org.odk.collect.android.preferences.filters.WhitespaceFilter;
import org.odk.collect.android.utilities.AuthDialogUtility;
import org.odk.collect.android.utilities.ToastUtils;
import org.odk.collect.android.utilities.UrlUtils;
import org.odk.collect.android.utilities.WebUtils;

import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static org.odk.collect.android.utilities.gdrive.GoogleAccountsManager.REQUEST_ACCOUNT_PICKER;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_FORMLIST_URL;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_SUBMISSION_URL;


public class ServerPreferencesFragment extends BasePreferenceFragment implements View.OnTouchListener,
        Preference.OnPreferenceChangeListener, GoogleAccountsManager.GoogleAccountSelectionListener {
    private static final String KNOWN_URL_LIST = "knownUrlList";
    protected EditTextPreference serverUrlPreference;
    protected EditTextPreference usernamePreference;
    protected EditTextPreference passwordPreference;
    protected boolean credentialsHaveChanged = false;
    protected EditTextPreference submissionUrlPreference;
    protected EditTextPreference formListUrlPreference;
    private ListPopupWindow listPopupWindow;
    private List<String> urlList;
    private Preference selectedGoogleAccountPreference;
    private GoogleAccountsManager accountsManager;


    public void addAggregatePreferences() {
        addPreferencesFromResource(R.xml.aggregate_preferences);

        serverUrlPreference = (EditTextPreference) findPreference(
                PreferenceKeys.KEY_SERVER_URL);
        usernamePreference = (EditTextPreference) findPreference(PreferenceKeys.KEY_USERNAME);
        passwordPreference = (EditTextPreference) findPreference(PreferenceKeys.KEY_PASSWORD);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String urlListString = prefs.getString(KNOWN_URL_LIST, "");
        if (urlListString.isEmpty()) {
            urlList = new ArrayList<>();
        } else {
            urlList =
                    new Gson().fromJson(urlListString, new TypeToken<List<String>>() {
                    }.getType());
        }
        if (urlList.size() == 0) {
            addUrlToPreferencesList(getString(R.string.default_server_url), prefs);
        }

        urlDropdownSetup();

        serverUrlPreference.getEditText().setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down, 0);
        serverUrlPreference.getEditText().setOnTouchListener(this);
        serverUrlPreference.setOnPreferenceChangeListener(this);
        serverUrlPreference.setSummary(serverUrlPreference.getText());
        serverUrlPreference.getEditText().setFilters(
                new InputFilter[]{new ControlCharacterFilter(), new WhitespaceFilter()});

        usernamePreference.setOnPreferenceChangeListener(this);
        usernamePreference.setSummary(usernamePreference.getText());
        usernamePreference.getEditText().setFilters(
                new InputFilter[]{new ControlCharacterFilter()});

        passwordPreference.setOnPreferenceChangeListener(this);
        maskPasswordSummary(passwordPreference.getText());
        passwordPreference.getEditText().setFilters(
                new InputFilter[]{new ControlCharacterFilter()});
    }

    public void addGooglePreferences() {
        addPreferencesFromResource(R.xml.google_preferences);
        selectedGoogleAccountPreference = findPreference(PreferenceKeys.KEY_SELECTED_GOOGLE_ACCOUNT);

        EditTextPreference googleSheetsUrlPreference = (EditTextPreference) findPreference(
                PreferenceKeys.KEY_GOOGLE_SHEETS_URL);
        googleSheetsUrlPreference.setOnPreferenceChangeListener(this);

        String currentGoogleSheetsURL = googleSheetsUrlPreference.getText();
        if (currentGoogleSheetsURL != null && currentGoogleSheetsURL.length() > 0) {
            googleSheetsUrlPreference.setSummary(currentGoogleSheetsURL + "\n\n"
                    + getString(R.string.google_sheets_url_hint));
        }

        googleSheetsUrlPreference.getEditText().setFilters(new InputFilter[]{
                new ControlCharacterFilter(), new WhitespaceFilter()
        });
        initAccountPreferences();
    }

    public void addOtherPreferences() {
        addAggregatePreferences();
        addPreferencesFromResource(R.xml.other_preferences);

        formListUrlPreference = (EditTextPreference) findPreference(KEY_FORMLIST_URL);
        submissionUrlPreference = (EditTextPreference) findPreference(KEY_SUBMISSION_URL);

        InputFilter[] filters = {new ControlCharacterFilter(), new WhitespaceFilter()};

        serverUrlPreference.getEditText().setFilters(filters);

        formListUrlPreference.setOnPreferenceChangeListener(this);
        formListUrlPreference.setSummary(formListUrlPreference.getText());
        formListUrlPreference.getEditText().setFilters(filters);

        submissionUrlPreference.setOnPreferenceChangeListener(this);
        submissionUrlPreference.setSummary(submissionUrlPreference.getText());
        submissionUrlPreference.getEditText().setFilters(filters);
    }

    public void initAccountPreferences() {
        accountsManager = new GoogleAccountsManager(this);
        accountsManager.setListener(this);
        accountsManager.disableAutoChooseAccount();

        selectedGoogleAccountPreference.setSummary(accountsManager.getSelectedAccount());
        selectedGoogleAccountPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                accountsManager.chooseAccount();
                return true;
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        accountsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void addUrlToPreferencesList(String url, SharedPreferences prefs) {
        urlList.add(0, url);
        String urlListString = new Gson().toJson(urlList);
        prefs
                .edit()
                .putString(KNOWN_URL_LIST, urlListString)
                .apply();
    }

    private void urlDropdownSetup() {
        listPopupWindow = new ListPopupWindow(getActivity());
        setupUrlDropdownAdapter();
        listPopupWindow.setAnchorView(serverUrlPreference.getEditText());
        listPopupWindow.setModal(true);
        listPopupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                serverUrlPreference.getEditText().setText(urlList.get(position));
                listPopupWindow.dismiss();
            }
        });
    }

    private void setupUrlDropdownAdapter() {
        ArrayAdapter adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, urlList);
        listPopupWindow.setAdapter(adapter);
    }

    @Override
    public void onPause() {
        super.onPause();

        if (credentialsHaveChanged) {
            AuthDialogUtility.setWebCredentialsFromPreferences();
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        final int DRAWABLE_RIGHT = 2;
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (event.getX() >= (v.getWidth() - ((EditText) v)
                    .getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                InputMethodManager imm = (InputMethodManager) getActivity()
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                listPopupWindow.show();
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {

        switch (preference.getKey()) {

            case PreferenceKeys.KEY_SERVER_URL:

                String url = newValue.toString();

                // remove all trailing "/"s
                while (url.endsWith("/")) {
                    url = url.substring(0, url.length() - 1);
                }

                if (UrlUtils.isValidUrl(url)) {
                    preference.setSummary(newValue.toString());
                    SharedPreferences prefs = PreferenceManager
                            .getDefaultSharedPreferences(getActivity().getApplicationContext());
                    String urlListString = prefs.getString(KNOWN_URL_LIST, "");

                    urlList =
                            new Gson().fromJson(urlListString,
                                    new TypeToken<List<String>>() {
                                    }.getType());

                    if (!urlList.contains(url)) {
                        // We store a list with at most 5 elements
                        if (urlList.size() == 5) {
                            urlList.remove(4);
                        }
                        addUrlToPreferencesList(url, prefs);
                        setupUrlDropdownAdapter();
                    }
                } else {
                    ToastUtils.showShortToast(R.string.url_error);
                    return false;
                }
                break;

            case PreferenceKeys.KEY_USERNAME:
                String username = newValue.toString();

                // do not allow leading and trailing whitespace
                if (!username.equals(username.trim())) {
                    ToastUtils.showShortToast(R.string.username_error_whitespace);
                    return false;
                }

                preference.setSummary(username);
                clearCachedCrendentials();

                // To ensure we update current credentials in CredentialsProvider
                credentialsHaveChanged = true;

                return true;

            case PreferenceKeys.KEY_PASSWORD:
                String pw = newValue.toString();

                // do not allow leading and trailing whitespace
                if (!pw.equals(pw.trim())) {
                    ToastUtils.showShortToast(R.string.password_error_whitespace);
                    return false;
                }

                maskPasswordSummary(pw);
                clearCachedCrendentials();

                // To ensure we update current credentials in CredentialsProvider
                credentialsHaveChanged = true;
                break;

            case PreferenceKeys.KEY_GOOGLE_SHEETS_URL:
                url = newValue.toString();

                // remove all trailing "/"s
                while (url.endsWith("/")) {
                    url = url.substring(0, url.length() - 1);
                }

                if (UrlUtils.isValidUrl(url)) {
                    preference.setSummary(url + "\n\n" + getString(R.string.google_sheets_url_hint));
                } else if (url.length() == 0) {
                    preference.setSummary(getString(R.string.google_sheets_url_hint));
                } else {
                    ToastUtils.showShortToast(R.string.url_error);
                    return false;
                }
                break;
        }
        return true;
    }

    private void maskPasswordSummary(String password) {
        passwordPreference.setSummary(password != null && password.length() > 0
                ? "********"
                : "");
    }

    private void clearCachedCrendentials() {
        String server = (String) GeneralSharedPreferences
                .getInstance().get(PreferenceKeys.KEY_SERVER_URL);
        Uri u = Uri.parse(server);
        WebUtils.clearHostCredentials(u.getHost());
        Collect.getInstance().getCookieStore().clear();
    }

    protected void setDefaultAggregatePaths() {
        GeneralSharedPreferences sharedPreferences = GeneralSharedPreferences.getInstance();
        sharedPreferences.reset(KEY_FORMLIST_URL);
        sharedPreferences.reset(KEY_SUBMISSION_URL);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null && data.getExtras() != null) {
                    String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    accountsManager.setSelectedAccountName(accountName);
                }
                break;
        }
    }

    @Override
    public void onGoogleAccountSelected(String accountName) {
        selectedGoogleAccountPreference.setSummary(accountName);
    }
}
