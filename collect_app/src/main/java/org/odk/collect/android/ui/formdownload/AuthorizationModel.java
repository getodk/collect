package org.odk.collect.android.ui.formdownload;

public class AuthorizationModel {

    private final String url;
    private final String username;
    private final String password;

    public AuthorizationModel(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    public String getUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
