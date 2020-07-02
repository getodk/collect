package odk.hedera.collect.openrosa;

import android.webkit.MimeTypeMap;

import odk.hedera.collect.openrosa.okhttp.OkHttpConnection;
import odk.hedera.collect.openrosa.okhttp.OkHttpOpenRosaServerClientProvider;

import okhttp3.OkHttpClient;

public class OkHttpConnectionHeadRequestTest extends OpenRosaHeadRequestTest {

    @Override
    protected OpenRosaHttpInterface buildSubject() {
        return new OkHttpConnection(
                new OkHttpOpenRosaServerClientProvider(new OkHttpClient()),
                new CollectThenSystemContentTypeMapper(MimeTypeMap.getSingleton()),
                USER_AGENT
        );
    }
}
