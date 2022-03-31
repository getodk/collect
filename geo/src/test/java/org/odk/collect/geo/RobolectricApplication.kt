package org.odk.collect.geo

import android.app.Application
import org.odk.collect.androidshared.ui.Animations

class RobolectricApplication : Application(), GeoDependencyComponentProvider {

    override lateinit var geoDependencyComponent: GeoDependencyComponent

    override fun onCreate() {
        super.onCreate()
        Animations.DISABLE_ANIMATIONS = true
    }
}
