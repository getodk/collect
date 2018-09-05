package org.odk.collect.android;

import android.support.test.filters.Suppress;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.http.CollectServerClient;
import org.odk.collect.android.http.HttpClientConnection;
import org.odk.collect.android.http.HttpGetResult;
import org.odk.collect.android.utilities.WebCredentialsUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;

/**
 * An on-device test for TLS server name indication support.
 *
 * @see <a href="https://github.com/opendatakit/collect/issues/199">COLLECT-199</a>
 */
@Suppress
@RunWith(AndroidJUnit4.class)
public class SNITest {

    private static final String SNI_URI = "https://sni.velox.ch/";
    private static final String SUCCESS_SENTINEL = "sent the following TLS server name indication extension";

    @Test
    public void testThatHttpClientSupportsSNI() throws Exception {
        CollectServerClient serverClient = new CollectServerClient(new HttpClientConnection(), new WebCredentialsUtils());
        HttpGetResult inputStreamResult = serverClient.getHttpInputStream(SNI_URI, null);
        assertHttpSuccess(inputStreamResult.getStatusCode());
        assertPageContent(inputStreamResult.getInputStream());
    }

    @Test
    public void urlConnectionSupportsSNI() throws IOException {
        HttpsURLConnection conn = (HttpsURLConnection) new URL(SNI_URI).openConnection();
        assertHttpSuccess(conn.getResponseCode());
        assertPageContent(conn.getInputStream());
    }

    /*
     * Confirms the request returned HTTP success (200) or fails test.
     */
    private void assertHttpSuccess(int statusCode) {
        assertEquals(HttpsURLConnection.HTTP_OK, statusCode);
    }

    /*
     * Confirms that the response content contains confirmation that SNI is working.
     */
    private void assertPageContent(InputStream contentStream) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(contentStream));
        String line;
        while ((line = in.readLine()) != null) {
            if (line.contains(SUCCESS_SENTINEL)) {
                return;
            }
        }
        fail(String.format("did not find sentinel '%s' in response", SUCCESS_SENTINEL));
    }
}
