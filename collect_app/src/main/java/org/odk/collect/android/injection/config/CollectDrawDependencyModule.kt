package org.odk.collect.android.injection.config

import org.odk.collect.android.storage.StorageSubdirectory
import org.odk.collect.async.Scheduler
import org.odk.collect.draw.DrawDependencyModule
import org.odk.collect.settings.SettingsProvider
import java.io.File

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
        return applicationComponent.storagePathProvider().getOdkDirPath(StorageSubdirectory.CACHE) + File.separator + "tmp.jpg"
    }
}
