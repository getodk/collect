package org.odk.collect.android.http;

import android.webkit.MimeTypeMap;

import org.junit.runner.RunWith;
import org.odk.collect.android.http.okhttp.OkHttpConnection;
import org.odk.collect.android.http.okhttp.OkHttpOpenRosaServerClientFactory;
import org.odk.collect.android.http.openrosa.OpenRosaHttpInterface;
import org.robolectric.RobolectricTestRunner;

import java.util.Date;

import okhttp3.OkHttpClient;

@RunWith(RobolectricTestRunner.class)
public class OkHttpConnectionGetRequestTest extends OpenRosaGetRequestTest {

    @Override
    protected OpenRosaHttpInterface buildSubject() {
        return new OkHttpConnection(
                new OkHttpOpenRosaServerClientFactory(new OkHttpClient.Builder(), Date::new),
                new CollectThenSystemContentTypeMapper(MimeTypeMap.getSingleton())
        );
    }
}