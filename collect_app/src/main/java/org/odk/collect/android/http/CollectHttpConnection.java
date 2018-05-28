package org.odk.collect.android.http;

import android.support.annotation.NonNull;

import org.odk.collect.android.utilities.ResponseMessageParser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;

public interface CollectHttpConnection {

    @NonNull CollectInputStreamResult getHTTPInputStream(@NonNull URI uri, final String contentType, boolean calculateHash) throws Exception;

    int httpHeadRequest(@NonNull URI uri, Map<String, String> responseHeaders) throws Exception;

    ResponseMessageParser uploadFiles(@NonNull List<File> fileList, @NonNull File submissionFile, @NonNull URI uri) throws IOException;

    void clearCookieStore();

    void clearHostCredentials(String host);

    void addCredentials(String username, String password, String host);
}
