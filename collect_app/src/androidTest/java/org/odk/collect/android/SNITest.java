package org.odk.collect.android;

import android.support.test.filters.Suppress;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opendatakit.httpclientandroidlib.HttpResponse;
import org.opendatakit.httpclientandroidlib.HttpStatus;
import org.opendatakit.httpclientandroidlib.client.HttpClient;
import org.opendatakit.httpclientandroidlib.client.methods.HttpGet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;

import javax.net.ssl.HttpsURLConnection;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;
import static org.odk.collect.android.utilities.WebUtils.CONNECTION_TIMEOUT;
import static org.odk.collect.android.utilities.WebUtils.createHttpClient;
import static org.odk.collect.android.utilities.WebUtils.createOpenRosaHttpGet;

/**
 * An on-device test for TLS server name indication support.
 *
 * @see <a href="https://github.com/opendatakit/collect/issues/199">COLLECT-199</a>
 */
@Suppress
@RunWith(AndroidJUnit4.class)
public class SNITest {

    public static final URI SNI_URI = URI.create("https://sni.velox.ch/");
    public static final String SUCCESS_SENTINEL = "sent the following TLS server name indication extension";

    @Test
    public void apacheHttpClientSupportsSNI() throws IOException {
        HttpClient client = createHttpClient(CONNECTION_TIMEOUT);
        HttpGet req = createOpenRosaHttpGet(SNI_URI);
        HttpResponse rsp = client.execute(req);
        assertHttpSuccess(rsp.getStatusLine().getStatusCode());
        assertPageContent(rsp.getEntity().getContent());
    }

    @Test
    public void urlConnectionSupportsSNI() throws IOException {
        HttpsURLConnection conn = (HttpsURLConnection) SNI_URI.toURL().openConnection();
        assertHttpSuccess(conn.getResponseCode());
        assertPageContent(conn.getInputStream());
    }

    /*
     * Confirms the request returned HTTP success (200) or fails test.
     */
    private void assertHttpSuccess(int statusCode) {
        assertEquals(HttpStatus.SC_OK, statusCode);
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
