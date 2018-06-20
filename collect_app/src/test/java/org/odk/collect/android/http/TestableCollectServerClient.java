package org.odk.collect.android.http;

import org.odk.collect.android.http.mock.DaggerMockHttpComponent;

public final class TestableCollectServerClient extends CollectServerClient {

    public TestableCollectServerClient() {
        DaggerMockHttpComponent.builder().build().inject(this);
    }

    public static void setInstance(CollectServerClient clientInstance) {
        instance = clientInstance;
    }

}
