package org.odk.collect.android.injection.config

import org.odk.collect.projects.ProjectsDependencyModule
import org.odk.collect.projects.ProjectsRepository

class CollectProjectsDependencyModule(private val projectsRepository: ProjectsRepository) :
    ProjectsDependencyModule() {
    override fun providesProjectsRepository(): ProjectsRepository {
        return projectsRepository
    }
}
