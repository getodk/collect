package org.odk.collect.android.http;

public class HttpCredentials implements HttpCredentialsInterface {

    private final String username;
    private final String password;

    public HttpCredentials(String username, String password) {
        this.username = (username == null) ? "" : username;
        this.password = (password == null) ? "" : password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean equals(HttpCredentialsInterface credentialsInterface) {
        return getUsername().equals(credentialsInterface.getUsername()) &&
                getPassword().equals(credentialsInterface.getPassword());
    }

}
