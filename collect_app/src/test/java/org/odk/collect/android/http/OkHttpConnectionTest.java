package org.odk.collect.android.http;

import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.tls.internal.TlsUtil;

@RunWith(RobolectricTestRunner.class)
public class OkHttpConnectionTest extends OpenRosaHttpInterfaceTest {

    @Override
    protected void startHttpsMockWebServer(MockWebServer mockWebServer) throws IOException {
        mockWebServer.useHttps(TlsUtil.localhost().sslSocketFactory(), false);
        mockWebServer.start();
    }

    @Override
    protected OpenRosaHttpInterface buildSubject() {
        return new OkHttpConnection(new OkHttpClient.Builder()
                .sslSocketFactory(TlsUtil.localhost().sslSocketFactory(), TlsUtil.localhost().trustManager())
        );
    }
}