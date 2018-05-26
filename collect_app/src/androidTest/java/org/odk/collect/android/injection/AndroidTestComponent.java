package org.odk.collect.android.injection;


import android.app.Application;

import org.odk.collect.android.GuidanceHintFormTest;
import org.odk.collect.android.ResetAppStateTestCase;
import org.odk.collect.android.SharedPreferencesTest;
import org.odk.collect.android.dao.FormsDaoTest;
import org.odk.collect.android.injection.config.AppComponent;
import org.odk.collect.android.injection.config.AppModule;
import org.odk.collect.android.injection.config.scopes.PerApplication;
import org.odk.collect.android.settings.QrCodeTest;
import org.odk.collect.android.tasks.DownloadFormListTaskTest;
import org.odk.collect.android.tasks.InstanceServerUploaderTest;
import org.odk.collect.android.utilities.ImageConverterTest;
import org.odk.collect.android.utilities.WebUtilsTest;

import dagger.BindsInstance;
import dagger.Component;
import dagger.android.support.AndroidSupportInjectionModule;

@PerApplication
@Component(modules = {
        AndroidSupportInjectionModule.class,
        AppModule.class,
        AndroidTestModule.class
})
public interface AndroidTestComponent extends AppComponent {

    void inject(ResetAppStateTestCase resetAppStateTestCase);

    void inject(FormsDaoTest formsDaoTest);

    void inject(SharedPreferencesTest sharedPreferencesTest);

    void inject(QrCodeTest qrCodeTest);

    void inject(GuidanceHintFormTest guidanceHintFormTest);

    void inject(ImageConverterTest imageConverterTest);

    void inject(InstanceServerUploaderTest instanceServerUploaderTest);

    void inject(WebUtilsTest webUtilsTest);

    void inject(DownloadFormListTaskTest downloadFormListTaskTest);

    @Component.Builder
    interface Builder {

        @BindsInstance
        Builder application(Application application);

        AndroidTestComponent build();
    }
}
