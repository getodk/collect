package org.odk.collect.projects.support

import android.app.Application
import org.odk.collect.projects.DaggerProjectsDependencyComponent
import org.odk.collect.projects.ProjectsDependencyComponent
import org.odk.collect.projects.ProjectsDependencyComponentProvider

class RobolectricApplication : Application(), ProjectsDependencyComponentProvider {

    override lateinit var projectsDependencyComponent: ProjectsDependencyComponent

    override fun onCreate() {
        super.onCreate()
        projectsDependencyComponent = DaggerProjectsDependencyComponent.builder().build()
    }
}
