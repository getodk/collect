package org.odk.collect.android.http.openrosa;

import androidx.annotation.Nullable;

import org.odk.collect.android.http.HttpCredentialsInterface;

public interface OpenRosaServerClientFactory {

    OpenRosaServerClient create(String schema, String userAgent, @Nullable HttpCredentialsInterface credentialsInterface);
}
