package org.odk.collect.android.injection.config

import org.odk.collect.projects.ProjectsDependencyModule
import org.odk.collect.projects.ProjectsRepository

class CollectProjectsDependencyModule(
    private val appDependencyComponent: AppDependencyComponent
) : ProjectsDependencyModule() {
    override fun providesProjectsRepository(): ProjectsRepository {
        return appDependencyComponent.projectsRepository()
    }
}
