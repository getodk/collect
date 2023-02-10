package org.odk.collect.android.openrosa;

import org.odk.collect.android.openrosa.okhttp.OkHttpOpenRosaServerClientProvider;

import okhttp3.OkHttpClient;
import okhttp3.tls.internal.TlsUtil;

public class OkHttpOpenRosaServerClientProviderTest extends OpenRosaServerClientProviderTest {

    @Override
    protected OpenRosaServerClientProvider buildSubject() {
        OkHttpClient baseClient = new OkHttpClient.Builder()
                .sslSocketFactory(
                        TlsUtil.localhost().sslSocketFactory(),
                        TlsUtil.localhost().trustManager())
                .build();
        
        return new OkHttpOpenRosaServerClientProvider(baseClient);
    }
}
