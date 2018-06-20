package org.odk.collect.android.http.mock;

import org.odk.collect.android.http.HttpClientConnection;
import org.odk.collect.android.http.HttpInterface;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class MockHttpInterfaceModule {

    @Provides
    @Singleton
    public HttpInterface provideHttpInterface() {
        return new HttpClientConnection();
    }

}



