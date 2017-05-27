package org.odk.collect.android.preferences;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.text.InputFilter;

import org.odk.collect.android.R;
import org.odk.collect.android.preferences.filters.ControlCharacterFilter;
import org.odk.collect.android.preferences.filters.WhitespaceFilter;
import org.odk.collect.android.utilities.ToastUtils;
import org.odk.collect.android.utilities.UrlUtils;

import java.util.ArrayList;


public class GooglePreferencesFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {
    private ListPreference selectedGoogleAccountPreference;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.google_preferences);

        selectedGoogleAccountPreference = (ListPreference) findPreference(
                PreferenceKeys.KEY_SELECTED_GOOGLE_ACCOUNT);
        selectedGoogleAccountPreference.setOnPreferenceChangeListener(this);
        selectedGoogleAccountPreference.setSummary(selectedGoogleAccountPreference.getValue());

        EditTextPreference googleSheetsUrlPreference = (EditTextPreference) findPreference(
                PreferenceKeys.KEY_GOOGLE_SHEETS_URL);
        googleSheetsUrlPreference.setOnPreferenceChangeListener(this);

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

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        switch (preference.getKey()) {
            case PreferenceKeys.KEY_SELECTED_GOOGLE_ACCOUNT:
                int index = ((ListPreference) preference).findIndexOfValue(newValue
                        .toString());
                String value =
                        (String) ((ListPreference) preference).getEntryValues()[index];
                preference.setSummary(value);
                break;
            case PreferenceKeys.KEY_GOOGLE_SHEETS_URL:
                String url = newValue.toString();

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
}
