package org.odk.collect.android.injection.config;

import android.app.Application;
import android.content.Context;
import android.telephony.SmsManager;

import org.odk.collect.android.dao.FormsDao;
import org.odk.collect.android.dao.InstancesDao;
import org.odk.collect.android.events.RxEventBus;
import org.odk.collect.android.http.CollectServerClient;
import org.odk.collect.android.http.HttpClientConnection;
import org.odk.collect.android.http.OpenRosaHttpInterface;
import org.odk.collect.android.tasks.sms.SmsSubmissionManager;
import org.odk.collect.android.tasks.sms.contracts.SmsSubmissionManagerContract;
import org.odk.collect.android.utilities.WebCredentialsUtils;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Add Application level dependency providers here (annotated with @Provides),
 * i.e. if you want to inject something into the Collect instance.
 */
@Module
public class AppDependencyModule {

    @Provides
    SmsManager provideSmsManager() {
        return SmsManager.getDefault();
    }

    @Provides
    SmsSubmissionManagerContract provideSmsSubmissionManager(Application application) {
        return new SmsSubmissionManager(application);
    }

    @Provides
    Context context(Application application) {
        return application;
    }

    @Provides
    InstancesDao provideInstancesDao() {
        return new InstancesDao();
    }

    @Provides
    FormsDao provideFormsDao() {
        return new FormsDao();
    }

    @Provides
    @Singleton
    RxEventBus provideRxEventBus() {
        return new RxEventBus();
    }

    @Provides
    OpenRosaHttpInterface provideHttpInterface() {
        return new HttpClientConnection();
    }

    @Provides
    CollectServerClient provideCollectServerClient(OpenRosaHttpInterface httpInterface, WebCredentialsUtils webCredentialsUtils) {
        return new CollectServerClient(httpInterface, webCredentialsUtils);
    }

    @Provides
    WebCredentialsUtils provideWebCredentials() {
        return new WebCredentialsUtils();
    }

}
