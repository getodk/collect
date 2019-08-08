package org.odk.collect.android.http;

import org.odk.collect.android.http.okhttp.OkHttpOpenRosaServerClientFactory;
import org.odk.collect.android.http.openrosa.OpenRosaServerClientFactory;
import org.odk.collect.android.utilities.Clock;

import okhttp3.OkHttpClient;
import okhttp3.tls.internal.TlsUtil;

public class OkHttpOpenRosaServerClientFactoryTest extends OpenRosaServerClientFactoryTest {

    @Override
    protected OpenRosaServerClientFactory buildSubject(Clock clock) {
        OkHttpClient.Builder baseClient = new OkHttpClient.Builder()
                .sslSocketFactory(
                        TlsUtil.localhost().sslSocketFactory(),
                        TlsUtil.localhost().trustManager());
        
        return new OkHttpOpenRosaServerClientFactory(baseClient, clock);
    }

    @Override
    protected Boolean useRealHttps() {
        return true;
    }
}
