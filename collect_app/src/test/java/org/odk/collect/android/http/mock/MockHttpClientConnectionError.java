package org.odk.collect.android.http.mock;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.odk.collect.android.http.HttpCredentialsInterface;
import org.odk.collect.android.http.HttpGetResult;

import java.net.URI;

public class MockHttpClientConnectionError extends MockHttpClientConnection {

    @Override
    @NonNull
    public HttpGetResult executeGetRequest(@NonNull URI uri, @Nullable String contentType, @Nullable HttpCredentialsInterface credentials) throws Exception {
        return null;
    }
}
