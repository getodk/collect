package org.odk.collect.android.injection;


import android.app.Application;

import org.odk.collect.android.GuidanceHintFormTest;
import org.odk.collect.android.ResetAppStateTestCase;
import org.odk.collect.android.SharedPreferencesTest;
import org.odk.collect.android.dao.FormsDaoTest;
import org.odk.collect.android.injection.config.AppComponent;
import org.odk.collect.android.injection.config.scopes.PerApplication;
import org.odk.collect.android.settings.QrCodeTest;

import dagger.BindsInstance;
import dagger.Component;
import dagger.android.support.AndroidSupportInjectionModule;

@PerApplication
@Component(modules = {
        AndroidSupportInjectionModule.class,
        AndroidTestModule.class,
        ActivityBuilder.class
})
public interface AndroidTestComponent extends AppComponent {

    void inject(ResetAppStateTestCase resetAppStateTestCase);

    void inject(FormsDaoTest formsDaoTest);

    void inject(SharedPreferencesTest sharedPreferencesTest);

    void inject(QrCodeTest qrCodeTest);

    void inject(GuidanceHintFormTest guidanceHintFormTest);

    @Component.Builder
    interface Builder {

        @BindsInstance
        Builder application(Application application);

        AndroidTestComponent build();
    }
}
