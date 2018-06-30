package org.odk.collect.android.http.mock;

import org.odk.collect.android.http.TestableCollectServerClient;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class MockHttpInterfaceModule {

    @Provides
    @Singleton
    public MockHttpClientConnection provideHttpInterface() {
        return new MockHttpClientConnection();
    }

    @Provides
    @Singleton
    public TestableCollectServerClient provideCollectServerClient(MockHttpClientConnection httpInterface) {
        return new TestableCollectServerClient(httpInterface);
    }

}



