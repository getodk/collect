package org.odk.collect.android.injection.config

import org.odk.collect.async.Scheduler
import org.odk.collect.draw.DrawDependencyModule
import org.odk.collect.settings.SettingsProvider

class CollectDrawDependencyModule(
    private val scheduler: () -> Scheduler,
    private val settingsProvider: () -> SettingsProvider
) : DrawDependencyModule() {
    override fun providesScheduler(): Scheduler {
        return scheduler()
    }

    override fun providesSettingsProvider(): SettingsProvider {
        return settingsProvider()
    }
}
