package org.odk.collect.draw

import dagger.Component
import dagger.Module
import dagger.Provides
import org.odk.collect.async.Scheduler
import org.odk.collect.settings.SettingsProvider
import javax.inject.Singleton

interface DrawDependencyComponentProvider {
    val drawDependencyComponent: DrawDependencyComponent
}

@Component(modules = [DrawDependencyModule::class])
@Singleton
interface DrawDependencyComponent {
    fun inject(drawActivity: DrawActivity)
}

@Module
open class DrawDependencyModule {

    @Provides
    open fun providesScheduler(): Scheduler {
        throw UnsupportedOperationException("This should be overridden by dependent application")
    }

    @Provides
    open fun providesSettingsProvider(): SettingsProvider {
        throw UnsupportedOperationException("This should be overridden by dependent application")
    }
}
