package org.odk.collect.android.http;

import org.junit.runner.RunWith;
import org.odk.collect.android.http.okhttp.OkHttpConnection;
import org.odk.collect.android.http.okhttp.OkHttpOpenRosaServerClientProvider;
import org.odk.collect.android.http.openrosa.OpenRosaHttpInterface;
import org.robolectric.RobolectricTestRunner;

import okhttp3.OkHttpClient;

@RunWith(RobolectricTestRunner.class)
public class OkHttpConnectionPostRequest extends OpenRosaPostRequestTest {

    @Override
    protected OpenRosaHttpInterface buildSubject(OpenRosaHttpInterface.FileToContentTypeMapper mapper) {
        return new OkHttpConnection(
                new OkHttpOpenRosaServerClientProvider(new OkHttpClient()),
                mapper
        );
    }
}
