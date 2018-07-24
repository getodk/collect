package org.odk.collect.android.http.injection;

import org.odk.collect.android.http.CollectServerClient;
import org.odk.collect.android.http.HttpClientConnection;
import org.odk.collect.android.http.OpenRosaHttpInterface;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class HttpInterfaceModule {

    @Provides
    @Singleton
    public OpenRosaHttpInterface provideHttpInterface() {
        return new HttpClientConnection();
    }

    @Provides
    @Singleton
    public CollectServerClient provideCollectServerClient(OpenRosaHttpInterface httpInterface) {
        return new CollectServerClient(httpInterface);
    }

}
