package org.odk.collect.android.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
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
import org.odk.collect.android.utilities.AuthDialogUtility;
import org.odk.collect.android.utilities.ToastUtils;
import org.odk.collect.android.utilities.UrlUtils;
import org.odk.collect.android.utilities.WebUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by shobhit on 5/3/17.
 */

public class AggregatePreferencesFragment extends PreferenceFragment implements View.OnTouchListener, Preference.OnPreferenceChangeListener {
    private static final String KNOWN_URL_LIST = "knownUrlList";
    protected EditTextPreference mServerUrlPreference;
    protected EditTextPreference mUsernamePreference;
    protected EditTextPreference mPasswordPreference;
    protected boolean mCredentialsHaveChanged = false;

    private ListPopupWindow mListPopupWindow;
    private List<String> mUrlList;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.aggregate_preferences);

        mServerUrlPreference = (EditTextPreference) findPreference(
                PreferenceKeys.KEY_SERVER_URL);
        mUsernamePreference = (EditTextPreference) findPreference(PreferenceKeys.KEY_USERNAME);
        mPasswordPreference = (EditTextPreference) findPreference(PreferenceKeys.KEY_PASSWORD);

        PreferenceCategory aggregatePreferences = (PreferenceCategory) findPreference(
                getString(R.string.aggregate_preferences));

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String urlListString = prefs.getString(KNOWN_URL_LIST, "");
        if (urlListString.isEmpty()) {
            mUrlList = new ArrayList<>();
        } else {
            mUrlList =
                    new Gson().fromJson(urlListString, new TypeToken<List<String>>() {
                    }.getType());
        }
        if (mUrlList.size() == 0) {
            addUrlToPreferencesList(getString(R.string.default_server_url), prefs);
        }

        urlDropdownSetup();

        mServerUrlPreference.getEditText().setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down, 0);
        mServerUrlPreference.getEditText().setOnTouchListener(this);
        mServerUrlPreference.setOnPreferenceChangeListener(this);
        mServerUrlPreference.setSummary(mServerUrlPreference.getText());
        mServerUrlPreference.getEditText().setFilters(
                new InputFilter[]{new ControlCharacterFilter(), new WhitespaceFilter()});

        mUsernamePreference.setOnPreferenceChangeListener(this);
        mUsernamePreference.setSummary(mUsernamePreference.getText());
        mUsernamePreference.getEditText().setFilters(
                new InputFilter[]{new ControlCharacterFilter()});

        mPasswordPreference.setOnPreferenceChangeListener(this);
        maskPasswordSummary(mPasswordPreference.getText());
        mPasswordPreference.getEditText().setFilters(
                new InputFilter[]{new ControlCharacterFilter()});
    }

    private void addUrlToPreferencesList(String url, SharedPreferences prefs) {
        mUrlList.add(0, url);
        String urlListString = new Gson().toJson(mUrlList);
        prefs
                .edit()
                .putString(KNOWN_URL_LIST, urlListString)
                .apply();
    }

    private void urlDropdownSetup() {
        mListPopupWindow = new ListPopupWindow(getActivity());
        setupUrlDropdownAdapter();
        mListPopupWindow.setAnchorView(mServerUrlPreference.getEditText());
        mListPopupWindow.setModal(true);
        mListPopupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mServerUrlPreference.getEditText().setText(mUrlList.get(position));
                mListPopupWindow.dismiss();
            }
        });
    }

    private void setupUrlDropdownAdapter() {
        ArrayAdapter adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, mUrlList);
        mListPopupWindow.setAdapter(adapter);
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mCredentialsHaveChanged) {
            AuthDialogUtility.setWebCredentialsFromPreferences(getActivity().getBaseContext());
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        final int DRAWABLE_RIGHT = 2;
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (event.getX() >= (v.getWidth() - ((EditText) v)
                    .getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                InputMethodManager imm = (InputMethodManager) getActivity().
                        getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                mListPopupWindow.show();
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
                    SharedPreferences prefs = PreferenceManager.
                            getDefaultSharedPreferences(getActivity().getApplicationContext());
                    String urlListString = prefs.getString(KNOWN_URL_LIST, "");

                    mUrlList =
                            new Gson().fromJson(urlListString,
                                    new TypeToken<List<String>>() {
                                    }.getType());

                    if (!mUrlList.contains(url)) {
                        // We store a list with at most 5 elements
                        if (mUrlList.size() == 5) {
                            mUrlList.remove(4);
                        }
                        addUrlToPreferencesList(url, prefs);
                        setupUrlDropdownAdapter();
                    }
                } else {
                    ToastUtils.shortDuration(String.valueOf(R.string.url_error));
                    return false;
                }
                break;

            case PreferenceKeys.KEY_USERNAME:
                String username = newValue.toString();

                // do not allow leading and trailing whitespace
                if (!username.equals(username.trim())) {
                    ToastUtils.shortDuration(String.valueOf(R.string.username_error_whitespace));
                    return false;
                }

                preference.setSummary(username);
                clearCachedCrendentials();

                // To ensure we update current credentials in CredentialsProvider
                mCredentialsHaveChanged = true;

                return true;

            case PreferenceKeys.KEY_PASSWORD:
                String pw = newValue.toString();

                // do not allow leading and trailing whitespace
                if (!pw.equals(pw.trim())) {
                    ToastUtils.shortDuration(String.valueOf(R.string.password_error_whitespace));
                    return false;
                }

                maskPasswordSummary(pw);
                clearCachedCrendentials();

                // To ensure we update current credentials in CredentialsProvider
                mCredentialsHaveChanged = true;
                break;
        }
        return true;
    }

    private void maskPasswordSummary(String password) {
        mPasswordPreference.setSummary(password != null && password.length() > 0
                ? "********"
                : "");
    }

    private void clearCachedCrendentials() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(
                getActivity().getBaseContext());
        String server = settings.getString(PreferenceKeys.KEY_SERVER_URL,
                getString(R.string.default_server_url));
        Uri u = Uri.parse(server);
        WebUtils.clearHostCredentials(u.getHost());
        Collect.getInstance().getCookieStore().clear();
    }
}
