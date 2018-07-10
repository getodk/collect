package org.odk.collect.android.http.injection;

import org.odk.collect.android.http.CollectServerClient;
import org.odk.collect.android.http.HttpClientConnection;
import org.odk.collect.android.http.HttpInterface;

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

    @Provides
    @Singleton
    public CollectServerClient provideCollectServerClient(HttpInterface httpInterface) {
        return new CollectServerClient(httpInterface);
    }

}
