package org.odk.collect.android.injection.config;

import android.app.Application;
import android.content.Context;
import android.telephony.SmsManager;

import org.odk.collect.android.dao.FormsDao;
import org.odk.collect.android.dao.InstancesDao;
import org.odk.collect.android.database.ActivityLogger;
import org.odk.collect.android.events.RxEventBus;
import org.odk.collect.android.injection.ViewModelBuilder;
import org.odk.collect.android.injection.config.architecture.ViewModelFactoryModule;
import org.odk.collect.android.injection.config.scopes.PerApplication;
import org.odk.collect.android.logic.PropertyManager;
import org.odk.collect.android.tasks.sms.SmsSubmissionManager;
import org.odk.collect.android.tasks.sms.contracts.SmsSubmissionManagerContract;
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
@Module(includes = {ViewModelFactoryModule.class, ViewModelBuilder.class})
public class AppModule {

    @Provides
    SmsManager provideSmsManager() {
        return SmsManager.getDefault();
    }

    @Provides
    SmsSubmissionManagerContract provideSmsSubmissionManager(Application application) {
        return new SmsSubmissionManager(application);
    }

    @Provides
    InstancesDao provideInstancesDao() {
        return new InstancesDao();
    }

    @Provides
    FormsDao provideFormsDao() {
        return new FormsDao();
    }

    @PerApplication
    @Provides
    RxEventBus provideRxEventBus() {
        return new RxEventBus();
    }

    @Provides
    Context bindContext(Application application) {
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
