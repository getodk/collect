package org.odk.collect.android.utilities;

import android.support.annotation.NonNull;
import java.io.InputStream;
import java.net.URI;
import java.util.Map;

public interface CollectHttpConnection {

    InputStream getInputStream(@NonNull URI uri, final String contentType, final int connectionTimeout, Map<String, String> responseHeaders) throws Exception;

    Map<String, String> getHeaders();

    void clearCookieStore();

    void clearHostCredentials(String host);

    void addCredentials(String username, String password, String host);
}
