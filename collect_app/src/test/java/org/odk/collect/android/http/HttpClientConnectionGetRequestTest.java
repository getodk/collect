package org.odk.collect.android.http;

import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class HttpClientConnectionGetRequestTest extends OpenRosaGetRequestTest {

    @Override
    protected OpenRosaHttpInterface buildSubject() {
        return new HttpClientConnection(new CollectThenSystemContentTypeMapper());
    }

    @Override
    protected Boolean useRealHttps() {
        return false;
    }
}
