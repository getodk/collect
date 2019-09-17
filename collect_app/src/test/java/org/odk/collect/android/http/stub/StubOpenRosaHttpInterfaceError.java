package org.odk.collect.android.http.stub;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.odk.collect.android.http.openrosa.HttpCredentialsInterface;
import org.odk.collect.android.http.openrosa.HttpGetResult;

import java.net.URI;

public class StubOpenRosaHttpInterfaceError extends StubOpenRosaHttpInterface {

    @Override
    @NonNull
    public HttpGetResult executeGetRequest(@NonNull URI uri, @Nullable String contentType, @Nullable HttpCredentialsInterface credentials) throws Exception {
        return null;
    }
}
