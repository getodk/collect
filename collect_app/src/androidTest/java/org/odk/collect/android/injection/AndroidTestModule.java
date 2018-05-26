package org.odk.collect.android.injection;


import android.app.Application;
import android.content.Context;

import org.odk.collect.android.database.ActivityLogger;
import org.odk.collect.android.injection.config.scopes.PerApplication;
import org.odk.collect.android.logic.PropertyManager;
import org.odk.collect.android.utilities.AgingCredentialsProvider;
import org.opendatakit.httpclientandroidlib.client.CookieStore;
import org.opendatakit.httpclientandroidlib.client.CredentialsProvider;
import org.opendatakit.httpclientandroidlib.impl.client.BasicCookieStore;

import dagger.Module;
import dagger.Provides;

/**
 * Test Module used for instrumentation testing
 */
@Module
class AndroidTestModule {

    @Provides
    Context context(Application application) {
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
    PropertyManager providePropertyManager(Context context) {
        return new PropertyManager(context);
    }

    @PerApplication
    @Provides
    ActivityLogger provideActivityLogger(PropertyManager propertyManager) {
        return new ActivityLogger(propertyManager.getSingularProperty(PropertyManager.PROPMGR_DEVICE_ID));
    }
}