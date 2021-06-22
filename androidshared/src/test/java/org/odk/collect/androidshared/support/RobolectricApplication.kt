package org.odk.collect.androidshared.support

import android.app.Application
import org.odk.collect.androidshared.AndroidSharedDependencyComponent
import org.odk.collect.androidshared.AndroidSharedDependencyComponentProvider
import org.odk.collect.androidshared.AndroidSharedDependencyModule
import org.odk.collect.androidshared.DaggerAndroidSharedDependencyComponent

/**
 * Used as the Application in tests in in the `test/src` root. This is setup in `robolectric.properties`
 */
class RobolectricApplication : Application(), AndroidSharedDependencyComponentProvider {

    override lateinit var androidSharedDependencyComponent: AndroidSharedDependencyComponent

    override fun onCreate() {
        super.onCreate()
        androidSharedDependencyComponent = DaggerAndroidSharedDependencyComponent.builder()
            .application(this)
            .build()
    }

    fun setupDependencies(dependencyModule: AndroidSharedDependencyModule) {
        androidSharedDependencyComponent = DaggerAndroidSharedDependencyComponent.builder()
            .dependencyModule(dependencyModule)
            .application(this)
            .build()
    }
}
