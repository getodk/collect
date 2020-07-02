package odk.hedera.collect.openrosa;

import odk.hedera.collect.openrosa.okhttp.OkHttpConnection;
import odk.hedera.collect.openrosa.okhttp.OkHttpOpenRosaServerClientProvider;

import okhttp3.OkHttpClient;

public class OkHttpConnectionPostRequest extends OpenRosaPostRequestTest {

    @Override
    protected OpenRosaHttpInterface buildSubject(OpenRosaHttpInterface.FileToContentTypeMapper mapper) {
        return new OkHttpConnection(
                new OkHttpOpenRosaServerClientProvider(new OkHttpClient()),
                mapper,
                "Test Agent"
        );
    }
}
