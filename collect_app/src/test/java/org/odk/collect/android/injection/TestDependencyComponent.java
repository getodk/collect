package org.odk.collect.android.injection;

import android.app.Application;

import org.odk.collect.android.http.CollectServerClientTest;
import org.odk.collect.android.injection.config.AppDependencyComponent;
import org.odk.collect.android.sms.SmsSenderJobTest;
import org.odk.collect.android.sms.SmsServiceTest;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;

@Component(modules = {
        TestDependencyModule.class
})
@Singleton
public interface TestDependencyComponent extends AppDependencyComponent {

    @Component.Builder
    interface Builder {

        @BindsInstance
        TestDependencyComponent.Builder application(Application application);

        TestDependencyComponent build();
    }

    void inject(SmsSenderJobTest smsSenderJobTest);

    void inject(SmsServiceTest smsServiceTest);

    void inject(CollectServerClientTest collectServerClientTest);
}