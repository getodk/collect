package org.odk.collect.android.http;

import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class HttpClientConnectionHeadRequestTest extends OpenRosaHeadRequestTest {

    @Override
    protected OpenRosaHttpInterface buildSubject() {
        return new HttpClientConnection();
    }

    @Override
    protected Boolean useRealHttps() {
        return false;
    }
}
