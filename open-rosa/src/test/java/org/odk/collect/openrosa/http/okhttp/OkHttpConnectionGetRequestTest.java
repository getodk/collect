package org.odk.collect.openrosa.http.okhttp;

import android.webkit.MimeTypeMap;

import org.odk.collect.openrosa.http.CollectThenSystemContentTypeMapper;
import org.odk.collect.openrosa.http.OpenRosaGetRequestTest;
import org.odk.collect.openrosa.http.OpenRosaHttpInterface;

import okhttp3.OkHttpClient;

public class OkHttpConnectionGetRequestTest extends OpenRosaGetRequestTest {

    @Override
    protected OpenRosaHttpInterface buildSubject() {
        return new OkHttpConnection(
                new OkHttpOpenRosaServerClientProvider(new OkHttpClient()),
                new CollectThenSystemContentTypeMapper(MimeTypeMap.getSingleton()),
                USER_AGENT
        );
    }
}
