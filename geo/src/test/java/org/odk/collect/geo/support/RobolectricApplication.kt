package org.odk.collect.geo.support

import android.app.Application
import org.odk.collect.androidshared.ui.Animations
import org.odk.collect.geo.GeoDependencyComponent
import org.odk.collect.geo.GeoDependencyComponentProvider

class RobolectricApplication : Application(), GeoDependencyComponentProvider {

    override lateinit var geoDependencyComponent: GeoDependencyComponent

    override fun onCreate() {
        super.onCreate()
        Animations.DISABLE_ANIMATIONS = true
    }
}
