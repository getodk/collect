package org.odk.collect.android.http;

import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class OkHttpConnectionTest extends OpenRosaHttpInterfaceTest {

    @Override
    protected OpenRosaHttpInterface subject() {
        return new OkHttpConnection();
    }
}