package org.odk.collect.android.http;

import org.odk.collect.android.http.mock.DaggerMockHttpComponent;
import org.odk.collect.android.http.mock.MockHttpClientConnection;
import org.odk.collect.android.http.mock.MockHttpInterfaceModule;

public final class TestableCollectServerClient extends CollectServerClient {

    public TestableCollectServerClient() {
        DaggerMockHttpComponent.builder().build().inject(this);
    }

    public static void setInstance(CollectServerClient clientInstance) {
        instance = clientInstance;
    }

    public static void setGetHttpShouldReturnNull(boolean getHttpShouldReturnNull) {
        MockHttpClientConnection mockedClient = (MockHttpClientConnection)getInstance().httpConnection;
        mockedClient.setGetHttpShouldReturnNull(getHttpShouldReturnNull);
    }

}
