package org.odk.collect.android.http;

import org.odk.collect.android.http.mock.MockHttpClientConnection;

import javax.inject.Inject;

public final class TestableCollectServerClient extends CollectServerClient {

    @Inject
    public TestableCollectServerClient(MockHttpClientConnection httpInterface) {
        super(httpInterface);
    }

    public void setGetHttpShouldReturnNull(boolean getHttpShouldReturnNull) {
        MockHttpClientConnection mockedClient = (MockHttpClientConnection) httpInterface;
        mockedClient.setGetHttpShouldReturnNull(getHttpShouldReturnNull);
    }

}
