/*
 * Copyright (C) 2014 Nafundi
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

package org.odk.collect.android.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListPopupWindow;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.utilities.AuthDialogUtility;
import org.odk.collect.android.utilities.UrlUtils;
import org.odk.collect.android.utilities.WebUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles aggregate specific preferences.
 *
 * @author Carl Hartung (chartung@nafundi.com)
 */
public class AggregatePreferencesActivity extends PreferenceActivity {

    private static final String KNOWN_URL_LIST = "knownUrlList";

    protected EditTextPreference mServerUrlPreference;
    protected EditTextPreference mUsernamePreference;
    protected EditTextPreference mPasswordPreference;
    protected boolean mCredentialsHaveChanged = false;

    private ListPopupWindow mListPopupWindow;
    private List<String> mUrlList;

    @Override
    protected void onPause() {
        super.onPause();

        if (mCredentialsHaveChanged) {
            AuthDialogUtility.setWebCredentialsFromPreferences(getBaseContext());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.aggregate_preferences);

        mServerUrlPreference = (EditTextPreference) findPreference(
                PreferencesActivity.KEY_SERVER_URL);
        mUsernamePreference = (EditTextPreference) findPreference(PreferencesActivity.KEY_USERNAME);
        mPasswordPreference = (EditTextPreference) findPreference(PreferencesActivity.KEY_PASSWORD);

        PreferenceCategory aggregatePreferences = (PreferenceCategory) findPreference(
                getString(R.string.aggregate_preferences));

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String urlListString = prefs.getString(KNOWN_URL_LIST, "");
        if (urlListString.isEmpty()){
            mUrlList = new ArrayList<>();
        } else {
            mUrlList =
                    new Gson().fromJson(urlListString, new TypeToken<List<String>>() {}.getType());
        }
        if (mUrlList.size() == 0) {
            addUrlToPreferencesList(getString(R.string.default_server_url), prefs);
        }

        urlDropdownSetup();

        mServerUrlPreference.getEditText().setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down, 0);
        mServerUrlPreference.getEditText().setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_RIGHT = 2;
                if(event.getAction() == MotionEvent.ACTION_UP){
                    if (event.getX() >= (v.getWidth() - ((EditText) v)
                            .getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                        mListPopupWindow.show();
                        return true;
                    }
                }
                return false;
            }
        });

        mServerUrlPreference
                .setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference,
                            Object newValue) {
                        String url = newValue.toString();

                        // remove all trailing "/"s
                        while (url.endsWith("/")) {
                            url = url.substring(0, url.length() - 1);
                        }

                        if (UrlUtils.isValidUrl(url)) {
                            preference.setSummary(newValue.toString());
                            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                            String urlListString = prefs.getString(KNOWN_URL_LIST, "");

                            mUrlList =
                                    new Gson().fromJson(urlListString,
                                            new TypeToken<List<String>>() {}.getType());

                            if (!mUrlList.contains(url)) {
                                // We store a list with at most 5 elements
                                if (mUrlList.size() == 5) {
                                    mUrlList.remove(4);
                                }
                                addUrlToPreferencesList(url, prefs);
                                setupUrlDropdownAdapter();
                            }
                            return true;
                        } else {
                            Toast.makeText(getApplicationContext(),
                                    R.string.url_error, Toast.LENGTH_SHORT)
                                    .show();
                            return false;
                        }
                    }
                });
        mServerUrlPreference.setSummary(mServerUrlPreference.getText());
        mServerUrlPreference.getEditText().setFilters(
                new InputFilter[]{new ControlCharacterFilter(), new WhitespaceFilter()});

        mUsernamePreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                String username = newValue.toString();

                // do not allow leading and trailing whitespace
                if (!username.equals(username.trim())) {
                    Toast.makeText(getApplicationContext(),
                            R.string.username_error_whitespace, Toast.LENGTH_SHORT)
                            .show();
                    return false;
                }

                preference.setSummary(username);
                clearCachedCrendentials();

                // To ensure we update current credentials in CredentialsProvider
                mCredentialsHaveChanged = true;

                return true;
            }
        });
        mUsernamePreference.setSummary(mUsernamePreference.getText());
        mUsernamePreference.getEditText().setFilters(
                new InputFilter[]{new ControlCharacterFilter()});

        mPasswordPreference
                .setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference,
                            Object newValue) {
                        String pw = newValue.toString();

                        // do not allow leading and trailing whitespace
                        if (!pw.equals(pw.trim())) {
                            Toast.makeText(getApplicationContext(),
                                    R.string.password_error_whitespace, Toast.LENGTH_SHORT)
                                    .show();
                            return false;
                        }

                        maskPasswordSummary(pw);
                        clearCachedCrendentials();

                        // To ensure we update current credentials in CredentialsProvider
                        mCredentialsHaveChanged = true;

                        return true;
                    }
                });

        maskPasswordSummary(mPasswordPreference.getText());
        mPasswordPreference.getEditText().setFilters(
                new InputFilter[]{new ControlCharacterFilter()});
    }

    private void urlDropdownSetup() {
        mListPopupWindow = new ListPopupWindow(this);
        setupUrlDropdownAdapter();
        mListPopupWindow.setAnchorView(mServerUrlPreference.getEditText());
        mListPopupWindow.setModal(true);
        mListPopupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
                mServerUrlPreference.getEditText().setText(mUrlList.get(position));
                mListPopupWindow.dismiss();
            }
        });
    }

    private void setupUrlDropdownAdapter() {
        ArrayAdapter adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mUrlList);
        mListPopupWindow.setAdapter(adapter);
    }

    private void addUrlToPreferencesList(String url, SharedPreferences prefs) {
        mUrlList.add(0, url);
        String urlListString = new Gson().toJson(mUrlList);
        prefs
                .edit()
                .putString(KNOWN_URL_LIST, urlListString)
                .apply();
    }

    private void maskPasswordSummary(String password) {
        mPasswordPreference.setSummary(password != null && password.length() > 0
                ? "********"
                : "");
    }

    private void clearCachedCrendentials() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(
                getBaseContext());
        String server = settings.getString(PreferencesActivity.KEY_SERVER_URL,
                getString(R.string.default_server_url));
        Uri u = Uri.parse(server);
        WebUtils.clearHostCredentials(u.getHost());
        Collect.getInstance().getCookieStore().clear();
    }

}

/**
 * Rejects edits that contain whitespace.
 */
class WhitespaceFilter implements InputFilter {
    public CharSequence filter(CharSequence source, int start, int end,
            Spanned dest, int dstart, int dend) {
        for (int i = start; i < end; i++) {
            if (Character.isWhitespace(source.charAt(i))) {
                return "";
            }
        }
        return null;
    }
}

/**
 * Rejects edits that contain control characters, including linefeed and carriage return.
 */
class ControlCharacterFilter implements InputFilter {
    public CharSequence filter(CharSequence source, int start, int end,
            Spanned dest, int dstart, int dend) {
        for (int i = start; i < end; i++) {
            if (Character.getType((source.charAt(i))) == Character.CONTROL) {
                return "";
            }
        }
        return null;
    }
}