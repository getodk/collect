package org.odk.collect.android.injection.config

import org.odk.collect.async.Scheduler
import org.odk.collect.draw.DrawDependencyModule
import org.odk.collect.settings.SettingsProvider

class CollectDrawDependencyModule(
    private val applicationComponent: AppDependencyComponent
) : DrawDependencyModule() {
    override fun providesScheduler(): Scheduler {
        return applicationComponent.scheduler()
    }

    override fun providesSettingsProvider(): SettingsProvider {
        return applicationComponent.settingsProvider()
    }

    override fun providesImagePath(): String {
        return applicationComponent.storagePathProvider().getTmpImageFilePath()
    }
}
