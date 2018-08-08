package org.odk.collect.android.injection;

import android.app.Application;
import android.content.Context;
import android.telephony.SmsManager;

import org.odk.collect.android.dao.FormsDao;
import org.odk.collect.android.dao.InstancesDao;
import org.odk.collect.android.events.RxEventBus;
import org.odk.collect.android.http.TestableCollectServerClient;
import org.odk.collect.android.http.mock.MockHttpClientConnection;
import org.odk.collect.android.injection.config.scopes.PerApplication;
import org.odk.collect.android.tasks.sms.SmsSubmissionManager;
import org.odk.collect.android.tasks.sms.contracts.SmsSubmissionManagerContract;

import dagger.Module;
import dagger.Provides;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
        return new SmsSubmissionManager(application);
    }

    @Provides
    FormsDao provideFormsDao() {
        FormsDao formsDao = mock(FormsDao.class);
        when(formsDao.isFormEncrypted(anyString(), anyString())).thenReturn(false);
        return formsDao;
    }

    @Provides
    InstancesDao provideInstancesDao() {
        return mock(InstancesDao.class);
    }

    @Provides
    Context context(Application application) {
        return application;
    }

    @PerApplication
    @Provides
    RxEventBus provideRxEventBus() {
        return new RxEventBus();
    }

    @Provides
    MockHttpClientConnection provideMockHttpClientConnection() {
        return new MockHttpClientConnection();
    }

    @Provides
    TestableCollectServerClient provideTestCollectServerClient(MockHttpClientConnection httpClientConnection) {
        return new TestableCollectServerClient(httpClientConnection);
    }

}
