package org.odk.collect.android.http;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class HttpInterfaceModule {

    @Provides
    @Singleton
    public HttpInterface provideHttpInterface() {
        return new HttpClientConnection();
    }

}
