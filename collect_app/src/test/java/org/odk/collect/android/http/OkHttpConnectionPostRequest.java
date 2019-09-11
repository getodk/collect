package org.odk.collect.android.http;

import org.odk.collect.android.http.openrosa.okhttp.OkHttpConnection;
import org.odk.collect.android.http.openrosa.okhttp.OkHttpOpenRosaServerClientProvider;
import org.odk.collect.android.http.openrosa.OpenRosaHttpInterface;

import okhttp3.OkHttpClient;

public class OkHttpConnectionPostRequest extends OpenRosaPostRequestTest {

    @Override
    protected OpenRosaHttpInterface buildSubject(OpenRosaHttpInterface.FileToContentTypeMapper mapper) {
        return new OkHttpConnection(
                new OkHttpOpenRosaServerClientProvider(new OkHttpClient()),
                mapper,
                "Test Agent"
        );
    }
}
