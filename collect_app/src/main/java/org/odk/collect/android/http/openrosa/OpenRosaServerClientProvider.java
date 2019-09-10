package org.odk.collect.android.http.openrosa;

import androidx.annotation.Nullable;

import org.odk.collect.android.http.HttpCredentialsInterface;

public interface OpenRosaServerClientProvider {

    OpenRosaServerClient get(String schema, String userAgent, @Nullable HttpCredentialsInterface credentialsInterface);
}
