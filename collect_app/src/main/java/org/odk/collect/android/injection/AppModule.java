package org.odk.collect.android.injection;

import android.app.Application;
import android.support.annotation.NonNull;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.database.ActivityLogger;
import org.odk.collect.android.injection.scopes.PerApplication;
import org.odk.collect.android.utilities.AgingCredentialsProvider;
import org.opendatakit.httpclientandroidlib.client.CookieStore;
import org.opendatakit.httpclientandroidlib.client.CredentialsProvider;
import org.opendatakit.httpclientandroidlib.impl.client.BasicCookieStore;

import dagger.Module;
import dagger.Provides;

/**
 * Add Application level providers here, i.e. if you want to
 * inject something into the Collect instance.
 */
@Module
class AppModule {

    @PerApplication
    @Provides
    Collect provideApplication(Application application) {
        return (Collect) application;
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
}