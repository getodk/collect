package org.odk.collect.android.openrosa;

import androidx.annotation.NonNull;

public interface OpenRosaServerClientProvider {

    OpenRosaServerClient get(String schema, String userAgent, @NonNull HttpCredentialsInterface credentialsInterface);
}
