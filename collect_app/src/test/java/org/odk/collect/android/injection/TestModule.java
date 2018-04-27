package org.odk.collect.android.injection;

import android.app.Application;
import android.telephony.SmsManager;

import com.birbit.android.jobqueue.JobManager;
import com.birbit.android.jobqueue.config.Configuration;

import org.odk.collect.android.injection.config.scopes.PerApplication;
import org.odk.collect.android.jobs.test.timer.MockTimer;
import org.odk.collect.android.tasks.sms.SmsService;
import org.odk.collect.android.tasks.sms.SmsSubmissionManagerImpl;
import org.odk.collect.android.tasks.sms.contracts.SmsSubmissionManagerContract;
import org.odk.collect.android.utilities.AgingCredentialsProvider;
import org.opendatakit.httpclientandroidlib.client.CookieStore;
import org.opendatakit.httpclientandroidlib.client.CredentialsProvider;
import org.opendatakit.httpclientandroidlib.impl.client.BasicCookieStore;

import dagger.Module;
import dagger.Provides;

/**
 * Test Module used for unit testing.
 * Providing seemingly production dependencies because they are all
 * running on the Shadows of Robolectric.
 */
@Module
public class TestModule {

    @Provides
    SmsManager provideSmsManager() {
        return SmsManager.getDefault();
    }

    @Provides
    SmsSubmissionManagerContract provideSmsSubmissionManager(Application application) {
        return new SmsSubmissionManagerImpl(application);
    }

    @Provides
    public JobManager provideJobManager(Application application) {
        return new JobManager(new Configuration.Builder(application)
                .timer(new MockTimer())
                .inTestMode().build());
    }

    @Provides
    public SmsService provideSmsService(Application application) {
        return new SmsService(application);
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

}
