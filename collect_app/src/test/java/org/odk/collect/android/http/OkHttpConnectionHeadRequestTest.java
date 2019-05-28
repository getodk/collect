package org.odk.collect.android.http;

import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import okhttp3.OkHttpClient;
import okhttp3.tls.internal.TlsUtil;

@RunWith(RobolectricTestRunner.class)
public class OkHttpConnectionHeadRequestTest extends OpenRosaHeadRequestTest {

    @Override
    protected OpenRosaHttpInterface buildSubject() {
        return new OkHttpConnection(new OkHttpClient.Builder()
                .sslSocketFactory(TlsUtil.localhost().sslSocketFactory(), TlsUtil.localhost().trustManager()),
                new CollectThenSystemContentTypeMapper()
        );
    }

    @Override
    protected Boolean useRealHttps() {
        return true;
    }
}
