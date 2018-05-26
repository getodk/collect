package org.odk.collect.android.injection;


import android.app.Application;

import org.odk.collect.android.ResetAppStateTestCase;
import org.odk.collect.android.injection.config.AppComponent;
import org.odk.collect.android.injection.config.scopes.PerApplication;

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

    @Component.Builder
    interface Builder {

        @BindsInstance
        Builder application(Application application);

        AndroidTestComponent build();
    }
}
