package org.odk.collect.android.openrosa;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

import org.junit.Test;
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

    @Test
    public void differentCredentialsHaveDifferentInstances() {
        OpenRosaServerClientProvider provider = buildSubject();

        OpenRosaServerClient instance1 = provider.get("http", "Android", new HttpCredentials("user", "pass"));
        OpenRosaServerClient instance2 = provider.get("http", "Android", new HttpCredentials("other", "pass"));
        OpenRosaServerClient instance3 = provider.get("http", "Android", new HttpCredentials("user", "pass"));


        assertThat(instance1, not(equalTo(instance2)));
        assertThat(instance1, equalTo(instance3));
    }
}
