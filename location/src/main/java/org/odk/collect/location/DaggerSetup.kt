package org.odk.collect.location

import dagger.Component
import dagger.Module
import dagger.Provides
import org.odk.collect.androidshared.utils.UniqueIdGenerator
import org.odk.collect.location.tracker.LocationTrackerService
import javax.inject.Singleton

interface LocationDependencyComponentProvider {
    val locationDependencyComponent: LocationDependencyComponent
}

@Component(modules = [LocationDependencyModule::class])
@Singleton
interface LocationDependencyComponent {
    fun inject(locationTrackerService: LocationTrackerService)
}

@Module
open class LocationDependencyModule {

    @Provides
    open fun providesUniqueIdGenerator(): UniqueIdGenerator {
        throw UnsupportedOperationException("This should be overridden by dependent application")
    }
}
