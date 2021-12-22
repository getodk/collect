package org.odk.collect.geo

import android.app.Application
import org.odk.collect.androidshared.ui.Animations
import org.odk.collect.strings.localization.LocalizedApplication
import java.util.Locale

class RobolectricApplication : Application(), LocalizedApplication, GeoDependencyComponentProvider {

    override val locale: Locale
        get() = this.resources.configuration.locales[0]

    override lateinit var geoDependencyComponent: GeoDependencyComponent

    override fun onCreate() {
        super.onCreate()
        Animations.DISABLE_ANIMATIONS = true
    }
}
