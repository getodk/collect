package org.odk.collect.android.http;

import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class OkHttpConnectionPostRequest extends OpenRosaPostRequestTest {

    @Override
    protected OpenRosaHttpInterface buildSubject() {
        return new OkHttpConnection(null);
    }
}
