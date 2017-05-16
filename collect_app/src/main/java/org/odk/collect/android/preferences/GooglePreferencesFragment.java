package org.odk.collect.android.preferences;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.text.InputFilter;

import org.odk.collect.android.R;
import org.odk.collect.android.preferences.filters.ControlCharacterFilter;
import org.odk.collect.android.preferences.filters.WhitespaceFilter;
import org.odk.collect.android.utilities.ToastUtils;
import org.odk.collect.android.utilities.UrlUtils;

import java.util.ArrayList;


public class GooglePreferencesFragment extends PreferenceFragment {
    protected EditTextPreference googleSheetsUrlPreference;
    private ListPreference selectedGoogleAccountPreference;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.google_preferences);

        boolean adminMode = getActivity().getIntent().getBooleanExtra(PreferencesActivity.INTENT_KEY_ADMIN_MODE,
                false);

        SharedPreferences adminPreferences = getActivity().getSharedPreferences(
                AdminPreferencesActivity.ADMIN_PREFERENCES, 0);

        selectedGoogleAccountPreference = (ListPreference) findPreference(
                PreferenceKeys.KEY_SELECTED_GOOGLE_ACCOUNT);
        PreferenceCategory googlePreferences = (PreferenceCategory) findPreference(
                getString(R.string.google_preferences));

        selectedGoogleAccountPreference
                .setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        int index = ((ListPreference) preference).findIndexOfValue(newValue
                                .toString());
                        String value =
                                (String) ((ListPreference) preference).getEntryValues()[index];
                        preference.setSummary(value);
                        return true;
                    }
                });
        selectedGoogleAccountPreference.setSummary(selectedGoogleAccountPreference.getValue());

        googleSheetsUrlPreference = (EditTextPreference) findPreference(
                PreferenceKeys.KEY_GOOGLE_SHEETS_URL);
        googleSheetsUrlPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                String url = newValue.toString();

                // remove all trailing "/"s
                while (url.endsWith("/")) {
                    url = url.substring(0, url.length() - 1);
                }

                if (UrlUtils.isValidUrl(url)) {
                    preference.setSummary(url + "\n\n" + getString(R.string.google_sheets_url_hint));
                    return true;
                } else if (url.length() == 0) {
                    preference.setSummary(getString(R.string.google_sheets_url_hint));
                    return true;
                } else {
                    ToastUtils.showShortToast(R.string.url_error);
                    return false;
                }
            }
        });

        String currentGoogleSheetsURL = googleSheetsUrlPreference.getText();
        if (currentGoogleSheetsURL.length() > 0) {
            googleSheetsUrlPreference.setSummary(currentGoogleSheetsURL + "\n\n"
                    + getString(R.string.google_sheets_url_hint));
        }

        googleSheetsUrlPreference.getEditText().setFilters(new InputFilter[]{
                new ControlCharacterFilter(), new WhitespaceFilter()
        });
    }


    @Override
    public void onResume() {
        super.onResume();
        // get list of google accounts
        final Account[] accounts = AccountManager.get(getActivity().getApplicationContext())
                .getAccountsByType("com.google");
        ArrayList<String> accountEntries = new ArrayList<String>();
        ArrayList<String> accountValues = new ArrayList<String>();

        for (Account account : accounts) {
            accountEntries.add(account.name);
            accountValues.add(account.name);
        }
        accountEntries.add(getString(R.string.no_account));
        accountValues.add("");

        selectedGoogleAccountPreference.setEntries(accountEntries
                .toArray(new String[accountEntries.size()]));
        selectedGoogleAccountPreference.setEntryValues(accountValues
                .toArray(new String[accountValues.size()]));
    }
}
