package org.odk.collect.android.http;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import okhttp3.OkHttpClient;
import okhttp3.tls.internal.TlsUtil;

@RunWith(RobolectricTestRunner.class)
public class OkHttpConnectionPostRequest extends OpenRosaPostRequestTest {

    @Override
    protected OpenRosaHttpInterface buildSubject(OpenRosaHttpInterface.FileToContentTypeMapper mapper) {
        return new OkHttpConnection(new OkHttpClient.Builder()
                .sslSocketFactory(TlsUtil.localhost().sslSocketFactory(), TlsUtil.localhost().trustManager()),
                mapper
        );
    }

    @Before
    public void resetStaticState() {
        OkHttpConnection.reset();
    }

    @Override
    protected Boolean useRealHttps() {
        return true;
    }
}
