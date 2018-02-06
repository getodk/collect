package org.odk.collect.android.injection.config;

import android.app.Application;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.injection.ActivityBuilder;
import org.odk.collect.android.injection.ViewModelBuilder;
import org.odk.collect.android.injection.config.architecture.ViewModelFactoryModule;
import org.odk.collect.android.injection.config.scopes.PerApplication;

import dagger.BindsInstance;
import dagger.Component;
import dagger.android.support.AndroidSupportInjectionModule;

/**
 * Primary module, bootstraps the injection system and
 * injects the main Collect instance here.
 * <p>
 * Shouldn't be modified unless absolutely necessary.
 */
@PerApplication
@Component(modules = {
        AndroidSupportInjectionModule.class,
        AppModule.class,
        ActivityBuilder.class,
        ViewModelBuilder.class,
        ViewModelFactoryModule.class,
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
