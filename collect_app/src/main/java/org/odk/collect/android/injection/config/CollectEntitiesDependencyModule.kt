package org.odk.collect.android.injection.config

import org.odk.collect.android.application.CollectComposeThemeProvider
import org.odk.collect.androidshared.ui.ComposeThemeProvider
import org.odk.collect.async.Scheduler
import org.odk.collect.entities.EntitiesDependencyModule
import org.odk.collect.entities.storage.EntitiesRepository

class CollectEntitiesDependencyModule(private val appDependencyComponent: AppDependencyComponent) : EntitiesDependencyModule() {
    override fun providesEntitiesRepository(): EntitiesRepository {
        val projectId: String =
            appDependencyComponent.currentProjectProvider().requireCurrentProject().uuid
        return appDependencyComponent.entitiesRepositoryProvider().create(projectId)
    }

    override fun providesScheduler(): Scheduler {
        return appDependencyComponent.scheduler()
    }

    override fun providesComposeThemeProvider(): ComposeThemeProvider {
        return object : CollectComposeThemeProvider {}
    }
}
