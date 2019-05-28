package org.odk.collect.android.http;

import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class HttpClientConnectionPostRequestTest extends OpenRosaPostRequestTest {

    @Override
    protected OpenRosaHttpInterface buildSubject() {
        return new HttpClientConnection();
    }

    @Override
    protected Boolean useRealHttps() {
        return false;
    }
}
