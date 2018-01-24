package org.odk.collect.android.injection.config;

import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.database.ActivityLogger;
import org.odk.collect.android.injection.ViewModelBuilder;
import org.odk.collect.android.injection.config.architecture.ViewModelFactoryModule;
import org.odk.collect.android.injection.config.scopes.PerApplication;
import org.odk.collect.android.location.client.LocationClient;
import org.odk.collect.android.location.client.LocationClients;
import org.odk.collect.android.utilities.AgingCredentialsProvider;
import org.opendatakit.httpclientandroidlib.client.CookieStore;
import org.opendatakit.httpclientandroidlib.client.CredentialsProvider;
import org.opendatakit.httpclientandroidlib.impl.client.BasicCookieStore;

import java.text.DecimalFormat;

import dagger.Module;
import dagger.Provides;

/**
 * Add Application level providers here, i.e. if you want to
 * inject something into the Collect instance.
 */
@Module(includes = {ViewModelFactoryModule.class, ViewModelBuilder.class})
class AppModule {

    @PerApplication
    @Provides
    Collect provideApplication(Application application) {
        return (Collect) application;
    }


    @PerApplication
    @Provides
    Context provideContext(Application application) {
        return application;
    }

    @PerApplication
    @Provides
    CredentialsProvider provideCredentialsProvider() {
        // retain credentials for 7 minutes...
        return new AgingCredentialsProvider(7 * 60 * 1000);
    }

    @PerApplication
    @Provides
    CookieStore provideCookieStore() {
        // share all session cookies across all sessions.
        return new BasicCookieStore();
    }

    @PerApplication
    @Provides
    ActivityLogger provideActivityLogger(@NonNull Collect collect) {
        return collect.getActivityLogger();
    }

    @NonNull
    @Provides
    @PerApplication
    LocationClient provideLocationClient(@NonNull Context context) {
        return LocationClients.clientForContext(context);
    }

    @NonNull
    @Provides
    @PerApplication
    DecimalFormat provideDecimalFormat() {
        return new DecimalFormat("#.##");
    }
}
