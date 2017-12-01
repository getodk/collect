package org.odk.collect.android.injection;

import org.odk.collect.android.injection.architecture.ViewModelModule;

import dagger.Module;

/**
 * Add Application level providers here, i.e. if you want to
 * inject something into the Collect instance.
 */
@Module(includes = ViewModelModule.class)
class AppModule {

}
