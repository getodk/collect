package org.odk.collect.android.openrosa;

import org.junit.Test;
import org.odk.collect.android.openrosa.okhttp.OkHttpOpenRosaServerClientProvider;

import okhttp3.OkHttpClient;
import okhttp3.tls.internal.TlsUtil;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

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
    public void credentialsHaveChangedTest() {
        OkHttpOpenRosaServerClientProvider clientProvider = (OkHttpOpenRosaServerClientProvider) buildSubject();
        HttpCredentials newCredentials = new HttpCredentials("Admin", "Admin");

        assertFalse(clientProvider.credentialsHaveChanged(null));
        assertTrue(clientProvider.credentialsHaveChanged(newCredentials));

        clientProvider.get("https", "Dalvik/2.1.0 (Linux; U; Android 9; Android SDK built for x86 Build/PSR1.180720.093) org.odk.collect.android/v1.23.3-127-g2e2b1ac76", newCredentials);

        assertTrue(clientProvider.credentialsHaveChanged(null));
        assertFalse(clientProvider.credentialsHaveChanged(newCredentials));
    }
}
