package org.odk.collect.android.http;

public class HttpCredentials implements HttpCredentialsInterface {

    private final String username;
    private final String password;

    public HttpCredentials(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }
}
