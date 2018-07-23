package org.odk.collect.android.utilities;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.odk.collect.android.http.HttpCredentials;
import org.odk.collect.android.http.HttpCredentialsInterface;
import org.odk.collect.android.preferences.GeneralSharedPreferences;
import org.odk.collect.android.preferences.PreferenceKeys;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class WebCredentialsUtils {

    private static WebCredentialsUtils instance;

    private final Map<String, HttpCredentialsInterface> hostCredentials = new HashMap<>();

    private WebCredentialsUtils() {}

    /**
     * A factory method to return a new WebCredentialsUtils object.
     *
     * @return an instance of WebCredentialsUtils
     */
    public static synchronized WebCredentialsUtils getInstance() {
        if (instance == null) {
            instance = new WebCredentialsUtils();
        }
        return instance;
    }

    public void saveCredentials(@NonNull String url, @NonNull String username, @NonNull String password) {
        if (username.isEmpty()) {
            return;
        }

        String host = Uri.parse(url).getHost();
        hostCredentials.put(host, new HttpCredentials(username, password));
    }

    public void saveCredentialsPreferences(String userName, String password) {
        GeneralSharedPreferences.getInstance().save(PreferenceKeys.KEY_USERNAME, userName);
        GeneralSharedPreferences.getInstance().save(PreferenceKeys.KEY_PASSWORD, password);
    }

    public String getServerFromPreferences() {
        if (GeneralSharedPreferences.getInstance() == null) {
            return "";
        }
        return (String) GeneralSharedPreferences.getInstance().get(PreferenceKeys.KEY_SERVER_URL);
    }

    public String getPasswordFromPreferences() {
        if (GeneralSharedPreferences.getInstance() == null) {
            return "";
        }
        return (String) GeneralSharedPreferences.getInstance().get(PreferenceKeys.KEY_PASSWORD);
    }

    public String getUserNameFromPreferences() {
        if (GeneralSharedPreferences.getInstance() == null) {
            return "";
        }
        return (String) GeneralSharedPreferences.getInstance().get(PreferenceKeys.KEY_USERNAME);
    }

    /**
     * Returns a credentials object from the url
     *
     * @param url to find the credentials object
     * @return either null or an instance of HttpCredentialsInterface
     */
    public @Nullable HttpCredentialsInterface getCredentials(@NonNull URI url) {
        String serverUrl = getServerFromPreferences();
        if (serverUrl != null && serverUrl.equalsIgnoreCase(url.toString())) {
            return new HttpCredentials(getUserNameFromPreferences(), getPasswordFromPreferences());
        } else {
            String host = url.getHost();
            return hostCredentials.get(host);
        }
    }

}
