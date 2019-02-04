package org.odk.collect.android.http;

public interface HttpCredentialsInterface {
    String getUsername();

    String getPassword();

    boolean equals(HttpCredentialsInterface credentialsInterface);
}
