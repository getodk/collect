package org.odk.collect.androidshared

import android.app.Application
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 * This module follows the Android docs's multi-module example for Dagger. Any application that
 * depends on this module should implement the provider interface and return a constructed
 * component. This gives the application the opportunity to override dependencies if it wants to.
 *
 * @see [Using Dagger in multi-module apps](https://developer.android.com/training/dependency-injection/dagger-multi-module)
 */

interface AndroidSharedDependencyComponentProvider {
    val androidSharedDependencyComponent: AndroidSharedDependencyComponent
}

@Component(modules = [AndroidSharedDependencyModule::class])
@Singleton
interface AndroidSharedDependencyComponent {

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun application(application: Application): Builder

        fun dependencyModule(androidSharedDependencyModule: AndroidSharedDependencyModule): Builder

        fun build(): AndroidSharedDependencyComponent
    }

    fun inject(colorPickerDialog: ColorPickerDialog)
}

@Module
open class AndroidSharedDependencyModule {

    @Provides
    open fun providesColorPickerViewModel(): ColorPickerViewModel.Factory {
        return ColorPickerViewModel.Factory()
    }
}
