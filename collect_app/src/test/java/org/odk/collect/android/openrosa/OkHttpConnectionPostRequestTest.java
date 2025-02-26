package org.odk.collect.android.openrosa;

import org.odk.collect.openrosa.http.OpenRosaHttpInterface;
import org.odk.collect.openrosa.http.okhttp.OkHttpConnection;
import org.odk.collect.openrosa.http.okhttp.OkHttpOpenRosaServerClientProvider;

import okhttp3.OkHttpClient;

public class OkHttpConnectionPostRequestTest extends OpenRosaPostRequestTest {

    @Override
    protected OpenRosaHttpInterface buildSubject(OpenRosaHttpInterface.FileToContentTypeMapper mapper) {
        return new OkHttpConnection(
                new OkHttpOpenRosaServerClientProvider(new OkHttpClient()),
                mapper,
                "Test Agent"
        );
    }
}
