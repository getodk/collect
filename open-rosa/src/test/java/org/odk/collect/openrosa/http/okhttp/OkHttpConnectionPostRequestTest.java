package org.odk.collect.openrosa.http.okhttp;

import org.odk.collect.openrosa.http.OpenRosaHttpInterface;
import org.odk.collect.openrosa.http.OpenRosaPostRequestTest;

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
