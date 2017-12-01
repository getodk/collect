package org.odk.collect.android.injection;

import android.app.Application;

import org.odk.collect.android.application.Collect;

import dagger.BindsInstance;
import dagger.Component;
import dagger.android.support.AndroidSupportInjectionModule;

/**
 * Primary module, bootstraps the injection system and
 * injects the main Collect instance here.
 */
@Component(modules = {
        AndroidSupportInjectionModule.class,
        AppModule.class,
        ActivityBuilder.class
})
public interface AppComponent {

    @Component.Builder
    interface Builder {

        @BindsInstance
        Builder application(Application application);

        AppComponent build();
    }

    void inject(Collect collect);
}
