package org.odk.collect.projects

import dagger.Component
import dagger.Module
import dagger.Provides
import org.odk.collect.projects.providers.ProjectsProvider
import org.odk.collect.shared.UUIDGenerator
import javax.inject.Singleton

interface ProjectsDependencyComponentProvider {
    val projectsDependencyComponent: ProjectsDependencyComponent
}

@Component(modules = [ProjectsDependencyModule::class])
@Singleton
interface ProjectsDependencyComponent {

    fun inject(addProjectDialog: AddProjectDialog)

    fun inject(projectsProvider: ProjectsProvider)
}

@Module
open class ProjectsDependencyModule {

    @Provides
    open fun providesProjectsRepository(): ProjectsRepository {
        return InMemProjectsRepository(UUIDGenerator())
    }
}
