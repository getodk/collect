package org.odk.collect.android.utilities;

import android.net.Uri;
import android.support.annotation.NonNull;

import org.odk.collect.android.http.CollectServerClient;
import org.odk.collect.android.http.injection.DaggerHttpComponent;
import org.odk.collect.android.preferences.GeneralSharedPreferences;
import org.odk.collect.android.preferences.PreferenceKeys;

import javax.inject.Inject;

public class WebCredentialsUtils {

    @Inject
    CollectServerClient collectServerClient;

    public WebCredentialsUtils() {
        DaggerHttpComponent.builder().build().inject(this);
    }

    public void setWebCredentialsFromPreferences() {
        String username = getUserNameFromPreferences();
        String password = getPasswordFromPreferences();

        if (username == null || username.isEmpty()) {
            return;
        }

        String host = Uri.parse(getServerFromPreferences()).getHost();
        collectServerClient.addCredentials(username, password, host);
    }

    public void setWebCredentials(@NonNull String url, @NonNull String username, @NonNull String password) {
        if (username.isEmpty()) {
            return;
        }

        String host = Uri.parse(url).getHost();
        collectServerClient.addCredentials(username, password, host);
    }

    public String getServerFromPreferences() {
        return (String) GeneralSharedPreferences.getInstance().get(PreferenceKeys.KEY_SERVER_URL);
    }

    public String getPasswordFromPreferences() {
        return (String) GeneralSharedPreferences.getInstance().get(PreferenceKeys.KEY_PASSWORD);
    }

    public String getUserNameFromPreferences() {
        return (String) GeneralSharedPreferences.getInstance().get(PreferenceKeys.KEY_USERNAME);
    }

    public void saveCredentials(String userName, String password) {
        GeneralSharedPreferences.getInstance().save(PreferenceKeys.KEY_USERNAME, userName);
        GeneralSharedPreferences.getInstance().save(PreferenceKeys.KEY_PASSWORD, password);
    }

}
