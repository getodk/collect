package org.odk.collect.draw

import android.app.Application
/**
 * Used as the Application in tests in in the `test/src` root. This is setup in `robolectric.properties`
 */
internal class RobolectricApplication : Application(), DrawDependencyComponentProvider {

    override lateinit var drawDependencyComponent: DrawDependencyComponent

    fun setupDependencies(dependencyModule: DrawDependencyModule) {
        drawDependencyComponent = DaggerDrawDependencyComponent.builder()
            .drawDependencyModule(dependencyModule)
            .build()
    }
}
