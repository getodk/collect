package org.odk.collect.projects

import dagger.Component
import dagger.Module
import dagger.Provides
import org.odk.collect.shared.UUIDGenerator
import javax.inject.Singleton

interface ProjectsDependencyComponentProvider {
    fun getProjectsDependencyComponent(): ProjectsDependencyComponent
}

@Component(modules = [ProjectsDependencyModule::class])
@Singleton
interface ProjectsDependencyComponent {

    fun inject(addProjectDialog: AddProjectDialog)
}

@Module
open class ProjectsDependencyModule {

    @Provides
    open fun providesProjectsRepository(): ProjectsRepository {
        return InMemProjectsRepository(UUIDGenerator())
    }
}
