package org.odk.collect.android.support

import androidx.test.core.app.ApplicationProvider
import org.odk.collect.android.application.Collect
import org.odk.collect.android.injection.config.AppDependencyModule
import org.odk.collect.android.injection.config.DaggerAppDependencyComponent

object CollectHelpers {
    fun overrideAppDependencyModule(appDependencyModule: AppDependencyModule) {
        val application = ApplicationProvider.getApplicationContext<Collect>()
        val testComponent = DaggerAppDependencyComponent.builder()
            .application(application)
            .appDependencyModule(appDependencyModule)
            .build()
        application.component = testComponent
    }
}
